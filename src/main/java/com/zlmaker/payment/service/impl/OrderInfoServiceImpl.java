package com.zlmaker.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zlmaker.payment.enums.OrderStatus;
import com.zlmaker.payment.mapper.OrderInfoMapper;
import com.zlmaker.payment.mapper.ProductMapper;
import com.zlmaker.payment.pojo.OrderInfo;
import com.zlmaker.payment.pojo.Product;
import com.zlmaker.payment.service.OrderInfoService;
import com.zlmaker.payment.util.OrderNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author zl-maker
 */
@Service
public class OrderInfoServiceImpl
        extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements OrderInfoService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 根据商品id创建订单
     *
     * @param productId
     * @param type
     */
    @Override
    public OrderInfo createOrderByProductId(Long productId, String type) {
        // 根据商品id获取没有支付的订单
        OrderInfo orderInfo = this.getNoPayOrderByProductId(productId, type);
        if (orderInfo != null) {
            return orderInfo;
        }
        // 获取商品信息
        Product product = productMapper.selectById(productId);
        // 生成订单
        orderInfo = new OrderInfo();
        orderInfo.setTitle(product.getTitle());
        orderInfo.setOrderNo(OrderNoUtil.getOrderNo());
        orderInfo.setProductId(productId);
        orderInfo.setTotalFee(product.getPrice());
        orderInfo.setOrderStatus(OrderStatus.NOTPAY.getType());
        orderInfo.setPaymentType(type);
        baseMapper.insert(orderInfo);
        return orderInfo;
    }

    /**
     * 根据商品id获取没有支付的订单
     *
     * @param productId
     * @param type
     * @return OrderInfo
     */
    @Override
    public OrderInfo getNoPayOrderByProductId(Long productId, String type) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.eq("payment_type", type);
        queryWrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 保存二维码
     *
     * @param orderNo
     * @param codeUrl
     */
    @Override
    public void saveCodeUrl(String orderNo, String codeUrl) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCodeUrl(codeUrl);
        baseMapper.update(orderInfo, queryWrapper);
    }

    /**
     * 获得所有订单列表
     *
     * @return List<OrderInfo>
     */
    @Override
    public List<OrderInfo> getAllOrderInfo() {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 更新订单状态
     *
     * @param orderNo
     * @param orderStatus
     */
    @Override
    public void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus(orderStatus.getType());
        baseMapper.update(orderInfo, queryWrapper);
    }

    /**
     * 获得订单状态
     *
     * @param orderNo
     * @return String
     */
    @Override
    public String getOrderStatus(String orderNo) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        return orderInfo == null ? null : orderInfo.getOrderStatus();
    }

    /**
     * 查询超时订单，并且未支付的订单
     *
     * @param minutes
     * @param type
     * @return List<OrderInfo>
     */
    @Override
    public List<OrderInfo> getNoPayOrderByDuration(int minutes, String type) {
        // 使用现在的时间减去超时的5分钟，得到5分钟之前的时间
        Instant oldTime = Instant.now().minus(Duration.ofHours(8)).minus(Duration.ofMinutes(minutes));
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        queryWrapper.eq("payment_type", type);
        queryWrapper.le("create_time", oldTime);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据订单编号获得订单
     *
     * @param orderNo
     * @return OrderInfo
     */
    @Override
    public OrderInfo getOrderByOrderNo(String orderNo) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        return baseMapper.selectOne(queryWrapper);
    }
}
