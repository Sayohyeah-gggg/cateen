package com.xawl.cateen.controller.admin;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.config.MinioProperties;
import com.xawl.cateen.service.storage.ImageUploadService;
import com.xawl.cateen.vo.mini.UploadResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin-side image upload controller.
 *
 * <p>When MinIO is enabled, images will be stored in the configured {@code admin} bucket.</p>
 */
@Api(tags = "管理端-文件上传")
@Slf4j
@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class AdminUploadController {

    private final ImageUploadService imageUploadService;
    private final MinioProperties minioProperties;

    @ApiOperation(value = "上传图片", notes = "管理端上传图片（MinIO: admin 桶）")
    @PostMapping("/image")
    public Result<UploadResultVO> uploadImage(
            @ApiParam(value = "图片文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "图片类型", example = "food") @RequestParam(defaultValue = "food") String type
    ) {
        log.info("Admin upload image: type={}, filename={}, size={}", type, file.getOriginalFilename(), file.getSize());

        String bucket = minioProperties.getBucket().getAdmin();
        UploadResultVO result = imageUploadService.uploadImage(file, bucket, type);
        return Result.success("上传成功", result);
    }
}

