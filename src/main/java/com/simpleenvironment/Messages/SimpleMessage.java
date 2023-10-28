package com.simpleenvironment.Messages;

import java.io.Serializable;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public class SimpleMessage implements Serializable {
    
    private String message;
    private Type type;
    private int desiredTemperature;

    private ActorRef actorRef;

    private ActorSelection controlPanelRef;

    public SimpleMessage(String message, Type type){
        this.message = message;
        this.type = type;
    }

    public SimpleMessage(ActorRef actorRef, Type type){
        this.actorRef = actorRef;
        this.type = type;
    }

    public SimpleMessage(ActorSelection controlPanelRef, Type type){
        this.controlPanelRef = controlPanelRef;
        this.type = type;
    }

    public SimpleMessage(int desiredTemperature, Type type){
        this.desiredTemperature = desiredTemperature;
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

    public ActorRef getChildActor(){
        return this.actorRef;
    }

    public ActorSelection getControlPanelRef(){
        return this.controlPanelRef;
    }

    public ActorRef getParentActor(){
        return this.actorRef;
    }

    public int getDesiredTemperature(){
        return this.desiredTemperature;
    }
}
