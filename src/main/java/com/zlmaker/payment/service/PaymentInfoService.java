package com.zlmaker.payment.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zlmaker.payment.pojo.PaymentInfo;

import java.math.BigDecimal;

/**
 * @author zl-maker
 */
public interface PaymentInfoService
        extends IService<PaymentInfo> {

    /**
     * 记录支付日志
     *
     * @param orderData
     */
    void createPaymentInfo(JSONObject orderData);

    /**
     * 记录支付日志
     *
     * @param orderNo
     * @param totalAmount
     * @param tradeStatus
     * @param requestParams
     */
    void createPaymentInfoForAliPay(String orderNo, BigDecimal totalAmount, String tradeStatus, JSONObject requestParams);
}
