package com.fih.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SendMessageEntity implements Serializable {

    public SendMessageEntity(String senderId,String geterId,String message){
        this.senderId=senderId;
        this.geterId=geterId;
        this.message=message;
    }

    private String senderId;

    private String geterId;

    private String message;
}
