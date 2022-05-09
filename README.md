## 项目介绍

payment是集成微信支付V3和支付宝支付的开源支付系统，使用SpringBoot+Vue主流框架进行开发。

本项目内置了微信支付商户证书和密钥公钥，支付宝沙箱环境。

主要参考官方文档资料：

*   [微信支付API字典](https://pay.weixin.qq.com/wiki/doc/apiv3/apis/index.shtml)
*   [微信支付SDK](https://github.com/wechatpay-apiv3/wechatpay-apache-httpclient)
*   [支付宝支付API字典](https://opendocs.alipay.com/apis/api_1/alipay.trade.page.pay)

## 项目已实现的功能

### 微信支付

*   native下单API
*   支付通知API
*   定时查询订单
*   取消订单API
*   查询订单API
*   申请退款API
*   查询退款API
*   退款订单通知API
*   下载帐单

### 支付宝支付

*   统一收单下单并支付页面接口
*   统一收单线下交易查询
*   统一收单交易退款接口
*   统一收单交易退款查询
*   查询对账单下载地址
*   统一收单交易关闭接口
*   收单退款冲退完成通知

## 微信支付基础知识

[微信支付基础知识](https://zl-maker.github.io/2022/05/04/%E5%9C%A8%E7%BA%BF%E6%94%AF%E4%BB%98payment/%E5%BE%AE%E4%BF%A1%E6%94%AF%E4%BB%98%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86/)

## 核心技术栈

|      软件名称      |        描述        |     版本     |
| :----------------: | :----------------: | :----------: |
|        JDK         |      Java环境      |     1.8      |
|     SpringBoot     |      开发框架      |    2.5.12    |
|       MySQL        |       数据库       |    8.0.28    |
|    MyBatis-Plus    |  MyBatis增强工具   |    3.4.2     |
| 微信支付 APIv3 SDK |  微信支付开发环境  |    0.4.5     |
|   支付宝支付SDK    | 支付宝支付开发环境 | 4.22.110.ALL |
|      knife4j       |     Swagger ui     |    2.0.7     |

## 支付配置

### 支付宝支付配置

```java
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

```

### 微信支付配置

```java
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

```

### alipay.properties

```properties
alipay.app-id=2021000119635499
alipay.seller-id=2088621957993562
alipay.gateway-url=https://openapi.alipaydev.com/gateway.do
alipay.merchant-private-key=MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCJPnt6TMZ1A06SMeNxQT0WhGbRd74JBCcdqQt4POzBMJ1NE6U/OiG2xZEnUqYWC2ukkOgZZEvTuWDI9q2aEFN7E2Fcj39JCwRmE0an153JIOkb9L2nngTsAAN7cwLZZ6/dAPnrZjtN0f/HRVBXPSNLBdpoS/pbKRurM2NccZkbTZtluCdt4IvBBjjcV3DJOCYP9yrLTP3HYDxep3HrZCvXuR2Iseb+c6qc4IF/2UKQTz+iavxCy3zJTYbDzD0cL7yC4HmSD7vbNGZvkzZB1RNWt0NILj2LdFG10T7zZahN461FiYozRfD7LDPXqq+uuZMM5i0jpXVrs2MDk6HeS0lJAgMBAAECggEAF87wCrpQ3zGwqqne4+HGYCad046rN9MxmfKeW8Bt7eGqGBnlW7+Q460ITkMHLuHSTZ0ZtnXwtYz+Hj60xPo6ESq+hBkcoqY3oCGN60X7SE3eQoxFblN6VRp3gC3me6KCHpuxv0Vf2lMoxP/gPRINElG0ns03ZCMQerWSchH+1n5xUX/SrsgYDLaHfCxSpGsI/iyjTHXl+KqZeiFoRY+0tlJCTsc6P8JBYEeI5l8Iza/CjxDgFT41B4RksMw9ZEUCwxMiQhqIOThdTxtpA+MpUjoizhngq1xAXMcEz3QlnV7V2icyWjDAAz7bMCISUBa2MLkNWb392/yROKCBcXKYZQKBgQDY5T8QbC3GiUw0IQrVwm1A46zBzMDuQV+a4/2q63f8BBRZcbOluzxuCSfsTIVFKF1eJqpD75+76rB2z39P5xSS2/9SuO9FofV5iUDZuC93mOvR5nwh0rkgizhes81p+i7S9VcQcLTM/gK8ta3VXC3Pv/9bIGlTyFPLS0iMLUJlywKBgQCh/PIjoBFqKWpB6wSTn8hotvvE0WU1XMhm09WnlrMN2O+TuL71PoVS5vhCBgCJW5e3OOOS1K8uYLWaAQY9g/PvzYWLKF7CkCZNTvdds67QMreU9Cfm3jOwUkOibXarJEwY0l52k4xihQ6o8QGvG6XmNw3oq3RX/t/Y0DC84lMKuwKBgQCBQnYIAoBxToe0lXCQnfNgdY8SXEUqeJlShMc7YmM6NPAvsfxfK6vC4///6kaORZUHNEHKhPcMFbyeweBcrRlswGF0WjR2qiPSD4MvfX4EZ4U6rYKS4bNkerPYdI1ZuDjJjl8ZtCF7/XGCJz/25J2Eryauly1OOhf+Etqkd6CXawKBgDlmf4seMm2TBWMcW3/QM9zfUnHY3Ws+WIkPcXs0THiQsbx/z7Lpl6bbz4bdx5zkxusXDpU+JmFhxZgv2r07n9oO0s6P3JxHJjtoywD6Je0Cu8jdh7IodNp7HBpXfaCBeTGmgfC0sh9LFPnKhRU+z9e3FIepEc4Is9uJUmvsKw73AoGAM44/Oz7axzcVEIaD/7jyy1GiaYCmI55qqjocXfbK9QlWDiLnGfbx2UB6BC1y7WD6a9bMpatLXppTDEL+qHX0jUp0u06LQnJi9SAUBcff4LioSJxBrYx6ovYjAEmpoyiW/AAQBJ1oyp1UvpX0avfMa0Hdo1e+YnFEZluCr184uns=
alipay.alipay-public-key=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvKoCpGtG31iY2Llj3t8MKRpaVCntnalWmXbKcHCiaYcUUjzcMPH3/tOr5ORK5W1NjuLu9uRrsXqROBmQYN+0y4nea+fU989i2IxtOGR/h2Kvhyyk/lPjNkmgz7K8VqbGGeVzTadPSK49FcrDVEshJ6C92vEKq6TmUfhKgCLiLZ288fHBDRvzUnoj8O/LBXiDroq1zX+DXYCHcQeFljkF5ivyxDZBkWl23hYTtnFClvN5lVLK4d294wyprF7IMv3XhQOfJS+Pr527CsfwT0JE44lyidCvslvn9DSdHdIfTIzHDsIEiDzB/OvRCbwpEA7UGnWQxQf7AusX2TTRQHWW7wIDAQAB
alipay.content-key=D8entyfafkkFwtMbUqj3Mw==
alipay.return-url=http://localhost:8080/#/success
alipay.notify-url=http://woshishabi.vaiwan.cn/api/ali-pay/trade/notify
```

### wxpay.properties

```properties
wxpay.mch-id=1558950191
wxpay.mch-serial-no=34345964330B66427E0D3D28826C4993C77E631F
wxpay.private-key-path=apiclient_key.pem
wxpay.api-v3-key=UDuLFDcmy5Eb6o0nTNZdu6ek4DDh4K8B
wxpay.appid=wx74862e0dfcf69954
wxpay.domain=https://api.mch.weixin.qq.com
wxpay.notify-domain=http://woshishabi.vaiwan.cn
wxpay.partnerKey=T6m9iK73b0kn9g5v426MKfHQH7X8rKwb
```

## 如何使用本项目

1.   下载本项目

```bash
git clone https://github.com/zl-maker/payment.git
```

2.   运行payment.sql文件

3.   修改application.yml中的数据源配置

4.   下载钉钉内网穿透(用来接受微信和支付宝的通知)

     [内网穿透之HTTP穿透](https://open.dingtalk.com/document/resourcedownload/http-intranet-penetration)

5.   启动钉钉内网穿透工具(官方文档里有具体使用)

6.   启动本项目前端demo

     ```bash
     cd payment-front
     npm install
     npm run serve
     ```

7.   在网页中打开localhost:8080

## 项目优化

[在线支付优化](https://zl-maker.github.io/2022/05/04/%E5%9C%A8%E7%BA%BF%E6%94%AF%E4%BB%98payment/%E5%9C%A8%E7%BA%BF%E6%94%AF%E4%BB%98%E4%BC%98%E5%8C%96/)

## 最后结语

欢迎大家积极提issues、pull request，来帮助本项目更加完善。

感谢大家Star本项目！













