package com.zlmaker.payment.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 订单号工具类
 *
 * @author zl-maker
 */
public class OrderNoUtil {

    /**
     * 获取订单编号
     *
     * @return
     */
    public static String getOrderNo() {
        return "ORDER_" + getNo();
    }

    /**
     * 获取退款单编号
     *
     * @return
     */
    public static String getRefundNo() {
        return "REFUND_" + getNo();
    }

    /**
     * 获取编号
     *
     * @return
     */
    public static String getNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }

}
