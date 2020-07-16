package org.lmx.framework.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class AppAccount {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AppAccount.class);
        log.info("服务启动成功，bean总数:{}", context.getBeanDefinitionCount());
    }
}
