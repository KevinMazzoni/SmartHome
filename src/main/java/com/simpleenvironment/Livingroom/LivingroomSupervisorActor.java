package com.simpleenvironment.Livingroom;

import java.time.Duration;

import com.simpleenvironment.Messages.Appliance;
import com.simpleenvironment.Messages.SimpleMessage;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

public class LivingroomSupervisorActor extends AbstractActor {

	private ActorRef tvActor;

	private ActorSelection controlPanelActor;

	private boolean tvOn;

	private static final boolean ON = true;
	private static final boolean OFF = false;

	private int tvConsumption;

     // #strategy
    private static SupervisorStrategy strategy =
        new OneForOneStrategy(
            1, // Max no of retries
            Duration.ofMinutes(1), // Within what time period
            DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
                .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
      return strategy;
    }

	public LivingroomSupervisorActor() {
		this.tvOn = OFF;
		this.tvConsumption = 0;
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
					// .match(TemperatureMessage.class, this::onTemperatureMessage)
					.match(SimpleMessage.class, this::onSimpleMessage)
				  	.match(String.class, message -> {
                    	System.out.println("LivingroomSupervisorActor ha ricevuto il messaggio: " + message);
                	})
		          	.build();
	}

	void onSimpleMessage(SimpleMessage msg) throws Exception {

		switch(msg.getType()){
			case INFO:
				System.out.println("Ho ricevuto un INFO message");
				break;
			case INFO_CHILD:
				if(msg.getAppliance().equals(Appliance.TV)){
					this.tvActor = msg.getChildActor();
					System.out.println("Sono il LivingroomSupervisorActor, setto il child tvActor a: " + this.tvActor);
				}
				break;
			case INFO_CONTROLPANEL:
				System.out.println("Sono il LivingroomSupervisorActor, ho ricevuto un INFO_CONTROLPANEL; getcontrolpanelref(): " + msg.getControlPanelRef());
				this.controlPanelActor = msg.getControlPanelRef();
				break;
			default:
				break;
		}

	}

	static Props props() {
		return Props.create(LivingroomSupervisorActor.class);
	}
}
