package com.coolweather.android.util;

import android.util.Log;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;

import static org.litepal.LitePalBase.TAG;

/**
 * 处理请求
 */
public class HttpUtil {

    /**
     * 发送请求，并在回调中响应
     * @param address 地址
     * @param callback 回调方法
     */
    public static void sendOkHttpRequest(final String address, Callback callback){




                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder().url(address).build();

                  client.newCall(request).enqueue(callback);
    };


}
