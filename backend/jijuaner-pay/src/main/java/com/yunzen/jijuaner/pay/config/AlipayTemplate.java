package com.yunzen.jijuaner.pay.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.yunzen.jijuaner.pay.vo.AliPayVo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class AlipayTemplate {
    private AlipayTemplate() {
    }

    // 在支付宝创建的应用的id
    @Value("${config.alipay.app-id}")
    private String appId;

    // 商户私钥, 您的PKCS8格式RSA2私钥
    @Value("${config.alipay.merchant-private-key}")
    private String merchantPrivateKey;

    // 支付宝公钥, 查看地址: https://openhome.alipay.com/platform/keyManage.html 对应APPID下的支付宝公钥.
    @Value("${config.alipay.alipay-public-key}")
    private String alipayPublicKey;

    // 服务器异步通知路径, 告诉我们支付成功的信息
    // 不能加自定义参数, 支付宝服务端要能正常访问
    @Value("${config.alipay.notify-url}")
    private String notifyUrl;

    // 页面跳转同步通知路径, 告诉客户端支付成功的信息
    // 不能加自定义参数, 支付宝服务端要能正常访问, 客户端要能成功访问
    // TODO 生成订单页 http://app.jijuaner.vaiwan.com:3000/succuss?charset=utf-8&out_trade_no=1508991159629955074&method=alipay.trade.page.pay.return&total_amount=1.00&sign=OwrdJhNC4zyhlWwz8VwzC4ppgqbnTmL3T46W7m%2B8pud1uRHWhCg%2Fnrt2PGvZ6xYc%2FwPtc4C1ndK%2BcnoPdGRd5H8VoydyQhCLwBy2QJLiGE5QBD7BXAXAVlGWPOiRKvAFivxy3muEzLJ4gZi8%2FrX6ownzy48ZgV013k2XRRrMC6%2F3ltj6QqwkvlqdgN4AhgoqjZzSQltCXnlJXUaTB5NIgJrC%2FzTDnkUalsCznaIwv2fphoksJ1NmpTK1Jg7dO29yWScma0a1IOgKucjkpK%2FmaHzHOcZPCJgGCDcmyVwY9bUQnKmYSJOOkhz3gRNHfPrQWGvdB%2B%2BBTHK6y9OEzdcTQw%3D%3D&trade_no=2022033022001475870501841475&auth_app_id=2021000118648236&version=1.0&app_id=2021000118648236&sign_type=RSA2&seller_id=2088621957105329&timestamp=2022-03-30%2010%3A15%3A53
    @Value("${config.alipay.return-url}")
    private String returnUrl;

    // 签名方式
    private String signType = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 支付宝网关 https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(AliPayVo vo) throws AlipayApiException {

        // 根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl, appId, merchantPrivateKey, "json", charset,
                alipayPublicKey, signType);

        // 创建一个支付请求, 设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(returnUrl);
        alipayRequest.setNotifyUrl(notifyUrl);

        alipayRequest.setBizContent(
                "{\"out_trade_no\":\"" + vo.getOutTradeNo() + "\"," // 商户订单号, 商户网站订单系统中唯一订单号，必填
                        + "\"total_amount\":\"" + vo.getTotalAmount() + "\"," // 付款金额, 必填
                        + "\"subject\":\"" + vo.getSubject() + "\"," // 订单名称, 必填
                        + "\"body\":\"" + vo.getBody() + "\"," // 商品描述, 选填
                        + "\"timeout_express\":\"5m\"," // 5分钟后关单
                        + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        // 会收到支付宝的响应, 响应的是一个页面, 浏览器渲染这个页面, 就会跳转来到支付宝的收银台页面
        return alipayClient.pageExecute(alipayRequest).getBody();
    }
}
