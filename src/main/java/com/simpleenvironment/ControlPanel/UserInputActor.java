package com.simpleenvironment.ControlPanel;

import java.util.Scanner;

import com.simpleenvironment.Messages.Room;
import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.Type;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class UserInputActor extends AbstractActor {

    private ActorRef serverActor;
    Scanner scanner = new Scanner(System.in);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SimpleMessage.class, this::handleUserInput)
                .build();
    }

    private void handleUserInput(SimpleMessage msg) {
        if(msg.getType().equals(Type.INFO_SERVER)){
            serverActor = msg.getSiblingActor();
        }
        if(msg.getType().equals(Type.INPUT_ENVIRONMENT)){
            // System.out.println("Ricevuto Type.INPUT");
            int choice = showCli(msg.getEnergyConsumption());
            serverActor.tell(new SimpleMessage(choice, Type.INPUT_ENVIRONMENT), self());
        }
        if(msg.getType().equals(Type.INPUT_HVAC)){
            boolean choice = inputHVAC();
            // System.out.println("Choicein UserInpiutActor: " + choice);
            serverActor.tell(new SimpleMessage(choice, Type.INPUT_HVAC, msg.getRoom()), self());
        }
        if(msg.getType().equals(Type.INPUT_TEMPERATURE)){
            int desiredTemperature = inputTemperature();
            serverActor.tell(new SimpleMessage(msg.getResettingChoice(), desiredTemperature, Type.INPUT_TEMPERATURE, msg.getRoom()), self());
        }
        if(msg.getType().equals(Type.INPUT_REACHED_TEMPERATURE)){
            int choice = inputReachedTemperature(msg);
            serverActor.tell(new SimpleMessage(choice, Type.INPUT_REACHED_TEMPERATURE, msg.getRoom()), self());
        }
        if(msg.getType().equals(Type.INPUT_CONTINUE)){
            String choice = inputContinue();
            serverActor.tell(new SimpleMessage(choice, Type.INPUT_CONTINUE, msg.getRoom()), self());
        }
    }

    private int showCli(int energyConsumption) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\nCONSUMO TOTALE ATTUALE: \u001B[33m" + energyConsumption + " W\u001B[0m\n");
        System.out.println("Scegli la stanza: \n1. Cucina\n2. Camera da letto");
        int choice = scanner.nextInt();
        System.out.println("\n");
        return choice;
    }

    private boolean inputHVAC() {
        boolean choice;
        boolean answerValid = false;
        while(!answerValid){
            System.out.print("\nVuoi accendere il condizionatore? (y/n)\t");
            String answer = scanner.next();
            System.out.println();
            if(answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("n")){
                answerValid = true;
                choice = (answer.equalsIgnoreCase("y")) ? true : false;
                return choice;
            }
            else
                System.out.println("\u001B[31mSeleziona una risposta valida per favore\u001B[0m\n");
        }
        return false;
    }

    private int inputTemperature() {
        boolean validAnswer = false;
        while(!validAnswer){
            System.out.print("Quale temperatura (°C) vuoi?\u001B[32m\t");
            try{
                int choice = scanner.nextInt();
                System.out.println("\u001B[0m");
                if(choice >= 0 && choice <= 50){
                    validAnswer = true;
                    return choice;
                }
                else
                    System.out.println("\u001B[31mScegli una temperatura nel range 0-50 °C\u001B[0m\n");
            }
            catch(Exception e){
                System.out.println("\u001B[31mSeleziona una risposta valida per favore\u001B[0m\n");
            }
        }
        return 0;
    }

    private int inputReachedTemperature(SimpleMessage msg) {
        boolean validAnswer = false;
        if(msg.getRoom().equals(Room.KITCHEN)){
            System.out.println("\nL'ambiente cucina ha raggiunto la temperatura di \u001B[32m" + msg.getCurrentTemperature() + "° C\u001B[0m. Cosa desideri fare?");
            while(!validAnswer){
                if(msg.getHVACOn())
                    System.out.println("1 -> mantenere la temperatura.\n2 -> spegnere il climatizzatore\n3 -> impostare una temperatura diversa");
                else
                    System.out.println("1 -> mantenere la temperatura.\n3 -> impostare una temperatura diversa");
                try{
                    int choice = scanner.nextInt();
                    if(choice == 1 || choice == 2 || choice == 3){
                        System.out.print("\nTemperatura cucina: " + msg.getCurrentTemperature() + "° C");
                        System.out.println("\tConsumo elettrico cucina: " + msg.getCurrentConsumption() + " W");
                        System.out.println();
                        validAnswer = true;
                        return choice;
                    }
                    else
                        System.out.println("\u001B[31mSeleziona una risposta valida per favore\u001B[0m");
                }
                catch(Exception e){
                    System.out.println("\u001B[31mSeleziona una risposta valida per favore\u001B[0m");
                    continue;
                }
            }
        }
        else if(msg.getRoom().equals(Room.BEDROOM)){
            System.out.println("\nL'ambiente camera da letto ha raggiunto la temperatura di \u001B[32m" + msg.getCurrentTemperature() + "° C\u001B[0m. Cosa desideri fare?");
            while(!validAnswer){
                if(msg.getHVACOn())
                    System.out.println("1 -> mantenere la temperatura.\n2 -> spegnere il climatizzatore\n3 -> impostare una temperatura diversa");
                else
                    System.out.println("1 -> mantenere la temperatura.\n3 -> impostare una temperatura diversa");
                try{
                    int choice = scanner.nextInt();
                    if(choice == 1 || choice == 2 || choice == 3){
                        System.out.print("\nTemperatura camera da letto: " + msg.getCurrentTemperature() + "° C");
                        System.out.println("\tConsumo elettrico camera da letto: " + msg.getCurrentConsumption() + " W");
                        System.out.println();
                        validAnswer = true;
                        return choice;
                    }
                    else
                        System.out.println("\u001B[31m\nSeleziona una risposta valida per favore\u001B[0m\n");
                }
                catch(Exception e){
                    System.out.println("\u001B[31mSeleziona una risposta valida per favore\u001B[0m");
                    continue;
                }
            }
        }
        return 0;
    }

    private String inputContinue() {
        boolean answerValid = false;
        while(!answerValid){
            System.out.print("Vuoi proseguire con un altro ambiente? (y/n)\t");
            try{
                String choiceString = scanner.next();
                System.out.println();
                if(choiceString.equalsIgnoreCase("y") || choiceString.equalsIgnoreCase("n")){
                    answerValid = true;
                    return choiceString;
                }
                else
                    System.out.println("\u001B[31mSeleziona una risposta valida per favore\u001B[0m\n");
            }
            catch(Exception e){
                System.out.println("\u001B[31mSeleziona una risposta valida per favore\u001B[0m\n");
                continue;
            }
        }
        return "";
    }

    static Props props() {
        return Props.create(UserInputActor.class);
    }
}

