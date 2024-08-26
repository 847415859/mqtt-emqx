package com.qiankun.config;

import com.qiankun.client.MyMqttClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MqttConfigration
 */
@Configuration
@ConditionalOnProperty(value = "mqtt.enable", havingValue = "true")
public class MqttConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "mqtt")
    public MqttConfig mqttConfig() {
        return new MqttConfig();
    }

    /**
     * 订阅mqtt
     * @return
     */
    @Bean
    public MyMqttClient getMqttPushClient(MqttConfig mqttConfig) {
        MyMqttClient myMqttClient = new MyMqttClient(mqttConfig);
        myMqttClient.connect();
        return myMqttClient;
    }

}
