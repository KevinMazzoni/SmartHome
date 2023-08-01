package com.environment;

import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class TemperatureSensorActor extends AbstractActor {

	public static final int INFO_MSG = 0;
	public static final int SET_MSG = 1;
	public static final int FAULT = -1;

	private double temperature;

	public TemperatureSensorActor() {
		this.temperature = 0;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(TemperatureMessage.class, this::onTemperatureMessage).build();
	}

	void onTemperatureMessage(TemperatureMessage msg) throws Exception {
		if (msg.getType() == CounterSupervisor.INFO_MSG){
			System.out.println("I'm sending back the temperature");
			this.temperature = msg.getTemperature();
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
		return Props.create(CounterActor.class);
	}

}
