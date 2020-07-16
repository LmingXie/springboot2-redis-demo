package org.lmx.framework.redis.lock;

import java.util.concurrent.TimeUnit;

/**
 * 功能描述: 分布式锁接口
 *
 * @author LM.X
 * @date 2020/4/27 9:44
 */
public interface DistributedLock {

    /**
     * 功能描述: 获取锁，如果获取不成功则一直等待直到lock被获取
     *
     * @param key       锁的key
     * @param leaseTime 加锁的时间，超过这个时间后锁便自动解锁；
     *                  如果leaseTime为-1，则保持锁定直到显式解锁
     * @param unit      {@code leaseTime} 参数的时间单位
     * @param isFair    是否公平锁
     * @return 锁对象
     * @author LM.X
     * @date 2020/7/10 14:58
     */
    Object lock(String key, long leaseTime, TimeUnit unit, boolean isFair);

    /**
     * 功能描述: 获取锁，如果获取不成功则一直等待直到lock被获取
     *
     * @param key       锁的key
     * @param leaseTime 加锁的时间，超过这个时间后锁便自动解锁；
     *                  如果leaseTime为-1，则保持锁定直到显式解锁
     * @param unit      {@code leaseTime} 参数的时间单位
     * @return 锁对象
     */
    Object lock(String key, long leaseTime, TimeUnit unit);

    /**
     * 功能描述: 获取锁，如果获取不成功则一直等待直到lock被获取
     *
     * @param key    锁的key
     * @param isFair 是否公平锁
     * @return 锁对象
     * @author LM.X
     * @date 2020/7/10 15:00
     */
    Object lock(String key, boolean isFair);

    /**
     * 功能描述: 获取锁，如果获取不成功则一直等待直到lock被获取
     *
     * @param key 锁的key
     * @return 锁对象
     * @author LM.X
     * @date 2020/7/10 15:00
     */
    Object lock(String key);

    /**
     * 功能描述:  尝试获取锁，如果锁不可用则等待最多waitTime时间后放弃
     *
     * @param key       锁的key
     * @param waitTime  获取锁的最大尝试时间(单位 {@code unit})
     * @param leaseTime 加锁的时间，超过这个时间后锁便自动解锁；
     *                  如果leaseTime为-1，则保持锁定直到显式解锁
     * @param unit      {@code waitTime} 和 {@code leaseTime} 参数的时间单位
     * @param isFair    是否公平锁
     * @return 锁对象
     * @author LM.X
     * @date 2020/7/10 15:01
     */
    Object tryLock(String key, long waitTime, long leaseTime, TimeUnit unit, boolean isFair);

    /**
     * 功能描述: 尝试获取锁，如果锁不可用则等待最多waitTime时间后放弃
     *
     * @param key       锁的key
     * @param waitTime  获取锁的最大尝试时间(单位 {@code unit})
     * @param leaseTime 加锁的时间，超过这个时间后锁便自动解锁；
     *                  如果leaseTime为-1，则保持锁定直到显式解锁
     * @param unit      {@code waitTime} 和 {@code leaseTime} 参数的时间单位
     * @return 锁对象
     * @author LM.X
     * @date 2020/7/10 15:02
     */
    Object tryLock(String key, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 功能描述: 尝试获取锁，如果锁不可用则等待最多waitTime时间后放弃
     *
     * @param key      锁的key
     * @param waitTime 获取锁的最大尝试时间(单位 {@code unit})
     * @param unit     {@code waitTime} 和 {@code leaseTime} 参数的时间单位
     * @param isFair   是否公平锁
     * @return 锁对象
     * @author LM.X
     * @date 2020/7/10 15:02
     */
    Object tryLock(String key, long waitTime, TimeUnit unit, boolean isFair);

    /**
     * 功能描述: 尝试获取锁，如果锁不可用则等待最多waitTime时间后放弃
     *
     * @param key      锁的key
     * @param waitTime 获取锁的最大尝试时间(单位 {@code unit})
     * @param unit     {@code waitTime} 和 {@code leaseTime} 参数的时间单位
     * @return 锁对象
     * @author LM.X
     * @date 2020/7/10 15:02
     */
    Object tryLock(String key, long waitTime, TimeUnit unit);

    /**
     * 释放锁
     *
     * @param key
     * @return 释放结果
     * @author LM.X
     * @date 2019/12/24 14:31
     */
    boolean unlock(Object key);
}