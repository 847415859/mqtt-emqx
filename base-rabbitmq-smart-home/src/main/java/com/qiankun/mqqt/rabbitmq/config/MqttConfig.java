package com.qiankun.mqqt.rabbitmq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Date : 2024/08/27 13:47
 * @Auther : tiankun
 */
@Getter
@Setter
@Component
@IntegrationComponentScan("com.qiankun.mqqt")
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {

    /**
     * 服务端clientId
     */
    private String serverClientId;
    /**
     * 客户端id
     */
    private String clientId;
    /**
     * mqtt 地址
     * eg: 192.168.126.100:1883
     */
    private String servers;
    /**
     * 默认主题
     * 默认发送到 amqp.topic交换机, 设置的 defaultTopic 作为 routeKey
     */
    private String defaultTopic;
    /**
     * rabbitmq 用户名
     */
    private String username;
    /**
     * rabbitmq 密码
     */
    private String password;
}
