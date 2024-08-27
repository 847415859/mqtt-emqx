package com.qiankun.mqtt.example.listener;


import com.qiankun.mqtt.client.listener.IMqttListener;
import com.qiankun.mqtt.client.model.message.IMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName DevicePropertyMqttListener
 * @Description
 * @Author wzq
 * @Date 2024/1/22 16:51
 * @Version 1.0
 */
@Slf4j
@Component
public class DevicePropertyMqttListener implements IMqttListener {


    private String topic;

    public DevicePropertyMqttListener() {
        this.topic = "sys/+/+/thing/event/property/post";
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void onMessage(IMessage message) {

        log.info("==> DevicePropertyMqttListener topic[{}]  message[{}]",message.getHeaders().get("topic"),message.getMessageContent());
    }
}
