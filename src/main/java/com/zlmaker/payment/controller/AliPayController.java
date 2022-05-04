package com.zlmaker.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.zlmaker.payment.config.AliPayClientConfig;
import com.zlmaker.payment.enums.OrderStatus;
import com.zlmaker.payment.pojo.ResponseResult;
import com.zlmaker.payment.service.AliPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/ali-pay")
@Api(tags = "网站支付宝支付")
@Slf4j
public class AliPayController {

    @Autowired
    private AliPayService aliPayService;
    @Autowired
    private AliPayClientConfig aliPayClientConfig;


    @ApiOperation("下单并支付界面")
    @PostMapping("/trade/pay/{productId}")
    public ResponseResult tradePay(@PathVariable Long productId) throws AlipayApiException {
        // 支付宝平台接受request请求对象后，会为开发者生成一个html形式的form表单，包含自动提交的脚本
        String form = aliPayService.tradeCreate(productId);
        return ResponseResult.success("下单成功", form);
    }

    @SneakyThrows
    @ApiOperation("支付通知")
    @PostMapping("/trade/notify")
    public String tradeNotify(@RequestParam Map<String, String> requestParams) {
        // 异步通知验签
        boolean signVerified = AlipaySignature.rsaCheckV1(requestParams, aliPayClientConfig.getAlipayPublicKey(), AlipayConstants.CHARSET_UTF8, AlipayConstants.SIGN_TYPE_RSA2);
        if (!signVerified) {
            log.error("异步通知验签失败");
            return "failure";
        }
        // 对支付结果中的具体业务信息进行二次校验
        return aliPayService.checkRequestParams(requestParams);
    }

    @ApiOperation("用户取消订单")
    @PostMapping("/trade/close/{orderNo}")
    public ResponseResult close(@PathVariable String orderNo) throws AlipayApiException {
        aliPayService.closeOrder(orderNo, OrderStatus.CANCEL);
        return ResponseResult.success("取消订单成功");
    }

    @ApiOperation("查询订单：测试专用")
    @GetMapping("/trade/query/{orderNo}")
    public ResponseResult queryOrder(@PathVariable String orderNo) {
        String result = aliPayService.queryOrder(orderNo);
        return ResponseResult.success("查询订单成功", result);
    }

    @ApiOperation("申请退款")
    @PostMapping("/trade/refund/{orderNo}/{reason}")
    public ResponseResult refund(@PathVariable String orderNo, @PathVariable String reason) throws AlipayApiException {
        aliPayService.refund(orderNo, reason);
        return ResponseResult.success("退款成功");
    }

    @ApiOperation("查询退款：测试专用")
    @GetMapping("/trade/fastpay/refund/{orderNo}")
    public ResponseResult queryRefund(@PathVariable String orderNo) throws AlipayApiException {
        String result = aliPayService.queryRefund(orderNo);
        return ResponseResult.success("查询退款成功", result);
    }

    @ApiOperation("获取帐单url")
    @GetMapping("/bill/downloadurl/{billDate}/{type}")
    public ResponseResult queryTradeBill(@PathVariable String billDate, @PathVariable String type) throws AlipayApiException {
        String downloadUrl = aliPayService.queryBill(billDate, type);
        return ResponseResult.success("获取帐单成功", downloadUrl);
    }
}
