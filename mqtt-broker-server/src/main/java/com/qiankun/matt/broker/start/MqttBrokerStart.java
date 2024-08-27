package com.qiankun.matt.broker.start;

import com.qiankun.matt.broker.broker.StandaloneMQTTBroker;
import com.qiankun.matt.broker.broker.StandaloneMQTTBrokerBuilder;
import com.qiankun.matt.broker.context.ApplicationContext;
import com.qiankun.matt.broker.security.IAuthSecurity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;

/**
 * @Description:
 * @Date : 2024/08/27 11:38
 * @Auther : tiankun
 */
@Slf4j
@Component
public class MqttBrokerStart implements CommandLineRunner, Runnable {
    @Override
    public void run() {
        StandaloneMQTTBroker standaloneMQTTBroker = new StandaloneMQTTBroker(new StandaloneMQTTBrokerBuilder());
        try {
            standaloneMQTTBroker.start();
            this.initAuth();
        } catch (Exception e) {
            log.error("mqtt broker error msg[{}]",e.getMessage(),e);
        }
    }

    @Override
    public void run(String... args){
        ThreadFactory threadFactory = StandaloneMQTTBroker.newThreadFactory("mqtt-broker-thread", true, 1);
        threadFactory.newThread(this::run).start();
    }

    /**
     * 初始化认证信息
     * @throws Exception
     */
    private void initAuth() throws Exception {
        IAuthSecurity.AuthInfo authInfo1 = new IAuthSecurity.AuthInfo();
        authInfo1.setName("device:85211001");
        authInfo1.setPassword("a123456");
        ApplicationContext.getAuthSecurity().addAuthSecurity(authInfo1);
        IAuthSecurity.AuthInfo authInfo2 = new IAuthSecurity.AuthInfo();
        authInfo2.setName("device:85211002");
        authInfo2.setPassword("a123456");
        ApplicationContext.getAuthSecurity().addAuthSecurity(authInfo2);
    }
}
