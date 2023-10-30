
package com.simpleenvironment.Bedroom;

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

public class BedroomHVACActor extends AbstractActor {

    private int energyConsumption;
    private ActorRef bedroomSupervisorActor;
    private ActorRef bedroomTemperatureSensorActor;

    private static final boolean FIRST = true;
    private static final boolean NOT_FIRST = false;

    public BedroomHVACActor(String string) {
        energyConsumption = (int) (Math.round(Math.random() * 100) % 11) + 1;
    }

    @Override
	public Receive createReceive() {
		return receiveBuilder()
            .match(SimpleMessage.class, this::onSimpleMessage)
            .match(TemperatureMessage.class, message -> {
                        System.out.println("BedroomHVACActor ha ricevuto il TemperatureMessage: " + message);
            })
            .match(String.class, message -> {
                        // System.out.println("BedroomHVACActor ha ricevuto il messaggio: " + message);
            })
            .build();
	}

    void onSimpleMessage(SimpleMessage msg) throws Exception {
        switch(msg.getType()){
            case INFO_PARENT:
                System.out.println("Sono il HVACActor, ho ricevuto un INFO_PARENT con parent: " + msg.getParentActor());
                this.bedroomSupervisorActor = msg.getParentActor();
                break;
            case INFO_SIBLING:
                System.out.println("Sono il HVACActor, ho ricevuto un INFO_SIBLING con sibling: " + msg.getSiblingActor());
                this.bedroomTemperatureSensorActor = msg.getSiblingActor();
                break;
            case DESIRED_TEMPERATURE:
                System.out.println("Sono il BedroomHVACActor e ho ricevuto un DESIRED_TEMPERATURE con temperatura: " + msg.getDesiredTemperature());
                this.bedroomSupervisorActor.tell(new TemperatureMessage(msg.getDesiredTemperature(), this.energyConsumption, Room.BEDROOM, Appliance.HVAC, FIRST), self());
                break;
            case STOP_HVAC:
                System.out.println("Sono il BedroomHVACActor e ho ricevuto un STOP_HVAC con temperatura: " + msg.getDesiredTemperature());
                this.bedroomSupervisorActor.tell(new TemperatureMessage(msg.getDesiredTemperature(), 0, Room.BEDROOM, Appliance.HVAC, FIRST), self());
                break;
            case STOP_SENDING:
                break;
            default:
                break;
        }
        // System.out.println("Sono il BedroomHVACActor! Ho ricevuto il SimpleMessage: " + msg.getMessage() + " di tipo: " + msg.getType());
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
        return Props.create(BedroomHVACActor.class);
    }
}
