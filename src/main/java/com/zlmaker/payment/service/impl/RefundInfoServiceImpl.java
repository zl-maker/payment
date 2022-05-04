package com.zlmaker.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zlmaker.payment.mapper.RefundInfoMapper;
import com.zlmaker.payment.pojo.OrderInfo;
import com.zlmaker.payment.pojo.RefundInfo;
import com.zlmaker.payment.service.OrderInfoService;
import com.zlmaker.payment.service.RefundInfoService;
import com.zlmaker.payment.util.OrderNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zl-maker
 */
@Service
public class RefundInfoServiceImpl
        extends ServiceImpl<RefundInfoMapper, RefundInfo>
        implements RefundInfoService {
    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * 根据订单编号创建退款单
     *
     * @param orderNo
     * @param reason
     * @return
     */
    @Override
    public RefundInfo createRefundByOrderNo(String orderNo, String reason) {
        // 根据订单编号获得订单
        OrderInfo orderInfo = orderInfoService.getOrderByOrderNo(orderNo);
        RefundInfo refundInfo = new RefundInfo();
        // 订单编号
        refundInfo.setOrderNo(orderNo);
        // 退款单编号
        refundInfo.setRefundNo(OrderNoUtil.getRefundNo());
        // 原订单金额
        refundInfo.setTotalFee(orderInfo.getTotalFee());
        // 退款金额
        refundInfo.setRefund(orderInfo.getTotalFee());
        // 退款原因
        refundInfo.setReason(reason);
        baseMapper.insert(refundInfo);
        return refundInfo;
    }

    /**
     * 更新退款单
     *
     * @param responseBody
     */
    @Override
    public void updateRefund(String responseBody) {
        JSONObject responseObject = JSONObject.parseObject(responseBody);
        // 根据退款单编号修改退款单
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("refund_no", responseObject.getString("out_refund_no"));
        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setRefundId(responseObject.getString("refund_id"));
        // 修改退款的状态&将响应结果存入数据库content字段
        String status = responseObject.getString("status");
        String refundStatus = responseObject.getString("refund_status");
        // 请求退款的判断
        if (status != null) {
            refundInfo.setRefundStatus(status);
            refundInfo.setContentReturn(responseBody);
        }
        // 退款通知的判断
        if (refundStatus != null) {
            refundInfo.setRefundStatus(refundStatus);
            refundInfo.setContentNotify(responseBody);
        }
        // 更新退款单
        baseMapper.update(refundInfo, queryWrapper);

    }

    /**
     * 根据订单号创建支付宝支付退款单
     *
     * @param orderNo
     * @param reason
     * @return
     */
    @Override
    public RefundInfo createRefundByOrderNoForAliPay(String orderNo, String reason) {
        // 根据订单号获取订单信息
        OrderInfo orderInfo = orderInfoService.getOrderByOrderNo(orderNo);
        // 根据订单号生成退款订单
        RefundInfo refundInfo = new RefundInfo();
        // 订单编号
        refundInfo.setOrderNo(orderNo);
        // 退款单编号
        refundInfo.setRefundNo(OrderNoUtil.getRefundNo());
        // 原订单金额(分)
        refundInfo.setTotalFee(orderInfo.getTotalFee());
        // 退款金额(分)
        refundInfo.setRefund(orderInfo.getTotalFee());
        // 退款原因
        refundInfo.setReason(reason);
        // 保存退款订单
        baseMapper.insert(refundInfo);
        return refundInfo;
    }

    /**
     * 更新支付宝支付退款单
     *
     * @param refundNo
     * @param body
     * @param type
     */
    @Override
    public void updateRefundForAliPay(String refundNo, String body, String type) {
        //根据退款单编号修改退款单
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("refund_no", refundNo);

        // 设置要修改的字段
        RefundInfo refundInfo = new RefundInfo();
        // 退款状态
        refundInfo.setRefundStatus(type);
        // 将全部响应结果存入数据库的content字段
        refundInfo.setContentReturn(body);

        //更新退款单
        baseMapper.update(refundInfo, queryWrapper);

    }
}
