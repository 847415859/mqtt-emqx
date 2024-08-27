package com.qiankun.mqtt.client.model.resp;

import java.util.Map;

/**
 * @Description:
 * @Date : 2024/08/26 18:05
 * @Auther : tiankun
 */
public interface IMqttResp {
    Integer getCode();

    String getMessage();

    Map getData();
}
