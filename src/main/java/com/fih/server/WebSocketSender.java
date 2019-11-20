package com.fih.server;

import com.fih.entity.SendMessageEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class WebSocketSender {

    private final int pageSize=100;

    private final String msgSplit=":@";

    /**
     * 是否设置了收件人
     *
     * @return
     */
    public boolean hasAddressee(String message) throws Exception {
        if(StringUtils.hasLength(message)&&message.contains(msgSplit)){
            return true;
        }
        return false;
    }

    /**
     * 获取消息收取用户id
     *
     * @return
     */
    public List<String> get2uids(String message) throws Exception {
        if (!hasAddressee(message))
            return null;

        List<String> list=new ArrayList<>();

        String touids=message.split(msgSplit)[0];
        if(touids.contains(",")){
            Arrays.asList(touids.split(",")).forEach(touid -> list.add(touid));
        }else{
            list.add(touids);
        }

        return list;
    }

    /**
     * 发消息给指定用户
     *
     * @param message
     */
    public void send2User(String uid,String message) throws Exception {
        List<String> touids=get2uids(message);
        if(touids.size()>pageSize){
            /*List<WebSocketListenerHandle> list=new ArrayList<>();
            touids.forEach(touid -> {
                WebSocketListenerHandle webSocketServer=GlobalAttr.webSocketMap.get(touid);
                if(!ObjectUtils.isEmpty(webSocketServer))
                    list.add(webSocketServer);

            });*/
            sendAllThread(uid,touids,message);
        }else{
            send2User(uid,touids,message);
        }

    }

    /**
     * 发给指定用户
     *
     * @param touids  接收用户组
     * @param message   信息
     */
    public void send2User(String uid,List<String> touids,String message) throws Exception {
        touids.forEach(touid -> {
            /*WebSocketListenerHandle webSocketServer=GlobalAttr.webSocketMap.get(touid);
            if(ObjectUtils.isEmpty(webSocketServer))
                log.error("{}用户不在线，发送消息失败",touid);
            else{
                sendMessage(webSocketServer,new SendMessageEntity(uid,touid,message));
            }*/

            sendMessage(GlobalAttr.webSocketMap.get(touid),
                    new SendMessageEntity(uid,touid,message));
        });
    }

    /**
     * 群发消息
     *
     * 不包含自己
     *
     * @param message
     */
    public void sendAll(String uid,String message) throws Exception {
        if (GlobalAttr.webSocketMap.size()>pageSize){
            sendAllThread(uid,message);
        }else
            sendAllLoop(uid,message);

    }

    public void sendAllLoop(String uid,String message) throws Exception {
        GlobalAttr.webSocketMap.entrySet().stream().forEach(keset ->{
            String touid=keset.getKey();
            if(!uid.equals(touid)){//信息不发给自己
                sendMessage(GlobalAttr.webSocketMap.get(touid),
                        new SendMessageEntity(uid,touid,message));
            }
        });
    }

    public void sendAllThread(String uid,String message) throws Exception {
        //map转换为list，方便后续根据下标遍历取值
//        List<WebSocketListenerHandle> list=new ArrayList<>(GlobalAttr.webSocketMap.values());
        List<String> touids=new ArrayList<>(GlobalAttr.webSocketMap.keySet());
        sendAllThread(uid,touids,message);
    }

    /**
     * Thread 发送
     *
     * @param list      接收人WebSocketServer
     * @param touids    接收人websocket id
     * @param uid       发送人id
     * @param message   信息
     */
    public void sendAllThread(String uid,List<String> touids, String message) throws Exception {
        //var totalPage = (total + pageSize - 1)/pageSize;
        int totalPage=(touids.size()+pageSize-1)/pageSize;

        int current=1;

        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

        while (current<=totalPage){
            int currentMix=(current-1)*pageSize+1;
            int currentMax=current*pageSize;
            if(currentMax>touids.size())
                currentMax=touids.size();

            int finalCurrentMax = currentMax;

            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    for(int i = (currentMix-1); i< finalCurrentMax; i++){
                        log.info("currentMix:{},currentMax:{},i:{}",currentMix, finalCurrentMax,i);
                        if(uid!=touids.get(i))//信息不发给自己
                            sendMessage(GlobalAttr.webSocketMap.get(touids.get(i)),
                                    new SendMessageEntity(uid,touids.get(i),message));
                    }
                }
            });

            ++current;
        }
    }

    /**
     * 查看用户是否在线
     *
     * @param uid
     * @return
     */
    public boolean hasOnline(String uid) throws Exception {
        return !ObjectUtils.isEmpty(GlobalAttr.userOnlineMap.get(uid));
    }


    public synchronized void sendMessage(WebSocketListenerHandle webSocketServer,SendMessageEntity sme){
        try {
            String message=sme.getMessage();
            if(hasAddressee(message))
                message=message.split(msgSplit)[1];

            if(!hasOnline(sme.getGeterId()))
                log.error("{}用户不在线，发送消息失败,离线消息",sme.getGeterId());
            else if(ObjectUtils.isEmpty(webSocketServer.getSession()) || (!webSocketServer.getSession().isOpen())){
                //若用户意外终端链接，并没有onClose()，手动onClose()，记录离线信息
                log.error("{}用户意外断开链接，发送消息失败,离线消息",sme.getGeterId());
                webSocketServer.onClose(sme.getGeterId());
            }else {
                webSocketServer.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}