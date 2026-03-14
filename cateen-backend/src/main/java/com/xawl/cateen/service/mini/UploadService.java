package com.xawl.cateen.service.mini;

import com.xawl.cateen.vo.mini.UploadResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 *
 * @author xawl
 * @date 2025-10-05
 */
@Slf4j
@Service
public class UploadService {

    // 允许的图片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    
    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${upload.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 上传图片
     */
    public UploadResultVO uploadImage(MultipartFile file, String type) {
        // 1. 验证文件
        validateFile(file);
        
        // 2. 生成文件名和路径
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = generateFilename(extension);
        String relativePath = generateRelativePath(type);
        
        // 3. 确保目录存在
        String fullPath = uploadPath + File.separator + relativePath;
        createDirectoryIfNotExists(fullPath);
        
        // 4. 保存文件
        try {
            Path targetPath = Paths.get(fullPath, newFilename).toAbsolutePath();
            // 确保父目录存在
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath);
            log.info("文件上传成功: {}", targetPath);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
        
        // 5. 生成访问URL
        String accessUrl = String.format("%s/uploads/%s/%s", 
                baseUrl, relativePath.replace("\\", "/"), newFilename);
        
        // 6. 返回结果
        return UploadResultVO.builder()
                .url(accessUrl)
                .filename(newFilename)
                .size(file.getSize())
                .contentType(file.getContentType())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        // 验证文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new RuntimeException("文件名不合法");
        }
        
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("不支持的文件格式，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
        
        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过5MB");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 生成唯一文件名
     */
    private String generateFilename(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    /**
     * 生成相对路径（按类型和日期分类）
     */
    private String generateRelativePath(String type) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("images%s%s%s%s", File.separator, type, File.separator, dateStr);
    }

    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new RuntimeException("创建目录失败: " + path);
            }
        }
    }
}
