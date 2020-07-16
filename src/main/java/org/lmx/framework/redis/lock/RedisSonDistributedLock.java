package org.lmx.framework.redis.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lmx.framework.redis.constants.CommonConstants;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 功能描述:
 * RedisSon分布式锁实现，基本锁功能的抽象实现
 * 本接口能满足绝大部分的需求，高级的锁功能，请自行扩展或直接使用原生api
 *
 * @author LM.X
 * @date 2020/7/10 16:13
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(prefix = "demo.lock", name = "locker-type", havingValue = "redis", matchIfMissing = true)
public class RedisSonDistributedLock implements DistributedLock {
    private final RedissonClient redisSon;

    private RLock getLock(String key, boolean isFair) {
        if (isFair) {
            return redisSon.getFairLock(CommonConstants.PREFIX + key);
        }
        return redisSon.getLock(CommonConstants.PREFIX + key);
    }

    @Override
    public RLock lock(String key, long leaseTime, TimeUnit unit, boolean isFair) {
        RLock lock = getLock(key, isFair);
        lock.lock(leaseTime, unit);
        return lock;
    }

    @Override
    public RLock lock(String key, long leaseTime, TimeUnit unit) {
        return lock(key, leaseTime, unit, false);
    }

    @Override
    public RLock lock(String key, boolean isFair) {
        return lock(key, -1, null, isFair);
    }

    @Override
    public RLock lock(String key) {
        return lock(key, -1, null, false);
    }

    @Override
    public RLock tryLock(String key, long waitTime, long leaseTime, TimeUnit unit, boolean isFair) {
        RLock lock = getLock(key, isFair);
        try {
            if (lock.tryLock(waitTime, leaseTime, unit)) {
                return lock;
            }
        } catch (InterruptedException e) {
            log.error("尝试获取Redis锁异常：", e);
        }
        return null;
    }

    @Override
    public RLock tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        return tryLock(key, waitTime, leaseTime, unit, false);
    }

    @Override
    public RLock tryLock(String key, long waitTime, TimeUnit unit, boolean isFair) {
        return tryLock(key, waitTime, -1, unit, isFair);
    }

    @Override
    public RLock tryLock(String key, long waitTime, TimeUnit unit) {
        return tryLock(key, waitTime, -1, unit, false);
    }

    @Override
    public boolean unlock(Object lock) {
        if (lock != null) {
            if (lock instanceof RLock) {
                RLock rLock = (RLock) lock;
                if (rLock.isLocked()) {
                    rLock.unlock();
                    return true;
                }
                log.error("解锁锁失败，没有锁住。");
            }else{
                log.error("解锁锁失败，非RLock类型锁。key：{} ", lock);
            }
        }
        return false;
    }
}
