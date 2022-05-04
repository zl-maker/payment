package com.zlmaker.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zlmaker.payment.pojo.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zl-maker
 */
@Mapper
public interface ProductMapper
        extends BaseMapper<Product> {

}
