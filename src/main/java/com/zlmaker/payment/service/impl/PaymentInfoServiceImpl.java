package com.zlmaker.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zlmaker.payment.enums.PayType;
import com.zlmaker.payment.mapper.PaymentInfoMapper;
import com.zlmaker.payment.pojo.PaymentInfo;
import com.zlmaker.payment.service.PaymentInfoService;
import com.zlmaker.payment.util.WxTotalFeeUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author zl-maker
 */
@Service
public class PaymentInfoServiceImpl
        extends ServiceImpl<PaymentInfoMapper, PaymentInfo>
        implements PaymentInfoService {

    /**
     * 记录支付日志
     *
     * @param orderData
     */
    @Override
    public void createPaymentInfo(JSONObject orderData) {
        // 从订单数据中获取相对应的值
        String orderNo = orderData.getString("out_trade_no");
        String transactionId = orderData.getString("transaction_id");
        String tradeType = orderData.getString("trade_type");
        String tradeState = orderData.getString("trade_state");
        JSONObject amount = orderData.getJSONObject("amount");
        int payerTotal = amount.getIntValue("payer_total");
        String content = orderData.toJSONString();
        // 封装成订单日志
        PaymentInfo paymentInfo = new PaymentInfo(orderNo, transactionId, PayType.WXPAY.getType(), tradeType, tradeState,
                WxTotalFeeUtil.totalFeeDivide(payerTotal), content);
        baseMapper.insert(paymentInfo);
    }

    /**
     * 记录支付日志
     *
     * @param orderNo
     * @param totalAmount
     * @param tradeStatus
     * @param requestParams
     */
    @Override
    public void createPaymentInfoForAliPay(String orderNo, BigDecimal totalAmount, String tradeStatus, Map<String, String> requestParams) {
        String paramsContent = JSONObject.toJSONString(requestParams);
        // 封装日志对象
        PaymentInfo paymentInfo = new PaymentInfo(orderNo, requestParams.get("transaction_id"), PayType.ALIPAY.getType(), "电脑网站支付", tradeStatus, totalAmount, paramsContent);
        baseMapper.insert(paymentInfo);
    }
}
