package com.qiankun.matt.broker.broker;

/**
 * @Description:
 * @Date : 2024/08/27 11:41
 * @Auther : tiankun
 */
public abstract class AbstractMQTTBrokerBuilder implements IMQTTBrokerBuilder {
    int connectRateLimit = 1000;
    long writeLimit = 512 * 1024;
    long readLimit = 512 * 1024;
    int maxBytesInMessage = 256 * 1024;
    int mqttBossThreads = 64;

    int mqttWorkerThreads = 64;

    int port = 9000;
}
