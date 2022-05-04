package com.zlmaker.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zlmaker.payment.enums.OrderStatus;
import com.zlmaker.payment.pojo.OrderInfo;

import java.util.List;

/**
 * @author zl-maker
 */
public interface OrderInfoService
        extends IService<OrderInfo> {

    /**
     * 根据商品id创建订单
     *
     * @param productId
     * @param type
     * @return OrderInfo
     */
    OrderInfo createOrderByProductId(Long productId, String type);

    /**
     * 根据商品id获取没有支付的订单
     *
     * @param productId
     * @return OrderInfo
     */
    OrderInfo getNoPayOrderByProductId(Long productId,String type);

    /**
     * 保存二维码
     *
     * @param orderNo
     * @param codeUrl
     */
    void saveCodeUrl(String orderNo, String codeUrl);

    /**
     * 获得所有订单列表
     *
     * @return List<OrderInfo>
     */
    List<OrderInfo> getAllOrderInfo();

    /**
     * 更新订单状态
     *
     * @param orderNo
     * @param orderStatus
     */
    void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus);

    /**
     * 获得订单状态
     *
     * @param orderNo
     * @return String
     */
    String getOrderStatus(String orderNo);

    /**
     * 查询超时订单，并且未支付的订单
     *
     * @param minutes
     * @param type
     * @return List<OrderInfo>
     */
    List<OrderInfo> getNoPayOrderByDuration(int minutes, String type);

    /**
     * 根据订单编号获得订单
     * @param orderNo
     * @return OrderInfo
     */
    OrderInfo getOrderByOrderNo(String orderNo);
}
