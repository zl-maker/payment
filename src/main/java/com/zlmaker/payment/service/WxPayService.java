package com.zlmaker.payment.service;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

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
    String nativePay(Long productId) throws Exception;

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
    void cancelOrder(String orderNo) throws IOException;

    /**
     * 关闭订单
     *
     * @param orderNo
     */
    void closeOrder(String orderNo) throws IOException;

    /**
     * 查询订单：测试订单状态
     *
     * @param orderNo
     * @return String
     */
    String queryOrder(String orderNo) throws IOException;

    /**
     * 检查订单状态
     *
     * @param orderNo
     */
    void checkOrderStatus(String orderNo) throws IOException;

    /**
     * 退款
     *
     * @param orderNo
     * @param reason
     */
    void refund(String orderNo, String reason) throws IOException;

    /**
     * 查询退款
     *
     * @param refundNo
     * @return
     */
    String queryRefund(String refundNo) throws IOException;

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
    String queryBill(String billDate, String type) throws Exception;

    /**
     * 下载帐单
     *
     * @param billDate
     * @param type
     * @return
     */
    String downloadBill(String billDate, String type) throws Exception;

    /**
     * 处理通知
     * @param request
     * @param response
     * @param requestBody
     * @return
     */
    String handleNotify(HttpServletRequest request, HttpServletResponse response, JSONObject requestBody);

    /**
     * 处理get请求
     * @param uri
     * @return
     */
    String handleGetRequest(String uri) throws IOException;

    /**
     * 处理post请求
     * @param uri
     * @param requestData
     * @return
     */
    String handlePostRequest(String uri, String requestData) throws IOException;
}
