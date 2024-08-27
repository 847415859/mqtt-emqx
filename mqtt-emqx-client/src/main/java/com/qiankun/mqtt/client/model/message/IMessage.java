package com.qiankun.mqtt.client.model.message;

import java.util.Map;

/**
 * @Description:
 * @Date : 2024/08/26 18:09
 * @Auther : tiankun
 */
public interface IMessage {
    String getDeviceId();

    String getProductId();

    Map<String, Object> getHeaders();

    String getMessageContent();
}
