package com.xawl.cateen.vo.mini;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 文件上传结果VO
 *
 * @author xawl
 * @date 2025-10-05
 */
@Data
@Builder
@ApiModel(description = "文件上传结果")
public class UploadResultVO {

    @ApiModelProperty(value = "文件访问URL", example = "http://localhost:8080/uploads/images/20231005/abc123.jpg")
    private String url;

    @ApiModelProperty(value = "文件名", example = "abc123.jpg")
    private String filename;

    @ApiModelProperty(value = "文件大小（字节）", example = "1024000")
    private Long size;

    @ApiModelProperty(value = "文件类型", example = "image/jpeg")
    private String contentType;

    @ApiModelProperty(value = "上传时间戳", example = "1696512000000")
    private Long timestamp;
}
