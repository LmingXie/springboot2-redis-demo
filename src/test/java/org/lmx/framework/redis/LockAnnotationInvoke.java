package org.lmx.framework.redis;

import lombok.extern.slf4j.Slf4j;
import org.lmx.framework.redis.annotation.Lock;
import org.springframework.stereotype.Component;

/**
 * 功能描述：Lock注解测试
 *
 * @author: LM.X
 * @create: 2020-07-13 09:28
 **/
@Slf4j
@Component
public class LockAnnotationInvoke {
    private Integer value = 100;

    @Lock(key = "rHv5yThoNKynpBNdJ2hUYWQ22RHDM2MULX", waitTime = 10, leaseTime = 3)
    public boolean down(boolean flag) {
        try {
            log.info("integer----->{}", value);
            ++value;
            flag = false;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}
