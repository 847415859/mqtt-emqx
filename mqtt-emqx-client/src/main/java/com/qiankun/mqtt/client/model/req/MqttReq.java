package com.qiankun.mqtt.client.model.req;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Description:
 * @Date : 2024/08/26 18:03
 * @Auther : tiankun
 */
@Data
@Accessors(chain = true)
public class MqttReq implements IMqttReq{

    /**
     * 主题
     */
    private String topic;

    /**
     * qos(消息服务质量)
     * - QoS 0表示最多一次，
     * - QoS 1表示至少一次，
     * - QoS 2表示只有一次。
     */
    private Integer qos;

    /**
     * 消息内容
     */
    private String messageContent;
}
