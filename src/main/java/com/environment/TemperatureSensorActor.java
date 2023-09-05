package com.environment;

import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class TemperatureSensorActor extends AbstractActor {

	private double temperature;

	public TemperatureSensorActor() {
		
		// final ActorRef supervisor = sys.actorOf(CounterSupervisorActor.props(), "supervisor");
		
		// this.temperature = 0;

		// while(true){
		// 	try {
		// 		Thread.sleep(1000, 0);

		// 	} catch (Exception e) {
		// 		// TODO: handle exception
		// 	}
		// }
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(TemperatureMessage.class, this::onTemperatureMessage).build();
	}

	void onTemperatureMessage(TemperatureMessage msg) throws Exception {
		if (msg.getType() == ControlPanel.INFO_MSG){
			this.temperature = ((Math.random()*100)%50);
			System.out.println("I'm sending back the temperature");
			// this.temperature = msg.getTemperature();
			System.out.println("Telling the temperature from TemperatureSensorActor, temperature is: " + this.temperature);
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
		return Props.create(TemperatureSensorActor.class);
	}

}
