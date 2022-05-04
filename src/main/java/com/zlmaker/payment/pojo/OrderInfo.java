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
@TableName("t_order_info")
public class OrderInfo
        extends BaseEntity {
    /**
     * 订单标题
     */
    private String title;
    /**
     * 商户订单编号
     */
    private String orderNo;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 支付产品id
     */
    private Long productId;
    /**
     * 订单金额
     */
    private BigDecimal totalFee;
    /**
     * 订单二维码连接
     */
    private String codeUrl;
    /**
     * 订单状态
     */
    private String orderStatus;
    /**
     * 支付类型
     */
    private String paymentType;
}
