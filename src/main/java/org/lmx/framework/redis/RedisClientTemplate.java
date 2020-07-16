package org.lmx.framework.redis;

import lombok.extern.slf4j.Slf4j;
import org.lmx.framework.redis.base.AbstractTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 功能描述: Redis工具类
 *
 * <pre>
 *     通过{demo.redis.hash-key-enabled}配置项可以开启压缩模式。
 * </pre>
 *
 * @author LM.X
 * @return
 * @date 2020/7/16 18:43
 */
@Slf4j
@Component
@ConditionalOnExpression("${demo.redis.enabled:false}")
public final class RedisClientTemplate extends AbstractTemplate {
    //    private final RedisTemplate<String, Object> jacksonRedisTemplate;
    private final StringRedisTemplate redisTemplate;

    public RedisClientTemplate(@Value("${demo.redis.hash-key-enabled:false}") Boolean isHashKey, @Value("${demo.redis.hash-key-count:0}") Integer keyCount, StringRedisTemplate redisTemplate) {
        super(isHashKey, keyCount);
        this.redisTemplate = redisTemplate;
    }

    // =============================common============================

    public boolean expire(String key, long time, TimeUnit unit) {
        try {
            if (time > 0) {
                redisTemplate.expire(hashKey(key), time, unit);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis Client expire Error:", e);
            return false;
        }
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        return expire(hashKey(key), time, TimeUnit.SECONDS);
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(hashKey(key), TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(hashKey(key));
        } catch (Exception e) {
            log.error("Redis client call hasKey() Error:", e);
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public boolean del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                return redisTemplate.delete(hashKey(key[0]));
            } else {
                List<String> ks = new ArrayList<>();
                for (String k : key) {
                    ks.add(hashKey(k));
                }
                Long delCount = redisTemplate.delete(ks);
                return key.length == delCount;
            }
        }
        return false;
    }

    // ============================String=============================


