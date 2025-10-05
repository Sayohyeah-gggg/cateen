package com.xawl.cateen.controller.mini;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.service.mini.UploadService;
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
}
