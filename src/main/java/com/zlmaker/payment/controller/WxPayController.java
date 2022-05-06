package com.zlmaker.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.zlmaker.payment.exception.WxPayApiException;
import com.zlmaker.payment.pojo.ResponseResult;
import com.zlmaker.payment.service.WxPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 微信支付前端控制器
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.controller
 * @className WechatPayController
 * @date 2022/4/24 下午11:27
 */
@CrossOrigin
@RestController
@RequestMapping("/api/wx-pay")
@Api(tags = "网站微信支付API")
public class WxPayController {
    @Autowired
    private WxPayService wxPayService;

    @ApiOperation("下单支付，生成支付二维码")
    @PostMapping("/native/{productId}")
    public ResponseResult nativePay(@PathVariable Long productId) throws WxPayApiException {
        String result = wxPayService.nativePay(productId);
        return ResponseResult.success("下单成功", JSONObject.parseObject(result));
    }

    @SneakyThrows
    @ApiOperation("订单通知")
    @PostMapping("/native/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response) {
        JSONObject responseObject = new JSONObject();
        // 处理通知
        String decryptData = wxPayService.handleNotify(request);
        if (decryptData == null) {
            // 设置失败响应状态码,封装响应体
            response.setStatus(500);
            responseObject.put("code", "ERROR");
            responseObject.put("message", "失败");
            return responseObject.toJSONString();
        }
        // 处理订单
        wxPayService.handleOrder(decryptData);
        // 设置成功响应状态码,封装响应体
        response.setStatus(200);
        responseObject.put("code", "SUCCESS");
        responseObject.put("message", "成功");
        return responseObject.toJSONString();

    }

    @ApiOperation("用户取消订单")
    @PostMapping("/cancel/{orderNo}")
    public ResponseResult cancel(@PathVariable String orderNo) throws WxPayApiException {
        wxPayService.cancelOrder(orderNo);
        return ResponseResult.success("订单已取消");
    }

    @ApiOperation("查询订单：测试专用")
    @GetMapping("/query-order/{orderNo}")
    public ResponseResult queryOrder(@PathVariable String orderNo) throws WxPayApiException {
        String result = wxPayService.queryOrder(orderNo);
        return ResponseResult.success("查询成功", result);
    }

    @ApiOperation("申请退款")
    @PostMapping("/refund/{orderNo}/{reason}")
    public ResponseResult refund(@PathVariable String orderNo, @PathVariable String reason) throws WxPayApiException {
        wxPayService.refund(orderNo, reason);
        return ResponseResult.success("退款成功");
    }

    @ApiOperation("查询退款：测试专用")
    @GetMapping("/query-refund/{refundNo}")
    public ResponseResult queryRefund(@PathVariable String refundNo) throws WxPayApiException {
        String result = wxPayService.queryRefund(refundNo);
        return ResponseResult.success("查询成功", result);
    }

    @SneakyThrows
    @ApiOperation("退款结果通知")
    @PostMapping("/refund/notify")
    public String refundNotify(HttpServletRequest request, HttpServletResponse response) {
        JSONObject responseObject = new JSONObject();
        // 处理通知
        String decryptData = wxPayService.handleNotify(request);
        if (decryptData == null) {
            // 设置失败响应状态码,封装响应体
            response.setStatus(500);
            responseObject.put("code", "ERROR");
            responseObject.put("message", "失败");
            return responseObject.toJSONString();
        }
        // 处理退款单
        wxPayService.handleRefund(decryptData);
        // 设置成功响应状态码,封装响应体
        response.setStatus(200);
        responseObject.put("code", "SUCCESS");
        responseObject.put("message", "成功");
        return responseObject.toJSONString();
    }

    @ApiOperation("获取帐单url：测试专用")
    @GetMapping("/query-bill/{billDate}/{type}")
    public ResponseResult queryTradeBill(@PathVariable String billDate, @PathVariable String type) throws WxPayApiException {
        String downloadUrl = wxPayService.queryBill(billDate, type);
        return ResponseResult.success("获取帐单成功", downloadUrl);
    }

    @ApiOperation("下载帐单")
    @GetMapping("/downloadbill/{billDate}/{type}")
    public ResponseResult downloadBill(@PathVariable String billDate, @PathVariable String type) throws WxPayApiException {
        String result = wxPayService.downloadBill(billDate, type);
        return ResponseResult.success("下载帐单成功", result);
    }
}
