package com.zlmaker.payment.controller;

import com.zlmaker.payment.enums.OrderStatus;
import com.zlmaker.payment.pojo.OrderInfo;
import com.zlmaker.payment.pojo.ResponseResult;
import com.zlmaker.payment.service.OrderInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单管理前端控制器
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.controller
 * @className OrderInfoController
 * @date 2022/4/26 下午2:56
 */
@CrossOrigin
@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/order-info")
public class OrderInfoController {
    @Autowired
    private OrderInfoService orderInfoService;

    @GetMapping("/list")
    @ApiOperation("获得所有订单列表")
    public ResponseResult getAllOrderInfo() {
        List<OrderInfo> orderInfoList = orderInfoService.getAllOrderInfo();
        return ResponseResult.success("获取订单列表成功", orderInfoList);
    }

    @GetMapping("/query-order-status/{orderNo}")
    @ApiOperation("查询订单状态")
    public ResponseResult queryOrderStatus(@PathVariable String orderNo) {
        String orderStatus = orderInfoService.getOrderStatus(orderNo);
        if (OrderStatus.SUCCESS.getType().equals(orderStatus)) {
            return ResponseResult.success("支付成功");
        }
        return ResponseResult.success("支付中...", 101);
    }
}
