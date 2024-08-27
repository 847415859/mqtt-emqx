// package com.qiankun.mqqt.rabbitmq.config;
//
// import lombok.extern.slf4j.Slf4j;
// import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.integration.annotation.ServiceActivator;
// import org.springframework.integration.channel.DirectChannel;
// import org.springframework.integration.core.MessageProducer;
// import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
// import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
// import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
// import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
// import org.springframework.messaging.MessageChannel;
// import org.springframework.messaging.MessageHandler;
// import org.springframework.messaging.MessagingException;
//
// /**
//  * @Description:
//  * @Date : 2024/08/27 13:40
//  * @Auther : tiankun
//  */
// @Configuration
// @Slf4j
// public class IotMqttSubscriberConfig {
//     @Autowired
//     private MqttConfig mqttConfig;
//
//     @Bean
//     @ConditionalOnMissingBean
//     public MqttPahoClientFactory mqttClientFactory() {
//         DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
//         MqttConnectOptions options = new MqttConnectOptions();
//         options.setServerURIs(mqttConfig.getServers().split(","));
//         factory.setConnectionOptions(options);
//         return factory;
//     }
//
//     @Bean(name = "iotMqttInputChannel")
//     public MessageChannel iotMqttInputChannel() {
//         return new DirectChannel();
//     }
//
//     @Bean(name = "iotMqttSubscriber")
//     public MessageProducer inbound() {
//         MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(mqttConfig.getClientId(), mqttClientFactory(), mqttConfig.getDefaultTopic());
//         adapter.setCompletionTimeout(5000);
//         adapter.setConverter(new DefaultPahoMessageConverter());
//         adapter.setQos(1);
//         adapter.setOutputChannel(iotMqttInputChannel());
//         return adapter;
//     }
//
//     /**
//      * @author xiaofu
//      * @description 消息订阅
//      * @date 2020/6/8 18:20
//      */
//     @Bean(name = "handlerTest")
//     @ServiceActivator(inputChannel = "iotMqttInputChannel")
//     public MessageHandler handlerTest() {
//         return message -> {
//             try {
//                 String string = message.getPayload().toString();
//                 log.info("接收到消息：{}" , string);
//             } catch (MessagingException ex) {
//                 log.error("消息处理异常: ", ex);
//             }
//         };
//     }
// }
