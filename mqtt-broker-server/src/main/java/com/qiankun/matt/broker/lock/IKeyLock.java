package com.qiankun.matt.broker.lock;

import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Date : 2024/08/27 11:32
 * @Auther : tiankun
 */
public interface IKeyLock {

    boolean tryLock(String key);

    /**
     * 对某个key加超时锁
     * @param key 需要加锁的key
     * @param timeout 获取锁的超时时间 超过这个时间则加锁失败
     * @param timeUnit 时间单位 毫秒、秒、分、小时....
     * @return
     */
    boolean tryLock(String key, int timeout, TimeUnit timeUnit);

    /**
     * 释放锁
     * @param key 需要释放锁的key
     * @return
     */
    void unlock(String key);

}