    public boolean setNx(String key, Object value, int seconds, TimeUnit unit) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(hashKey(key), serializer(value), seconds, unit);
        } catch (Exception e) {
            log.error("Redis Client setNx Error:", e);
            return false;
        }
    }

    public boolean setNx(String key, Object value, int seconds) {
        return setNx(hashKey(key), serializer(value), seconds, TimeUnit.SECONDS);
    }

    public boolean setEx(String key, Object value, int seconds) {
        return setEx(hashKey(key), serializer(value), seconds, TimeUnit.SECONDS);
    }

    /**
     * set值并设置超时时间
     *
     * @param key     String键
     * @param value   值
     * @param seconds 时间步长
     * @param unit    时间尺度
     * @return true Or false
     */
    public boolean setEx(String key, Object value, int seconds, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(hashKey(key), serializer(value), seconds, unit);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            return (key == null) ? null : deserializer(redisTemplate.opsForValue().get(hashKey(key)), clazz);
        } catch (Exception e) {
            log.error("Redis client call get(T) Error:", e);
            return null;
        }
    }

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        try {
            return (key == null) ? null : redisTemplate.opsForValue().get(hashKey(key));
        } catch (Exception e) {
            log.error("Redis client call get() Error:", e);
            return null;
        }
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(hashKey(key), serializer(value));
            return true;
        } catch (Exception e) {
            log.error("Redis client call set(T) Error:", e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(hashKey(key), serializer(value), time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis client call set(2) Error:", e);
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(hashKey(key), delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(hashKey(key), -delta);
    }
    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public <T> T hGet(String key, String item, Class<T> clazz) {
        Object o = redisTemplate.opsForHash().get(hashKey(key), item);
        return deserializer(o.toString(), clazz);
    }

    public Object hGet(String key, String item) {
        return redisTemplate.opsForHash().get(hashKey(key), item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmGet(String key) {
        return redisTemplate.opsForHash().entries(hashKey(key));
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmSet(String key, Map<String, String> map) {
        try {
            redisTemplate.opsForHash().putAll(hashKey(key), map);
            return true;
        } catch (Exception e) {
            log.error("Redis client call hmSet() Error:", e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hSet(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(hashKey(key), item, serializer(value));
            return true;
        } catch (Exception e) {
            log.error("Redis client call hSet() Error:", e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hDel(String key, String... item) {
        redisTemplate.opsForHash().delete(hashKey(key), item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(hashKey(key), item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public double hIncr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(hashKey(key), item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    public double hDecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(hashKey(key), item, -by);
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<String> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(hashKey(key));
        } catch (Exception e) {
            log.error("Redis client call sGet() Error:", e);
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(hashKey(key), serializer(value));
        } catch (Exception e) {
            log.error("Redis client call Key(Str() Error:", e);
            return false;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(hashKey(key));
        } catch (Exception e) {
            log.error("Redis client call sGetSetSize() Error:", e);
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, String... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(hashKey(key), values);
            return count;
        } catch (Exception e) {
            log.error("Redis client call setRemove() Error:", e);
            return 0;
        }
    }
    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List<String> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(hashKey(key), start, end);
        } catch (Exception e) {
            log.error("Redis client call lGet() Error:", e);
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(hashKey(key));
        } catch (Exception e) {
            log.error("Redis client call lGetListSize() Error:", e);
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(hashKey(key), index);
        } catch (Exception e) {
            log.error("Redis client call lGetIndex() Error:", e);
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(hashKey(key), serializer(value));
            return true;
        } catch (Exception e) {
            log.error("Redis client call lSet(1) Error:", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            key = hashKey(key);
            redisTemplate.opsForList().rightPush(key, serializer(value));
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis client call lSet(2) Error:", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, List<String> value) {
        try {
            redisTemplate.opsForList().rightPushAll(hashKey(key), value);
            return true;
        } catch (Exception e) {
            log.error("Redis client call lSet() Error:", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, List<String> value, long time) {
        try {
            key = hashKey(key);
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis client call lSet() Error:", e);
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(hashKey(key), index, serializer(value));
            return true;
        } catch (Exception e) {
            log.error("Redis client call lUpdateIndex() Error:", e);
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(hashKey(key), count, serializer(value));
            return remove;
        } catch (Exception e) {
            log.error("Redis client call lRemove() Error:", e);
            return 0;
        }
    }
    // ===============================高级特性=================================

    /*
     * <pre>RedisCluster中使用pipeline时必须满足pipeline打包的所有命令key在RedisCluster的同一个slot上。</pre>
     * <pre>
     *     {@link RedisCluster} 还可以通过其高级特性{@code hashtag}，实现强相关性的一组Key落在同一个slot哈希槽上面。
     *
     *  但，一定要注意，不能把key的离散性变得非常差，例如：
     *
     *  没有利用hashtag特性之前，key是这样的：mall:sale:freq:ctrl:860000000000001，很明显这种key由于与用户相关，所以离散性非常好。
     *
     *  而使用hashtag以后，key是这样的：mall:sale:freq:ctrl:{860000000000001}，这种key还是与用户相关，所以离散性依然非常好。
     *
     *  原链接：https://blog.csdn.net/tianyaleixiaowu/article/details/104964304
     *  </pre>
     */

    /**
     * 功能描述: 通过pipeline优化 hmSet命令的性能
     *
     * @param hKey    哈希键
     * @param hashes  哈希值列表
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 返回值，hMSet无返回值，因此默认返回expire的结果
     * @author LM.X
     * @date 2020/7/15 17:29
     */
    public List<Object> pipelineHMSet(final String hKey, Map<byte[], byte[]> hashes, final long timeout, final TimeUnit unit) {
        // RedisCallback 不支持事务  SessionCallback 支持事务
        return redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    //开启事物
                    byte[] hKeyBytes = hKey.getBytes();
                    connection.hMSet(hKeyBytes, hashes);
                    connection.expire(hKeyBytes, TimeoutUtils.toSeconds(timeout, unit));

                    // 返回值自动装配
                    return null;
                }, // 自定义序列化
                redisTemplate.getKeySerializer());
    }

    public List<Object> pipelineHMSetByZip(final String hKey, Map<byte[], byte[]> hashes, final long timeout, final TimeUnit unit) {
        // RedisCallback 不支持事务  SessionCallback 支持事务
        return redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    //开启事物
                    byte[] hKeyBytes = hashKey(hKey).getBytes();
                    connection.hMSet(hKeyBytes, hashes);
                    connection.expire(hKeyBytes, TimeoutUtils.toSeconds(timeout, unit));

                    // 返回值自动装配
                    return null;
                }, // 自定义序列化
                redisTemplate.getKeySerializer());
    }

    /**
     * 功能描述: 通过pipeline优化 hSet命令的性能
     *
     * @param hKey    哈希键
     * @param item    项
     * @param value   值
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return java.util.List<java.lang.Object>
     * @author LM.X
     * @date 2020/7/15 18:11
     */
    public List<Object> pipelineHSet(final String hKey, final String item, String value, final long timeout, final TimeUnit unit) {
        // RedisCallback 不支持事务  SessionCallback 支持事务
        return redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    //开启事物
                    byte[] hKeyBytes = hKey.getBytes();
                    connection.hSet(hKeyBytes, item.getBytes(), value.getBytes());
                    connection.expire(hKeyBytes, TimeoutUtils.toSeconds(timeout, unit));

                    // 返回值自动装配
                    return null;
                }, // 自定义序列化
                redisTemplate.getKeySerializer());
    }

    /**
     * 功能描述: 压缩方式存储的Set
     *
     * <pre>
     *     需开启配置项：demo.redis.hash-key-enabled
     *     若不需要设定超时时间，推荐使用压缩方式的Set。
     * </pre>
     *
     * @param hKey
     * @param item
     * @param value
     * @return boolean
     * @author LM.X
     * @date 2020/7/16 13:51
     */
    public boolean setByZip(final String hKey, final String item, Object value) {
        try {
            redisTemplate.opsForHash().put(super.hashKey(hKey), super.BKDRHashCode(item), super.serializer(value));
        } catch (Exception e) {
            log.error("Redis client call setByZip() Error：", e);
            return false;
        }
        return true;
    }

    /**
     * 功能描述: 压缩方式存储Get
     *
     * @param item
     * @param clazz
     * @return T
     * @author LM.X
     * @date 2020/7/16 14:13
     */
    public <T> T getByZip(final String hKey, final String item, Class<T> clazz) {
        try {
            Object obj = redisTemplate.opsForHash().get(super.hashKey(hKey), super.BKDRHashCode(item));
            return deserializer(obj.toString(), clazz);
        } catch (Exception e) {
            log.error("Redis client call getByZip() Error:", e);
            return null;
        }
    }
}