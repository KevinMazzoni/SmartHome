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

    private static boolean isClientRunning = false;

    public static void main(String[] args) {

        // Carica la configurazione da application.conf
        Config config = ConfigFactory.load();

        // Crea un sistema degli attori
        final ActorSystem system = ActorSystem.create("ServerSystem", config);
        ActorRef temperatureSensor;
        ActorRef server;

        //Timeout attesa temperatureSensor
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        // Crea l'attore del server
        final ActorRef controlPanelActor = system.actorOf(ControlPanelActor.props(), "ControlPanelActor");
        
        //Creo TemperatureSensorActor e ServerActor nel contesto di ControlPanel, così facendo ControlPanel supervisiona sia il TemperatureSensorActor che il ServerActor
        try{
            // scala.concurrent.Future<Object> waitingForTemperatureSensorActor = ask(controlPanelActor, Props.create(TemperatureSensorActor.class), 5000);
            // temperatureSensor = (ActorRef) waitingForTemperatureSensorActor.result(timeout, null);

            scala.concurrent.Future<Object> waitingForServerActor = ask(controlPanelActor, Props.create(ServerActor.class), 5000);
            server = (ActorRef) waitingForServerActor.result(timeout, null);

            server.tell("MESSAGGIO DAL CONTROL PANEL", controlPanelActor);

            // Attendi che il client venga avviato (puoi sostituire questo con la tua logica)
            // while (!isClientRunning) {
            //     try {
            //         Thread.sleep(1000); // Attendi 1 secondo
            //     } catch (InterruptedException e) {
            //         Thread.currentThread().interrupt();
            //     }
            // }

            System.out.println("CLIENT RUNNING!!!!!!");
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
