package com.provadistribuzione;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class ServerActor extends AbstractActor {

    private static boolean isClientRunning = false;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    System.out.println("Server ha ricevuto il messaggio: " + message);
                })
                .matchEquals("ClientStarted", message -> {
                    isClientRunning = true;
                    System.out.println("Il client è stato avviato.");
                })
                .build();
    }

    public static void main(String[] args) {
        // Carica la configurazione da application.conf
        Config config = ConfigFactory.load();

        // Crea un sistema degli attori
        ActorSystem system = ActorSystem.create("ServerSystem", config);

        // Crea l'attore del server
        // ServerActor serverActor = new ServerActor();
        ActorRef serverActor = system.actorOf(Props.create(ServerActor.class), "ServerActor");

        // Attendi che il client venga avviato (puoi sostituire questo con la tua logica)
        while (!isClientRunning) {
            try {
                Thread.sleep(1000); // Attendi 1 secondo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Il ServerActor ha terminato di eseguire il main");
    }

    static Props props() {
        return Props.create(ServerActor.class);
    }
}
