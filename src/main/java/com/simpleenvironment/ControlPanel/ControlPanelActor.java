package com.simpleenvironment.ControlPanel;

import java.time.Duration;

import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.TemperatureMessage;
import com.simpleenvironment.Messages.Type;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberExited;
import akka.cluster.ClusterEvent.MemberJoined;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.japi.pf.DeciderBuilder;

public class ControlPanelActor extends AbstractActor {

	private ActorRef serverActor;

	private int kitchenCurrentTemperature;
	private int bedroomCurrentTemperature;

	private boolean kitchenRunning = false;
	private boolean bedroomRunning = false;

	private Cluster cluster = Cluster.get(getContext().getSystem());

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

	@Override
    public void preStart() {
        // Sottoscrivi questo attore a ricevere notifiche sui cambiamenti dei membri del cluster
        // cluster.subscribe(getSelf(), MemberEvent.class);
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
    }

    @Override
    public void postStop() {
        // Annulla la registrazione quando l'attore viene fermato
        cluster.unsubscribe(getSelf());
    }

	public ControlPanelActor() {
		this.kitchenCurrentTemperature = -1;
		this.bedroomCurrentTemperature = -1;
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
					.match(TemperatureMessage.class, this::onTemperatureMessage)
					.match(SimpleMessage.class, this::onSimpleMessage)
				  	.match(String.class, message -> {
                    	System.out.println("ControlPanelActor ha ricevuto il messaggio: " + message);
                	})
					.match(CurrentClusterState.class, state -> {
                    	// Ricevi una notifica sullo stato attuale del cluster
						System.out.println("Current members: " + state.getMembers());
					})
					.match(MemberUp.class, memberUp -> {
						// Ricevi una notifica quando un membro si unisce al cluster
						// System.out.println("Member is Up: " + memberUp.member().address());
						
					})
					.match(MemberRemoved.class, memberRemoved -> {
						// Ricevi una notifica quando un membro viene rimosso dal cluster
						System.out.println("Member is Removed: " + memberRemoved.member().address());
					})
					.match(MemberEvent.class, memberEvent -> {
						// Altri tipi di eventi relativi ai membri del cluster possono essere gestiti qui
						if (memberEvent instanceof MemberExited) {
							// System.out.println("Member is Exited: " + ((MemberExited) memberEvent).member());
							switch(((MemberExited) memberEvent).member().address().toString()){
								case "akka://ServerSystem@127.0.0.1:2553":
									System.out.println("Kitchen exiting");
									this.kitchenRunning = false;
									this.serverActor.tell(new SimpleMessage("Kitchen exiting", Type.KITCHEN_OFF), self());
									break;
								case "akka://ServerSystem@127.0.0.1:2554":
									System.out.println("Bedroom exiting");
									this.bedroomRunning = false;
									this.serverActor.tell(new SimpleMessage("Bedroom exiting", Type.BEDROOM_OFF), self());
									break;
								default:
									System.out.println("NOT_RECOGNIZED up and running");
									break;
							}
						} else if (memberEvent instanceof MemberJoined) {
							// System.out.println("Member Joined: " + ((MemberJoined) memberEvent).member());
							switch(((MemberJoined) memberEvent).member().address().toString()){
								case "akka://ServerSystem@127.0.0.1:2553":
									System.out.println("Kitchen up and running");
									this.kitchenRunning = true;
									this.serverActor.tell(new SimpleMessage("Kitchen active and running", Type.KITCHEN_ON), self());
									break;
								case "akka://ServerSystem@127.0.0.1:2554":
									System.out.println("Bedroom up and running");
									this.bedroomRunning = true;
									this.serverActor.tell(new SimpleMessage("Bedroom active and running", Type.BEDROOM_ON), self());
									break;
								default:
									System.out.println("NOT_RECOGNIZED up and running");
									break;
							}
						} else if (memberEvent instanceof MemberEvent) {
							// Gestisci altri tipi di eventi se necessario
						}
					})
		          	.build();
	}

	void onTemperatureMessage(TemperatureMessage msg){
		this.serverActor.tell(msg, ActorRef.noSender());
	}

	void onSimpleMessage(SimpleMessage msg){

		switch(msg.getType()){
			case START:
				this.serverActor.tell(msg, self());
				break;
			case INFO_CHILD:
				this.serverActor = msg.getChildActor();
				System.out.println("INFO_CHILD ARRIVATO");
				// System.out.println("Sto settando il childActor a: " + msg.getChildActor());
				// this.serverActor.tell(new SimpleMessage("Prova di invio di un simplemessage da ControlPanelActor a ServerActor", Type.INFO), serverActor);
				break;
			case TV_CONSUMPTION:
				this.serverActor.tell(msg, self());
			default:
				break;
		}
	}

	static Props props() {
		return Props.create(ControlPanelActor.class);
	}
}
