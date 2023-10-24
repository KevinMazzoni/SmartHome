
package com.simpleenvironment.Kitchen;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.simpleenvironment.Messages.SimpleMessage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class TemperatureSensorActor2 extends AbstractActor {
    private final ActorSelection serverActor;

    public TemperatureSensorActor2(ActorSelection serverActor) {
        this.serverActor = serverActor;
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
                    .match(SimpleMessage.class, message -> {
                    	System.out.println("TemperatureSensorActor2 ha ricevuto il SimpleMessage: " + message.getMessage());
                	})
				  	.match(String.class, message -> {
                    	System.out.println("TemperatureSensorActor2 ha ricevuto il messaggio: " + message);
                	})
		          	.build();
	}


    public static void main(String[] args) {

        Config config = ConfigFactory.load();
        
        //Creazione del sistema di attori
        ActorSystem system = ActorSystem.create("ServerSystem", config);

        System.out.println("PORTA DEL TEMPERATURE SENSOR: " + System.getenv("PORT"));

        // Crea un riferimento all'attore del server
        ActorSelection controlPanelActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/ControlPanelActor");

        // Crea e avvia l'attore del client, passando il riferimento all'attore del server
        system.actorOf(TemperatureSensorActor2.props(controlPanelActor), "TemperatureSensorActor2");

        // Valido
        system.scheduler().scheduleWithFixedDelay(
                Duration.Zero(), // Ritardo prima dell'esecuzione (0 indica che inizia immediatamente)
                Duration.create(1, TimeUnit.SECONDS), // Intervallo tra gli invii dei messaggi
                () -> controlPanelActor.tell("25.0 in stanza 2", ActorRef.noSender()), // Azione da eseguire
                system.dispatcher()
        );

    }

    public static Props props(ActorSelection serverActor) {
        return Props.create(TemperatureSensorActor2.class, serverActor);
    }
}
