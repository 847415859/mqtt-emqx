package com.qiankun.mqtt.client.listener;

import com.qiankun.mqtt.client.model.message.IMessage;

/**
 * @Description:
 * @Date : 2024/08/26 18:16
 * @Auther : tiankun
 */
public interface IMqttListener {
    /**
     * Description: 路由topic
     **/
    String getTopic();

    /**
     * Description: 消费类型 广播、集群模式
     **/
    String getType();

    /**
     * Description: 消息处理
     **/
    void onMessage(IMessage message);
}
