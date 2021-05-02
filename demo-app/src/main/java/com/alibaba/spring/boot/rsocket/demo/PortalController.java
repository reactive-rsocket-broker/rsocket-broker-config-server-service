package com.alibaba.spring.boot.rsocket.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RefreshScope
public class PortalController {
    @Value("${nick:unknown}")
    private String nick;

    @GetMapping("/")
    public Mono<String> index() {
        return Mono.just("Hello " + nick);
    }

}
