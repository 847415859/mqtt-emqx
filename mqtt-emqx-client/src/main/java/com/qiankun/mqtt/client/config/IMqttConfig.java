package com.qiankun.mqtt.client.config;

import java.util.List;

/**
 * @Description:
 * @Date : 2024/08/26 18:14
 * @Auther : tiankun
 */
public interface IMqttConfig {
    String getHost();

    Integer getTcpPort();

    Integer getHttpPort();

    String getUsername();

    String getPassword();

    Boolean getSsl();

    //订阅的系统topic
    List<String> getSysSubscribeTopics();

    String getAppId();

    String getAppSecret();
}
