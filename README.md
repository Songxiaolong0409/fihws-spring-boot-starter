# WebSocket springBoot RibbitMq集群模式

通过nginx做反向代理负载均衡，当接收到客户端发送的消息时，放入mq中。
然后所有应用从mq中获取消息，发送给应用已注册的用户。

## 使用方式

### 1. 引用jar

    <dependency>
          <groupId>com.fih</groupId>
          <artifactId>fihws-spring-boot-starter</artifactId>
          <version>2.01.01.191115-SNAPSHOT</version>
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
    
## 6.nginx.config 配置

	map $http_upgrade $connection_upgrade {
        default upgrade;
        '' close;
    }
	
	upstream ws {
		server 192.168.168.220:8083;
		server 192.168.168.220:8084;
	}
	
	server {
		listen	9999;
		server_name 192.168.166.103;
		location / {
			proxy_pass	http://ws;
			proxy_read_timeout 300s;
			proxy_set_header Host $host;
			proxy_set_header X-Real-IP $remote_addr;
			proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
			proxy_http_version 1.1;
			proxy_set_header Upgrade $http_upgrade;
			proxy_set_header Connection $connection_upgrade;
		}
	}
   
## 7. 启动jar

    java -server -Xms2048m -Xmx2048m -Xmn512m -jar xxx.jar 