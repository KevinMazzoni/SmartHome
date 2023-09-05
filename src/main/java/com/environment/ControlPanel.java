package com.environment;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

// import com.environment.HVACActor;

public class ControlPanel {

	public static final int NORMAL_OP = 0;
	public static final int FAULT_OP = -1;

	public static final int FAULTS = 1;
	
	public static final int INFO_MSG = 0;
	public static final int SET_MSG = 1;
	public static final int FAULT = -1;
	public static final int INFO_CONSUMPTION = 2;

	public static void main(String[] args) {
		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef supervisor = sys.actorOf(ControlPanelActor.props(), "supervisor");

		// final ActorRef HVAC = sys.actorOf(HVACActor.props(), "HVAC");
		// final ActorRef TemperatureSensorActor = sys.actorOf(TemperatureSensorActor.props(), "HVAC");
		// HVAC ! Message("Hello from actor A");

		// ActorRef HVAC, temperatureSensor;

		int choice;
		boolean toContinue = true;
		Scanner scanner = new Scanner(System.in);

		while(toContinue){
			
			System.out.println("Entrato in while to continue");
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

			System.out.println("HAS NEXT INT: " + scanner.hasNextInt());
			choice = scanner.nextInt();

			System.out.println("You chose: " + choice);

			switch(choice){
				case 1:
					toContinue = HVACCase(supervisor, timeout);
					System.out.println("toContinue: " + toContinue);
					break;
				case 2:
					toContinue = dishwasherCase(supervisor, timeout);
					break;
				case 3:
					toContinue = infotainmentCase(supervisor, timeout);
					break;
				default:
					errorCase();
					break;
			}

			

		}
		scanner.close();
		sys.terminate();
	}

	private static void errorCase() {
	}

	private static boolean infotainmentCase(ActorRef supervisor, Duration timeout) {
		ActorRef infotainment;

		return true;
	}

	private static boolean dishwasherCase(ActorRef supervisor, Duration timeout) {
		ActorRef dishwasher;

		return true;
	}

	private static boolean HVACCase(ActorRef supervisor, Duration timeout) {
		ActorRef HVAC, temperatureSensor;
		int choice;
		boolean toContinue;

		Scanner scanner = new Scanner(System.in);

		try {
				
			// Asks the supervisor to create the child actor and returns a reference
			scala.concurrent.Future<Object> waitingForHVAC = ask(supervisor, Props.create(HVACActor.class), 5000);
			scala.concurrent.Future<Object> waitingForTemperatureSensor = ask(supervisor, Props.create(TemperatureSensorActor.class), 5000);
			HVAC = (ActorRef) waitingForHVAC.result(timeout, null);
			temperatureSensor = (ActorRef) waitingForTemperatureSensor.result(timeout, null);

			HVAC.tell(new TemperatureMessage(INFO_CONSUMPTION), ActorRef.noSender()); //Cercare di mettere qui IP ed una porta. 
																					//Far sì che, quando l'attore viene creato, notifica il supervisor dicendogli "guarda che esisto"
																					//L'IP e la porta a cui risponderà l'attore è quella del supervisor.
			temperatureSensor.tell(new TemperatureMessage(INFO_MSG), ActorRef.noSender());

			// for (int i = 0; i < FAULTS; i++)
			// 	counter.tell(new DataMessage(FAULT_OP), ActorRef.noSender());

			HVAC.tell(new TemperatureMessage(INFO_MSG), ActorRef.noSender());

			TimeUnit.SECONDS.sleep(1);

		} catch (TimeoutException | InterruptedException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("Do you want to proceed with another appliance? Insert 0 to terminate, 1 to continue");
		choice = scanner.nextInt();
		toContinue = (choice == 1) ? true : false;
		scanner.close();
			
		return toContinue;
	}

}
