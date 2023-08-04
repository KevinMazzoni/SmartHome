package com.environment;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeoutException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class ControlPanel {
	
	public static final int INFO_MSG = 0;
	public static final int SET_MSG = 1;
	public static final int FAULT = -1;

	public static final int FAULTS = 3;


	public static void main(String[] args) {
		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef supervisor = sys.actorOf(ControlPanelActor.props(), "supervisor");

		ActorRef hvac;
		try {
			
			// Asks the supervisor to create the child actor and returns a reference
			scala.concurrent.Future<Object> waitingForHvac = ask(supervisor, Props.create(HVACActor.class), 5000);
			hvac = (ActorRef) waitingForHvac.result(timeout, null);

			hvac.tell(new TemperatureMessage(INFO_MSG, 30), ActorRef.noSender());

			for (int i = 0; i < FAULTS; i++)
				hvac.tell(new TemperatureMessage(FAULT), ActorRef.noSender());

			hvac.tell(new TemperatureMessage(INFO_MSG, 20), ActorRef.noSender());

			sys.terminate();

		} catch (TimeoutException | InterruptedException e1) {
		
			e1.printStackTrace();
		}

	}

}
