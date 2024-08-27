package com.qiankun.mqqt.rabbitmq.product;

import com.qiankun.mqqt.rabbitmq.gateway.IotMqttGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description:
 * @Date : 2024/08/27 14:16
 * @Auther : tiankun
 */
@Controller
@RequestMapping("/fun")
public class IotMqttController {
    @Autowired
    private IotMqttGateway mqttGateway;

    @RequestMapping("/sendMessage")
    @ResponseBody
    public String sendMqtt(@RequestParam(value = "topic") String topic, @RequestParam(value = "message") String message) {
        mqttGateway.sendMessage2Mqtt(message, topic);
        return "SUCCESS";
    }

    @RequestMapping("/index")
    public String index() {
        return "index";
    }
}
