package org.lmx.framework.redis.base;

import com.alibaba.fastjson.JSON;

import java.util.zip.CRC32;

/**
 * 功能描述：抽象基础模板
 * <pre>
 *  利用Redis Hash数据结构的zipList编码结构特性，重写Key-Value存储方法，节省内存。
 *
 *  当哈希对象可以同时满足以下两个条件时， 哈希对象使用 ziplist 编码：
 *
 *      1、 哈希对象保存的所有键值对的键和值的字符串长度都小于 64 字节；
 *      2、 哈希对象保存的键值对数量小于 512 个；
 *
 *   不能满足这两个条件的哈希对象需要使用 hashtable 编码。
 *
 *  这两个条件的上限值是可以修改的， 具体请看redis.conf配置文件中关于 hash-max-ziplist-value 选项和 hash-max-ziplist-entries 选项的说明。
 * </pre>
 *
 * @author: LM.X
 * @create: 2020-07-16 11:01
 **/
public abstract class AbstractTemplate {
    /**
     * 序列化
     * @param value
     * @return
     */
    protected String serializer(Object value) {
        if (value instanceof String) {
            return value.toString();
        }
        return JSON.toJSONString(value);
    }

    /**
     * 反序列化
     * @param value
     * @param clazz
     * @param <T>
     * @return
     */
    protected  <T> T deserializer(String value, Class<T> clazz) {
        return JSON.parseObject(value, clazz);
    }


    /**
     * 启用 hashKey时生效。
     * <p>
     * 这里设计最大可存储1千万个key-value。
     * <pre>
     *  将1千万个键值对，放到N个bucket中，每个bucket是一个redis的hash数据结构。
     *  并且，每个bucket内不超过默认的512个元素（如果改了配置文件，如1024，则不能超过修改后的值），以避免hash将编码方式从zipList变成hashTable。
     *
     *     1千万 / 512 = 19531。
     *
     *  由于将来要将所有的key进行哈希算法，来尽量均摊到所有bucket里，但由于哈希函数的不确定性，未必能完全平均分配。
     *  所以我们要预留一些空间，譬如我分配25000个bucket，或30000个bucket。
     * </pre>
     */
    protected int KEY_COUNT = 25000;

    protected Boolean isHashKey;

    public AbstractTemplate(boolean isHashKey, int keyCount) {
        this.isHashKey = isHashKey;
        if (keyCount > KEY_COUNT) {
            this.KEY_COUNT = keyCount;
        }
    }

    /**
     * 功能描述: 计算Redis的hash Key，决定将key放到哪个bucket。
     * <pre>
     *  该哈希算法可以将一个字符串变成一个long型的数字，通过获取这个md5型的key的crc32后，再对bucket的数量进行取余，就可以确定该key要被放到哪个bucket中。
     * </pre>
     *
     * @param key 原哈希键
     * @return 返回CRC32计算后的hash key
     * @author LM.X
     * @date 2020/7/15 19:30
     */
    public String hashKey(String key) {
        if (!isHashKey) {
            return key;
        }
        CRC32 crc32 = new CRC32();
        crc32.update(key.getBytes());
        return crc32.getValue() % KEY_COUNT + "";
    }

    /**
     * 功能描述: 计算Hash表内层field
     * <pre>
     * BKDRHash算法 可以有效避免redis里hash结构内层field哈希碰撞的概率，发生碰撞field将被覆盖。
     *
     * 该算法可以将字符串转化成一个Long整形的数字。
     *
     * 原则上，Key-Value存储时，往往Key是被设计成唯一不重复的，故碰撞的概率非常小。
     * </pre>
     *
     * @param field
     * @return BKDRHash
     * @author LM.X
     * @date 2020/7/15 19:33
     */
    public String BKDRHashCode(String field) {
        if (!isHashKey) {
            return field;
        }
        //  31, 131, 1313, 13131, 131313 etc.. 一个质数，值越大哈希越零散
        int seed = 31;
        int hash = 0;
        for (int i = 0; i < field.length(); i++) {
            hash = hash * seed + field.charAt(i);
        }
        return String.valueOf(hash);
    }
}
