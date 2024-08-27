package com.qiankun.matt.broker.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: 基于 ReentrantKeyLock 实现的锁
 * @Date : 2024/08/27 11:32
 * @Auther : tiankun
 */
@Slf4j
public class ReentrantKeyLock implements IKeyLock{
    //通过ConcurrentHashMap来保存各个key的锁
    private final Map<String, ReentrantLock> lockTable =new ConcurrentHashMap<>();

    /**
     * 通过key来获取ReentrantLock
     * @param key key
     * @return 该key对应的ReentrantLock
     */
    private ReentrantLock getLock(String key) {
        ReentrantLock lock = lockTable.get(key);
        if (lock == null ){
            lock = create(key);
        }
        return lock;
    }

    private void removeLock(String key){
        lockTable.remove(key);
    }

    private synchronized ReentrantLock create(String key) {
        ReentrantLock lock = lockTable.get(key);
        if (lock == null){
            // 这个方法在同一时间只能被一个线程访问
            lock = new ReentrantLock();
            lockTable.putIfAbsent(key,lock);
        }
        return lock;
    }
    @Override
    public boolean tryLock(String key) {
        return getLock(key).tryLock();

    }

    @Override
    public boolean tryLock(String key, int timeout, TimeUnit timeUnit) {
        ReentrantLock reentrantLock = getLock(key);
        try {
            return reentrantLock.tryLock(timeout, timeUnit);
        } catch (InterruptedException e) {
            log.error("try lock error msg[{}]",e.getMessage(),e);
        }
        return false;
    }

    @Override
    public void unlock(String key) {
        ReentrantLock lock = getLock(key);
        if (lock == null) {
            return;
        }
        lock.unlock();
        removeLock(key);
    }
}
