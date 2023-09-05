package com.environment;

import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class HVACActor extends AbstractActor {

	private double temperature;

	public HVACActor() {
		this.temperature = 0;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(TemperatureMessage.class, this::onTemperatureMessage).build();
	}

	void onTemperatureMessage(TemperatureMessage msg) throws Exception {
		
		switch(msg.getType()){
			case ControlPanel.SET_MSG:
				System.out.println("I'm setting the temperature to " + msg.getTemperature() + " degrees.");
				this.temperature = msg.getTemperature();
				break;
			case ControlPanel.INFO_MSG:
				System.out.println("I received an INFO_MSG, type: " + msg.getType() + " temperature: " + msg.getTemperature());
				getSender().tell(new TemperatureMessage(0, 1), getSelf());
				break;
			case ControlPanel.INFO_CONSUMPTION:
				System.out.println("I received an INFO_CONSUMPTION message, type: " + msg.getType() + " temperature: " + msg.getTemperature());
				System.out.println("Instantaneous energy consumption: " + Math.random()*1000 + " kW");
				getSender().tell(new TemperatureMessage(0, 1), getSelf());
				break;
			case ControlPanel.FAULT:
				System.out.println("Received a fault.");
				break;
			default:
				System.out.println("Message received is not valid");
				break;
			
		}
	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) {
		System.out.print("Preparing to restart...");		
	}
	
	@Override
	public void postRestart(Throwable reason) {
		System.out.println("...now restarted!");	
	}
	
	static Props props() {
		return Props.create(HVACActor.class);
	}

}
