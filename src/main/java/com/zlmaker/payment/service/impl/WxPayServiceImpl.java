package com.zlmaker.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import com.zlmaker.payment.config.WxPayConfig;
import com.zlmaker.payment.constant.GlobalConstant;
import com.zlmaker.payment.enums.*;
import com.zlmaker.payment.pojo.OrderInfo;
import com.zlmaker.payment.pojo.RefundInfo;
import com.zlmaker.payment.service.OrderInfoService;
import com.zlmaker.payment.service.PaymentInfoService;
import com.zlmaker.payment.service.RefundInfoService;
import com.zlmaker.payment.service.WxPayService;
import com.zlmaker.payment.util.WxTotalFeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 微信支付服务接口实现类
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.service.impl
 * @className WechatPayServiceImpl
 * @date 2022/4/24 下午11:32
 */
@Service
@Slf4j
public class WxPayServiceImpl
        implements WxPayService {
    /**
     * 可重入锁
     * 采用数据锁进行并发控制，
     * 以避免函数重入造成的数据混乱
     */
    private final ReentrantLock lock = new ReentrantLock();
    @Autowired
    private WxPayConfig wxPayConfig;
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private CloseableHttpClient wxPayClient;
    @Autowired
    private PaymentInfoService paymentInfoService;
    @Autowired
    private RefundInfoService refundInfoService;
    @Autowired
    private NotificationHandler notificationHandler;

    /**
     * 创建订单，如果失败会自动回滚，调用native接口
     *
     * @param productId
     * @return String
     */
    @Override
    @Transactional
    public String nativePay(Long productId) throws Exception {
        //生成订单
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId, PayType.WXPAY.getType());
        String orderNo = orderInfo.getOrderNo();
        String oldCodeUrl = orderInfo.getCodeUrl();
        if (StringUtils.hasText(oldCodeUrl)) {
            JSONObject result = new JSONObject();
            result.put("codeUrl", oldCodeUrl);
            result.put("orderNo", orderNo);
            return result.toJSONString();
        }

        // 请求body参数
        JSONObject paramsObject = new JSONObject();
        paramsObject.put("appid", wxPayConfig.getAppid());
        paramsObject.put("mchid", wxPayConfig.getMchId());
        paramsObject.put("description", orderInfo.getTitle());
        paramsObject.put("out_trade_no", orderNo);
        paramsObject.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxPayNotifyType.NATIVE_NOTIFY.getType()));
        JSONObject amountObject = new JSONObject();
        amountObject.put("total", WxTotalFeeUtil.totalFeeMultiply(orderInfo.getTotalFee()));
        amountObject.put("currency", "CNY");
        paramsObject.put("amount", amountObject);
        // 转换成json字符串
        String requestData = JSONObject.toJSONString(paramsObject);
        // 获取连接微信发送请求
        String uri = wxPayConfig.getDomain().concat(WxPayApiType.NATIVE_PAY.getType());

        // 处理post请求
        String responseBody = this.handlePostRequest(uri, requestData);

        // 转换为响应结果
        JSONObject resultObject = JSONObject.parseObject(responseBody);
        // 保存二维码
        String codeUrl = resultObject.getString("code_url");
        orderInfoService.saveCodeUrl(orderNo, codeUrl);
        // 封装响应体
        JSONObject result = new JSONObject();
        result.put("codeUrl", codeUrl);
        result.put("orderNo", orderNo);
        return result.toJSONString();

    }

    /**
     * 处理订单
     *
     * @param decryptData
     */
    @Override
    @Transactional
    public void handleOrder(String decryptData) {
        JSONObject orderData = JSONObject.parseObject(decryptData);
        String orderNo = orderData.getString("out_trade_no");
        // 尝试获取锁，成功则返回true,失败则返回false,不必一直等待
        if (lock.tryLock()) {
            try {
                // 处理重复通知
                // 接口调用的幂等性原则，即无论调用多少次接口，产生的结果都是一致的
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    return;
                }
                // 更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
                // 记录支付日志
                paymentInfoService.createPaymentInfo(orderData);
            } finally {
                lock.unlock();
            }

        }
    }

    /**
     * 用户取消订单
     *
     * @param orderNo
     */
    @Override
    @Transactional
    public void cancelOrder(String orderNo) throws IOException {
        // 关闭订单
        this.closeOrder(orderNo);
        // 更新商户端的订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);
    }

    /**
     * 关闭订单
     *
     * @param orderNo
     */
    @Override
    public void closeOrder(String orderNo) throws IOException {
        // 创建远程请求对象
        String uri = String.format(WxPayApiType.CLOSE_ORDER_BY_NO.getType(), orderNo);
        uri = wxPayConfig.getDomain().concat(uri);
        // 封装json请求体
        JSONObject requestObject = new JSONObject();
        requestObject.put("mchid", wxPayConfig.getMchId());
        String requestJson = requestObject.toJSONString();
        // 发起post请求
        this.handlePostRequest(uri, requestJson);
    }

    /**
     * 查询订单：测试订单状态
     *
     * @param orderNo
     * @return String
     */
    @Override
    public String queryOrder(String orderNo) throws IOException {
        // 创建远程请求对象
        String uri = String.format(WxPayApiType.ORDER_QUERY_BY_NO.getType(), orderNo);
        uri = wxPayConfig.getDomain().concat(uri).concat("?mchid=").concat(wxPayConfig.getMchId());
        return this.handleGetRequest(uri);
    }

    /**
     * 检查订单状态
     * 根据订单号查询微信支付查单接口，核实订单状态
     * 如果订单已支付，则更新商户端订单状态
     * 如果订单没支付，则调用关单接口关闭订单，并更新商户端订单状态
     *
     * @param orderNo
     */
    @Override
    @Transactional
    public void checkOrderStatus(String orderNo) throws IOException {
        String result = this.queryOrder(orderNo);
        JSONObject resultObject = JSONObject.parseObject(result);
        // 获取微信支付端订单状态
        String tradeState = resultObject.getString("trade_state");
        if (WxPayTradeState.SUCCESS.getType().equals(tradeState)) {
            // 更新商户端订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
            // 记录支付日志
            paymentInfoService.createPaymentInfo(resultObject);
        }
        if (WxPayTradeState.NOTPAY.getType().equals(tradeState)) {
            // 调用关单接口
            this.closeOrder(orderNo);
            // 更新商户端订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
        }
    }

    /**
     * 退款
     *
     * @param orderNo
     * @param reason
     */
    @Override
    @Transactional
    public void refund(String orderNo, String reason) throws IOException {
        // 根据订单编号创建退款单
        RefundInfo refundInfo = refundInfoService.createRefundByOrderNo(orderNo, reason);

        //调用统一下单API
        String uri = wxPayConfig.getDomain().concat(WxPayApiType.DOMESTIC_REFUNDS.getType());

        // 请求body参数
        JSONObject requestObject = new JSONObject();
        // 订单编号
        requestObject.put("out_trade_no", orderNo);
        // 退款单编号
        requestObject.put("out_refund_no", refundInfo.getRefundNo());
        // 退款原因
        requestObject.put("reason", reason);
        // 退款通知地址
        requestObject.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxPayNotifyType.REFUND_NOTIFY.getType()));

        JSONObject amountObject = new JSONObject();
        //退款金额
        amountObject.put("refund", WxTotalFeeUtil.totalFeeMultiply(refundInfo.getRefund()));
        // 原订单金额
        amountObject.put("total", WxTotalFeeUtil.totalFeeMultiply(refundInfo.getTotalFee()));
        // 退款币种
        amountObject.put("currency", "CNY");
        requestObject.put("amount", amountObject);

        // 将参数转换成json字符串
        String requestJson = JSONObject.toJSONString(requestObject);

        // 发起post请求
        String responseBody = this.handlePostRequest(uri, requestJson);

        // 更新订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_PROCESSING);

        // 更新退款单
        refundInfoService.updateRefund(responseBody);


    }

    /**
     * 查询退款
     *
     * @param refundNo
     * @return
     */
    @Override
    public String queryRefund(String refundNo) throws IOException {
        // 创建远程请求对象
        String uri = String.format(WxPayApiType.DOMESTIC_REFUNDS_QUERY.getType(), refundNo);
        uri = wxPayConfig.getDomain().concat(uri);
        return this.handleGetRequest(uri);
    }

    /**
     * 处理退款单
     *
     * @param decryptData
     */
    @Override
    @Transactional
    public void handleRefund(String decryptData) {
        JSONObject orderData = JSONObject.parseObject(decryptData);
        String orderNo = orderData.getString("out_trade_no");
        // 尝试获取锁，成功则返回true,失败则返回false,不必一直等待
        if (lock.tryLock()) {
            try {
                // 处理重复通知
                // 接口调用的幂等性原则，即无论调用多少次接口，产生的结果都是一致的
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!OrderStatus.REFUND_PROCESSING.getType().equals(orderStatus)) {
                    return;
                }
                // 更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);
                // 更新退款单
                refundInfoService.updateRefund(decryptData);
            } finally {
                // 要主动释放锁
                lock.unlock();
            }
        }
    }

    /**
     * 查询帐单
     *
     * @param billDate
     * @param type
     * @return
     */
    @Override
    public String queryBill(String billDate, String type) throws Exception {
        String uri;
        if (GlobalConstant.BILL_TYPE_TRADEBILL.equals(type)) {
            uri = WxPayApiType.TRADE_BILLS.getType();
        } else if (GlobalConstant.BILL_TYPE_FUNDFLOWBILL.equals(type)) {
            uri = WxPayApiType.FUND_FLOW_BILLS.getType();
        } else {
            throw new IOException("不支持的帐单类型");
        }
        uri = wxPayConfig.getDomain().concat(uri).concat("?bill_date=").concat(billDate);

        // 处理get请求
        String responseBody = this.handleGetRequest(uri);
        JSONObject responseObject = JSONObject.parseObject(responseBody);
        return responseObject.getString("download_url");
    }

    /**
     * 下载帐单
     *
     * @param billDate
     * @param type
     * @return
     */
    @Override
    public String downloadBill(String billDate, String type) throws Exception {
        String downloadUrl = this.queryBill(billDate, type);
        return this.handleGetRequest(downloadUrl);
    }

    /**
     * 处理通知
     *
     * @param request
     * @param response
     * @param requestBody
     * @return
     */
    @Override
    public String handleNotify(HttpServletRequest request, HttpServletResponse response, JSONObject requestBody) {
        // 处理请求参数
        String serial = request.getHeader("Wechatpay-Serial");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        String signature = request.getHeader("Wechatpay-Signature");
        // 构造微信请求体
        NotificationRequest wxRequest = new NotificationRequest.Builder()
                .withSerialNumber(serial)
                .withNonce(nonce).withTimestamp(timestamp)
                .withSignature(signature)
                .withBody(requestBody.toJSONString()).build();
        // 定义回调通知
        Notification notification;
        // 签名的验证
        try {
            notification = notificationHandler.parse(wxRequest);
        } catch (Exception e) {
            throw new RuntimeException("签名验证失败");
        }
        // 从回调通知中获取解密报文
        return notification.getDecryptData();
    }

    /**
     * 处理get请求
     *
     * @param uri
     * @return
     */
    @Override
    public String handleGetRequest(String uri) throws IOException {
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("Accept", "application/json");
        // 完成签名并执行请求
        try (CloseableHttpResponse response = wxPayClient.execute(httpGet)) {
            // 获取响应体
            String responseBody = EntityUtils.toString(response.getEntity());
            // 获取响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != GlobalConstant.STATUS_CODE_SUCCESS && statusCode != GlobalConstant.STATUS_CODE_SUCCESS_NO_BODY) {
                // 处理失败
                log.error("查询失败");
                throw new IOException("查询失败");
            }
            return responseBody;
        }
    }

    /**
     * 处理post请求
     *
     * @param uri
     * @param requestData
     * @return
     */
    @Override
    public String handlePostRequest(String uri, String requestData) throws IOException {
        String responseBody = null;
        HttpPost httpPost = new HttpPost(uri);
        // 封装数据到请求体
        StringEntity entity = new StringEntity(requestData, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //完成签名并执行请求
        try (CloseableHttpResponse response = wxPayClient.execute(httpPost)) {

            if (response.getEntity() != null) {
                // 获得响应体
                responseBody = EntityUtils.toString(response.getEntity());
            }
            // 获得响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != GlobalConstant.STATUS_CODE_SUCCESS && statusCode != GlobalConstant.STATUS_CODE_SUCCESS_NO_BODY) {
                // 处理失败
                log.error("请求失败");
                throw new IOException("请求失败");
            }
            return responseBody;
        }
    }
}
