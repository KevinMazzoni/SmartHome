package com.distributed;

import java.time.Duration;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.FI.UnitApply;

public class ServerActor extends AbstractActor {

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

	public ServerActor() {
	}

	@Override
	public Receive createReceive() {
		// Creates the child actor within the supervisor actor context
		return receiveBuilder()
		          .match(Props.class, props -> { getSender().tell(getContext().actorOf(props), getSelf());})
		          .match(SimpleMessage.class, this::onSimpleMessage)
				  .build();
	}

	void onSimpleMessage(SimpleMessage msg) {
		System.out.println("Ho ricevuto un SimpleMessage, indirizzo: " + msg.getServerAddr());
	}

	static Props props() {
		return Props.create(ServerActor.class);
	}

}
