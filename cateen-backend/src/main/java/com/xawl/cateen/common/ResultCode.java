package com.xawl.cateen.common;

/**
 * 响应状态码常量
 *
 * @author xawl
 * @date 2025-10-03
 */
public class ResultCode {

    /**
     * 成功
     */
    public static final int SUCCESS = 200;

    /**
     * 请求参数错误
     */
    public static final int BAD_REQUEST = 400;

    /**
     * 未授权/未登录
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 无权限访问
     */
    public static final int FORBIDDEN = 403;

    /**
     * 资源不存在
     */
    public static final int NOT_FOUND = 404;

    /**
     * 服务器内部错误
     */
    public static final int INTERNAL_ERROR = 500;

    private ResultCode() {
    }

}

