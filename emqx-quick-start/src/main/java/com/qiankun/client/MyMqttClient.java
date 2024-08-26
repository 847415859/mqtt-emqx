package com.qiankun.client;


import com.qiankun.config.MqttConfig;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName MyMqttClient
 * @Description
 * @Author wzq
 * @Date 2024/1/14 14:02
 * @Version 1.0
 */
public class MyMqttClient implements MqttCallbackExtended {

    private static final Logger logger = LoggerFactory.getLogger(MyMqttClient.class);

    private MqttConfig mqttConfig;

    public static MqttClient client;

    public MyMqttClient(MqttConfig mqttConfig) {
        this.mqttConfig = mqttConfig;
    }

    private static MqttClient getClient() {
        return client;
    }

    private static void setClient(MqttClient client) {
        MyMqttClient.client = client;
    }

    /**
     * 客户端连接
     */
    public void connect() {
        MqttClient client;
        try {
            client = new MqttClient(mqttConfig.getHostUrl(), mqttConfig.getClientId(),
                    new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(mqttConfig.getUsername());
            options.setPassword(mqttConfig.getPassword().toCharArray());
            options.setConnectionTimeout(mqttConfig.getTimeout());
            options.setKeepAliveInterval(mqttConfig.getKeepAlive());
            options.setAutomaticReconnect(mqttConfig.getReconnect());
            options.setCleanSession(mqttConfig.getCleanSession());
            MyMqttClient.setClient(client);
            // 设置回调
            client.setCallback(this);
            client.connect(options);
        } catch (Exception e) {
            logger.error("MyMqttClient connect error,message:{}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 重新连接
     */
    public void reconnection() {
        try {
            client.connect();
        } catch (MqttException e) {
            logger.error("MyMqttClient reconnection error,message:{}", e.getMessage());
        }
    }

    /**
     * 订阅某个主题
     *
     * @param topic 主题
     * @param qos   连接方式
     */
    public void subscribe(String topic, int qos) {
        logger.info("========================【开始订阅主题:" + topic + "】========================");
        try {
            client.subscribe(topic, qos);
        } catch (MqttException e) {
            logger.error("MyMqttClient subscribe error,message:{}", e.getMessage());
        }
    }

    /**
     * 发布消息
     *
     * @param retained 是否保留
     * @param topic 主题，格式： server:${env}:report:${topic}
     * @param content 消息内容
     */
    public void publish(boolean retained, String topic, String content) {
        MqttMessage message = new MqttMessage();
        message.setQos(mqttConfig.getQos());
        message.setRetained(retained);
        message.setPayload(content.getBytes());
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            logger.error("MqttSendClient publish error,message:{}", e.getMessage());
        }
    }


    /**
     * 取消订阅某个主题
     *
     * @param topic
     */
    public void unsubscribe(String topic) {
        logger.info("========================【取消订阅主题:" + topic + "】========================");
        try {
            client.unsubscribe(topic);
        } catch (MqttException e) {
            logger.error("MyMqttClient unsubscribe error,message:{}", e.getMessage());
        }
    }

    /**
     * 客户端断开后触发
     *
     * @param throwable
     */
    @Override
    public void connectionLost(Throwable throwable) {
        logger.info("连接断开，可以重连");
        if (client == null || client.isConnected()) {
            logger.info("【emqx重新连接】....................................................");
            this.reconnection();
        }
    }

    /**
     * 客户端收到消息触发
     *
     * @param topic       主题
     * @param mqttMessage 消息
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        logger.info("【接收消息主题】:" + topic);
        logger.info("【接收消息Qos】:" + mqttMessage.getQos());
        logger.info("【接收消息内容】:" + new String(mqttMessage.getPayload()));
        //        int i = 1/0;
    }

    /**
     * 发布消息成功
     *
     * @param token token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        String[] topics = token.getTopics();
        for (String topic : topics) {
            logger.info("向主题【" + topic + "】发送消息成功！");
        }
        try {
            MqttMessage message = token.getMessage();
            byte[] payload = message.getPayload();
            String s = new String(payload, "UTF-8");
            logger.info("【消息内容】:" + s);
        } catch (Exception e) {
            logger.error("MyMqttCallback deliveryComplete error,message:{}", e.getMessage());
        }
    }

    /**
     * 连接emq服务器后触发
     *
     * @param b
     * @param s
     */
    @Override
    public void connectComplete(boolean b, String s) {
        logger.info("============================= 客户端【" + client.getClientId() + "】连接成功！=============================");
        // 以/#结尾表示订阅所有以test开头的主题
        // 订阅所有机构主题
        this.subscribe("mqtt/quick/msg-send", 0);
    }
}

