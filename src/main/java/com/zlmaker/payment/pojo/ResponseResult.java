package com.zlmaker.payment.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应实体类
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.pojo
 * @className ResponseMessage
 * @date 2022/4/23 下午9:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult {
    /**
     * 状态码
     */
    private Integer code;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应数据
     */
    private Object data;

    /**
     * 响应成功
     */
    public static ResponseResult success(String message) {
        return new ResponseResult(200, message, null);
    }

    /**
     * 响应成功（带数据）
     */
    public static ResponseResult success(String message, Object data) {
        return new ResponseResult(200, message, data);
    }

    /**
     * 响应失败
     */
    public static ResponseResult error(String message) {
        return new ResponseResult(500, message, null);
    }

    /**
     * 响应失败（带数据）
     */
    public static ResponseResult error(String message, Object data) {
        return new ResponseResult(500, message, data);
    }

}
