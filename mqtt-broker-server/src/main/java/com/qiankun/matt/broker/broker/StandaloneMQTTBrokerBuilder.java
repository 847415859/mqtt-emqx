package com.qiankun.matt.broker.broker;

import java.util.UUID;

/**
 * @Description:
 * @Date : 2024/08/27 11:42
 * @Auther : tiankun
 */
public class StandaloneMQTTBrokerBuilder extends AbstractMQTTBrokerBuilder {

    String id = UUID.randomUUID().toString();

    @Override
    public String brokerId() {
        return id;
    }

    @Override
    public IMQTTBroker build() {
        return new StandaloneMQTTBroker(this);
    }
}
