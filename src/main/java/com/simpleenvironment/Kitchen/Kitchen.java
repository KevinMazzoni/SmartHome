
package com.simpleenvironment.Kitchen;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import static akka.pattern.Patterns.ask;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.Type;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Kitchen {

    public static void main(String[] args) {

        Config config = ConfigFactory.load();
        
        //Creazione del sistema di attori
        ActorSystem system = ActorSystem.create("ServerSystem", config);

        //Creo il riferimento al temperatureSensor della cucina
        ActorRef kitchenTemperatureSensorActor;

        //Timeout attesa temperatureSensor
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        System.out.println("PORTA DELLA KITCHEN ROOM: " + System.getenv("PORT"));

        // Crea l'attore del server
        final ActorRef kitchenSupervisorActor = system.actorOf(KitchenSupervisorActor.props(), "KitchenSupervisorActor");

        try{
            //Creo KitchenTemperatureSensorActor nel contesto di KitchenSupervisorActor, così facendo KitchenSupervisorActor supervisiona il KitchenTemperatureSensorActor
            scala.concurrent.Future<Object> waitingForKitchenTemperatureSensorActor = ask(kitchenSupervisorActor, Props.create(KitchenTemperatureSensorActor.class), 5000);
            kitchenTemperatureSensorActor = (ActorRef) waitingForKitchenTemperatureSensorActor.result(timeout, null);

            kitchenTemperatureSensorActor.tell("MESSAGGIO DAL KITCHEN SUPERVISOR ACTOR", kitchenSupervisorActor);

        }
        catch(TimeoutException te){
            System.out.println("TimeoutException occurred!\n");
            te.printStackTrace();
        }
        catch(InterruptedException ie){
            System.out.println("InterruptedException occurred!\n");
            ie.printStackTrace();
        }

        



        // Crea un riferimento all'attore del controlPanel
        ActorSelection controlPanelActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/ControlPanelActor");

        controlPanelActor.tell(new SimpleMessage("Prova invio SimpleMessage da Kitchen a ControlPanelActor", Type.INFO), ActorRef.noSender());

        // Crea e avvia l'attore del client, passando il riferimento all'attore del server
        // system.actorOf(Kitchen.props(controlPanelActor), "TemperatureSensorActor2");

        // Valido
        system.scheduler().scheduleWithFixedDelay(
                Duration.Zero(), // Ritardo prima dell'esecuzione (0 indica che inizia immediatamente)
                Duration.create(1, TimeUnit.SECONDS), // Intervallo tra gli invii dei messaggi
                () -> controlPanelActor.tell("25.0 in stanza Kitchen", ActorRef.noSender()), // Azione da eseguire
                system.dispatcher()
        );

    }

    public static Props props(ActorSelection serverActor) {
        return Props.create(Kitchen.class, serverActor);
    }
}
