package com.xawl.cateen.dto.mini;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分片上传初始化请求
 */
@Data
@ApiModel("分片上传初始化请求")
public class MultipartUploadInitDTO {

    @ApiModelProperty("文件名")
    private String filename;

    @ApiModelProperty("文件总大小")
    private Long fileSize;

    @ApiModelProperty("文件类型")
    private String contentType;

    @ApiModelProperty("分片大小（字节）")
    private Long chunkSize;

    @ApiModelProperty("分片总数")
    private Integer totalChunks;

    @ApiModelProperty("文件MD5")
    private String fileMd5;

    @ApiModelProperty("上传类型：image/video")
    private String uploadType;

    @ApiModelProperty("业务类型：avatar/comment/food/forum")
    private String businessType;
}
