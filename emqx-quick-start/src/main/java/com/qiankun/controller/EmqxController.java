package com.qiankun.controller;


import com.qiankun.client.MyMqttClient;
import com.qiankun.common.Result;
import com.qiankun.req.PublishReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MqttController
 */
@RequestMapping("/web/emqx")
@RestController
public class EmqxController {

    @Autowired
    private MyMqttClient myMqttClient;

    @PostMapping("/tcp/publish")
    public Result<Void> tcpPublish(@RequestBody PublishReq req){
        this.myMqttClient.publish(false, req.getTopic(), req.getSendMessage());
        return Result.ok();
    }

}
