package com.fih.server;

import com.alibaba.fastjson.JSONObject;
import com.fih.config.RabbitMQConfig;
import com.fih.entity.RabbitPushMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@Slf4j
public class RabbitMsgListener {

    private static WebSocketSender webSocketSender;

    @Autowired
    public void setWebSocketSender(WebSocketSender webSocketSender){
        RabbitMsgListener.webSocketSender=webSocketSender;
    }

    /**
     * 消费 QUERY_PUSH_CONN 消息
     */
    @RabbitListener(queues=RabbitMQConfig.TOPIC_PUSH_CONN+".${push.config.queue}")
    public void addUser(RabbitPushMessage message) {
        log.info("【{}】：{}",RabbitMQConfig.TOPIC_PUSH_CONN,JSONObject.toJSON(message));

        if(!ObjectUtils.isEmpty(message)){
            if(RabbitPushMessage.ADD.equals(message.getType())){

                GlobalAttr.userOnlineMap.put(message.getUid(),message.getUid());
                log.info("{}加入，当前在线人数为 {}" , message.getUid(),GlobalAttr.getOnlineCount());
            }else if(RabbitPushMessage.DEL.equals(message.getType())){

                GlobalAttr.userOnlineMap.remove(message.getUid());
                if(!ObjectUtils.isEmpty(GlobalAttr.webSocketMap.get(message.getUid()))){
                    GlobalAttr.webSocketMap.remove(message.getUid());
                    log.info("{}断开连接，当前在线人数为 {}" , message.getUid(),GlobalAttr.getOnlineCount());
                }

            }
        }
    }

    /**
     * 消费 QUERY_PUSH 消息
     * @param msg
     */
    @RabbitListener(queues=RabbitMQConfig.TOPIC_PUSH_MSG+".${push.config.queue}")
    public void sendMsg(RabbitPushMessage message) {
        log.info("【{}】：{}",RabbitMQConfig.TOPIC_PUSH_MSG,JSONObject.toJSON(message));

        if(!ObjectUtils.isEmpty(message)){
            /*WebSocketListenerHandle concurrentHashMap= GlobalAttr.webSocketMap.get(message.getUid());
            if(ObjectUtils.isEmpty(concurrentHashMap)){
                log.error("MQ里有未读消息。。。");
                return;
            }

            WebSocketSender webSocketSender=GlobalAttr.webSocketMap.get(message.getUid()).getWebSocketSender();
*/
            if(!ObjectUtils.isEmpty(webSocketSender)){
                if(webSocketSender.hasAddressee(message.getMessage())){
                    webSocketSender.get2uids(message.getMessage()).forEach(uid -> {
                        if(!ObjectUtils.isEmpty(GlobalAttr.webSocketMap.get(uid))){
                            webSocketSender.send2User(message.getUid(),message.getMessage());
                        }
                    });
                }else {
                    webSocketSender.sendAll(message.getUid(),message.getMessage());
                }
            }

        }

    }
}
