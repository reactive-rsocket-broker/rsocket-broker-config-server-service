package com.alibaba.rsocket.config.server;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

/**
 * configuration service support to include put, get and delete operatioons
 *
 * @author leijuan
 */
public interface ConfigurationServiceSupport {

    Mono<Void> put(@NotNull String appName, @NotNull String key, @NotNull String value);

    Mono<String> get(@NotNull String appName, @NotNull String key);

    Mono<Void> delete(@NotNull String appName, @NotNull String key);
}
