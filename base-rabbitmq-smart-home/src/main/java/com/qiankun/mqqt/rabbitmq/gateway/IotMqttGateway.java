package com.qiankun.mqqt.rabbitmq.gateway;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * @Description: 网关
 * @Date : 2024/08/27 13:39
 * @Auther : tiankun
 */
@MessagingGateway(defaultRequestChannel = "iotMqttInputChannel")
public interface IotMqttGateway {
    // 向默认的 topic 发送消息
    void sendMessage2Mqtt(String payload);
    // 向指定的 topic 发送消息
    void sendMessage2Mqtt(String payload,@Header(MqttHeaders.TOPIC) String topic);
    // 向指定的 topic 发送消息，并指定服务质量参数
    void sendMessage2Mqtt(@Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos, String payload);
}
