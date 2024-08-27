package com.qiankun.mqtt.client.api;

import com.qiankun.mqtt.client.model.req.IMqttReq;
import com.qiankun.mqtt.client.model.resp.IMqttResp;

/**
 * @Description:
 * @Date : 2024/08/26 18:01
 * @Auther : tiankun
 */
public interface IMqttClientApi {
    /**
     * Http 方式推送消息
     * @param request
     * @return
     */
    IMqttResp httpPublish(IMqttReq request);

    /**
     * tcp 方式推送消息
     * @param request
     * @return
     */
    IMqttResp tcpPublish(IMqttReq request);
}
