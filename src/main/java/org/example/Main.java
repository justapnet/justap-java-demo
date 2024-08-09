package org.example;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import justap.*;
import justap.auth.*;
import io.swagger.client.model.*;
import justap_sdk.DefaultApi;

import java.io.File;
import okio.Buffer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.*;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Main {
    public static void main(String[] args) throws Exception {
//        wechatpayLite();
        decodeNotifyData();
    }

    public static void decodeNotifyData() throws Exception {
        Boolean isEncrypted = true;
        if (!isEncrypted) return;

        String dataCiphertext = "6VyyZrGRsCEdCZNE+sUBKA==";
        String key = "f1gjoObO";


        Base64.Decoder decoder = Base64.getDecoder();
        SymmetricCrypto des = new SymmetricCrypto(SymmetricAlgorithm.DES, key.getBytes(StandardCharsets.UTF_8));
        String data = des.decryptStr(decoder.decode(dataCiphertext));

        System.out.println(data);


//        Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding");
//        IvParameterSpec ips = new IvParameterSpec(decoder.decode(encryptedIv));
//        SecretKey k = convertStringToSecretKeyto(encryptedKey);
//        c.init(Cipher.DECRYPT_MODE, k, ips);
//        byte output[] = c.doFinal(decoder.decode(dataCiphertext));

//        SymmetricCrypto des = new SymmetricCrypto(SymmetricAlgorithm.DES, encryptedKey.getBytes());
//        String data = des.decryptStr(decoder.decode(dataCiphertext));
    }

    public static String desDecrypt(byte[] data, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE,
                    SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key)),
                    new IvParameterSpec(key)
            );

            return new String(cipher.doFinal(data));
        } catch (Exception e) {
            return null;
        }
    }

    public static SecretKey convertStringToSecretKeyto(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey originalKey = new SecretKeySpec(decodedKey, "DESede");
        return originalKey;
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