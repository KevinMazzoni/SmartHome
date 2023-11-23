package com.simpleenvironment.ControlPanel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.simpleenvironment.Messages.Room;
import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.Type;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class UserInputActor extends AbstractActor {

    Scanner scanner = new Scanner(System.in);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SimpleMessage.class, this::handleUserInput)
                .build();
    }

    private void handleUserInput(SimpleMessage msg) {
        if(msg.getType().equals(Type.INPUT_ENVIRONMENT)){
            // System.out.println("Ricevuto Type.INPUT");
            int choice = showCli();
            context().parent().tell(new SimpleMessage(choice, Type.INPUT_ENVIRONMENT), self());
        }
        if(msg.getType().equals(Type.INPUT_HVAC)){
            boolean choice = inputHVAC();
            // System.out.println("Choicein UserInpiutActor: " + choice);
            context().parent().tell(new SimpleMessage(choice, Type.INPUT_HVAC, msg.getRoom()), self());
        }
        if(msg.getType().equals(Type.INPUT_TEMPERATURE)){
            int desiredTemperature = inputTemperature();
            context().parent().tell(new SimpleMessage(msg.getResettingChoice(), desiredTemperature, Type.INPUT_TEMPERATURE, msg.getRoom()), self());
        }
        // // Gestisci l'input utente e inoltra il messaggio all'attore appropriato
        // if (userInput.equalsIgnoreCase("kitchen")) {
        //     // Inoltra il messaggio a KitchenActor
        //     context().parent().tell(new Type.KITCHEN_OFF(), self());
        // } else if (userInput.equalsIgnoreCase("bedroom")) {
        //     // Inoltra il messaggio a BedroomActor
        //     // context().parent().tell(new Type.BEDROOM_OFF(), self());
        // }
        // // Altri casi di gestione dell'input utente
    }

    private int showCli() {
        // System.out.println("\nCONSUMO TOTALE ATTUALE: \u001B[33m" + (this.bedroomCurrentConsumption + this.kitchenCurrentConsumption) + " W\u001B[0m\n");
        System.out.println("Scegli la stanza: \n1. Cucina\n2. Camera da letto\n3. Salotto");
        int choice = scanner.nextInt();
        System.out.println("\n");
        return choice;
    }

    private boolean inputHVAC() {
        boolean choice;
        System.out.print("\nVuoi accendere il condizionatore? (y/n)\t");
        String answer = scanner.next();
        // System.out.println("ANSWERRR: " + answer);
        choice = (answer.equalsIgnoreCase("y")) ? true : false;
        return choice;
    }

    private int inputTemperature() {
        // System.out.println("NEXTT: " + scanner.next());
        System.out.print("Quale temperatura (°C) vuoi?\u001B[32m\t");
        int choice = scanner.nextInt();
        System.out.println("\u001B[0m");
        return choice;
    }

    static Props props() {
        return Props.create(UserInputActor.class);
    }
}

