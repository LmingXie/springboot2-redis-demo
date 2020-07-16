package org.lmx.framework.redis.exception;

/**
 * 功能描述: 分布式锁异常
 *
 * @author LM.X
 * @date 2020/7/10 9:21
 */
public class LockException extends RuntimeException {
    private static final long serialVersionUID = 6610083281801529147L;

    public LockException(String message) {
        super(message);
    }
}