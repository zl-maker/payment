package com.zlmaker.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zlmaker.payment.pojo.RefundInfo;

/**
 * @author zl-maker
 */
public interface RefundInfoService
        extends IService<RefundInfo> {

    /**
     * 根据订单编号创建退款单
     *
     * @param orderNo
     * @param reason
     * @return
     */
    RefundInfo createRefundByOrderNo(String orderNo, String reason);

    /**
     * 更新退款单
     *
     * @param responseBody
     */
    void updateRefund(String responseBody);

    /**
     * 根据订单号创建支付宝支付退款单
     *
     * @param orderNo
     * @param reason
     * @return
     */
    RefundInfo createRefundByOrderNoForAliPay(String orderNo, String reason);

    /**
     * 更新支付宝支付退款单
     *
     * @param refundNo
     * @param body
     * @param type
     */
    void updateRefundForAliPay(String refundNo, String body, String type);
}
