package com.xawl.cateen.dto.mini;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分片上传请求
 */
@Data
@ApiModel("分片上传请求")
public class MultipartUploadChunkDTO {

    @ApiModelProperty("上传ID")
    private String uploadId;

    @ApiModelProperty("分片序号（从0开始）")
    private Integer chunkNumber;

    @ApiModelProperty("分片数据")
    private byte[] chunkData;

    @ApiModelProperty("分片大小")
    private Long chunkSize;

    @ApiModelProperty("文件MD5")
    private String fileMd5;
}
