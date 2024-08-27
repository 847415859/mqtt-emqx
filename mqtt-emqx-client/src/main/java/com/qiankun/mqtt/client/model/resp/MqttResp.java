package com.qiankun.mqtt.client.model.resp;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @Description:
 * @Date : 2024/08/26 18:05
 * @Auther : tiankun
 */
@Data
@Accessors(chain = true)
public class MqttResp implements IMqttResp {
    private Integer code;

    private String message;

    private Map data;
}
