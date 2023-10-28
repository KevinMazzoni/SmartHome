
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

public class KitchenTemperatureSensorActor extends AbstractActor {

    private int temperature;
    private int energyConsumption;
    private ActorRef kitchenSupervisorActor;
    private boolean continueToSend;

    private int counter;

    private static final boolean FIRST = true;
    private static final boolean NOT_FIRST = false;

    public KitchenTemperatureSensorActor(String string) {
        // System.out.println("Questa è la stringa che mi è arrivata in fase di costruzione: " + string);
        temperature = (int) (Math.round(Math.random() * 100) % 40);
        energyConsumption = (int) (Math.round(Math.random() * 100) % 11);
        this.continueToSend = true;
        this.counter = 0;
    }

    @Override
	public Receive createReceive() {
		return receiveBuilder()
            .match(SimpleMessage.class, this::onSimpleMessage)
            .match(TemperatureMessage.class, this::onTemperatureMessage)
            .match(String.class, message -> {
                        // System.out.println("KitchenTemperatureSensorActor ha ricevuto il messaggio: " + message);
            })
            .build();
	}

    void onTemperatureMessage(TemperatureMessage msg) throws Exception {
        System.out.println("Sono il KitchenTemperatureSensorActor e ho ricevuto un TemperatureMessage con desired temperature: " + msg.getTemperature());
        if(this.temperature >= msg.getTemperature())
            while(this.temperature > msg.getTemperature()){
                this.temperature --;
                this.kitchenSupervisorActor.tell(new TemperatureMessage(this.temperature, this.energyConsumption, Room.KITCHEN, Appliance.TEMPERATURE_SENSOR, FIRST), self());
                Thread.sleep(1000);
            }
        else
            while(this.temperature < msg.getTemperature()){
                this.temperature ++;
                this.kitchenSupervisorActor.tell(new TemperatureMessage(this.temperature, this.energyConsumption, Room.KITCHEN, Appliance.TEMPERATURE_SENSOR, FIRST), self());
                Thread.sleep(1000);
            }
    }

    void onSimpleMessage(SimpleMessage msg) throws Exception {
        switch(msg.getType()){
            case INFO:
                System.out.println("Sono il KitchenTemperatureSensorActor e ho ricevuto un SimpleMessage di tipo INFO: " + msg.getMessage());
                break;
            case INFO_PARENT:
                this.kitchenSupervisorActor = msg.getParentActor();
                break;
            case INFO_TEMPERATURE:
                // System.out.println("Il sender di questo info_temperature è: " + sender() + " la temperatura che sto mandando è: " + this.temperature);
                this.kitchenSupervisorActor.tell(new TemperatureMessage(this.temperature, this.energyConsumption, Room.KITCHEN, Appliance.TEMPERATURE_SENSOR, FIRST), self());
                break;
            case STOP_SENDING:
                this.continueToSend = false;
                break;
            default:
                break;
        }
        // System.out.println("Sono il KitchenTemperatureSensorActor! Ho ricevuto il SimpleMessage: " + msg.getMessage() + " di tipo: " + msg.getType());
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
