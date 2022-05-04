package com.zlmaker.payment.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zl-maker
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_payment_info")
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfo
        extends BaseEntity {

    /**
     * 商品订单编号
     */
    private String orderNo;
    /**
     * 支付系统交易编号
     */
    private String transactionId;
    /**
     * 支付类型
     */
    private String paymentType;
    /**
     * 交易类型
     */
    private String tradeType;

    /**
     * 交易状态
     */
    private String tradeState;
    /**
     * 支付金额
     */
    private BigDecimal totalFee;

    /**
     * 通知参数
     */
    private String content;
}
