package org.example;

import justap.*;
import justap.auth.*;
import io.swagger.client.model.*;
import justap_sdk.DefaultApi;

import java.io.File;
import okio.Buffer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        wechatpayLite();
    }

    // 微信小程序支付 demo
    public static void wechatpayLite() throws GeneralSecurityException, IOException {
        // 初始化sdk
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");

        // 商家的私钥, 转成 pkcs8
        File file = new File("src/main/resources/merchant_priv_pkcs8.pem");

        // 从后台拿的平台公钥，转成 pkcs8
        File file2 = new File("src/main/resources/justap_pubkey_pkcs8.pem");

        ApiKeyAuth.setApiKey("sk_live_xxxxxxxxxxxxxxxxxxxxx");
        defaultClient.SetMerchantPrivateKeyByPath(file.getPath());
        defaultClient.SetJustapPublicKeyByPath(file2.getPath());

        defaultClient.setBasePath("https://trade.justap.cn");

        DefaultApi apiInstance = new DefaultApi(defaultClient);

        // 开始构造下单参数
        Float amout = 0.01f;
        V1CreateChargeRequest body = new V1CreateChargeRequest();
        body.setAmount(amout);

        // 后台的 appid
        body.setAppId("app_xxxxxxxx");

        // 订单标题
        body.setSubject("测试 java sdk 下单");

        // 描述
        body.setBody("测试 java sdk 下单 desc");

        // 单号
        body.setMerchantTradeId("java_test_" + System.currentTimeMillis());

        V1CreateChargeRequestExtra extra = new V1CreateChargeRequestExtra();
        V1ExtraWechatpayLite wxLite = new V1ExtraWechatpayLite();
        V1ExtraWechatpayPayer buyer = new V1ExtraWechatpayPayer();

        // openid
        buyer.setOpenid("onKn50A5QQBTOMFk0RyB4q1oEFvU");
        wxLite.setPayer(buyer);

        Buffer buffer = new Buffer();

        // 小程序支付
        extra.setWechatpayLite(wxLite);
        body.setExtra(extra);
        body.setClientIp("127.0.0.1");

        // 小程序支付的 channel
        body.setChannel(V1Channel.WECHATPAYLITE);

        // 下单
        try {
            V1ChargeResponse result = apiInstance.tradeServiceCharges(body);
            System.out.println(result);

            // 这个给小程序拉起支付  wx.requestPayment(json(conf))
            V1ExtraWechatpayAppletConfig conf = result.getData().getExtra().getWechatpayLite().getAppletConfig();
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#tradeServiceCharges");
            e.printStackTrace();
        }
    }
}