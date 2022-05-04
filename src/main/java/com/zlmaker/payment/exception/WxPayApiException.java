package com.zlmaker.payment.exception;

import com.alipay.api.AlipayApiErrorEnum;

/**
 * 微信支付api异常类
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.exception
 * @className WxPayApiException
 * @date 2022/5/1 下午2:20
 */
public class WxPayApiException
        extends Exception {
    private static final long serialVersionUID = -238091758285157332L;
    private String errCode;
    private String errMsg;

    public WxPayApiException() {
    }

    public WxPayApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public WxPayApiException(AlipayApiErrorEnum messageEnum, Throwable cause) {
        super(messageEnum.getErrMsg(), cause);
    }

    public WxPayApiException(String message) {
        super(message);
    }

    public WxPayApiException(AlipayApiErrorEnum messageEnum) {
        super(messageEnum.getErrMsg());
    }

    public WxPayApiException(Throwable cause) {
        super(cause);
    }

    public WxPayApiException(String errCode, String errMsg) {
        super(errCode + ":" + errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public String getErrCode() {
        return this.errCode;
    }

    public String getErrMsg() {
        return this.errMsg;
    }

}
