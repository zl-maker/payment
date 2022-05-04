package com.zlmaker.payment.util;

import java.math.BigDecimal;

/**
 * 微信金额格式化工具
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.payment.util
 * @className WxTotalFee
 * @date 2022/5/2 下午9:34
 */
public class WxTotalFeeUtil {
    /**
     * 将总金额乘以100,即转化为微信所需要的分为单位
     *
     * @param totalFee
     * @return
     */
    public static int totalFeeMultiply(BigDecimal totalFee) {
        return totalFee.multiply(new BigDecimal(100)).intValue();
    }

    public static BigDecimal totalFeeDivide(int totalFee) {
        return new BigDecimal(totalFee).divide(new BigDecimal(100));
    }
}
