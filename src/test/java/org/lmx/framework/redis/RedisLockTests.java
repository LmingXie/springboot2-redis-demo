package org.lmx.framework.redis;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lmx.framework.redis.lock.DistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁测试
 *
 * @description:
 * @author: Mr.LMing.X
 * @create: 2019-12-24 15:59
 **/

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RedisLockTests {

    @Autowired
    private DistributedLock lock;

    ExecutorService executorService = Executors.newFixedThreadPool(4, Executors.defaultThreadFactory());

    @Autowired
    private LockAnnotationInvoke invoke;

    @Test
    public void test() {
        log.info("==================开始执行任务=========================");
        CountDownLatch countDown = new CountDownLatch(100);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i <= 100; i++) {
            executorService.execute(() -> {
                boolean flag = true;
                do {
                    flag = invoke.down(flag);
                    countDown.countDown();
                } while (flag);
            });
        }

        try {
            countDown.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // (100 * 100) 为sleep阻塞时间
        log.info("==================任务执行完成==用时：{}ms=======================", System.currentTimeMillis() - startTime - (100 * 100));

    }

    @Autowired
    private RedisClientTemplate redisClientTemplate;

    @Data
    static class User {
        private String name;
        private Integer age;

        public User() {
        }

        public User(String name, Integer age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    public void test1() throws InterruptedException {
        redisClientTemplate.hSet("UserList", "zs", new User("张三", 21));

        User hGet = redisClientTemplate.hGet("UserList", "zs", User.class);

        log.info("---->>>{}", JSON.toJSONString(hGet));
    }


    @Test
    public void pipeline() {
        final String hKey = "lock:hkey";
        Map<byte[], byte[]> hashes = new HashMap() {{
            put("user", "{\n" +
                    "  \"clientType\": \"APP\",\n" +
                    "  \"countryCode\": \"+86\",\n" +
                    "  \"id\": 8,\n" +
                    "  \"mobileNumber\": \"13566669999\",\n" +
                    "  \"nickname\": \"13566669999\"\n" +
                    "}".getBytes());

            put("token", "This's a token.".getBytes());
        }};
        List<Object> objects = redisClientTemplate.pipelineHMSet(hKey, hashes, 3, TimeUnit.MINUTES);
        log.info("结果：{}", JSON.toJSONString(objects));
    }
}