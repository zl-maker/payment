package com.zlmaker.payment.service;

import com.alipay.api.AlipayApiException;
import com.zlmaker.payment.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付服务接口
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.service
 * @className AliPayService
 * @date 2022/4/30 下午4:44
 */
public interface AliPayService {
    /**
     * 创建订单
     *
     * @param productId
     * @return String
     */
    String tradeCreate(Long productId) throws AlipayApiException;

    /**
     * 关闭订单
     *
     * @param orderNo
     * @param orderStatus
     */
    void closeOrder(String orderNo, OrderStatus orderStatus) throws AlipayApiException;

    /**
     * 查询订单
     *
     * @param orderNo
     * @return String
     */
    String queryOrder(String orderNo);

    /**
     * 检查订单状态
     *
     * @param orderNo
     */
    void checkOrderStatus(String orderNo);

    /**
     * 退款
     *
     * @param orderNo
     * @param reason
     */
    void refund(String orderNo, String reason) throws AlipayApiException;

    /**
     * 查询退款
     *
     * @param orderNo
     * @return
     */
    String queryRefund(String orderNo) throws AlipayApiException;

    /**
     * 查询帐单
     *
     * @param billDate
     * @param type
     * @return
     */
    String queryBill(String billDate, String type) throws AlipayApiException;

    /**
     * 对支付结果中的具体业务信息进行二次校验
     *
     * @param requestParams
     * @return
     */
    String checkRequestParams(Map<String, String> requestParams);

    /**
     * 处理通知请求
     *
     * @param outTradeNo
     * @param totalAmount
     * @param tradeStatus
     * @param requestParams
     */
    void handleNotifyRequest(String outTradeNo, BigDecimal totalAmount, String tradeStatus, Map<String, String> requestParams);
}
