package com.fih.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 *  websocket服务端例子
 *  {uid}客户端的用户id
 */
@ServerEndpoint("/websocket/{uid}")
@Component
@Slf4j
public class WebSocketServer {

    private static WebSocketSender webSocketSender;

    @Autowired
    public void setWebSocketSender(WebSocketSender webSocketSender){
        WebSocketServer.webSocketSender=webSocketSender;
    }

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(@PathParam("uid") String uid, Session session) {
        if(webSocketSender.hasOnline(uid)){
            log.info("此用户已连接，关闭原会话，再重建会话。。。");
            /*
                若已存在的用户重联，防止用户意外断开后的重联，所以先调用关闭连接的方法，关闭原连接。
                再继续重联。
             */
            onClose(uid);
        }

        this.session = session;

        GlobalAttr.webSocketSet.add(this);
        GlobalAttr.addOnlineCount();

        GlobalAttr.webSocketMap.put(uid,this);

        log.info("{}加入会话，当前在线人数为 {}", uid,GlobalAttr.getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("uid") String uid) {
        GlobalAttr.webSocketSet.remove(this);
        GlobalAttr.subOnlineCount();
        GlobalAttr.webSocketMap.remove(uid);
        log.info("{}断开连接，当前在线人数为 {}" , uid,GlobalAttr.getOnlineCount());
    }


    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(@PathParam("uid") String uid,String message, Session session) throws Exception {
        try {
            this.session = session;
            log.info("{}" , message);

            if(webSocketSender.hasAddressee(message))
                webSocketSender.send2User(uid,message);
            else
                webSocketSender.sendAll(uid,message);
        } catch (Exception e) {
            onError(e);
        }
    }


    /**
     * 发生错误时调用
     **/
    @OnError
    public void onError(Throwable error) {
        log.error("onMessage方法异常{}" , error.toString());
        error.printStackTrace();
    }


    /**
     * 发送消息需注意方法加锁synchronized，避免阻塞报错
     * 注意session.getBasicRemote()与session.getAsyncRemote()的区别
     * @param message
     * @throws IOException
     */
    public synchronized void sendMessage(String message) throws IOException {
        log.info("正在发送消息：{}",message);

        this.session.getAsyncRemote().sendText(message);
    }

}