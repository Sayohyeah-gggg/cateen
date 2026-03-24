package com.xawl.cateen.service.mini;

import com.xawl.cateen.config.MinioProperties;
import com.xawl.cateen.service.storage.ImageUploadService;
import com.xawl.cateen.vo.mini.UploadResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Mini-program image upload service.
 *
 * <p>When MinIO is enabled, images will be stored in the configured {@code small} bucket.
 * Otherwise, it falls back to local filesystem (upload.path).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final ImageUploadService imageUploadService;
    private final MinioProperties minioProperties;

    public UploadResultVO uploadImage(MultipartFile file, String type) {
        String bucket = minioProperties.getBucket().getSmall();
        return imageUploadService.uploadImage(file, bucket, type);
    }

    public UploadResultVO uploadVideo(MultipartFile file) {
        String bucket = minioProperties.getBucket().getVideo();
        return imageUploadService.uploadVideo(file, bucket, "forum");
    }
}

