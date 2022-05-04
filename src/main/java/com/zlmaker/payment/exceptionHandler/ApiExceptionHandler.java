package com.zlmaker.payment.exceptionHandler;

import com.alipay.api.AlipayApiException;
import com.zlmaker.payment.exception.WxPayApiException;
import com.zlmaker.payment.pojo.ResponseResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.payment.exceptionHandler
 * @className GlobalExceptionHandler
 * @date 2022/5/1 下午11:03
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(AlipayApiException.class)
    public ResponseResult aliPayApiExceptionHandle(AlipayApiException e) {
        return ResponseResult.error(e.getMessage());
    }

    @ExceptionHandler(WxPayApiException.class)
    public ResponseResult wxPayApiExceptionHandle(WxPayApiException e) {
        return ResponseResult.error(e.getMessage());
    }
}
