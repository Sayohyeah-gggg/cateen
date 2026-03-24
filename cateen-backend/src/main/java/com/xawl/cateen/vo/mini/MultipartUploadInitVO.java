package com.xawl.cateen.vo.mini;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分片上传初始化响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("分片上传初始化响应")
public class MultipartUploadInitVO {

    @ApiModelProperty("上传ID")
    private String uploadId;

    @ApiModelProperty("分片大小（字节）")
    private Long chunkSize;

    @ApiModelProperty("分片总数")
    private Integer totalChunks;

    @ApiModelProperty("已上传分片数量")
    private Integer uploadedChunks;
}
