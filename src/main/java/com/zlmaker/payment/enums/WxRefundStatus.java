package com.zlmaker.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zl-maker
 */

@AllArgsConstructor
@Getter
public enum WxRefundStatus {

    /**
     * 退款成功
     */
    SUCCESS("SUCCESS"),

    /**
     * 退款关闭
     */
    CLOSED("CLOSED"),

    /**
     * 退款处理中
     */
    PROCESSING("PROCESSING"),

    /**
     * 退款异常
     */
    ABNORMAL("ABNORMAL");

    /**
     * 类型
     */
    private final String type;
}
