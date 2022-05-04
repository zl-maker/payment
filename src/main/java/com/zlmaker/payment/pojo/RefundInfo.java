package com.zlmaker.payment.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * @author zl-maker
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_refund_info")
public class RefundInfo
        extends BaseEntity {

    /**
     * 商品订单编号
     */
    private String orderNo;

    /**
     * 退款单编号
     */
    private String refundNo;

    /**
     * 支付系统退款单号
     */
    private String refundId;

    /**
     * 原订单金额
     */
    private BigDecimal totalFee;

    /**
     * 退款金额
     */
    private BigDecimal refund;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 退款单状态
     */
    private String refundStatus;

    /**
     * 申请退款返回参数
     */
    private String contentReturn;

    /**
     * 退款结果通知参数
     */
    private String contentNotify;
}
