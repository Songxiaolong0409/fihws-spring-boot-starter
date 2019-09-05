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
    
## 4. 环境

    jdk : 1.8.0_201-b09
    springboot : 2.1.7.RELEASE
    
## 5. 指定用户发送信息

    指定用户发送信息时 用户id和信息之间用":@"隔开，多个用户可用英文逗号分割
    示例：
        [用户id],[用户id2] :@ [需要发送的信息]
        
    群发信息，直接发送信息即可。