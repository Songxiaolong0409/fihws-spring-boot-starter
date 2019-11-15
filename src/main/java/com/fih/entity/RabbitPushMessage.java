package com.fih.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class RabbitPushMessage implements Serializable {

    public static final String ADD="+";

    public static final String DEL="-";

    public RabbitPushMessage(){}

    public RabbitPushMessage(String uid,String message,String type){
        this.message=message;
        this.type=type;
        this.uid=uid;
    }

    private String uid;

    private String message;

    private String type;

}
