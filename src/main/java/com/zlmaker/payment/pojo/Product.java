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
@TableName("t_product")
public class Product
        extends BaseEntity {

    /**
     * 商品名称
     */
    private String title;
    /**
     * 价格
     */
    private BigDecimal price;
}
