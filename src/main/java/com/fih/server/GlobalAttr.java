package com.fih.server;

import java.util.concurrent.ConcurrentHashMap;

/**
 * webSocket 全局属性
 */
public class GlobalAttr {

    public static int getOnlineCount() {
        return (int) userOnlineMap.mappingCount();
    }

    /**
     *  在线的用户链接
     */
    public static ConcurrentHashMap<String, WebSocketListenerHandle> webSocketMap=new ConcurrentHashMap<>();

    /**
     * 在线的用户都会通过mq同步到所有服务应用。
     */
    public static ConcurrentHashMap<String, String> userOnlineMap=new ConcurrentHashMap<>();

}
