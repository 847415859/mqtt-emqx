package com.qiankun.mqtt.example.listener;


import com.qiankun.mqtt.client.listener.IMqttListener;
import com.qiankun.mqtt.client.model.message.IMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 监听节点上下线的系统topic
 */
@Slf4j
@Component
public class SysMqttListener implements IMqttListener {


    private String topic;

    public SysMqttListener() {
        this.topic = "$SYS/brokers/+/clients/#";
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
        log.info("==> SysMqttListener topic[{}]  message[{}]",message.getHeaders().get("topic"),message.getMessageContent());
    }
}
