package com.simpleenvironment.ControlPanel;

import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.TemperatureMessage;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ServerActor extends AbstractActor {

    private int kitchenCurrentTemperature;
	private int bedroomCurrentTemperature;

    public ServerActor(){
        this.kitchenCurrentTemperature = -1;
		this.bedroomCurrentTemperature = -1;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    System.out.println("Server ha ricevuto il messaggio: " + message);
                })
                .match(SimpleMessage.class, this::onSimpleMessage)
                .match(TemperatureMessage.class, this::onTemperatureMessage)
                .matchAny(o -> {
                    // Ignora tutti gli altri tipi di messaggi
                    System.out.println("Server ha ricevuto un messaggio di tipo sconosciuto: " + o);
                })
                // .matchEquals("ClientStarted", message -> {
                //     isClientRunning = true;
                //     System.out.println("Il client è stato avviato.");
                // })
                .build();
    }

    void onSimpleMessage(SimpleMessage msg) throws Exception {
        System.out.println("ServerActor ha ricevuto il SimpleMessage: " + msg.getMessage());
    }

    void onTemperatureMessage(TemperatureMessage msg) throws Exception {
        // System.out.println("ServerActor ha ricevuto il TemperatureMessage: " + msg.getTemperature());

        // System.out.println("ControlPanelActor ha ricevuto il TemperatureMessage: " + msg.getTemperature() + " da " + getSender());
		switch(msg.getRoom()){
			case KITCHEN:
				if(msg.isFirstMeasure() || msg.getTemperature() != this.kitchenCurrentTemperature){
					System.out.println("Consumo elettrico cucina: " + msg.getEnergyConsumption() + " W");
                    System.out.println("Temperatura cucina: " + msg.getTemperature() + "° C");
                }
				this.kitchenCurrentTemperature = msg.getTemperature();
				break;
			case BEDROOM:
				
				break;
			default:
				break;
		}
    }
    
    static Props props() {
        return Props.create(ServerActor.class);
    }
}
