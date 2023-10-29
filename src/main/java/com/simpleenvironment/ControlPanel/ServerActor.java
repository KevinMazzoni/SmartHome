package com.simpleenvironment.ControlPanel;

import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.TemperatureMessage;
import com.simpleenvironment.Messages.Type;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ServerActor extends AbstractActor {

    private int kitchenCurrentTemperature;
	private int bedroomCurrentTemperature;
    private int desiredTemperature;

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
        if(msg.getType().equals(Type.DESIRED_TEMPERATURE))
            this.desiredTemperature = msg.getDesiredTemperature();
    }

    void onTemperatureMessage(TemperatureMessage msg) throws Exception {
        // System.out.println("ServerActor ha ricevuto il TemperatureMessage: " + msg.getTemperature());

        // System.out.println("ControlPanelActor ha ricevuto il TemperatureMessage: " + msg.getTemperature() + " da " + getSender());
		switch(msg.getRoom()){
			case KITCHEN:
				if(msg.isFirstMeasure() || msg.getTemperature() != this.kitchenCurrentTemperature){
                    if(msg.isFirstMeasure()) 
                        System.out.println("Temperatura cucina: "/*\u001B[33m"*/ + msg.getTemperature() + "° C\u001B[0m\tConsumo elettrico cucina: " + msg.getEnergyConsumption() + " W");  
                    else if(msg.getTemperature() > this.kitchenCurrentTemperature && msg.getTemperature() != this.desiredTemperature)
                        System.out.println("Temperatura cucina: \u001B[31m" + msg.getTemperature() + "° C\u001B[0m\tConsumo elettrico cucina: " + msg.getEnergyConsumption() + " W"); 
                    else if(msg.getTemperature() < this.kitchenCurrentTemperature && msg.getTemperature() != this.desiredTemperature)
                        System.out.println("Temperatura cucina: \u001B[34m" + msg.getTemperature() + "° C\u001B[0m\tConsumo elettrico cucina: " + msg.getEnergyConsumption() + " W");
                    else //Qui ho raggiunto la temperatura desiderata; Manda magari messaggio per segnalare fine
                        System.out.println("Temperatura cucina: \u001B[32m" + msg.getTemperature() + "° C\u001B[0m\tConsumo elettrico cucina: " + msg.getEnergyConsumption() + " W");
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
