package com.qiankun.mqtt.client.model.message;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @Description:
 * @Date : 2024/08/26 18:10
 * @Auther : tiankun
 */
@Data
@Accessors(chain = true)
public class CommonMessage implements IMessage {

    private String deviceId;

    private String productId;

    private String messageContent;

    protected volatile Map<String, Object> headers = Maps.newConcurrentMap();
}
