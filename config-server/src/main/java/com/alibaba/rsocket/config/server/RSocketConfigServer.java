package com.alibaba.rsocket.config.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RSocketConfigServer {
    public static void main(String[] args) {
        SpringApplication.run(RSocketConfigServer.class, args);
    }
}
