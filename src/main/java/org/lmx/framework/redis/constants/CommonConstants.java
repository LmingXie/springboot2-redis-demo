package org.lmx.framework.redis.constants;

/**
 * 功能描述: 常量类
 *
 * @author LM.X
 * @date 2020/4/24 16:52
 */
public interface CommonConstants {

    /**
     * 最长时间锁为1天
     */
    int MAX_EXPIRE_TIME = 24 * 60 * 60;

    /**
     * 系统时间偏移量15秒，服务器间的系统时间差不可以超过15秒,避免由于时间差造成错误的解锁
     */
    int OFFSET_TIME = 15;

    /**
     * 分隔符
     */
    String SEPARATOR = "$T$";

    /**
     * 锁前缀
     */
    String PREFIX = "lock:";
}
