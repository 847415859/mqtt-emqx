package com.qiankun.mqtt.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qiankun.mqtt.client.config.IMqttConfig;
import com.qiankun.mqtt.client.listener.IMqttListener;
import com.qiankun.mqtt.client.model.message.CommonMessage;
import com.qiankun.mqtt.client.model.req.IMqttReq;
import com.qiankun.mqtt.client.model.resp.IMqttResp;
import com.qiankun.mqtt.client.model.resp.MqttResp;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * @Description: MqttClientContext emqx-client 上下文
 * @Date : 2024/08/26 18:12
 * @Auther : tiankun
 */
@Slf4j
public class MqttClientContext extends AbstractVerticle implements IMqttClient{
    public static final Long RECONNECT_INTERVAL = 3600L;

    private static final String allWildcard = "#";
    private static final String singleWildcard = "+";

    private final static String SMART_UP_TOPIC_FORMAT = "$share/%s/%s";

    private final static List<String> topicFormatList = Lists.newArrayList(
            SMART_UP_TOPIC_FORMAT);

    // MQTT 配置
    private final IMqttConfig mqttConfig;
    // 监听器
    private final List<IMqttListener> IMqttListenerList;
    private CountDownLatch countDownLatch;
    // MQTT 客户端
    private MqttClient mqttClient;
    // 订阅组
    private String subscribeGroup;

    public MqttClientContext(IMqttConfig mqttConfig, List<IMqttListener> IMqttListenerList, String subscribeGroup) {
        this.mqttConfig = mqttConfig;
        this.IMqttListenerList = IMqttListenerList;
        this.subscribeGroup = subscribeGroup;
    }

