package com.distributedclient;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;

public class ClientActor extends AbstractActor {

	public ClientActor() {
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(SimpleMessage.class, this::onSimpleMessage)
							.match(OtherMessage.class, this::onOtherMessage).build();
	}

	void onSimpleMessage(SimpleMessage msg) {
		System.out.println("Ho ricevuto un SimpleMessage");
		System.out.println("L'indirizzo del server è: " + msg.getServerAddr());
		// ActorSelection server = getContext().actorSelection(msg.getServerAddr());
		// server.tell(new SimpleMessage(msg.getServerAddr()), ActorRef.noSender());
	}
	
	void onOtherMessage(OtherMessage msg) {
		System.out.println("Received other type of message");
	}

	static Props props() {
		return Props.create(ClientActor.class);
	}

}
