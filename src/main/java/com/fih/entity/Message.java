package com.fih.entity;

import lombok.Data;

@Data
public class Message<T> {

    public Message(T message){
        this.setMessage(message);
    }

    private String uid;

    private String name;

    private T message;
}
