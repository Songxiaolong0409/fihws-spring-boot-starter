# WebSocket springBoot 的单机版实现，后续会补充集群模式

## 使用方式

### 1. 引用jar

    <dependency>
          <groupId>com.fih</groupId>
          <artifactId>fihws-spring-boot-starter</artifactId>
          <version>1.0-SNAPSHOT</version>
    </dependency>

### 2. 配置日志（可选）

    spring:
      application:
        name: viss-server
    
    
    logging:
      config: classpath:logback-spring.xml
      path: logs/
      
### 3. 启动application,并访问websocket

    ws://[ip]:[端口号]/websocket/[客户端唯一识别id，用户id]