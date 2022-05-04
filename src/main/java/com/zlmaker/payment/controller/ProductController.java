package com.zlmaker.payment.controller;

import com.zlmaker.payment.pojo.Product;
import com.zlmaker.payment.pojo.ResponseResult;
import com.zlmaker.payment.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产品前端控制器
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.controller
 * @className ProductController
 * @date 2022/4/23 下午10:34
 */
@RestController
@CrossOrigin
@RequestMapping("/api/product")
@Api(tags = "商品管理")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    @ApiOperation("获取所有商品列表")
    public ResponseResult getAllProducts() {
        List<Product> products = productService.list();
        return ResponseResult.success("获取商品成功", products);
    }
}
