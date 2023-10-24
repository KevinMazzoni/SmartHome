
package com.simpleenvironment.Kitchen;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;

import com.simpleenvironment.Messages.SimpleMessage;

import java.util.Optional;

public class KitchenTemperatureSensorActor extends AbstractActor {

    public KitchenTemperatureSensorActor() {
    }

    @Override
	public Receive createReceive() {
		return receiveBuilder()
            .match(SimpleMessage.class, this::onSimpleMessage)
            .match(String.class, message -> {
                        System.out.println("KitchenTemperatureSensorActor ha ricevuto il messaggio: " + message);
            })
            .build();
	}

    void onSimpleMessage(SimpleMessage msg) throws Exception {
        System.out.println("Sono il KitchenTemperatureSensorActor! Ho ricevuto il SimpleMessage: " + msg.getMessage() + " di tipo: " + msg.getType());
    }

    @Override
	public void preRestart(Throwable reason, Optional<Object> message) {
		System.out.print("Preparing to restart...");		
	}
	
	@Override
	public void postRestart(Throwable reason) {
		System.out.println("...now restarted!");	
	}

    public static Props props(ActorSelection serverActor) {
        return Props.create(KitchenTemperatureSensorActor.class, serverActor);
    }
}
