RSocket Broker Config Client
============================
为RSocket Broker提供配置推送服务，方便各个应用快速使用配置推送服务。

### 如何使用？

* 在pom.xml中添加以下依赖

```
<dependency>
    <groupId>com.alibaba.rsocket</groupId>
    <artifactId>alibaba-broker-config-client-spring-boot-starter</artifactId>
    <version>1.1.1-SNAPSHOT</version>
</dependency>
```

* 在application.properties中添加以下配置，这三个配置项都是必须的

```
spring.application.name=your-app-name
rsocket.brokers=tcp://127.0.0.1:9999
rsocket.jwt-token=your_token_here
```

### 工作原理

* 应用启动时使用HTTP短连接和Broker通讯，获取应用对应的最新的配置，主要是HTTP简单，短连接，第一次获取配置非常方便。
* 应用启动后，通过metadataPush监听broker推送的cloud event(com.alibaba.rsocket.events.ConfigEvent)完成应用刷新

### 配置更新流程

* 首先你需要更新应用对应的配置，如下： 

```http request
### update app configuration
POST http://localhost:8280/config/update/demo-app/application.properties
Content-Type: text/plain

nick=leijuan2
email=xxx@yyy.com
```

* 接下来是触发应用对应的ConfigEvent事件，如下：


```http request
### Trigger app config push
POST http://localhost:8280/config/refresh/demo-app
```

Rsocket Broker Config Server采用Redis作为存储和通知，你还可以使用Redis Stream命令触发配置更新，如下：

```
XADD apps-config-stream * name demo-app key application.properties
```

### Redis的存储结构

RSocket Broker Config Server采用Redis作为存储后端，每一应用对应为一个hash结构，
一个应用可以包含多个配置项，如application.properties, domain.pem等，这些都是hash结构的key。

此外RSocket Broker Config Server采用Redis Stream进行Config触发通知，对应的stream名称为apps-config-stream，  
你可以触发某一应用所有实例的配置更新，也可以触发某一实例的配置，命令如下：

```
XADD apps-config-stream * name demo-app key application.properties
XADD apps-config-stream * name demo-app key application.properties  id xxxx
````
如果你使用其他结构来存储配置，确保和Redis进行同步即可，如git的webhook等。


