package com.simpleenvironment.Kitchen;

import java.time.Duration;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.TemperatureMessage;
import com.simpleenvironment.Messages.Type;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

public class KitchenSupervisorActor extends AbstractActor {

	private ActorRef kitchenTemperatureSensorActor;

     // #strategy
    private static SupervisorStrategy strategy =
        new OneForOneStrategy(
            1, // Max no of retries
            Duration.ofMinutes(1), // Within what time period
            DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart())
                .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
      return strategy;
    }

	public KitchenSupervisorActor(ActorRef kitchenTemperatureSensorActor) {
		this.kitchenTemperatureSensorActor = kitchenTemperatureSensorActor;
		System.out.println("kitchenTemperatureSensorActor è proprio: " + this.kitchenTemperatureSensorActor);
	}

	@Override
	public Receive createReceive() {
		// Creates the child actor within the supervisor actor context
		return receiveBuilder()
		          	.match(
		            	Props.class,
		              	props -> {
		                	getSender().tell(getContext().actorOf(props), getSelf());
		             	})
					.match(TemperatureMessage.class, message -> {
                    	System.out.println("KitchenSupervisorActor ha ricevuto il TemperatureMessage: " + message.getTemperature());
                	})
					.match(SimpleMessage.class, this::onSimpleMessage)
				  	.match(String.class, message -> {
                    	System.out.println("KitchenSupervisorActor ha ricevuto il messaggio: " + message);
                	})
		          	.build();
	}

	void onSimpleMessage(SimpleMessage msg) throws Exception {
		System.out.println("KitchenSupervisorActor ha ricevuto il SimpleMessage: " + msg.getMessage() + " di tipo: " + msg.getType());
		// System.out.println("INIZIO DELLE STAMPE PER FIGLIO");
		// System.out.println(getContext().actorSelection("KitchenTemperatureSensorActor"));
		// ActorSelection kitchenTemperatureSensorActor = getContext().actorSelection("KitchenTemperatureSensorActor");
		// kitchenTemperatureSensorActor.tell(new SimpleMessage("Questo è il messagio che inoltro dal KitchenSupervisorActor al KitchenTemperatureSensorActor", Type.INFO), ActorRef.noSender());
		// System.out.println(getContext().child("KitcheTemperatureSensorActor"));
		// System.out.println(getContext().findChild("KitcheTemperatureSensorActor"));
		// System.out.println(getContext().findChild("KitcheTemperatureSensorActor"));
		// System.out.println("FINITE LE STAMPE PER FIGLIO");

		switch(msg.getType()){
			case INFO_CHILD:
				System.out.println("INFO_CHILD message");
				this.kitchenTemperatureSensorActor = msg.getChildRef();
				// msg.getChildRef().tell(new SimpleMessage("Questo è il messagio che inoltro dal KitchenSupervisorActor al KitchenTemperatureSensorActor", Type.INFO), ActorRef.noSender());
				break;
			case INFO_TEMPERATURE:
				this.kitchenTemperatureSensorActor.tell(new SimpleMessage("Prova tell", Type.INFO_TEMPERATURE), getSelf());
				break;
			default:
				break;
		}

	}

	static Props props() {
		return Props.create(KitchenSupervisorActor.class);
	}
}
