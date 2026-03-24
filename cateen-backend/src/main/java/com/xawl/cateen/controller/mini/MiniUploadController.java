package com.xawl.cateen.controller.mini;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.mini.MultipartUploadChunkDTO;
import com.xawl.cateen.dto.mini.MultipartUploadInitDTO;
import com.xawl.cateen.service.mini.UploadService;
import com.xawl.cateen.service.storage.ImageUploadService;
import com.xawl.cateen.vo.mini.MultipartUploadInitVO;
import com.xawl.cateen.vo.mini.UploadResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 小程序端 - 文件上传控制器
 *
 * @author xawl
 * @date 2025-10-05
 */
@Api(tags = "小程序-文件上传")
@Slf4j
@RestController
@RequestMapping("/api/mini/upload")
@RequiredArgsConstructor
public class MiniUploadController {

    private final UploadService uploadService;
    private final ImageUploadService imageUploadService;

    /**
     * 上传图片
     * 
     * 支持的格式：jpg, jpeg, png, webp
     * 最大大小：5MB
     * 
     * @param file 图片文件
     * @param type 图片类型：avatar-头像，comment-评论，food-美食
     * @return 图片URL
     */
    @ApiOperation(value = "上传图片", notes = "上传图片文件，支持jpg/jpeg/png/webp格式，最大5MB")
    @PostMapping("/image")
    public Result<UploadResultVO> uploadImage(
            @ApiParam(value = "图片文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "图片类型", example = "comment") @RequestParam(defaultValue = "comment") String type
    ) {
        log.info("上传图片，type: {}, filename: {}, size: {} bytes",
                type, file.getOriginalFilename(), file.getSize());

        UploadResultVO result = uploadService.uploadImage(file, type);

        return Result.success("上传成功", result);
    }

    @ApiOperation(value = "上传视频", notes = "上传视频文件，支持mp4格式，最大20MB")
    @PostMapping("/video")
    public Result<UploadResultVO> uploadVideo(
            @ApiParam(value = "视频文件", required = true) @RequestParam("file") MultipartFile file
    ) {
        log.info("上传视频，filename: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        UploadResultVO result = uploadService.uploadVideo(file);

        return Result.success("上传成功", result);
    }

    /**
     * 初始化分片上传（支持断点续传）
     */
    @ApiOperation(value = "初始化分片上传", notes = "初始化分片上传，支持断点续传")
    @PostMapping("/multipart/init")
    public Result<MultipartUploadInitVO> initMultipartUpload(@RequestBody MultipartUploadInitDTO request) {
        log.info("初始化分片上传: filename={}, fileSize={}, totalChunks={}",
                request.getFilename(), request.getFileSize(), request.getTotalChunks());

        MultipartUploadInitVO result = imageUploadService.initMultipartUpload(request);

        return Result.success("初始化成功", result);
    }

    /**
     * 上传分片
     */
    @ApiOperation(value = "上传分片", notes = "上传单个分片")
    @PostMapping("/multipart/chunk")
    public Result<Void> uploadChunk(@RequestBody MultipartUploadChunkDTO request) {
        log.info("上传分片: uploadId={}, chunkNumber={}", request.getUploadId(), request.getChunkNumber());

        imageUploadService.uploadChunk(request);

        return Result.success("上传成功", null);
    }

    /**
     * 完成分片上传
     */
    @ApiOperation(value = "完成分片上传", notes = "完成所有分片上传并合并")
    @PostMapping("/multipart/complete/{uploadId}")
    public Result<UploadResultVO> completeMultipartUpload(
            @ApiParam(value = "上传ID") @PathVariable String uploadId) {
        log.info("完成分片上传: uploadId={}", uploadId);

        UploadResultVO result = imageUploadService.completeMultipartUpload(uploadId);

        return Result.success("上传成功", result);
    }

    /**
     * 中止分片上传
     */
    @ApiOperation(value = "中止分片上传", notes = "中止分片上传并清理已上传的分片")
    @DeleteMapping("/multipart/abort/{uploadId}")
    public Result<Void> abortMultipartUpload(
            @ApiParam(value = "上传ID") @PathVariable String uploadId) {
        log.info("中止分片上传: uploadId={}", uploadId);

        imageUploadService.abortMultipartUpload(uploadId);

        return Result.success("中止成功", null);
    }
}
