package com.qiankun.mqtt.config;

import com.alibaba.fastjson2.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.qiankun.mqtt.client.IMqttClient;
import com.qiankun.mqtt.client.MqttClientContext;
import com.qiankun.mqtt.client.api.IMqttClientApi;
import com.qiankun.mqtt.client.api.MqttApiImpl;
import com.qiankun.mqtt.client.config.IMqttConfig;
import com.qiankun.mqtt.client.config.MqttConfig;
import com.qiankun.mqtt.client.listener.IMqttListener;
import com.qiankun.mqtt.client.model.message.IMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.concurrent.*;

/**
 * @Description:
 * @Date : 2024/08/26 18:40
 * @Auther : tiankun
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "emqx.sdk.enable", havingValue = "true")
public class EmqxClientAutoConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "emqx.sdk")
    public IMqttConfig mqttConfig() {
        return new MqttConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public IMqttListener mqttListener() {
        return new IMqttListener() {
            @Override
            public String getTopic() {
                return "#";
            }

            @Override
            public String getType(){
                return "";
            }
            @Override
            public void onMessage(IMessage message) {
                LoggerFactory.getLogger(IMqttListener.class)
                        .info("[默认消息监听器]接收到消息. Message[{}]", JSON.toJSONString(message));
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({IMqttConfig.class, IMqttListener.class})
    public IMqttClient mqttClient(IMqttConfig emqXConfig, List<IMqttListener> IMqttListenerList, Environment environment) {
        String serviceName = environment.getProperty("spring.application.name");
        String env = environment.getProperty("spring.profiles.active");
        IMqttClient mqttClient = new MqttClientContext(emqXConfig, IMqttListenerList,serviceName+"-"+env);
        Executors.newSingleThreadExecutor().execute(mqttClient::connect);
        Runtime.getRuntime().addShutdownHook(new Thread(mqttClient::mqttStop));
        return mqttClient;
    }

    @Bean("mqttExecutorService")
    @ConditionalOnBean({IMqttClient.class, IMqttListener.class})
    public ExecutorService mqttExecutorService() {
        ExecutorService executorService = new ThreadPoolExecutor(
                1, 8,
                1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(1024),
                new ThreadFactoryBuilder().setNameFormat("mqttlogin-task-%d").build(),
                (r, executor) -> log.warn("MQTT  task executor rejectedExecution , Runnable Class : {}",
                        r.getClass().getSimpleName()));
        return executorService;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({IMqttConfig.class, IMqttClient.class})
    public IMqttClientApi mqttApi(IMqttClient client, IMqttConfig config) {
        return new MqttApiImpl(config, client);
    }
}
