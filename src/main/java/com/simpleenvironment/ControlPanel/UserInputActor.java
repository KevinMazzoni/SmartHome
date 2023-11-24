package com.simpleenvironment.ControlPanel;

import java.util.Scanner;

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
            int choice = showCli(msg.getEnergyConsumption());
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
        if(msg.getType().equals(Type.INPUT_REACHED_TEMPERATURE)){
            int choice = inputReachedTemperature(msg);
            context().parent().tell(new SimpleMessage(choice, Type.INPUT_REACHED_TEMPERATURE, msg.getRoom()), self());
        }
        if(msg.getType().equals(Type.INPUT_CONTINUE)){
            String choice = inputContinue();
            context().parent().tell(new SimpleMessage(choice, Type.INPUT_CONTINUE, msg.getRoom()), self());
        }
    }

    private int showCli(int energyConsumption) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\nCONSUMO TOTALE ATTUALE: \u001B[33m" + energyConsumption + " W\u001B[0m\n");
        System.out.println("Scegli la stanza: \n1. Cucina\n2. Camera da letto\n3. Salotto");
        int choice = scanner.nextInt();
        System.out.println("\n");
        return choice;
    }

    private boolean inputHVAC() {
        boolean choice;
        System.out.print("\nVuoi accendere il condizionatore? (y/n)\t");
        String answer = scanner.next();
        System.out.println();
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

    private int inputReachedTemperature(SimpleMessage msg) {
        if(msg.getRoom().equals(Room.KITCHEN)){
            System.out.println("\nL'ambiente cucina ha raggiunto la temperatura di \u001B[32m" + msg.getCurrentTemperature() + "° C\u001B[0m. Cosa desideri fare?");
            if(msg.getHVACOn())
                System.out.println("1 -> mantenere la temperatura.\n2 -> spegnere il climatizzatore\n3 -> impostare una temperatura diversa");
            else
                System.out.println("1 -> mantenere la temperatura.\n3 -> impostare una temperatura diversa");
            int choice = scanner.nextInt();
            System.out.print("\nTemperatura cucina: " + msg.getCurrentTemperature() + "° C");
            System.out.println("\tConsumo elettrico cucina: " + msg.getCurrentConsumption() + " W");
            return choice;
        }
        else if(msg.getRoom().equals(Room.BEDROOM)){
            System.out.println("\nL'ambiente camera da letto ha raggiunto la temperatura di \u001B[32m" + msg.getCurrentTemperature() + "° C\u001B[0m. Cosa desideri fare?");
            if(msg.getHVACOn())
                System.out.println("1 -> mantenere la temperatura.\n2 -> spegnere il climatizzatore\n3 -> impostare una temperatura diversa");
            else
                System.out.println("1 -> mantenere la temperatura.\n3 -> impostare una temperatura diversa");
            int choice = scanner.nextInt();
            System.out.print("\nTemperatura camera da letto: " + msg.getCurrentTemperature() + "° C");
            System.out.println("\tConsumo elettrico camera da letto: " + msg.getCurrentConsumption() + " W");
            System.out.println();
            return choice;
        }
        return 0;
    }

    private String inputContinue() {
        System.out.print("Vuoi proseguire con un altro ambiente? (y/n)\t");
        String choiceString = scanner.next();
        System.out.println();
        return choiceString;
    }

    static Props props() {
        return Props.create(UserInputActor.class);
    }
}

