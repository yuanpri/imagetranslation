package com.gpnu.yuan.livewords.util;


import static com.gpnu.yuan.livewords.util.Constant.baidu_translate_appId;
import static com.gpnu.yuan.livewords.util.Constant.baidu_translate_key;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.gpnu.yuan.livewords.model.TranslateResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TranslationUtil {

    private static ClipboardManager myClipboard;
    private static ClipData myClip;


    //只返回翻译
    public static String translateText(String sourceText, String fromLanguage, String targetLanguage) throws IOException {
        String salt = num(1);
        String sign = stringToMD5(baidu_translate_appId + sourceText + salt + baidu_translate_key);
        String url = "https://fanyi-api.baidu.com/api/trans/vip/translate" +
                "?appid=" + baidu_translate_appId + "&q=" + sourceText + "&from=" + fromLanguage + "&to=" +
                targetLanguage + "&salt=" + salt + "&sign=" + sign;

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        // 同步执行请求
        Response response = okHttpClient.newCall(request).execute();

        // 处理响应结果
        if (response.isSuccessful()) {
            // 尝试将JSON字符串转换为TranslateResult对象
            TranslateResult result = new Gson().fromJson(response.body().string(), TranslateResult.class);
            // 检查翻译结果是否有效
            if (result != null && result.getTrans_result() != null && result.getTrans_result().size() > 0
                    && result.getTrans_result().get(0).getDst() != null) {
                // 如果结果有效，调用回调的onSuccess方法，传递翻译结果给回调接口
                return result.getTrans_result().get(0).getDst();
            }
            // 如果数据为空或为null，调用回调的onFailure方法，传递错误信息给回调接口
            return "Data is empty or null";

        } else {
            throw new IOException("Request failed with code " + response.code());
        }
    }

    //返回TranslateResult类型
    public static TranslateResult translateTextSync(String sourceText, String fromLanguage, String targetLanguage) throws IOException {
        String salt = num(1);
        String sign = stringToMD5(baidu_translate_appId + sourceText + salt + baidu_translate_key);
        String url = "https://fanyi-api.baidu.com/api/trans/vip/translate" +
                "?appid=" + baidu_translate_appId + "&q=" + sourceText + "&from=" + fromLanguage + "&to=" +
                targetLanguage + "&salt=" + salt + "&sign=" + sign;

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        // 同步执行请求
        Response response = okHttpClient.newCall(request).execute();

        // 处理响应结果
        // 处理响应结果
        if (response.isSuccessful()) {
            // 尝试将JSON字符串转换为TranslateResult对象
            return new Gson().fromJson(response.body().string(), TranslateResult.class);
        } else {
            throw new IOException("Request failed with code " + response.code());
        }
    }





    private static String num(int a) {
        Random r = new Random(a);
        int ran1 = 0;
        for (int i = 0; i < 5; i++) {
            ran1 = r.nextInt(100);
        }
        return String.valueOf(ran1);
    }

    /**
     * 将字符串转成MD5值
     *
     * @param string
     * @return
     */
    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

}
