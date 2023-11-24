package com.simpleenvironment.ControlPanel;

import static akka.pattern.Patterns.ask;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.simpleenvironment.Messages.Appliance;
import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.TemperatureMessage;
import com.simpleenvironment.Messages.Type;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class ControlPanel {

    public static void main(String[] args) throws InterruptedException {

        // Carica la configurazione da application.conf
        Config config = ConfigFactory.load();

        // Crea un sistema degli attori
        final ActorSystem system = ActorSystem.create("ServerSystem", config);
        
        ActorRef server;
        ActorRef userInputActor;

        //Timeout attesa
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        // Crea l'attore ControlPanelActor, supervisore del ServerActor
        final ActorRef controlPanelActor = system.actorOf(ControlPanelActor.props(), "ControlPanelActor");
        
        try{

            //Creo ServerActor nel contesto di ControlPanel, così facendo ControlPanel supervisiona il ServerActor
            scala.concurrent.Future<Object> waitingForServerActor = ask(controlPanelActor, Props.create(ServerActor.class), 5000);
            server = (ActorRef) waitingForServerActor.result(timeout, null);

            //Creo UserInputActor nel contesto di ControlPanel, così facendo ControlPanel supervisiona UserInputActor
            scala.concurrent.Future<Object> waitingForUserInputActor = ask(controlPanelActor, Props.create(UserInputActor.class), 5000);
            userInputActor = (ActorRef) waitingForUserInputActor.result(timeout, null);
            
            //Informo il ControlPanelActor di suo figlio ServerActor
            controlPanelActor.tell(new SimpleMessage(server, Type.INFO_CHILD, Appliance.SERVER), ActorRef.noSender());

            //Informo il ControlPanelActor di suo figlio UserInputActor
            controlPanelActor.tell(new SimpleMessage(userInputActor, Type.INFO_CHILD, Appliance.USER_INPUT), ActorRef.noSender());

            //Informo il ServerActor dell'ActorSystem
            server.tell(new SimpleMessage(system, Type.INFO_ACTOR_SYSTEM), ActorRef.noSender());

            //Informo il ServerActor di suo padre ControlPanelActor
            server.tell(new SimpleMessage(controlPanelActor, Type.INFO_CONTROLPANEL, Appliance.SERVER), ActorRef.noSender());

            //Informo il ServerActor di UserInputActor
            server.tell(new SimpleMessage(userInputActor, Type.INFO_USER_INPUT, Appliance.USER_INPUT), ActorRef.noSender());

            //Informo UserInputActor di ServerActor
            userInputActor.tell(new SimpleMessage(server, Type.INFO_SERVER, Appliance.SERVER), ActorRef.noSender());

            server.tell(new SimpleMessage("START", Type.START), ActorRef.noSender());

        }
        catch(TimeoutException te){
            System.out.println("TimeoutException occurred!\n");
            te.printStackTrace();
        }

    }

    static Props props() {
        return Props.create(ServerActor.class);
    }
}
