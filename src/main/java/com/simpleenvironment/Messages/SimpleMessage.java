package com.simpleenvironment.Messages;

import java.io.Serializable;

import akka.actor.ActorRef;

public class SimpleMessage implements Serializable {
    
    private String message;
    private Type type;

    private ActorRef childRef;

    public SimpleMessage(String message, Type type){
        this.message = message;
        this.type = type;
    }

    public SimpleMessage(ActorRef childRef, Type type){
        this.childRef = childRef;
        this.type = type;
    }

    public String getMessage(){
        return this.message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public Type getType(){
        return this.type;
    }

    public ActorRef getChildRef(){
        return this.childRef;
    }
}
