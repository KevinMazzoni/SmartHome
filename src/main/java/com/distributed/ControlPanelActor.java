package com.distributed;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class ControlPanelActor extends AbstractActor {

	public ControlPanelActor() {
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(SimpleMessage.class, this::onMessage)
							.match(OtherMessage.class, this::onOtherMessage).build();
	}

	void onMessage(SimpleMessage msg) {
		System.out.println("Ho ricevuto un simple message; SONO DENTRO IL CONTROLPANELACTOR l'indirizzo del server è: " + msg.getServerAddr());
	}
	
	void onOtherMessage(OtherMessage msg) {
		System.out.println("Received other type of message");
	}

	static Props props() {
		return Props.create(ControlPanelActor.class);
	}

}
