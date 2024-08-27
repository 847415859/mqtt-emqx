package com.qiankun.mqtt.client;

import com.qiankun.mqtt.client.model.req.IMqttReq;
import com.qiankun.mqtt.client.model.resp.IMqttResp;

/**
 * @Description:  emqx-client 上下文
 * @Date : 2024/08/26 18:11
 * @Auther : tiankun
 */
public interface IMqttClient {
    /**
     * Description: EMQX 建立连接
     **/
    boolean connect();

    /**
     * Description: EMQX 关闭连接
     **/
    boolean disconnect();

    /**
     * Description: EMQX-CLIENT 是否在线
     **/
    boolean isConnect();

    /**
     * Description: EMQX-CLIENT 推送消息
     **/
    IMqttResp publish(IMqttReq request);

    /**
     * Description: 关闭 EMQX-CLIENT
     **/
    void mqttStop();

}
