
package com.simpleenvironment.Kitchen;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import static akka.pattern.Patterns.ask;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.simpleenvironment.Messages.Appliance;
import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.Type;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.concurrent.TimeoutException;

public class Kitchen {

    public static void main(String[] args) {

        Config config = ConfigFactory.load();
        
        //Creazione del sistema di attori
        ActorSystem system = ActorSystem.create("ServerSystem", config);

        //Creo i riferimenti al temperatureSensor e al HVAC della cucina
        ActorRef kitchenTemperatureSensorActor = null;
        ActorRef kitchenHVACActor = null;

        //Timeout attesa temperatureSensor
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        // Crea l'attore del server
        final ActorRef kitchenSupervisorActor = system.actorOf(KitchenSupervisorActor.props(), "KitchenSupervisorActor");

        try{
            //Creo KitchenTemperatureSensorActor nel contesto di KitchenSupervisorActor, così facendo KitchenSupervisorActor supervisiona il KitchenTemperatureSensorActor 
            scala.concurrent.Future<Object> waitingForKitchenTemperatureSensorActor = ask(kitchenSupervisorActor, Props.create(KitchenTemperatureSensorActor.class, "KitchenTemperatureSensorActor"), 5000);
            kitchenTemperatureSensorActor = (ActorRef) waitingForKitchenTemperatureSensorActor.result(timeout, null);

            //Creo KitchenHVACActor nel contesto di KitchenSupervisorActor, così facendo KitchenSupervisorActor supervisiona il KitchenHVACActor 
            scala.concurrent.Future<Object> waitingForKitchenHVACActor = ask(kitchenSupervisorActor, Props.create(KitchenHVACActor.class, "KitchenHVACActor"), 5000);
            kitchenHVACActor = (ActorRef) waitingForKitchenHVACActor.result(timeout, null);

            //Invio al kitchenSupervisorActor i riferimenti ai suoi figli
            kitchenSupervisorActor.tell(new SimpleMessage(kitchenTemperatureSensorActor, Type.INFO_CHILD, Appliance.TEMPERATURE_SENSOR), ActorRef.noSender());
            kitchenSupervisorActor.tell(new SimpleMessage(kitchenHVACActor, Type.INFO_CHILD, Appliance.HVAC), ActorRef.noSender());

            //Invio ai figli il riferimento al padre kitchenSupervisorActor
            kitchenTemperatureSensorActor.tell(new SimpleMessage(kitchenSupervisorActor, Type.INFO_PARENT, Appliance.KITCHEN_SUPERVISOR), ActorRef.noSender());
            kitchenHVACActor.tell(new SimpleMessage(kitchenSupervisorActor, Type.INFO_PARENT, Appliance.KITCHEN_SUPERVISOR), ActorRef.noSender());

            //Invio al kitchenHVACActor il riferimento al kitchenTemperatureSensorActor
            kitchenHVACActor.tell(new SimpleMessage(kitchenTemperatureSensorActor, Type.INFO_SIBLING, Appliance.TEMPERATURE_SENSOR), ActorRef.noSender());

            // Crea un riferimento all'attore controlPanelActor
            ActorSelection controlPanelActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/ControlPanelActor");

            //Invio al kitchenSupervsorActor il riferimento al ControlPanelActor
            kitchenSupervisorActor.tell(new SimpleMessage(controlPanelActor, Type.INFO_CONTROLPANEL), ActorRef.noSender());

        }
        catch(TimeoutException te){
            System.out.println("TimeoutException occurred!\n");
            te.printStackTrace();
        }
        catch(InterruptedException ie){
            System.out.println("InterruptedException occurred!\n");
            ie.printStackTrace();
        }

    }

    public static Props props(ActorSelection serverActor) {
        return Props.create(Kitchen.class, serverActor);
    }
}
