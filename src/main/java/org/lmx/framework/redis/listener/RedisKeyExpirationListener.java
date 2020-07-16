package org.lmx.framework.redis.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.listener.KeyspaceEventMessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 功能描述: Redis Key过期监听
 *
 * @author LM.X
 * @date 2020/7/4 17:11
 */
@Slf4j
@Component
@ConditionalOnBean(RedisMessageListenerContainer.class)
public class RedisKeyExpirationListener extends KeyspaceEventMessageListener implements
        ApplicationEventPublisherAware {

    private static final Topic KEY_EVENT_EXPIRED_TOPIC = new PatternTopic("__keyevent@0__:expired");

    private @Nullable
    ApplicationEventPublisher publisher;

    /**
     * Creates new {@link org.springframework.data.redis.connection.MessageListener} for {@code __keyevent@*__:expired} messages.
     *
     * @param listenerContainer must not be {@literal null}.
     */
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    protected void doRegister(RedisMessageListenerContainer listenerContainer) {
        listenerContainer.addMessageListener(this, KEY_EVENT_EXPIRED_TOPIC);
    }

    @Override
    protected void doHandleMessage(Message message) {
        publishEvent(new RedisKeyExpiredEvent(message.getBody()));
    }

    /**
     * Publish the event in case an {@link ApplicationEventPublisher} is set.
     *
     * @param event can be {@literal null}.
     */
    protected void publishEvent(RedisKeyExpiredEvent event) {

        if (publisher != null) {
            this.publisher.publishEvent(event);
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("redis key过期：{}", expiredKey);
        //业务逻辑处理。。。
    }
}