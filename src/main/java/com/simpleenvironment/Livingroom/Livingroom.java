
package com.simpleenvironment.Livingroom;

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

public class Livingroom {

    public static void main(String[] args) {

        Config config = ConfigFactory.load();
        
        //Creazione del sistema di attori
        ActorSystem system = ActorSystem.create("ServerSystem", config);

        //Creo i riferimenti al tvActor
        ActorRef tvActor = null;

        //Timeout attesa tvActor
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        // Crea l'attore supervisor della Livingroom
        final ActorRef livingroomSupervisorActor = system.actorOf(LivingroomSupervisorActor.props(), "LivingroomSupervisorActor");

        try{
            //Creo TvActor nel contesto di LivingroomSupervisorActor, così facendo LivingroomSupervisorActor supervisiona il TvActor 
            scala.concurrent.Future<Object> waitingForTvActor = ask(livingroomSupervisorActor, Props.create(TvActor.class, "TvActor"), 5000);
            tvActor = (ActorRef) waitingForTvActor.result(timeout, null);

            //Invio al livingroomSupervisorActor i riferimenti ai suoi figli
            livingroomSupervisorActor.tell(new SimpleMessage(tvActor, Type.INFO_CHILD, Appliance.TV), ActorRef.noSender());
   
            //Invio ai figli il riferimento al padre livingroomSupervisorActor
            tvActor.tell(new SimpleMessage(livingroomSupervisorActor, Type.INFO_PARENT, Appliance.LIVINGROOM_SUPERVISOR), ActorRef.noSender());

            // Crea un riferimento all'attore controlPanelActor
            ActorSelection controlPanelActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/ControlPanelActor");

            //Invio al livingroomSupervisorActor il riferimento al ControlPanelActor
            livingroomSupervisorActor.tell(new SimpleMessage(controlPanelActor, Type.INFO_CONTROLPANEL), ActorRef.noSender());

            system.scheduler().scheduleOnce(Duration.ofMillis(3000), tvActor, new SimpleMessage("Errore invio", Type.ERROR), system.dispatcher(), ActorRef.noSender());

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
        return Props.create(Livingroom.class, serverActor);
    }
}
