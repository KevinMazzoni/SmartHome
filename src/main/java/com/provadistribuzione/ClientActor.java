//File ClientActor.java
package com.provadistribuzione;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class ClientActor extends AbstractActor {
    private final ActorSelection serverActor;

    public ClientActor(ActorSelection serverActor) {
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
        Config config = ConfigFactory.load();
        ActorSystem system = ActorSystem.create("ClientSystem", config);

        System.out.println("PORTAAAAAAA: " + System.getenv("PORT"));

        // Crea un riferimento all'attore del server
        ActorSelection serverActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/ServerActor");

        // Crea e avvia l'attore del client, passando il riferimento all'attore del server
        system.actorOf(ClientActor.props(serverActor), "clientActor");

        // Invia un messaggio al server
        serverActor.tell("inviaMessaggio", ActorRef.noSender());

        // Prova a inviare un messaggio al server in un altro modo
        try {
            Timeout timeout = Timeout.durationToTimeout(Duration.create(5, TimeUnit.SECONDS));
            Future<Object> future = Patterns.ask(serverActor, "Provamessaggio", timeout);
            String result = (String) Await.result(future, timeout.duration());
            System.out.println("Risposta dal server: " + result);
        } catch (Exception e) {
            System.out.println("Errore durante l'attesa della risposta dal server: " + e.getMessage());
        } finally {
            system.terminate();
        }
    }

    public static Props props(ActorSelection serverActor) {
        return Props.create(ClientActor.class, serverActor);
    }
}
