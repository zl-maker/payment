package com.zlmaker.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment
 * @className WechatPayment
 * @date 2022/4/23 下午8:33
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.zlmaker.payment.mapper")
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
