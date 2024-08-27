package com.qiankun.mqtt.example.service;

import com.qiankun.mqtt.client.api.IMqttClientApi;
import com.qiankun.mqtt.client.model.req.IMqttReq;
import com.qiankun.mqtt.client.model.resp.IMqttResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName EmqxPublishService
 * @Description
 * @Author wzq
 * @Date 2024/7/10 16:34
 * @Version 1.0
 */

@Service
public class EmqxPublishService {

    @Autowired(required = false)
    private IMqttClientApi mqttClientApi;


   public IMqttResp httpPublish(IMqttReq request){
        IMqttResp iMqttResp = mqttClientApi.httpPublish(request);
        return iMqttResp;
    }

    public IMqttResp tcpPublish(IMqttReq request){
        IMqttResp iMqttResp = mqttClientApi.tcpPublish(request);
        return iMqttResp;
    }

}
