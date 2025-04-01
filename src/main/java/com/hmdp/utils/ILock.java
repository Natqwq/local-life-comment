package com.hmdp.utils;

public interface ILock {
    /**
     * 尝试获取锁
     * @param timeSec 秒
     * @return 是否获取成功
     */
    boolean tryLock(Long timeSec);
    /**
     * 释放锁
     */
    void unlock();
}
