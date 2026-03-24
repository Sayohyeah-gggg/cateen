package com.xawl.cateen.service.storage;

import com.xawl.cateen.common.ResultCode;
import com.xawl.cateen.config.MinioProperties;
import com.xawl.cateen.dto.mini.MultipartUploadChunkDTO;
import com.xawl.cateen.dto.mini.MultipartUploadInitDTO;
import com.xawl.cateen.exception.BusinessException;
import com.xawl.cateen.vo.mini.MultipartUploadInitVO;
import com.xawl.cateen.vo.mini.UploadResultVO;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Image upload service. Supports local filesystem and MinIO.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = Arrays.asList("mp4");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_VIDEO_FILE_SIZE = 200 * 1024 * 1024; // 200MB
    private static final long DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024; // 5MB per chunk
    private static final long CHUNK_UPLOAD_TIMEOUT = 30; // 30 minutes
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ObjectProvider<MinioClient> minioClientProvider;
    private final MinioProperties minioProperties;

    // Local filesystem fallback
    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${upload.base-url:http://localhost:8080}")
    private String baseUrl;

    private final Set<String> ensuredBuckets = ConcurrentHashMap.newKeySet();

    // 分片上传会话管理（用于断点续传）
    private final Map<String, MultipartUploadSession> uploadSessions = new ConcurrentHashMap<>();

    /**
     * 分片上传会话
     */
    private static class MultipartUploadSession {
        String uploadId;
        String bucket;
        String objectName;
        String filename;
        String contentType;
        Long fileSize;
        Integer totalChunks;
        Long chunkSize;
        Set<Integer> uploadedChunks = ConcurrentHashMap.newKeySet();
        long lastUpdateTime;
        String chunkDir; // 分片临时目录
    }

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

    /**
     * Upload a video file.
     *
     * @param bucket target bucket name (used only when MinIO enabled)
     * @param type   logical type folder (e.g. forum)
     */
    public UploadResultVO uploadVideo(MultipartFile file, String bucket, String type) {
        validateVideoFile(file);

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

            String objectName = String.format("videos/%s/%s/%s", safeType, dateStr, filename);
            uploadToMinio(minioClient, bucket, objectName, file);
            String accessUrl = buildMinioAccessUrl(bucket, objectName);
            log.info("Video uploaded to MinIO successfully: {}", accessUrl);

            return buildResult(accessUrl, filename, file);
        }

        // Local filesystem upload (kept for compatibility / dev fallback)
        String relativePath = String.format("videos%s%s%s%s", File.separator, safeType, File.separator, dateStr);
        saveToLocal(file, relativePath, filename);
        String accessUrl = String.format("%s/uploads/%s/%s",
                trimTrailingSlash(baseUrl),
                relativePath.replace("\\", "/"),
                filename);
        return buildResult(accessUrl, filename, file);
    }

    /**
     * Delete a file from MinIO or local filesystem by URL.
     *
     * @param fileUrl the file URL to delete
     */
    public void deleteFileByUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }

        if (minioProperties.isEnabled()) {
            deleteFromMinioByUrl(fileUrl);
        } else {
            deleteFromLocalByUrl(fileUrl);
        }
    }

    private void deleteFromMinioByUrl(String fileUrl) {
        try {
            // 解析 URL: http://localhost:9000/bucket/objectName
            String baseUrl = minioProperties.getPublicUrl() != null ?
                minioProperties.getPublicUrl() : minioProperties.getEndpoint();
            if (!StringUtils.hasText(baseUrl)) {
                log.warn("MinIO base URL is null, cannot delete file: {}", fileUrl);
                return;
            }

            // 提取 bucket 和 objectName
            if (!fileUrl.startsWith(baseUrl)) {
                log.warn("File URL does not match MinIO base URL: {}", fileUrl);
                return;
            }

            String pathWithoutBase = fileUrl.substring(baseUrl.length() + 1); // +1 to remove leading '/'
            int firstSlashIndex = pathWithoutBase.indexOf('/');
            if (firstSlashIndex <= 0) {
                log.warn("Invalid file URL format: {}", fileUrl);
                return;
            }

            String bucket = pathWithoutBase.substring(0, firstSlashIndex);
            String objectName = pathWithoutBase.substring(firstSlashIndex + 1);

            MinioClient minioClient = minioClientProvider.getIfAvailable();
            if (minioClient == null) {
                log.warn("MinIO client is not available");
                return;
            }

            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build()
            );
            log.info("Deleted file from MinIO: bucket={}, object={}", bucket, objectName);
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", fileUrl, e);
        }
    }

    private void deleteFromLocalByUrl(String fileUrl) {
        try {
            // 解析 URL: http://localhost:8080/uploads/...
            String uploadsPath = "/uploads/";
            int uploadsIndex = fileUrl.indexOf(uploadsPath);
            if (uploadsIndex < 0) {
                log.warn("File URL does not contain /uploads/: {}", fileUrl);
                return;
            }

            String relativePath = fileUrl.substring(uploadsIndex + uploadsPath.length());
            Path filePath = Paths.get(uploadPath, relativePath).toAbsolutePath();

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted file from local filesystem: {}", filePath);
            } else {
                log.warn("File does not exist: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Failed to delete file from local filesystem: {}", fileUrl, e);
        }
    }

    private void uploadToMinio(MinioClient minioClient, String bucket, String objectName, MultipartFile file) {
        ensureBucketExists(minioClient, bucket);

        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
        boolean isVideo = contentType.startsWith("video/") || objectName.contains("/videos/");

        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .contentType(contentType)
                            .stream(in, file.getSize(), -1)
                            .build()
            );
            log.info("Uploaded {} to MinIO: bucket={}, object={}, size={}", isVideo ? "video" : "image", bucket, objectName, file.getSize());
        } catch (Exception e) {
            log.error("MinIO upload failed: bucket={}, object={}", bucket, objectName, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, isVideo ? "视频上传失败" : "图片上传失败");
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

    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件名不合法");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的视频格式，仅支持: " + String.join(", ", ALLOWED_VIDEO_EXTENSIONS));
        }

        if (file.getSize() > MAX_VIDEO_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "视频大小不能超过20MB");
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

    /**
     * 初始化分片上传（支持断点续传）
     */
    public MultipartUploadInitVO initMultipartUpload(MultipartUploadInitDTO request) {
        // 验证请求参数
        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小无效");
        }
        if (request.getChunkSize() == null || request.getChunkSize() <= 0) {
            request.setChunkSize(DEFAULT_CHUNK_SIZE);
        }
        if (request.getTotalChunks() == null || request.getTotalChunks() <= 0) {
            request.setTotalChunks((int) Math.ceil((double) request.getFileSize() / request.getChunkSize()));
        }

        // 检查是否已有上传记录（断点续传）
        String sessionKey = generateSessionKey(request.getFileMd5());
        MultipartUploadSession existingSession = uploadSessions.get(sessionKey);

        if (existingSession != null) {
            // 检查会话是否过期
            if (System.currentTimeMillis() - existingSession.lastUpdateTime < TimeUnit.MINUTES.toMillis(CHUNK_UPLOAD_TIMEOUT)) {
                log.info("断点续传: uploadId={}, 已上传分片数={}/{}",
                    existingSession.uploadId, existingSession.uploadedChunks.size(), existingSession.totalChunks);

                return MultipartUploadInitVO.builder()
                    .uploadId(existingSession.uploadId)
                    .chunkSize(existingSession.chunkSize)
                    .totalChunks(existingSession.totalChunks)
                    .uploadedChunks(existingSession.uploadedChunks.size())
                    .build();
            } else {
                // 会话已过期，清理本地文件
                cleanupSessionFiles(existingSession);
                uploadSessions.remove(sessionKey);
            }
        }

        // 创建新的分片上传会话
        String safeType = sanitizeType(request.getBusinessType());
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String fileType = "video".equals(request.getUploadType()) ? "video" : "image";
        String extension = getFileExtension(request.getFilename());
        String filename = generateFilename(extension);
        String folderPrefix = fileType.equals("video") ? "videos" : "images";
        String bucket = fileType.equals("video") ? "video" : "small";

        String objectName = String.format("%s/%s/%s/%s", folderPrefix, safeType, dateStr, filename);

        // 创建分片临时目录
        Path chunkDir = Paths.get(uploadPath, "chunks", sessionKey);
        try {
            Files.createDirectories(chunkDir);
        } catch (IOException e) {
            log.error("创建分片目录失败: {}", chunkDir, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建上传目录失败");
        }

        String uploadId = sessionKey;

        MultipartUploadSession session = new MultipartUploadSession();
        session.uploadId = uploadId;
        session.bucket = bucket;
        session.objectName = objectName;
        session.filename = filename;
        session.contentType = request.getContentType();
        session.fileSize = request.getFileSize();
        session.totalChunks = request.getTotalChunks();
        session.chunkSize = request.getChunkSize();
        session.lastUpdateTime = System.currentTimeMillis();
        session.chunkDir = chunkDir.toString();

        uploadSessions.put(uploadId, session);

        log.info("初始化分片上传: uploadId={}, bucket={}, object={}, 总分片数={}",
            uploadId, bucket, objectName, request.getTotalChunks());

        return MultipartUploadInitVO.builder()
            .uploadId(uploadId)
            .chunkSize(request.getChunkSize())
            .totalChunks(request.getTotalChunks())
            .uploadedChunks(0)
            .build();
    }

    /**
     * 上传分片
     */
    public void uploadChunk(MultipartUploadChunkDTO request) {
        MultipartUploadSession session = uploadSessions.get(request.getUploadId());
        if (session == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传会话不存在或已过期");
        }

        // 更新会话时间
        session.lastUpdateTime = System.currentTimeMillis();

        // 检查分片是否已上传
        if (session.uploadedChunks.contains(request.getChunkNumber())) {
            log.info("分片已存在，跳过: uploadId={}, chunk={}", request.getUploadId(), request.getChunkNumber());
            return;
        }

        try {
            // 保存分片到本地临时文件
            Path chunkFile = Paths.get(session.chunkDir, "chunk_" + request.getChunkNumber() + ".tmp");
            Files.write(chunkFile, request.getChunkData());

            // 记录已上传的分片
            session.uploadedChunks.add(request.getChunkNumber());

            log.info("上传分片成功: uploadId={}, chunk={}, 已上传={}/{}",
                request.getUploadId(), request.getChunkNumber(), session.uploadedChunks.size(), session.totalChunks);

        } catch (Exception e) {
            log.error("上传分片失败: uploadId={}, chunk={}", request.getUploadId(), request.getChunkNumber(), e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "分片上传失败");
        }
    }

    /**
     * 完成分片上传
     */
    public UploadResultVO completeMultipartUpload(String uploadId) {
        MultipartUploadSession session = uploadSessions.get(uploadId);
        if (session == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传会话不存在或已过期");
        }

        // 检查是否所有分片都已上传
        if (session.uploadedChunks.size() < session.totalChunks) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                String.format("还有 %d 个分片未上传", session.totalChunks - session.uploadedChunks.size()));
        }

        // 合并分片
        Path mergedFile = null;
        try {
            // 合并所有分片到一个临时文件
            Path chunkDir = Paths.get(session.chunkDir);
            mergedFile = Files.createTempFile("merged_", "." + getFileExtension(session.filename));

            try (OutputStream out = Files.newOutputStream(mergedFile)) {
                for (int i = 0; i < session.totalChunks; i++) {
                    Path chunkFile = chunkDir.resolve("chunk_" + i + ".tmp");
                    Files.copy(chunkFile, out);
                }
            }

            log.info("分片合并完成: uploadId={}, 文件大小={}", uploadId, Files.size(mergedFile));

            // 上传到MinIO
            MinioClient minioClient = minioClientProvider.getIfAvailable();
            if (minioClient == null) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "MinIO未启用或配置错误");
            }

            ensureBucketExists(minioClient, session.bucket);

            String contentType = StringUtils.hasText(session.contentType) ? session.contentType : "application/octet-stream";
            try (InputStream in = Files.newInputStream(mergedFile)) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(session.bucket)
                        .object(session.objectName)
                        .contentType(contentType)
                        .stream(in, session.fileSize, -1)
                        .build()
                );
            }

            String accessUrl = buildMinioAccessUrl(session.bucket, session.objectName);

            // 清理会话和临时文件
            cleanupSessionFiles(session);
            uploadSessions.remove(uploadId);

            log.info("完成分片上传: uploadId={}, bucket={}, object={}, url={}",
                uploadId, session.bucket, session.objectName, accessUrl);

            return UploadResultVO.builder()
                .url(accessUrl)
                .filename(session.filename)
                .size(session.fileSize)
                .contentType(session.contentType)
                .timestamp(System.currentTimeMillis())
                .build();

        } catch (Exception e) {
            log.error("完成分片上传失败: uploadId={}", uploadId, e);
            cleanupSessionFiles(session);
            uploadSessions.remove(uploadId);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "完成上传失败");
        } finally {
            // 删除合并的临时文件
            if (mergedFile != null && Files.exists(mergedFile)) {
                try {
                    Files.delete(mergedFile);
                } catch (Exception e) {
                    log.warn("删除合并临时文件失败", e);
                }
            }
        }
    }

    /**
     * 中止分片上传
     */
    public void abortMultipartUpload(String uploadId) {
        MultipartUploadSession session = uploadSessions.remove(uploadId);
        if (session == null) {
            log.warn("上传会话不存在: {}", uploadId);
            return;
        }

        cleanupSessionFiles(session);
        log.info("中止分片上传: uploadId={}", uploadId);
    }

    /**
     * 检查并清理过期的上传会话
     */
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        long timeout = TimeUnit.MINUTES.toMillis(CHUNK_UPLOAD_TIMEOUT);

        for (Map.Entry<String, MultipartUploadSession> entry : uploadSessions.entrySet()) {
            if (now - entry.getValue().lastUpdateTime > timeout) {
                log.info("清理过期上传会话: uploadId={}", entry.getKey());
                cleanupSessionFiles(entry.getValue());
                uploadSessions.remove(entry.getKey());
            }
        }
    }

    /**
     * 清理会话文件
     */
    private void cleanupSessionFiles(MultipartUploadSession session) {
        if (session.chunkDir != null) {
            try {
                Path chunkDir = Paths.get(session.chunkDir);
                if (Files.exists(chunkDir)) {
                    Files.walk(chunkDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("删除分片文件失败: {}", path, e);
                            }
                        });
                    log.info("清理分片目录: {}", chunkDir);
                }
            } catch (Exception e) {
                log.error("清理分片目录失败: {}", session.chunkDir, e);
            }
        }
    }

    private String generateSessionKey(String fileMd5) {
        return fileMd5 != null ? "upload_" + fileMd5 : UUID.randomUUID().toString();
    }
}

