package com.zlmaker.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zl-maker
 */

@AllArgsConstructor
@Getter
public enum WxPayTradeState {

    /**
     * 支付成功
     */
    SUCCESS("SUCCESS"),

    /**
     * 未支付
     */
    NOTPAY("NOTPAY"),

    /**
     * 已关闭
     */
    CLOSED("CLOSED"),

    /**
     * 转入退款
     */
    REFUND("REFUND");

    /**
     * 类型
     */
    private final String type;
}
