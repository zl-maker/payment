package com.zlmaker.payment.config;

import com.alipay.api.*;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 支付宝配置类
 *
 * @author zl-maker
 * @packgaeName com.zlmaker.wechatPayment.config
 * @className AliPayConfig
 * @date 2022/4/30 下午4:07
 */
@Configuration
@PropertySource("classpath:aliPay.properties")
@Data
@ConfigurationProperties(prefix = "alipay")
public class AliPayClientConfig {
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 支付宝账号ID
     */
    private String sellerId;
    /**
     * 支付宝网关地址
     */
    private String gatewayUrl;
    /**
     * 商户私钥
     */
    private String merchantPrivateKey;
    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;
    /**
     * 接口内容加密秘钥
     */
    private String contentKey;
    /**
     * 页面跳转返回路径
     */
    private String returnUrl;
    /**
     * 异步通知路径
     */
    private String notifyUrl;

    @SneakyThrows
    @Bean
    public AlipayClient alipayClient(){
        AlipayConfig alipayConfig = new AlipayConfig();
        //设置网关地址
        alipayConfig.setServerUrl(gatewayUrl);
        //设置应用Id
        alipayConfig.setAppId(appId);
        //设置应用私钥
        alipayConfig.setPrivateKey(merchantPrivateKey);
        //设置请求格式，固定值json
        alipayConfig.setFormat(AlipayConstants.FORMAT_JSON);
        //设置字符集
        alipayConfig.setCharset(AlipayConstants.CHARSET_UTF8);
        //设置支付宝公钥
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        //设置签名类型
        alipayConfig.setSignType(AlipayConstants.SIGN_TYPE_RSA2);
        //构造client
        return new DefaultAlipayClient(alipayConfig);
    }
}
