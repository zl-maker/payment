package com.zlmaker.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zl-maker
 */

@AllArgsConstructor
@Getter
public enum WxPayNotifyType {

    /**
     * 支付通知
     */
    NATIVE_NOTIFY("/api/wx-pay/native/notify"),

    /**
     * 退款结果通知
     */
    REFUND_NOTIFY("/api/wx-pay/refund/notify");

    /**
     * 类型
     */
    private final String type;
}
