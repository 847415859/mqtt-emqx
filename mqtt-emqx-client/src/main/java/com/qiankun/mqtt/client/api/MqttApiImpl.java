package com.qiankun.mqtt.client.api;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.ImmutableMap;
import com.qiankun.mqtt.client.IMqttClient;
import com.qiankun.mqtt.client.config.IMqttConfig;
import com.qiankun.mqtt.client.model.req.IMqttReq;
import com.qiankun.mqtt.client.model.resp.IMqttResp;
import com.qiankun.mqtt.client.model.resp.MqttResp;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @Description:
 * @Date : 2024/08/26 18:29
 * @Auther : tiankun
 */
@Slf4j
public class MqttApiImpl implements IMqttClientApi{
    private final IMqttConfig mqttConfig;

    private final IMqttClient mqttClient;

    public MqttApiImpl(IMqttConfig mqttConfig, IMqttClient mqttClient) {
        this.mqttConfig = mqttConfig;
        this.mqttClient = mqttClient;
    }

    private static final MediaType HTTP_MEDIA_TYPE_JSON_UTF8 = MediaType.parse("application/json; charset=utf-8");



    @Override
    public IMqttResp httpPublish(IMqttReq request) {
        return (IMqttResp) BeanUtil.toBeanIgnoreError(this.callHttp(request), MqttResp.class);
    }

    @Override
    public IMqttResp tcpPublish(IMqttReq request) {
        return mqttClient.publish(request);
    }

    private Map<String, ?> callHttp(IMqttReq params) {
        String path = "";
        String url = mqttConfig.getHost() + path;
        log.debug("http url[{}] requestBodyStr[{}]", url, params.getMessageContent());

        Dict dict = Dict.create();
        dict.set("topic", params.getTopic());              //订阅主题
        dict.set("payload", params.getMessageContent());   //内容
        dict.set("qos", 0);                                //质量
        dict.set("retain",false);                          //是否保存
        String requestBodyStr = JSON.toJSONString(dict);


        RequestBody requestBody = RequestBody.create(HTTP_MEDIA_TYPE_JSON_UTF8, requestBodyStr);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .header("Authorization", Credentials.basic(mqttConfig.getAppId(), mqttConfig.getAppSecret()))
                .build();

        try (Response response = getHttpClientInstance().newCall(request).execute()) {
            log.debug("Call http success. url[{}] response[{}]", url, response);

            if (response.code() == 404) {
                return ImmutableMap.of("code", 404, "Message", "404 Not Found");
            } else if (!response.isSuccessful()) {
                return ImmutableMap.of("code", response.code(), "Message", "Server Error");
            }

            // 输出响应内容
            assert response.body() != null;
            String string = response.body().string();
            return JSON.parseObject(string);
        } catch (IOException e) {
            log.warn("Call http failed, {}. url[{}] requestBodyStr[{}]", e.getMessage(), url, requestBodyStr);
        }

        return Collections.emptyMap();
    }

    private OkHttpClient getHttpClientInstance() {
        return HttpClientHolder.HTTP_CLIENT_INSTANCE;
    }


    private static class HttpClientHolder {
        private static final OkHttpClient HTTP_CLIENT_INSTANCE = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(3))
                .writeTimeout(Duration.ofSeconds(3))
                .dispatcher(new Dispatcher(Executors.newFixedThreadPool(8)))
                .build();

    }
}
