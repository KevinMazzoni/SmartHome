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
        Config config = ConfigFactory.load(/*"client.conf"*/);
        ActorSystem system = ActorSystem.create("ClientSystem", config);

        System.out.println("PORTAAAAAAA: " + System.getenv("PORT"));

        // Crea un riferimento all'attore del server
        // ActorRef serverActor = system.actorSelection("akka://Server@192.168.132.1:25520/user/ServerActor").anchor();
        ActorRef serverActor = system.actorSelection("akka://Server@127.0.0.1:2551/user/ServerActor").anchor();

        // Crea e avvia l'attore del client, passando il riferimento all'attore del server
        ActorRef clientActor = system.actorOf(Props.create(ClientActor.class, serverActor), "clientActor");

        // Invia un messaggio al server
        clientActor.tell("inviaMessaggio", clientActor);

        //Provo ad inviare un messaggio al server in un altro modo
        serverActor.tell("Provamessaggio" , clientActor);

    }
}
