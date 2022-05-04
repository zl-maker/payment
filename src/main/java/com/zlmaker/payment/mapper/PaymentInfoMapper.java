package com.zlmaker.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zlmaker.payment.pojo.PaymentInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zl-maker
 */
@Mapper
public interface PaymentInfoMapper
        extends BaseMapper<PaymentInfo> {
}
