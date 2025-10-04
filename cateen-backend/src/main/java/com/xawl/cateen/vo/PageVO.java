package com.xawl.cateen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应VO
 *
 * @author xawl
 * @date 2025-10-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页数量
     */
    private Long pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

}

