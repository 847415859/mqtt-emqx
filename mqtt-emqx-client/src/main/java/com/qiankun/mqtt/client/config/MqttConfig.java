package com.qiankun.mqtt.client.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Description:
 * @Date : 2024/08/26 18:15
 * @Auther : tiankun
 */
@Data
@Accessors(chain = true)
public class MqttConfig implements IMqttConfig{

    /**
     * EMQX 地址
     */
    private String host;

    /**
     * EMQX tcp端口
     */
    private Integer tcpPort;

    /**
     * EMQX Http端口
     */
    private Integer httpPort;

    /**
     * EMQX 登录名称
     */
    private String username;

    /**
     * EMQX 登录名称
     */
    private String password;

    private String appId;

    private String appSecret;

    private Boolean ssl = false;

    //订阅的系统topic
    private List<String> sysSubscribeTopics;
}
