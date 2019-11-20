package com.fih.server;

import com.fih.config.RabbitMQConfig;
import com.fih.entity.RabbitPushMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  websocket服务端例子
 *  {uid}客户端的用户id
 */
@ServerEndpoint("/websocket/{uid}")
@Component
@Slf4j
public class WebSocketListenerHandle {

    private ExecutorService executorService=Executors.newCachedThreadPool();

    private static RabbitTemplate rabbitTemplate;

    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate){
        WebSocketListenerHandle.rabbitTemplate=rabbitTemplate;
    }

    private static WebSocketSender webSocketSender;

    @Autowired
    public void setWebSocketSender(WebSocketSender webSocketSender){
        WebSocketListenerHandle.webSocketSender=webSocketSender;
    }

    public WebSocketSender getWebSocketSender(){
        return  webSocketSender;
    }

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;


    private HttpSession httpSession;

    public Session getSession(){
        return session;
    }

    /**
     * 连接建立成功调用的方法
     *
     * 为了避免同一个用户多次打开websocket链接，当判断是同一个用户时，先关闭之前链接，再重新打开。
     */
    @OnOpen
    public void onOpen(@PathParam("uid") String uid, Session session,
                       EndpointConfig config) throws Exception {
        log.info("onOpen...");
        if(webSocketSender.hasOnline(uid)){
            log.info("此用户已连接，关闭原会话，再重建会话。。。");

            /*executorService.execute(new Runnable() {
                @Override
                public void run() {
                    config.getUserProperties().remove(HttpSession.class.getName());
                    onClose(uid);
                }
            });*/
            onClose(uid);
            onOpen(uid,session);
        }else
            onOpen(uid,session);
    }

    public void onOpen(String uid, Session session) throws Exception {
        //this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        this.session = session;

        //把新注册的用户放入mq中，同步到所有服务。
        RabbitPushMessage rabbitPushMessage=new RabbitPushMessage(uid,uid,RabbitPushMessage.ADD);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_PUSH,RabbitMQConfig.TOPIC_PUSH_CONN_ADD,rabbitPushMessage);

        GlobalAttr.webSocketMap.put(uid,this);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("uid") String uid) {
        log.info("onClose...");
        RabbitPushMessage rabbitPushMessage=new RabbitPushMessage(uid,uid,RabbitPushMessage.DEL);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_PUSH,RabbitMQConfig.TOPIC_PUSH_CONN_DEL,rabbitPushMessage);
    }


    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(@PathParam("uid") String uid,String message, Session session) throws Exception {
        log.info("onMessage...");
        try {
            this.session = session;
            log.info("{}" , message);

            RabbitPushMessage rabbitPushMessage=new RabbitPushMessage(uid,message,null);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_PUSH,RabbitMQConfig.TOPIC_PUSH_MSG_CONTENT,rabbitPushMessage);
        } catch (Exception e) {
            onError(uid,e);
        }
    }


    /**
     * 发生错误时调用
     **/
    @OnError
    public void onError(@PathParam("uid") String uid,Throwable error) {
        log.error("onMessage方法异常{}" , error.toString());
        onClose(uid);
    }


    /**
     * 发送消息需注意方法加锁synchronized，避免阻塞报错
     * 注意session.getBasicRemote()与session.getAsyncRemote()的区别
     * @param message
     * @throws IOException
     */
    public synchronized void sendMessage(String message) throws Exception {
        log.info("正在发送消息：{}",message);
        this.session.getBasicRemote().sendText(message);
    }

}