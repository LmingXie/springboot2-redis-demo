package org.lmx.framework.redis.lock;

import lombok.extern.slf4j.Slf4j;
import org.lmx.framework.redis.RedisClientTemplate;
import org.lmx.framework.redis.constants.CommonConstants;

@Slf4j
public class RedisDistributedLock {
    private final RedisClientTemplate redisClient;

    public RedisDistributedLock(RedisClientTemplate redisClient) {
        this.redisClient = redisClient;
    }

    public boolean acquire(String key, String value, int waitTime, int expire) {
        long start = System.currentTimeMillis();
        do {
            try {
                Boolean lock = acquire(key, value, expire);

                // 获取锁失败
                if (!lock && waitTime == 0) {
                    break;
                }
                Thread.sleep(500);
            } catch (Exception ex) {
                log.error("获取锁失败：{}", ex);
            }
        } while ((System.currentTimeMillis() - start) < waitTime * 1000);

        log.warn("获取分布式锁失败 key：" + key + " value:" + value);
        return Boolean.FALSE;
    }


    public boolean acquire(String key, String value, int expire) {
        key = CommonConstants.PREFIX + key;
        log.debug("开始获取分布式锁 key：" + key + " lock_key:" + key + " value:" + value);

        boolean isSuccess = redisClient.setNx(key, System.currentTimeMillis() + CommonConstants.SEPARATOR + value, (expire > CommonConstants.MAX_EXPIRE_TIME) ? CommonConstants.MAX_EXPIRE_TIME : expire);
        if (isSuccess) {
            log.info("成功获得分布式锁 key：" + key + " value:" + value);
            return Boolean.TRUE;
        }
        // 存在锁,并对死锁进行修复
        else {
            String desc = redisClient.get(key);

            // 首次锁检测
            if (desc != null && desc.indexOf(CommonConstants.SEPARATOR) > 0) {
                // 上次上锁时间
                long lastLockTime = Long.parseLong(desc.split("[" + CommonConstants.SEPARATOR + "]")[0]);
                // 明确死锁,利用setEx复写，再次设定一个合理的解锁时间让系统正常解锁
                if (System.currentTimeMillis() - lastLockTime > (expire + CommonConstants.OFFSET_TIME) * 1000) {
                    // 原子操作，只需要一次,【任然会发生小概率事件，多个服务同时发现死锁同时执行此行代码(并发),
                    // 为什么设置解锁时间为expire（而不是更小的时间），防止在解锁发送错乱造成新锁解锁】
                    redisClient.setEx(key, value, expire);
                    log.warn("发现死锁【" + expire + "秒后解锁】key：" + key + " desc:" + desc);
                } else {
                    log.debug("没有发现死锁，当前锁key：" + key + " desc:" + desc);
                }
            } else {
                log.warn("死锁解锁中key：" + key + " desc:" + desc);
            }
        }
        return Boolean.FALSE;
    }

    public boolean release(String key) {
        String lock_key = CommonConstants.PREFIX + key;
        try {
            return redisClient.del(lock_key);
        } catch (Exception ex) {
            log.error("解锁锁失败key：{} lock_key：{} Message：", key, lock_key, ex);
        }
        return Boolean.FALSE;
    }
}