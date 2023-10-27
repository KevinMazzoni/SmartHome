
package com.simpleenvironment.Kitchen;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;

import com.simpleenvironment.Messages.TemperatureMessage;
import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.Type;

import java.util.Optional;

public class KitchenTemperatureSensorActor extends AbstractActor {

    private int temperature;
    private ActorRef kitchenSupervisorActor;

    public KitchenTemperatureSensorActor(String string) {
        System.out.println("Questa è la stringa che mi è arrivata in fase di costruzione: " + string);
        temperature = (int) (Math.round(Math.random() * 100) % 40);
    }

    @Override
	public Receive createReceive() {
		return receiveBuilder()
            .match(SimpleMessage.class, this::onSimpleMessage)
            .match(TemperatureMessage.class, message -> {
                        System.out.println("KitchenTemperatureSensorActor ha ricevuto il TemperatureMessage: " + message);
            })
            .match(String.class, message -> {
                        System.out.println("KitchenTemperatureSensorActor ha ricevuto il messaggio: " + message);
            })
            .build();
	}

    void onSimpleMessage(SimpleMessage msg) throws Exception {
        switch(msg.getType()){
            case INFO_PARENT:
                this.kitchenSupervisorActor = msg.getParentActor();
                break;
            case INFO_TEMPERATURE:
                System.out.println("Il sender di questo info_temperature è: " + sender() + " la temperatura che sto mandando è: " + this.temperature);
                //Ripartire da qui, non riesco ad inviare un TemperatureMessage al KitchenSupervisorActor
                // this.kitchenSupervisorActor.tell(new TemperatureMessage(this.temperature), ActorRef.noSender());
                // this.kitchenSupervisorActor.tell(new SimpleMessage("Prova invio SimpleMessage a sto punto", Type.INFO), ActorRef.noSender());
                this.kitchenSupervisorActor.tell(new TemperatureMessage(this.temperature), ActorRef.noSender());

            

                break;
            default:
                break;
        }
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

    public static Props props() {
        return Props.create(KitchenTemperatureSensorActor.class);
    }
}
