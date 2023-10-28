package com.simpleenvironment.ControlPanel;

import static akka.pattern.Patterns.ask;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.TemperatureMessage;
import com.simpleenvironment.Messages.Type;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class ControlPanel {

    private static final int KITCHEN = 1;
    private static final int BEDROOM = 2;

    public static void main(String[] args) throws InterruptedException {

        // Carica la configurazione da application.conf
        Config config = ConfigFactory.load();

        // Crea un sistema degli attori
        final ActorSystem system = ActorSystem.create("ServerSystem", config);
        
        ActorRef server;

        //Timeout attesa
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        // Crea l'attore ControlPanelActor, supervisore del ServerActor
        final ActorRef controlPanelActor = system.actorOf(ControlPanelActor.props(), "ControlPanelActor");
        
        try{

            //Creo ServerActor nel contesto di ControlPanel, così facendo ControlPanel supervisiona il ServerActor
            scala.concurrent.Future<Object> waitingForServerActor = ask(controlPanelActor, Props.create(ServerActor.class), 5000);
            server = (ActorRef) waitingForServerActor.result(timeout, null);
            controlPanelActor.tell(new SimpleMessage(server, Type.INFO_CHILD), ActorRef.noSender());

        }
        catch(TimeoutException te){
            System.out.println("TimeoutException occurred!\n");
            te.printStackTrace();
        }
        catch(InterruptedException ie){
            System.out.println("InterruptedException occurred!\n");
            ie.printStackTrace();
        }


        int environment = showCli();

        if(environment == KITCHEN){
            ActorSelection kitchenSupervisorActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2553/user/KitchenSupervisorActor");
            kitchenSupervisorActor.tell(new SimpleMessage("Prova invio SimpleMessage di tipo INFO_TEMPERATURE dal ControlPanel a KitchenSupervisorActor", Type.INFO_TEMPERATURE), ActorRef.noSender());
            Thread.sleep(1000);
            boolean wantsAirConditioning = airConditioning();
            if(wantsAirConditioning){
                int desiredTemperature = setTemperature();
                kitchenSupervisorActor.tell(new SimpleMessage(desiredTemperature, Type.DESIRED_TEMPERATURE), controlPanelActor);
            }
        }
    }

    private static int showCli() {
        System.out.println("Scegli la stanza: \n1. Cucina\n2. Salotto\n3. Camera da letto");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        System.out.println();
        return choice;
    }

    private static boolean airConditioning(){
        boolean choice;
        System.out.println("\nVuoi accendere il condizionatore? y/n");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.next();
        choice = (answer.equalsIgnoreCase("y")) ? true : false;
        return choice;
    }

    private static int setTemperature(){
        System.out.println("Quale temperatura (°C) vuoi?");
        Scanner scanner = new Scanner(System.in);
        int desiredTemperature = scanner.nextInt();
        return desiredTemperature;
    }

    static Props props() {
        return Props.create(ServerActor.class);
    }
}
