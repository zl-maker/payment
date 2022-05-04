package com.zlmaker.payment.task;

import com.zlmaker.payment.enums.PayType;
import com.zlmaker.payment.pojo.OrderInfo;
import com.zlmaker.payment.service.AliPayService;
import com.zlmaker.payment.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 支付宝支付定时任务
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.task
 * @className AliPayTask
 * @date 2022/5/1 下午2:57
 */
@Component
public class AliPayTask {
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private AliPayService aliPayService;

    /**
     * 从第0秒开始每隔30秒执行一次，查询创建超过5分钟，并且未支付的订单
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm() {
        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByDuration(2, PayType.ALIPAY.getType());
        System.out.println("===========遍历支付宝支付超时订单========");
        for (OrderInfo orderInfo : orderInfoList) {
            String orderNo = orderInfo.getOrderNo();
            aliPayService.checkOrderStatus(orderNo);
        }
    }

}
