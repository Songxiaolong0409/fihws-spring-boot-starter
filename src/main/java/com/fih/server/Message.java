package com.fih.server;

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
