package com.zlmaker.payment.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import com.zlmaker.payment.exception.WxPayApiException;
import lombok.Data;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;


/**
 * @author zl-maker
 */
@Configuration
@PropertySource("classpath:wxPay.properties")
@ConfigurationProperties(prefix = "wxpay")
@Data
public class WxPayConfig {

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 商户API证书序列号
     */
    private String mchSerialNo;

    /**
     * 商户私钥文件
     */
    private String privateKeyPath;

    /**
     * APIv3密钥
     */
    private String apiV3Key;

    /**
     * APPID
     */
    private String appid;

    /**
     * 微信服务器地址
     */
    private String domain;

    /**
     * 接收结果通知地址
     */
    private String notifyDomain;

    /**
     * APIv2密钥
     */
    private String partnerKey;

    /**
     * 获取商户的私钥文件
     */
    private PrivateKey getPrivateKey() throws WxPayApiException {
        try {
            return PemUtil.loadPrivateKey(new FileInputStream(privateKeyPath));
        } catch (FileNotFoundException e) {
            throw new WxPayApiException(e.getMessage());
        }
    }

    /**
     * 获取签名验证器
     */
    @Bean
    public Verifier getVerifier() throws WxPayApiException {
        // 获取私钥
        PrivateKey privateKey = getPrivateKey();

        // 获取证书管理器实例
        CertificatesManager certificatesManager = CertificatesManager.getInstance();

        // 私钥签名对象
        PrivateKeySigner privateKeySigner = new PrivateKeySigner(mchSerialNo, privateKey);

        //身份认证对象
        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(mchId, privateKeySigner);

        // 向证书管理器增加需要自动更新平台证书的商户信息
        try {
            certificatesManager.putMerchant(mchId, wechatPay2Credentials, apiV3Key.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | GeneralSecurityException | HttpCodeException e) {
            throw new WxPayApiException(e.getMessage());
        }


        // 从证书管理器中获取verifier
        try {
            return certificatesManager.getVerifier(mchId);
        } catch (NotFoundException e) {
            throw new WxPayApiException(e.getMessage());
        }
    }


    /**
     * 获取http请求对象
     */
    @Bean(name = "wxPayClient")
    public CloseableHttpClient getWxPayClient(Verifier verifier) throws WxPayApiException {

        // 获得私钥
        PrivateKey privateKey = getPrivateKey();
        // 微信支付验证器
        WechatPay2Validator wechatPay2Validator = new WechatPay2Validator(verifier);
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create().withMerchant(mchId, mchSerialNo, privateKey).withValidator(wechatPay2Validator);
        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新

        return builder.build();
    }

    /**
     * 获取HttpClient，无需进行应答签名验证，跳过验签的流程
     */
    @Bean(name = "wxPayNoSignClient")
    public CloseableHttpClient getWxPayNoSignClient() throws WxPayApiException {

        //获取商户私钥
        PrivateKey privateKey = getPrivateKey();

        //用于构造HttpClient
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                //设置商户信息
                .withMerchant(mchId, mchSerialNo, privateKey)
                //无需进行签名验证、通过withValidator((response) -> true)实现
                .withValidator((response) -> true);
        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
        return builder.build();
    }

    /**
     * 微信支付回调请求处理器
     *
     * @param verifier
     * @return NotificationHandler
     */
    @Bean
    public NotificationHandler notificationHandler(Verifier verifier) {
        return new NotificationHandler(verifier, apiV3Key.getBytes(StandardCharsets.UTF_8));
    }

}
