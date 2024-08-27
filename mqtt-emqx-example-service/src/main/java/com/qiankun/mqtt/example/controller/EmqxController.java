package com.qiankun.mqtt.example.controller;


import com.qiankun.mqtt.client.model.req.MqttReq;
import com.qiankun.mqtt.example.common.Result;
import com.qiankun.mqtt.example.service.EmqxPublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName MqttController
 * @Description
 * @Author wzq
 * @Date 2024/1/22 16:31
 * @Version 1.0
 */
@RequestMapping("/web/emqx")
@RestController
public class EmqxController {

    @Autowired
    private EmqxPublishService emqxPublishService;

    @PostMapping("/tcp/publish")
    public Result<Void> tcpPublish(@RequestBody MqttReq req){
        emqxPublishService.tcpPublish(req);
        return Result.ok();
    }

    @PostMapping("/http/publish")
    public Result<Void> httpPublish(@RequestBody MqttReq req) {
        emqxPublishService.httpPublish(req);
        return Result.ok();
    }


}
