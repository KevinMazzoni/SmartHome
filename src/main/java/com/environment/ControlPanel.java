package com.environment;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

// import com.environment.HVACActor;

public class ControlPanel {

	public static final int NORMAL_OP = 0;
	public static final int FAULT_OP = -1;

	public static final int FAULTS = 1;
	
	public static final int INFO_MSG = 0;
	public static final int SET_MSG = 1;
	public static final int FAULT = -1;

	public static void main(String[] args) {
		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef supervisor = sys.actorOf(ControlPanelActor.props(), "supervisor");

		// final ActorRef HVAC = sys.actorOf(HVACActor.props(), "HVAC");
		// final ActorRef TemperatureSensorActor = sys.actorOf(TemperatureSensorActor.props(), "HVAC");
		// HVAC ! Message("Hello from actor A");

		ActorRef counter, HVAC, temperatureSensor;
		Scanner scanner = new Scanner(System.in);
		int choice;

		System.out.println("\n");
		System.out.println("**********************************************************************");
		System.out.println("******************************SMART HOME******************************");
		System.out.println();
		System.out.println("Choose the no. of appliance you want to check or set, and click enter:");
		System.out.println("1 -> HVAC system");
		System.out.println("2 -> kitchen machine");
		System.out.println("3 -> infotainment");
		System.out.println();
		System.out.println("**********************************************************************");

		choice = scanner.nextInt();

		System.out.println("You chose: " + choice);

		scanner.close();
		
		try {
			
			// Asks the supervisor to create the child actor and returns a reference
			scala.concurrent.Future<Object> waitingForHVAC = ask(supervisor, Props.create(HVACActor.class), 5000);
			scala.concurrent.Future<Object> waitingForTemperatureSensor = ask(supervisor, Props.create(TemperatureSensorActor.class), 5000);
			HVAC = (ActorRef) waitingForHVAC.result(timeout, null);
			temperatureSensor = (ActorRef) waitingForTemperatureSensor.result(timeout, null);

			HVAC.tell(new TemperatureMessage(INFO_MSG), temperatureSensor);

			// for (int i = 0; i < FAULTS; i++)
			// 	counter.tell(new DataMessage(FAULT_OP), ActorRef.noSender());

			HVAC.tell(new TemperatureMessage(INFO_MSG), ActorRef.noSender());

			sys.terminate();

		} catch (TimeoutException | InterruptedException e1) {
		
			e1.printStackTrace();
		}

	}

}
