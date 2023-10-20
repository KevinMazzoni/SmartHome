package com.simpleenvironment;

import static akka.pattern.Patterns.ask;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.TimeoutException;

public class ControlPanel {

    public static void main(String[] args) {

        // Carica la configurazione da application.conf
        Config config = ConfigFactory.load();

        // Crea un sistema degli attori
        final ActorSystem system = ActorSystem.create("ServerSystem", config);
        
        ActorRef server;

        //Timeout attesa temperatureSensor
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        // Crea l'attore del server
        final ActorRef controlPanelActor = system.actorOf(ControlPanelActor.props(), "ControlPanelActor");
        
        try{

            //Creo ServerActor nel contesto di ControlPanel, così facendo ControlPanel supervisiona il ServerActor
            scala.concurrent.Future<Object> waitingForServerActor = ask(controlPanelActor, Props.create(ServerActor.class), 5000);
            server = (ActorRef) waitingForServerActor.result(timeout, null);

            server.tell("MESSAGGIO DAL CONTROL PANEL", controlPanelActor);

        }
        catch(TimeoutException te){
            System.out.println("TimeoutException occurred!\n");
            te.printStackTrace();
        }
        catch(InterruptedException ie){
            System.out.println("InterruptedException occurred!\n");
            ie.printStackTrace();
        }

        System.out.println("Il ServerActor ha terminato di eseguire il main");
    }

    static Props props() {
        return Props.create(ServerActor.class);
    }
}
