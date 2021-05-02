package com.alibaba.rsocket.config.server;

import com.alibaba.rsocket.cloudevents.CloudEventsNotifyService;
import com.alibaba.rsocket.invocation.RSocketRemoteServiceBuilder;
import com.alibaba.rsocket.upstream.UpstreamManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
public class RSocketServicesConfiguration {
    @Bean
    public CloudEventsNotifyService cloudEventsNotifyService(UpstreamManager upstreamManager) {
        return RSocketRemoteServiceBuilder
                .client(CloudEventsNotifyService.class)
                .upstreamManager(upstreamManager)
                .build();
    }
}