    /**
     * Description: EMQX 建立连接
     * @Author: xmh
     **/
    @Override
    public boolean connect() {
        log.info("emqx connect ing ......");
        countDownLatch = new CountDownLatch(1);
        Vertx.vertx().deployVerticle(this);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Description:在 Vert.x 中，start 方法是 Verticle 类的一个生命周期方法，它在 verticle 被部署并准备好开始执行时被调用。如果你的 verticle 在启动时需要执行一些简单的同步操作，你可以重写这个方法来放置你的启动代码。
     * @Author: xmh
     **/
    @Override
    public void start() {
        if (Objects.isNull(this.mqttClient)) {
            this.mqttClient = MqttClient.create(vertx, buildMqttClientOptions());
        }
        //接收服务端消息处理handler
        mqttClient.publishHandler(pub -> {
            Buffer buffer = pub.payload();
            String topicName = pub.topicName();
            String[] split = topicName.split("/");
            String string = buffer.toString(StandardCharsets.UTF_8);
            CommonMessage message = new CommonMessage();
            HashMap<String, Object> headers = Maps.newHashMap();
            headers.put("topic",topicName);
            headers.put("qos",pub.qosLevel().value());
            message.setHeaders(headers);
            message.setMessageContent(string);
            IMqttListenerList.forEach(f -> {
                String topic = f.getTopic();
                if (topicRute(topic, split)){
                    f.onMessage(message);
                }
            });

        });
        mqttClient.closeHandler(unused -> getVertx().setTimer(RECONNECT_INTERVAL, h -> start()));
        mqttClient.connect(mqttConfig.getTcpPort(), mqttConfig.getHost(),
                s -> {
                    // 连接成功
                    if (s.succeeded()) {
                        log.info("IMqttClient connect success.");
                        subscribe();
                        countDownLatch.countDown();
                    } else {
                        log.error("IMqttClient connect fail: ", s.cause());
                        if (s.cause() != null) {
                            vertx.setTimer(RECONNECT_INTERVAL, handler -> this.start());
                        }
                    }
                });
    }

    /**
     * Description: EMQX 关闭连接
     * @Author: xmh
     **/
    @Override
    public boolean disconnect() {
        if (!isConnect()) {
            log.warn("IMqttClient no connect");
            return false;
        }
        vertx.undeploy(deploymentID(), (handler) -> {
            if (handler.succeeded()) {
                log.info("undeploy success");
            } else {
                log.warn("undeploy fail, {}. ", handler.cause().getMessage(), handler.cause());
            }
        });
        return true;
    }

    /**
     * Description: EMQX-CLIENT 是否在线
     * @Author: xmh
     **/
    @Override
    public boolean isConnect() {
        return Optional.ofNullable(mqttClient).map(MqttClient::isConnected).orElse(false);
    }


    /**
     * Description: EMQX-CLIENT 推送消息
     **/
    @Override
    public IMqttResp publish(IMqttReq request) {
        MqttResp response = new MqttResp();
        Buffer payload = Buffer.buffer(request.getMessageContent());
        mqttClient.publish(request.getTopic(), payload, MqttQoS.valueOf(request.getQos()), false, false, s -> {
            if (s.succeeded()) {
                log.info("===>IMqttClient publish success[{}]", s.result());
            } else {
                log.error("===>IMqttClient publish fail.", s.cause());
            }
        });
        response.setCode(200);
        return response;
    }

    /**
     * Description: 关闭 EMQX-CLIENT
     **/
    @Override
    public void mqttStop() {
        this.stop();
    }

    /**
     * Description: 在 Vert.x 中，如果你的 verticle 需要在停止时执行一些简单的同步清理任务，你可以重写 stop 方法来放置你的清理代码。这个方法在 Vert.x 准备停止 verticle 时调用，通常用于关闭资源，如数据库连接、网络连接、文件句柄等。
     * @Author: xmh
     **/
    @Override
    public void stop() {
        mqttClient.closeHandler(null);
        mqttClient.disconnect((handler) -> {
            if (handler.succeeded()) {
                vertx.close();
                log.info("IMqttClient disConnect success.");
            } else {
                log.error("IMqttClient disConnect fail: ", handler.cause());
            }
        }).exceptionHandler(event -> {
            log.error("IMqttClient fail: ", event.getCause());
        });
    }

    private MqttClientOptions buildMqttClientOptions() {
        MqttClientOptions options = new MqttClientOptions();
        options.setClientId("micro-client-" + RandomStringUtils.randomAlphanumeric(17));
        options.setMaxMessageSize(100_000_000);
        options.setKeepAliveInterval(60);
        options.setPassword(mqttConfig.getPassword());
        options.setUsername(mqttConfig.getUsername());
        options.setSsl(mqttConfig.getSsl());
        options.setReconnectInterval(RECONNECT_INTERVAL);
        options.setReconnectAttempts(Integer.MAX_VALUE);
        return options;
    }

    private boolean topicRute(String topic, String[] split) {
        String[] listenerTopic = topic.split("/");
        boolean flag = true;
        for (int i = 0; i < split.length; i++) {
            if (allWildcard.equals(listenerTopic[i])) {
                break;
            }
            if (singleWildcard.equals(listenerTopic[i])) {
                continue;
            }
            if (!split[i].equals(listenerTopic[i])) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    private void subscribe() {
        List<String> subscribeTopicSuffix = mqttConfig.getSysSubscribeTopics();
        if (subscribeTopicSuffix == null || subscribeTopicSuffix.isEmpty()) {
            throw new IllegalArgumentException("subscribe topic is empty");
        }
        List<String> subscribeTopic = subscribeTopicSuffix.stream()
                .flatMap(topicSuffix -> topicFormatList.stream().map(t -> String.format(t,subscribeGroup, topicSuffix)))
                .toList();
        subscribeTopic.forEach(topic -> mqttClient.subscribe(topic, 0, s -> {
            if (s.succeeded()) {
                log.info("===>IMqttClient subscribe success.  result[{}] topic[{}]", s.result(), topic);
            } else {
                log.error("===>IMqttClient subscribe fail. topic[{}] ", topic, s.cause());
            }
        }));
    }

}
