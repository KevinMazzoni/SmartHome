
package com.simpleenvironment.Bedroom;

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

import java.time.Duration;

public class Bedroom {

    public static void main(String[] args) {

        Config config = ConfigFactory.load();
        
        //Creazione del sistema di attori
        ActorSystem system = ActorSystem.create("ServerSystem", config);

        //Creo i riferimenti al temperatureSensor e al HVAC della camera
        ActorRef bedroomTemperatureSensorActor = null;
        ActorRef bedroomHVACActor = null;

        //Timeout attesa temperatureSensor
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        // Crea l'attore del server
        final ActorRef bedroomSupervisorActor = system.actorOf(BedroomSupervisorActor.props(), "BedroomSupervisorActor");

        try{
            //Creo BedroomTemperatureSensorActor nel contesto di BedroomSupervisorActor, così facendo BedroomSupervisorActor supervisiona il BedroomTemperatureSensorActor 
            scala.concurrent.Future<Object> waitingForBedroomTemperatureSensorActor = ask(bedroomSupervisorActor, Props.create(BedroomTemperatureSensorActor.class, "BedroomTemperatureSensorActor"), 5000);
            bedroomTemperatureSensorActor = (ActorRef) waitingForBedroomTemperatureSensorActor.result(timeout, null);

            //Creo BedroomHVACActor nel contesto di BedroomSupervisorActor, così facendo BedroomSupervisorActor supervisiona il BedroomHVACActor 
            scala.concurrent.Future<Object> waitingForBedroomHVACActor = ask(bedroomSupervisorActor, Props.create(BedroomHVACActor.class, "BedroomHVACActor"), 5000);
            bedroomHVACActor = (ActorRef) waitingForBedroomHVACActor.result(timeout, null);

            //Invio al bedroomSupervisorActor i riferimenti ai suoi figli
            bedroomSupervisorActor.tell(new SimpleMessage(bedroomTemperatureSensorActor, Type.INFO_CHILD, Appliance.TEMPERATURE_SENSOR), ActorRef.noSender());
            bedroomSupervisorActor.tell(new SimpleMessage(bedroomHVACActor, Type.INFO_CHILD, Appliance.HVAC), ActorRef.noSender());

            //Invio ai figli il riferimento al padre bedroomSupervisorActor
            bedroomTemperatureSensorActor.tell(new SimpleMessage(bedroomSupervisorActor, Type.INFO_PARENT, Appliance.BEDROOM_SUPERVISOR), ActorRef.noSender());
            bedroomHVACActor.tell(new SimpleMessage(bedroomSupervisorActor, Type.INFO_PARENT, Appliance.BEDROOM_SUPERVISOR), ActorRef.noSender());

            //Invio al bedroomHVACActor il riferimento al bedroomTemperatureSensorActor
            bedroomHVACActor.tell(new SimpleMessage(bedroomTemperatureSensorActor, Type.INFO_SIBLING, Appliance.TEMPERATURE_SENSOR), ActorRef.noSender());

            // Crea un riferimento all'attore controlPanelActor
            ActorSelection controlPanelActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/ControlPanelActor");

            //Invio al bedroomSupervsorActor il riferimento al ControlPanelActor
            bedroomSupervisorActor.tell(new SimpleMessage(controlPanelActor, Type.INFO_CONTROLPANEL), ActorRef.noSender());

            system.scheduler().scheduleOnce(Duration.ofMillis(3000), bedroomTemperatureSensorActor, new SimpleMessage("Errore invio", Type.ERROR), system.dispatcher(), ActorRef.noSender());

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
        return Props.create(Bedroom.class, serverActor);
    }
}
