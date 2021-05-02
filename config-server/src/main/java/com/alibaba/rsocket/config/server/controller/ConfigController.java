package com.alibaba.rsocket.config.server.controller;

import com.alibaba.rsocket.cloudevents.CloudEventsNotifyService;
import com.alibaba.rsocket.cloudevents.Json;
import com.alibaba.rsocket.cloudevents.RSocketCloudEventBuilder;
import com.alibaba.rsocket.config.server.ConfigurationServiceSupport;
import com.alibaba.rsocket.events.ConfigEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * config controller
 *
 * @author leijuan
 */
@RestController
@RequestMapping("/config")
public class ConfigController {
    @Autowired
    private ConfigurationServiceSupport configSupport;
    @Autowired
    private CloudEventsNotifyService notifyService;

    @PostMapping("/update/{appName}/{key}")
    public Mono<Void> update(@PathVariable(name = "appName") String appName,
                             @PathVariable(name = "key") String key,
                             @RequestBody String body) {
        return configSupport.put(appName, key, body);
    }

    @PostMapping("/refresh/{appName}")
    public Mono<Void> refresh(@PathVariable(name = "appName") String appName) {
        return configSupport.get(appName, "application.properties")
                .map(body -> {
                    ConfigEvent configEvent = new ConfigEvent(appName, "text/plain", body);
                    return Json.serializeAsText(RSocketCloudEventBuilder.builder(configEvent).build());
                }).flatMap(jsonText -> notifyService.notifyAll(appName, jsonText));
    }

    @PostMapping("/refresh/{appName}/{appId}")
    public Mono<Void> refresh(@PathVariable(name = "appName") String appName,
                              @PathVariable(name = "appId") String appId,
                              @RequestBody String body) {
        ConfigEvent configEvent = new ConfigEvent(appName, "text/plain", body);
        String jsonText = Json.serializeAsText(RSocketCloudEventBuilder.builder(configEvent).build());
        return notifyService.notify(appId, jsonText);
    }

    @GetMapping("/last/{appName}/{key}")
    public Mono<String> fetch(@PathVariable(name = "appName") String appName,
                              @PathVariable(name = "key") String key) {
        return configSupport.get(appName, key);
    }
}
