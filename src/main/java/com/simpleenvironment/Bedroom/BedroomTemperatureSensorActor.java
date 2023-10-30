
package com.simpleenvironment.Bedroom;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import com.simpleenvironment.Messages.TemperatureMessage;
import com.simpleenvironment.Messages.Appliance;
import com.simpleenvironment.Messages.Room;
import com.simpleenvironment.Messages.SimpleMessage;

public class BedroomTemperatureSensorActor extends AbstractActor {

    private int temperature;
    private int energyConsumption;
    private ActorRef bedroomSupervisorActor;

    private static final boolean FIRST = true;
    private static final boolean NOT_FIRST = false;

    public BedroomTemperatureSensorActor(String string) {
        temperature = (int) (Math.round(Math.random() * 100) % 40) + 1;
        energyConsumption = (int) (Math.round(Math.random() * 100) % 10) + 1;
    }

    @Override
	public Receive createReceive() {
		return receiveBuilder()
            .match(SimpleMessage.class, this::onSimpleMessage)
            .match(TemperatureMessage.class, this::onTemperatureMessage)
            .match(String.class, message -> {
                        // System.out.println("BedroomTemperatureSensorActor ha ricevuto il messaggio: " + message);
            })
            .build();
	}

    void onTemperatureMessage(TemperatureMessage msg) throws Exception {
        System.out.println("Sono il BedroomTemperatureSensorActor e ho ricevuto un TemperatureMessage con desired temperature: " + msg.getTemperature());
        if(this.temperature >= msg.getTemperature())
            while(this.temperature > msg.getTemperature()){
                this.temperature --;
                this.bedroomSupervisorActor.tell(new TemperatureMessage(this.temperature, this.energyConsumption, Room.BEDROOM, Appliance.TEMPERATURE_SENSOR, NOT_FIRST), self());
                Thread.sleep(1000);
            }
        else
            while(this.temperature < msg.getTemperature()){
                this.temperature ++;
                this.bedroomSupervisorActor.tell(new TemperatureMessage(this.temperature, this.energyConsumption, Room.BEDROOM, Appliance.TEMPERATURE_SENSOR, NOT_FIRST), self());
                Thread.sleep(1000);
            }
    }

    void onSimpleMessage(SimpleMessage msg) throws Exception {
        switch(msg.getType()){
            case INFO:
                System.out.println("Sono il BedroomTemperatureSensorActor e ho ricevuto un SimpleMessage di tipo INFO: " + msg.getMessage());
                break;
            case INFO_PARENT:
                this.bedroomSupervisorActor = msg.getParentActor();
                break;
            case INFO_TEMPERATURE:
                // System.out.println("Il sender di questo info_temperature è: " + sender() + " la temperatura che sto mandando è: " + this.temperature);
                this.bedroomSupervisorActor.tell(new TemperatureMessage(this.temperature, this.energyConsumption, Room.BEDROOM, Appliance.TEMPERATURE_SENSOR, FIRST), self());
                break;
            case ERROR:
                System.err.println("\u001B[31mBedroomTemperatureSensorActor in errore!\u001B[0m");
                throw new Exception("FAULT in BedroomTemperatureSensorActor");
            default:
                break;
        }
    }

    public static Props props() {
        return Props.create(BedroomTemperatureSensorActor.class);
    }
}
