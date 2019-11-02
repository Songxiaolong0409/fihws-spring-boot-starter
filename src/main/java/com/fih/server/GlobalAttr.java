package com.fih.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * webSocket 全局属性
 */
public class GlobalAttr {

    //静态变量，用来记录当前在线连接数。
    private static int onlineCount = 0;

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        GlobalAttr.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        if(GlobalAttr.getOnlineCount()>0)
            GlobalAttr.onlineCount--;
    }

    //concurrent包的线程安全，用来存放每个客户端对应的WebSocketServer对象。
    public static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();


    public static ConcurrentHashMap<String,WebSocketServer> webSocketMap=new ConcurrentHashMap<>();

}
