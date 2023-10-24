package com.simpleenvironment.Kitchen;

import java.time.Duration;

import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.TemperatureMessage;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

public class KitchenSupervisorActor extends AbstractActor {

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

	public KitchenSupervisorActor() {
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
					.match(SimpleMessage.class, message -> {
                    	System.out.println("KitchenSupervisorActor ha ricevuto il SimpleMessage: " + message.getMessage() + " di tipo: " + message.getType());
                	})
				  	.match(String.class, message -> {
                    	System.out.println("KitchenSupervisorActor ha ricevuto il messaggio: " + message);
                	})
		          	.build();
	}

	static Props props() {
		return Props.create(KitchenSupervisorActor.class);
	}
}
