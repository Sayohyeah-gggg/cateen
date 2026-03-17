package com.xawl.cateen.service.storage;

import com.xawl.cateen.common.ResultCode;
import com.xawl.cateen.config.MinioProperties;
import com.xawl.cateen.exception.BusinessException;
import com.xawl.cateen.vo.mini.UploadResultVO;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Image upload service. Supports local filesystem and MinIO.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ObjectProvider<MinioClient> minioClientProvider;
    private final MinioProperties minioProperties;

    // Local filesystem fallback
    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${upload.base-url:http://localhost:8080}")
    private String baseUrl;

    private final Set<String> ensuredBuckets = ConcurrentHashMap.newKeySet();

    /**
     * Upload an image file.
     *
     * @param bucket target bucket name (used only when MinIO enabled)
     * @param type   logical type folder (e.g. avatar/comment/food/forum)
     */
    public UploadResultVO uploadImage(MultipartFile file, String bucket, String type) {
        validateFile(file);

        String safeType = sanitizeType(type);
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = generateFilename(extension);
        String dateStr = LocalDate.now().format(DATE_FORMATTER);

        if (minioProperties.isEnabled()) {
            MinioClient minioClient = minioClientProvider.getIfAvailable();
            if (minioClient == null) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "MinIO未启用或配置错误");
            }
            if (!StringUtils.hasText(bucket)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "bucket不能为空");
            }

            String objectName = String.format("images/%s/%s/%s", safeType, dateStr, filename);
            uploadToMinio(minioClient, bucket, objectName, file);
            String accessUrl = buildMinioAccessUrl(bucket, objectName);
            log.info("Image uploaded to MinIO successfully: {}", accessUrl);

            return buildResult(accessUrl, filename, file);
        }

        // Local filesystem upload (kept for compatibility / dev fallback)
        String relativePath = String.format("images%s%s%s%s", File.separator, safeType, File.separator, dateStr);
        saveToLocal(file, relativePath, filename);
        String accessUrl = String.format("%s/uploads/%s/%s",
                trimTrailingSlash(baseUrl),
                relativePath.replace("\\", "/"),
                filename);
        return buildResult(accessUrl, filename, file);
    }

    private void uploadToMinio(MinioClient minioClient, String bucket, String objectName, MultipartFile file) {
        ensureBucketExists(minioClient, bucket);

        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .contentType(contentType)
                            .stream(in, file.getSize(), -1)
                            .build()
            );
            log.info("Uploaded image to MinIO: bucket={}, object={}, size={}", bucket, objectName, file.getSize());
        } catch (Exception e) {
            log.error("MinIO upload failed: bucket={}, object={}", bucket, objectName, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "图片上传失败");
        }
    }

    private void ensureBucketExists(MinioClient minioClient, String bucket) {
        if (ensuredBuckets.contains(bucket)) {
            return;
        }

        // Only one thread will do the exists/create check for a given bucket
        if (!ensuredBuckets.add(bucket)) {
            return;
        }

        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            }
        } catch (Exception e) {
            ensuredBuckets.remove(bucket); // allow retry
            log.error("Ensure MinIO bucket failed: {}", bucket, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "MinIO桶不存在且创建失败: " + bucket);
        }
    }

    private void saveToLocal(MultipartFile file, String relativePath, String filename) {
        Path dir = Paths.get(uploadPath, relativePath).toAbsolutePath();
        try {
            Files.createDirectories(dir);
            Path targetPath = dir.resolve(filename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Saved image to local filesystem: {}", targetPath);
        } catch (IOException e) {
            log.error("Local image save failed", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "图片保存失败");
        }
    }

    private UploadResultVO buildResult(String url, String filename, MultipartFile file) {
        return UploadResultVO.builder()
                .url(url)
                .filename(filename)
                .size(file.getSize())
                .contentType(file.getContentType())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件名不合法");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的文件格式，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小不能超过5MB");
        }
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String generateFilename(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    private String sanitizeType(String type) {
        if (!StringUtils.hasText(type)) {
            return "common";
        }
        String cleaned = type.trim().toLowerCase();
        cleaned = cleaned.replaceAll("[^a-z0-9_-]", "_");
        cleaned = cleaned.replaceAll("_+", "_");
        if (!StringUtils.hasText(cleaned)) {
            return "common";
        }
        // Prevent too-long path segments
        return cleaned.length() > 32 ? cleaned.substring(0, 32) : cleaned;
    }

    private String buildMinioAccessUrl(String bucket, String objectName) {
        String base = StringUtils.hasText(minioProperties.getPublicUrl())
                ? minioProperties.getPublicUrl()
                : minioProperties.getEndpoint();

        if (!StringUtils.hasText(base)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "MinIO public-url/endpoint 未配置");
        }

        base = trimTrailingSlash(base);
        String cleanObject = objectName.startsWith("/") ? objectName.substring(1) : objectName;
        return base + "/" + bucket + "/" + cleanObject;
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String v = value.trim();
        while (v.endsWith("/")) {
            v = v.substring(0, v.length() - 1);
        }
        return v;
    }
}

