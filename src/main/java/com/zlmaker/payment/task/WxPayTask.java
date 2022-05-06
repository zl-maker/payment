package com.zlmaker.payment.task;

import com.zlmaker.payment.enums.PayType;
import com.zlmaker.payment.exception.WxPayApiException;
import com.zlmaker.payment.pojo.OrderInfo;
import com.zlmaker.payment.service.OrderInfoService;
import com.zlmaker.payment.service.WxPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * 微信支付定时任务
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.task
 * @className WxPayTask
 * @date 2022/4/28 上午12:36
 */
@Component
public class WxPayTask {
    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private WxPayService wxPayService;

    /**
     * 从第0秒开始每隔30秒执行一次，查询创建超过5分钟，并且未支付的订单
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm() throws WxPayApiException, IOException {
        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByDuration(2, PayType.WXPAY.getType());
        System.out.println("===========遍历微信支付超时订单========");
        for (OrderInfo orderInfo : orderInfoList) {
            String orderNo = orderInfo.getOrderNo();
            wxPayService.checkOrderStatus(orderNo);
        }
    }


}
