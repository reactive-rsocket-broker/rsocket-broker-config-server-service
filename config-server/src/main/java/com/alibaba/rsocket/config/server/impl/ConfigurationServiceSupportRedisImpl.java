package com.alibaba.rsocket.config.server.impl;

import com.alibaba.rsocket.cloudevents.CloudEventsNotifyService;
import com.alibaba.rsocket.cloudevents.Json;
import com.alibaba.rsocket.cloudevents.RSocketCloudEventBuilder;
import com.alibaba.rsocket.config.server.ConfigurationServiceSupport;
import com.alibaba.rsocket.events.ConfigEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Map;

/**
 * Configuration Service with Redis implementation
 *
 * @author leijuan
 */
@Service
public class ConfigurationServiceSupportRedisImpl implements ConfigurationServiceSupport {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationServiceSupportRedisImpl.class);
    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;
    @Autowired
    private ReactiveRedisConnectionFactory connectionFactory;
    @Autowired
    private CloudEventsNotifyService notifyService;
    private Disposable notificationSubscriber;

    @PostConstruct
    public void init() {
        StreamReceiver.StreamReceiverOptions<String, MapRecord<String, String, String>> options = StreamReceiver.StreamReceiverOptions.builder().pollTimeout(Duration.ofMillis(500)).build();
        StreamReceiver<String, MapRecord<String, String, String>> receiver = StreamReceiver.create(connectionFactory, options);
        Flux<MapRecord<String, String, String>> notifications = receiver.receive(StreamOffset.latest("apps-config-stream"));
        this.notificationSubscriber = notifications.subscribe(message -> {
            final Map<String, String> body = message.getValue();
            String appName = body.get("name");
            String key = body.get("key");
            if (key != null && appName != null) {
                log.info("Received events from Redis stream {} for {}", message.getStream(), appName);
                redisTemplate.opsForHash().get(appName, key).map(value -> {
                    ConfigEvent configEvent = new ConfigEvent(appName, "text/plain", value.toString());
                    return RSocketCloudEventBuilder.builder(configEvent).build();
                }).subscribe(cloudEvent -> {
                    String jsonText = Json.serializeAsText(cloudEvent);
                    if (body.containsKey("id")) {
                        notifyService.notify(body.get("id"), jsonText).subscribe();
                    } else {
                        notifyService.notifyAll(appName, jsonText).subscribe();
                    }
                });
            }
        });
    }

    @PreDestroy
    public void close() {
        if (notificationSubscriber != null) {
            notificationSubscriber.dispose();
        }
    }

    @Override
    public Mono<Void> put(@NotNull String appName, @NotNull String key, @NotNull String value) {
        return redisTemplate.opsForHash().put(appName, key, value).then();
    }

    @Override
    public Mono<Void> delete(@NotNull String appName, @NotNull String key) {
        return redisTemplate.opsForHash().remove(appName, key).then();
    }

    @Override
    public Mono<String> get(@NotNull String appName, @NotNull String key) {
        return redisTemplate.opsForHash().get(appName, key).map(Object::toString);
    }
}
