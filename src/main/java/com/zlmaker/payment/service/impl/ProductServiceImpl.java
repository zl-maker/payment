package com.zlmaker.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zlmaker.payment.mapper.ProductMapper;
import com.zlmaker.payment.pojo.Product;
import com.zlmaker.payment.service.ProductService;
import org.springframework.stereotype.Service;

/**
 * @author zl-maker
 */
@Service
public class ProductServiceImpl
        extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

}
