package com.provadistribuzione;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ClientActor extends AbstractActor {
    private final ActorRef serverActor;

    public ClientActor(ActorRef serverActor) {
        this.serverActor = serverActor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("inviaMessaggio", message -> {
                    serverActor.tell("Ciao, Server!", getSelf());
                })
                .build();
    }

    public static void main(String[] args) {
        Config config = ConfigFactory.load("client.conf");
        ActorSystem system = ActorSystem.create("ClientSystem", config);

        // Crea un riferimento all'attore del server
        ActorRef serverActor = system.actorSelection("akka.tcp://ServerSystem@127.0.0.1:6123/user/serverActor").anchor();

        // Crea e avvia l'attore del client, passando il riferimento all'attore del server
        ActorRef clientActor = system.actorOf(Props.create(ClientActor.class, serverActor), "clientActor");

        // Invia un messaggio al server
        clientActor.tell("inviaMessaggio", clientActor);

    }
}
