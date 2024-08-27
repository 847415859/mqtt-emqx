package com.qiankun.mqtt.client.model.req;

/**
 * @Description:
 * @Date : 2024/08/26 18:02
 * @Auther : tiankun
 */
public interface IMqttReq {
    String getTopic();

    Integer getQos();

    String getMessageContent();

}
