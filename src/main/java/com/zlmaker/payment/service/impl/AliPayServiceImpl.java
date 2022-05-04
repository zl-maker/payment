package com.zlmaker.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.zlmaker.payment.config.AliPayClientConfig;
import com.zlmaker.payment.constant.GlobalConstant;
import com.zlmaker.payment.enums.AliPayTradeState;
import com.zlmaker.payment.enums.OrderStatus;
import com.zlmaker.payment.enums.PayType;
import com.zlmaker.payment.pojo.OrderInfo;
import com.zlmaker.payment.pojo.RefundInfo;
import com.zlmaker.payment.service.AliPayService;
import com.zlmaker.payment.service.OrderInfoService;
import com.zlmaker.payment.service.PaymentInfoService;
import com.zlmaker.payment.service.RefundInfoService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 支付宝支付服务实现类
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.service.impl
 * @className AliPayServiceImpl
 * @date 2022/4/30 下午4:44
 */
@Service
@Slf4j
public class AliPayServiceImpl
        implements AliPayService {
    /**
     * 重入锁
     */
    private final ReentrantLock lock = new ReentrantLock();
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private AliPayClientConfig aliPayClientConfig;
    @Autowired
    private RefundInfoService refundInfoService;
    @Autowired
    private PaymentInfoService paymentInfoService;


    /**
     * 创建订单,如果失败会自动回滚
     *
     * @param productId
     * @return String
     */
    @Override
    @Transactional(rollbackFor = AlipayApiException.class)
    public String tradeCreate(Long productId) throws AlipayApiException {
        // 生成订单
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId, PayType.ALIPAY.getType());
        // 调用支付宝接口
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 配置需要的公共请求参数
        // 支付完成后，支付宝发起异步通知的地址
        request.setNotifyUrl(aliPayClientConfig.getNotifyUrl());
        // 支付完成后，我们想让页面跳转回的页面，配置returnUrl
        request.setReturnUrl(aliPayClientConfig.getReturnUrl());
        // 组装当前业务方法的请求参数
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(orderInfo.getOrderNo());
        model.setTotalAmount(String.valueOf(orderInfo.getTotalFee()));
        model.setSubject(orderInfo.getTitle());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        // 装换为BizModel
        request.setBizModel(model);
        // 执行请求，调用支付宝接口
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);

        if (response.isSuccess()) {
            return response.getBody();
        }
        log.error(response.getSubMsg());
        throw new AlipayApiException(response.getSubMsg());

    }


    /**
     * 关闭订单
     *
     * @param orderNo
     * @param orderStatus
     * @throws AlipayApiException
     */
    @Override
    public void closeOrder(String orderNo, OrderStatus orderStatus) throws AlipayApiException {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeCloseResponse response = alipayClient.execute(request);
        if (!response.isSuccess()) {
            if (GlobalConstant.TRADE_NOT_EXIST.equals(response.getSubCode())) {
                // 更新商户端的订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
            }
            log.error(response.getSubMsg());
            throw new AlipayApiException(response.getSubMsg());
        }
        // 更新商户端的订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, orderStatus);
    }

    /**
     * 查询订单
     *
     * @param orderNo
     * @return
     */
    @SneakyThrows
    @Override
    public String queryOrder(String orderNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            return response.getBody();
        }
        log.error(response.getSubMsg());
        return null;
    }

    /**
     * 检查订单状态
     *
     * @param orderNo
     */
    @SneakyThrows
    @Override
    public void checkOrderStatus(String orderNo) {
        // 查询订单
        String result = this.queryOrder(orderNo);
        // 判断订单是否创建
        if (result == null) {
            log.error("核实订单未创建");
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
            return;
        }
        // 解析查单响应结果
        JSONObject resultObject = JSONObject.parseObject(result);
        JSONObject tradeQueryResponse = resultObject.getJSONObject("alipay_trade_query_response");
        String tradeStatus = tradeQueryResponse.getString("trade_status");
        if (AliPayTradeState.SUCCESS.getType().equals(tradeStatus)) {
            // 更新商户端订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
            // 记录支付日志
            BigDecimal totalAmount = tradeQueryResponse.getBigDecimal("total_amount");
            Map<String, String> map = tradeQueryResponse.toJavaObject(Map.class);
            paymentInfoService.createPaymentInfoForAliPay(orderNo, totalAmount, tradeStatus, map);
        }
        if (AliPayTradeState.NOTPAY.getType().equals(tradeStatus)) {
            log.error("核实订单未支付");
            // 调用关单接口
            this.closeOrder(orderNo, OrderStatus.CLOSED);
        }
    }

    /**
     * 退款
     *
     * @param orderNo
     * @param reason
     */
    @Override
    @Transactional(rollbackFor = AlipayApiException.class)
    public void refund(String orderNo, String reason) throws AlipayApiException {
        // 创建退款单
        RefundInfo refundInfo = refundInfoService.createRefundByOrderNoForAliPay(orderNo, reason);

        // 调用统一收单交易退款接口
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        // 组装当前业务方法的请求参数
        JSONObject bizContent = new JSONObject();
        // 订单编号
        bizContent.put("out_trade_no", orderNo);
        BigDecimal refund = refundInfo.getRefund();
        // 退款金额：不能大于支付金额
        bizContent.put("refund_amount", refund);
        // 退款原因(可选)
        bizContent.put("refund_reason", reason);
        request.setBizContent(bizContent.toString());

        // 执行请求，调用支付宝接口
        AlipayTradeRefundResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            // 更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);
            // 更新退款单
            refundInfoService.updateRefundForAliPay(
                    refundInfo.getRefundNo(),
                    response.getBody(),
                    AliPayTradeState.REFUND_SUCCESS.getType());

        } else {
            log.error("退款失败");
            //更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_ABNORMAL);
            //更新退款单
            refundInfoService.updateRefundForAliPay(
                    refundInfo.getRefundNo(),
                    response.getBody(),
                    AliPayTradeState.REFUND_ERROR.getType());
            throw new AlipayApiException("退款失败");
        }
    }

    /**
     * 查询退款
     *
     * @param orderNo
     * @return
     */
    @Override
    public String queryRefund(String orderNo) throws AlipayApiException {
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderNo);
        bizContent.put("out_request_no", orderNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            return response.getBody();
        }
        log.error("查询退款失败");
        throw new AlipayApiException("查询退款失败");
    }

    /**
     * 查询帐单
     *
     * @param billDate
     * @param type
     * @return
     */
    @Override
    public String queryBill(String billDate, String type) throws AlipayApiException {
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("bill_type", type);
        bizContent.put("bill_date", billDate);
        request.setBizContent(bizContent.toString());
        AlipayDataDataserviceBillDownloadurlQueryResponse response = alipayClient.execute(request);

        if (response.isSuccess()) {
            //获取账单下载地址
            JSONObject responseBody = JSONObject.parseObject(response.getBody());
            JSONObject billDownloadurlResponse = responseBody.getJSONObject("alipay_data_dataservice_bill_downloadurl_query_response");
            return billDownloadurlResponse.getString("bill_download_url");
        }
        log.error("获取下载地址失败");
        throw new AlipayApiException("获取下载地址失败");
    }

    /**
     * 对支付结果中的具体业务信息进行二次校验
     *
     * @param requestParams
     * @return
     */
    @Override
    public String checkRequestParams(Map<String, String> requestParams) {
        // 校验订单
        String outTradeNo = requestParams.get("out_trade_no");
        OrderInfo orderInfo = orderInfoService.getOrderByOrderNo(outTradeNo);
        String result = "failure";
        if (orderInfo == null) {
            log.error("订单不存在");
            return result;
        }
        // 校验金额
        String totalAmount = requestParams.get("total_amount");
        BigDecimal totalAmount1 = new BigDecimal(totalAmount);
        BigDecimal totalAmount2 = orderInfo.getTotalFee();
        if (!totalAmount1.equals(totalAmount2)) {
            log.error("总金额不一致");
            return result;
        }
        // 校验商户id
        String sellerId = requestParams.get("seller_id");
        String configSellerId = aliPayClientConfig.getSellerId();
        if (!Objects.equals(sellerId, configSellerId)) {
            log.error("商家pid校验失败");
            return result;
        }
        // 校验应用id
        String appId = requestParams.get("app_id");
        String configAppId = aliPayClientConfig.getAppId();
        if (!Objects.equals(appId, configAppId)) {
            log.error("应用id校验失败");
            return result;
        }
        // 校验用户支付状态
        String tradeStatus = requestParams.get("trade_status");
        if (!GlobalConstant.TRADE_STATUS_SUCCESS.equals(tradeStatus)) {
            log.error("用户支付未成功");
            return result;
        }
        // 校验成功
        result = "success";
        // 处理通知请求
        this.handleNotifyRequest(outTradeNo, totalAmount1, tradeStatus, requestParams);
        return result;
    }

    /**
     * 处理通知请求
     *
     * @param outTradeNo
     * @param totalAmount
     * @param tradeStatus
     * @param requestParams
     */
    @Override
    public void handleNotifyRequest(String outTradeNo, BigDecimal totalAmount, String tradeStatus, Map<String, String> requestParams) {
        // 获取订单状态
        String orderStatus = orderInfoService.getOrderStatus(outTradeNo);
        // 尝试获取锁，成功则返回true,失败则返回false,不必一直等待
        if (lock.tryLock()) {
            try {
                //处理重复通知
                //接口调用的幂等性：无论接口被调用多少次，以下业务执行一次
                if (OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    // 更新订单状态
                    orderInfoService.updateStatusByOrderNo(outTradeNo, OrderStatus.SUCCESS);
                    // 记录支付日志
                    paymentInfoService.createPaymentInfoForAliPay(outTradeNo, totalAmount, tradeStatus, requestParams);
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
