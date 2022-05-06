package com.zlmaker.payment.service;

import com.zlmaker.payment.exception.WxPayApiException;

import javax.servlet.http.HttpServletRequest;

/**
 * 微信支付服务接口
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.service
 * @className WechatPayService
 * @date 2022/4/24 下午11:30
 */
public interface WxPayService {

    /**
     * 下单支付，生成支付二维码
     *
     * @param productId
     * @return String
     */
    String nativePay(Long productId) throws WxPayApiException;

    /**
     * 处理订单
     *
     * @param decryptData
     */
    void handleOrder(String decryptData);

    /**
     * 用户取消订单
     *
     * @param orderNo
     */
    void cancelOrder(String orderNo) throws WxPayApiException;

    /**
     * 关闭订单
     *
     * @param orderNo
     */
    void closeOrder(String orderNo) throws WxPayApiException;

    /**
     * 查询订单：测试订单状态
     *
     * @param orderNo
     * @return String
     */
    String queryOrder(String orderNo) throws WxPayApiException;

    /**
     * 检查订单状态
     *
     * @param orderNo
     */
    void checkOrderStatus(String orderNo) throws WxPayApiException;

    /**
     * 退款
     *
     * @param orderNo
     * @param reason
     */
    void refund(String orderNo, String reason) throws WxPayApiException;

    /**
     * 查询退款
     *
     * @param refundNo
     * @return
     */
    String queryRefund(String refundNo) throws WxPayApiException;

    /**
     * 处理退款单
     *
     * @param decryptData
     */
    void handleRefund(String decryptData);

    /**
     * 查询帐单
     *
     * @param billDate
     * @param type
     * @return
     */
    String queryBill(String billDate, String type) throws WxPayApiException;

    /**
     * 下载帐单
     *
     * @param billDate
     * @param type
     * @return
     */
    String downloadBill(String billDate, String type) throws WxPayApiException;

    /**
     * 处理通知
     *
     * @param request
     * @return
     */
    String handleNotify(HttpServletRequest request) throws WxPayApiException;

    /**
     * 处理get请求
     *
     * @param uri
     * @return
     */
    String handleGetRequest(String uri) throws WxPayApiException;

    /**
     * 处理post请求
     *
     * @param uri
     * @param requestData
     * @return
     */
    String handlePostRequest(String uri, String requestData) throws WxPayApiException;

    /**
     * 处理不需要验签的get请求
     *
     * @param downloadUrl
     * @return
     */
    String handleGetRequestForNoSign(String downloadUrl) throws WxPayApiException;
}
