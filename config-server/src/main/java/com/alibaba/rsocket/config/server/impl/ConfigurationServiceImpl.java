package com.alibaba.rsocket.config.server.impl;

import com.alibaba.rsocket.RSocketService;
import com.alibaba.rsocket.config.ConfigurationService;
import com.alibaba.rsocket.config.server.ConfigurationServiceSupport;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RSocketService(serviceInterface = ConfigurationService.class)
@Service
public class ConfigurationServiceImpl implements ConfigurationService {
    @Autowired
    private ConfigurationServiceSupport configurationServiceSupport;

    @Override
    public Mono<String> get(@NotNull String appName, @NotNull String key) {
        return configurationServiceSupport.get(appName, key);
    }
}
