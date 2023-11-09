
package com.simpleenvironment.Livingroom;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import com.simpleenvironment.Messages.Room;
import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.Type;

public class TvActor extends AbstractActor {

    private int energyConsumption;
    private ActorRef livingroomSupervisorActor;

    private boolean tvOn = false;

    private static final boolean FIRST = true;
    private static final boolean NOT_FIRST = false;

    public TvActor(String string) {
        energyConsumption = (int) (Math.round(Math.random() * 100) % 10) + 1;
    }

    @Override
	public Receive createReceive() {
		return receiveBuilder()
            .match(SimpleMessage.class, this::onSimpleMessage)
            .build();
	}

    void onSimpleMessage(SimpleMessage msg) throws Exception {
        switch(msg.getType()){
            case INFO:
                System.out.println("Sono il TvActor e ho ricevuto un SimpleMessage di tipo INFO: " + msg.getMessage());
                break;
            case INFO_PARENT:
                this.livingroomSupervisorActor = msg.getParentActor();
                break;
            case INFO_CONSUMPTION:
                System.out.println("Sono il TvActor! Ho ricevuto il TV_CONSUMPTION: " + msg.getMessage());
                livingroomSupervisorActor.tell(new SimpleMessage(this.energyConsumption, Type.TV_CONSUMPTION, Room.LIVINGROOM), self());
                break;
            case TV_CONSUMPTION:
                System.out.println("Sono il TvActor! Ho ricevuto il TV_CONSUMPTION: " + msg.getMessage());
                livingroomSupervisorActor.tell(new SimpleMessage(this.energyConsumption, Type.TV_CONSUMPTION, Room.LIVINGROOM), self());
                break;
            case ERROR:
                System.err.println("\u001B[31mTvActor in errore!\u001B[0m");
                throw new Exception("FAULT in TvActor");
            default:
                break;
        }
    }

    public static Props props() {
        return Props.create(TvActor.class);
    }
}
