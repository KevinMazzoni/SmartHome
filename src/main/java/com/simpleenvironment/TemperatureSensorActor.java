//File ClientActor.java
package com.simpleenvironment;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.environment.HVACActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class TemperatureSensorActor extends AbstractActor {
    private final ActorSelection serverActor;

    public TemperatureSensorActor(ActorSelection serverActor) {
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
        ActorRef temperatureSensor;
        Config config = ConfigFactory.load();
        ActorSystem system = ActorSystem.create("ServerSystem", config);

        System.out.println("PORTA DEL TEMPERATURE SENSOR: " + System.getenv("PORT"));

        // Crea un riferimento all'attore del server
        ActorSelection serverActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/ControlPanelActor");

        // Crea e avvia l'attore del client, passando il riferimento all'attore del server
        system.actorOf(TemperatureSensorActor.props(serverActor), "temperatureSensorActor");

        // Invia un messaggio al server
        // serverActor.tell("Temperatura: 25°C", ActorRef.noSender());

        // Valido
        system.scheduler().scheduleWithFixedDelay(
                Duration.Zero(), // Ritardo prima dell'esecuzione (0 indica che inizia immediatamente)
                Duration.create(1, TimeUnit.SECONDS), // Intervallo tra gli invii dei messaggi
                () -> serverActor.tell("Temperatura: 25°C", ActorRef.noSender()), // Azione da eseguire
                system.dispatcher()
        );

        //Una prova
        // scala.concurrent.Future<Object> waitingForTemperatureSensor = ask(serverActor, Props.create(TemperatureSensorActor.class), 5000);
        // scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
		// temperatureSensor = (ActorRef) waitingForTemperatureSensor.result(timeout, null);

        // Prova a inviare un messaggio al server in un altro modo
        // try {
        //     Timeout timeout = Timeout.durationToTimeout(Duration.create(5, TimeUnit.SECONDS));
        //     Future<Object> future = Patterns.ask(serverActor, "Provamessaggio", timeout);
        //     String result = (String) Await.result(future, timeout.duration());
        //     System.out.println("Risposta dal server: " + result);
        // } catch (Exception e) {
        //     System.out.println("Errore durante l'attesa della risposta dal server: " + e.getMessage());
        // } finally {
        //     system.terminate();
        // }
        // system.terminate();
    }

    public static Props props(ActorSelection serverActor) {
        return Props.create(TemperatureSensorActor.class, serverActor);
    }
}
