package com.simpleenvironment.Messages;

import java.io.Serializable;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

public class SimpleMessage implements Serializable {
    
    private String message;
    private Type type;
    private int desiredTemperature;

    private ActorRef actorRef;
    private Appliance appliance;

    private ActorSelection controlPanelRef;

    private ActorSystem actorSystem;

    public SimpleMessage(String message, Type type){
        this.message = message;
        this.type = type;
    }

    public SimpleMessage(ActorRef actorRef, Type type, Appliance appliance){
        this.actorRef = actorRef;
        this.type = type;
        this.appliance = appliance;
    }

    public SimpleMessage(ActorSelection controlPanelRef, Type type){
        this.controlPanelRef = controlPanelRef;
        this.type = type;
    }

    public SimpleMessage(ActorSystem actorSystem, Type type){
        this.actorSystem = actorSystem;
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

    public Appliance getAppliance(){
        return this.appliance;
    }

    public ActorRef getChildActor(){
        return this.actorRef;
    }

    public ActorRef getParentActor(){
        return this.actorRef;
    }

    public ActorRef getSiblingActor(){
        return this.actorRef;
    }

    public ActorSelection getControlPanelRef(){
        return this.controlPanelRef;
    }

    public ActorSystem getActorSystem(){
        return this.actorSystem;
    }

    public int getDesiredTemperature(){
        return this.desiredTemperature;
    }
}
