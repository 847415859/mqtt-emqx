package com.qiankun.matt.broker.broker;

/**
 * @Description:
 * @Date : 2024/08/27 11:40
 * @Auther : tiankun
 */
public interface IMQTTBrokerBuilder {
    String brokerId();

    IMQTTBroker build();
}
