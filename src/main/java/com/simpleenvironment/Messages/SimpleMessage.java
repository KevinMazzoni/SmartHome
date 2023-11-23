package com.simpleenvironment.Messages;

import java.io.Serializable;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

public class SimpleMessage implements Serializable {
    
    private String message;
    private Type type;
    private int desiredTemperature;
    private boolean choice;

    private Room room;

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

    public SimpleMessage(int desiredTemperature, Type type, Room room){
        this.desiredTemperature = desiredTemperature;
        this.type = type;
        this.room = room;
    }

    public SimpleMessage(int choice, Type type){
        this.desiredTemperature = choice;
        this.type = type;
    }

    public SimpleMessage(boolean choice, Type type, Room room){
        this.choice = choice;
        this.type = type;
        this.room = room;
    }

    public SimpleMessage(boolean resettingChoice, int desiredTemperature, Type type, Room room){
        this.choice = resettingChoice;
        this.desiredTemperature = desiredTemperature;
        this.type = type;
        this.room = room;
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

    public Room getRoom(){
        return this.room;
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

    public int getEnergyConsumption(){
        return this.desiredTemperature;
    }

    public int getEnvironmentChoice(){
        return this.desiredTemperature;
    }

    public boolean getHVACChoice(){
        return this.choice;
    }

    public boolean getResettingChoice(){
        return this.choice;
    }
}
