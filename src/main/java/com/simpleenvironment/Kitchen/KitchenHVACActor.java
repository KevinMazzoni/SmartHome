
package com.simpleenvironment.Kitchen;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;

import com.simpleenvironment.Messages.TemperatureMessage;
import com.simpleenvironment.Messages.Appliance;
import com.simpleenvironment.Messages.Room;
import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.Type;

import java.util.Optional;

public class KitchenHVACActor extends AbstractActor {

    private int energyConsumption;
    private ActorRef kitchenSupervisorActor;
    private ActorRef kitchenTemperatureSensorActor;

    private static final boolean FIRST = true;
    private static final boolean NOT_FIRST = false;

    public KitchenHVACActor(String string) {
        energyConsumption = (int) (Math.round(Math.random() * 100) % 11);
    }

    @Override
	public Receive createReceive() {
		return receiveBuilder()
            .match(SimpleMessage.class, this::onSimpleMessage)
            .match(TemperatureMessage.class, message -> {
                        System.out.println("KitchenHVACActor ha ricevuto il TemperatureMessage: " + message);
            })
            .match(String.class, message -> {
                        // System.out.println("KitchenHVACActor ha ricevuto il messaggio: " + message);
            })
            .build();
	}

    void onSimpleMessage(SimpleMessage msg) throws Exception {
        switch(msg.getType()){
            case INFO_PARENT:
                System.out.println("Sono il HVACActor, ho ricevuto un INFO_PARENT con parent: " + msg.getParentActor());
                this.kitchenSupervisorActor = msg.getParentActor();
                break;
            case INFO_SIBLING:
                System.out.println("Sono il HVACActor, ho ricevuto un INFO_SIBLING con sibling: " + msg.getSiblingActor());
                this.kitchenTemperatureSensorActor = msg.getSiblingActor();
                break;
            case DESIRED_TEMPERATURE:
                System.out.println("Sono il KitchenHVACActor e ho ricevuto un DESIRED_TEMPERATURE con temperatura: " + msg.getDesiredTemperature());
                this.kitchenSupervisorActor.tell(new TemperatureMessage(msg.getDesiredTemperature(), this.energyConsumption, Room.KITCHEN, Appliance.HVAC, FIRST), self());
                break;
            case STOP_SENDING:
                break;
            default:
                break;
        }
        // System.out.println("Sono il KitchenHVACActor! Ho ricevuto il SimpleMessage: " + msg.getMessage() + " di tipo: " + msg.getType());
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
        return Props.create(KitchenHVACActor.class);
    }
}
