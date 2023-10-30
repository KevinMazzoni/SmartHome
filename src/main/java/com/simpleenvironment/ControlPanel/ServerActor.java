package com.simpleenvironment.ControlPanel;

import java.util.Scanner;

import com.simpleenvironment.Messages.Room;
import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.TemperatureMessage;
import com.simpleenvironment.Messages.Type;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class ServerActor extends AbstractActor {

    private static final int KITCHEN = 1;
    private static final int BEDROOM = 2;

    private boolean kitchenHVACOn;
    private boolean kitchenHVACOffSelectable;

    private boolean bedroomHVACOn;
    private boolean bedroomHVACOffSelectable;

    private int kitchenInitialTemperature;
    private int bedroomInitialTemperature;

    private int kitchenCurrentTemperature;
	private int bedroomCurrentTemperature;

    private int kitchenCurrentConsumption;
    private int bedroomCurrentConsumption;
    
    private int kitchenDesiredTemperature;
    private int bedroomDesiredTemperature;

    private ActorSystem system;
    
    private ActorSelection controlPanelActor;
    private ActorSelection kitchenSupervisorActor;
    private ActorSelection bedroomSupervisorActor;

    private static final boolean RESET = true;
    private static final boolean SET = false;

    public ServerActor(){
        this.kitchenCurrentTemperature = -1;
		this.bedroomCurrentTemperature = -1;

        this.kitchenCurrentConsumption = 0;
		this.bedroomCurrentConsumption = 0;

        this.kitchenHVACOn = false;
        this.bedroomHVACOn = false;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    System.out.println("Server ha ricevuto il messaggio: " + message);
                })
                .match(SimpleMessage.class, this::onSimpleMessage)
                .match(TemperatureMessage.class, this::onTemperatureMessage)
                .matchAny(o -> {
                    // Ignora tutti gli altri tipi di messaggi
                    System.out.println("Server ha ricevuto un messaggio di tipo sconosciuto: " + o);
                })
                // .matchEquals("ClientStarted", message -> {
                //     isClientRunning = true;
                //     System.out.println("Il client è stato avviato.");
                // })
                .build();
    }

    void onSimpleMessage(SimpleMessage msg) throws Exception {
        if(msg.getType().equals(Type.INFO_ACTOR_SYSTEM))
            this.system = msg.getActorSystem();
        if(msg.getType().equals(Type.INFO_CONTROLPANEL))
            this.controlPanelActor = msg.getControlPanelRef();
        if(msg.getType().equals(Type.DESIRED_TEMPERATURE)){
            if(msg.getRoom().equals(Room.KITCHEN))
                this.kitchenDesiredTemperature = msg.getDesiredTemperature();
            else if (msg.getRoom().equals(Room.BEDROOM))
                this.bedroomDesiredTemperature = msg.getDesiredTemperature();
        }
        if(msg.getType().equals(Type.START))
            start();
        if(msg.getType().equals(Type.INFO))
            System.out.println("Ricevuto INFO message: " + msg.getMessage());
    }

    void onTemperatureMessage(TemperatureMessage msg) throws Exception {

        // System.out.println("ControlPanelActor ha ricevuto il TemperatureMessage: " + msg.getTemperature() + " da " + getSender());
		switch(msg.getRoom()){
			case KITCHEN:
				if(msg.isFirstMeasure() || msg.getTemperature() != this.kitchenCurrentTemperature){
                    if(msg.isFirstMeasure()) {
                        this.kitchenInitialTemperature = msg.getTemperature();
                        System.out.print("Temperatura cucina: "/*\u001B[33m"*/ + msg.getTemperature() + "° C\u001B[0m");  
                    }
                    else if(msg.getTemperature() > this.kitchenCurrentTemperature && msg.getTemperature() != this.kitchenDesiredTemperature)
                        System.out.print("Temperatura cucina: \u001B[31m" + msg.getTemperature() + "° C\u001B[0m"); 
                    else if(msg.getTemperature() < this.kitchenCurrentTemperature && msg.getTemperature() != this.kitchenDesiredTemperature)
                        System.out.print("Temperatura cucina: \u001B[34m" + msg.getTemperature() + "° C\u001B[0m");
                    else {
                        //Qui ho raggiunto la temperatura desiderata; Manda magari messaggio per segnalare fine
                        System.out.print("Temperatura cucina: \u001B[32m" + msg.getTemperature() + "° C\u001B[0m");
                        System.out.println("\tConsumo elettrico cucina: \u001B[33m" + msg.getEnergyConsumption() + " W\u001B[0m");
                        this.kitchenCurrentTemperature = msg.getTemperature();
                        this.kitchenCurrentConsumption = msg.getEnergyConsumption();
                        reachedTemperature(Room.KITCHEN);
                        return;
                    }
                    System.out.println("\tConsumo elettrico cucina: \u001B[33m" + msg.getEnergyConsumption() + " W\u001B[0m");
                }

				this.kitchenCurrentTemperature = msg.getTemperature();
                this.kitchenCurrentConsumption = msg.getEnergyConsumption();

                if(!kitchenHVACOn){
                    airConditioningActivating(Room.KITCHEN);
                }

				break;
			case BEDROOM:
				if(msg.isFirstMeasure() || msg.getTemperature() != this.bedroomCurrentTemperature){
                    if(msg.isFirstMeasure()) {
                        this.bedroomInitialTemperature = msg.getTemperature();
                        System.out.print("Temperatura camera da letto: "/*\u001B[33m"*/ + msg.getTemperature() + "° C\u001B[0m");  
                    }
                    else if(msg.getTemperature() > this.bedroomCurrentTemperature && msg.getTemperature() != this.bedroomDesiredTemperature)
                        System.out.print("Temperatura camera da letto: \u001B[31m" + msg.getTemperature() + "° C\u001B[0m"); 
                    else if(msg.getTemperature() < this.bedroomCurrentTemperature && msg.getTemperature() != this.bedroomDesiredTemperature)
                        System.out.print("Temperatura camera da letto: \u001B[34m" + msg.getTemperature() + "° C\u001B[0m");
                    else {
                        //Qui ho raggiunto la temperatura desiderata; Manda magari messaggio per segnalare fine
                        System.out.print("Temperatura camera da letto: \u001B[32m" + msg.getTemperature() + "° C\u001B[0m");
                        System.out.println("\tConsumo elettrico camera da letto: \u001B[33m" + msg.getEnergyConsumption() + " W\u001B[0m");
                        this.bedroomCurrentTemperature = msg.getTemperature();
                        this.bedroomCurrentConsumption = msg.getEnergyConsumption();
                        reachedTemperature(Room.BEDROOM);
                        return;
                    }
                    System.out.println("\tConsumo elettrico camera da letto: \u001B[33m" + msg.getEnergyConsumption() + " W\u001B[0m");
                }

				this.bedroomCurrentTemperature = msg.getTemperature();
                this.bedroomCurrentConsumption = msg.getEnergyConsumption();

                if(!bedroomHVACOn){
                    airConditioningActivating(Room.BEDROOM);
                }

				break;
			default:
				break;
		}
    }

    private void start() {
        int environment = showCli();
        
        if(environment == KITCHEN){
            kitchenSupervisorActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2553/user/KitchenSupervisorActor");
            kitchenSupervisorActor.tell(new SimpleMessage("INFO_TEMPERATURE", Type.INFO_TEMPERATURE), ActorRef.noSender());
        }

        if(environment == BEDROOM){
            bedroomSupervisorActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2554/user/BedroomSupervisorActor");
            bedroomSupervisorActor.tell(new SimpleMessage("INFO_TEMPERATURE", Type.INFO_TEMPERATURE), ActorRef.noSender());
        }
    }

    private static int showCli() {
        System.out.println("Scegli la stanza: \n1. Cucina\n2. Camera da letto\n3. Salotto");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        System.out.println();
        return choice;
    }

    private boolean airConditioning(Room room){
        boolean choice;
        System.out.print("\nVuoi accendere il condizionatore? (y/n)\t");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.next();
        choice = (answer.equalsIgnoreCase("y")) ? true : false;
        if(room.equals(Room.KITCHEN))
            this.kitchenHVACOn = choice;
        else if(room.equals(Room.BEDROOM))
            this.bedroomHVACOn = choice;
        return choice;
    }

    private void airConditioningActivating(Room room){
        if(room.equals(Room.KITCHEN)){
            boolean wantsAirConditioning = airConditioning(room);
            if(wantsAirConditioning){
                kitchenHVACOn = true;
                this.kitchenHVACOffSelectable = true;
                this.kitchenDesiredTemperature = setTemperature(SET, Room.KITCHEN);
                this.kitchenSupervisorActor.tell(new SimpleMessage(kitchenDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.KITCHEN), self());
            }
            else{
                System.out.println();
                start();
            }
        }
        else if(room.equals(Room.BEDROOM)){
            boolean wantsAirConditioning = airConditioning(room);
            if(wantsAirConditioning){
                bedroomHVACOn = true;
                this.bedroomHVACOffSelectable = true;
                this.bedroomDesiredTemperature = setTemperature(SET, Room.BEDROOM);
                this.bedroomSupervisorActor.tell(new SimpleMessage(bedroomDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.BEDROOM), self());
            }
            else{
                System.out.println();
                start();
            }
        }
    }

    private int setTemperature(boolean resetting, Room room){
        if(room.equals(Room.KITCHEN)){
            System.out.print("Quale temperatura (°C) vuoi?\u001B[32m\t");
            Scanner scanner = new Scanner(System.in);
            this.kitchenDesiredTemperature = scanner.nextInt();
            System.out.println("\u001B[0m");
            if(resetting)
                this.kitchenSupervisorActor.tell(new SimpleMessage(kitchenDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.KITCHEN), self());
            return kitchenDesiredTemperature;
        }
        else if(room.equals(Room.BEDROOM)){
            System.out.print("Quale temperatura (°C) vuoi?\u001B[32m\t");
            Scanner scanner = new Scanner(System.in);
            this.bedroomDesiredTemperature = scanner.nextInt();
            System.out.println("\u001B[0m");
            if(resetting)
                this.bedroomSupervisorActor.tell(new SimpleMessage(bedroomDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.BEDROOM), self());
            return bedroomDesiredTemperature;
        }
        return 0;
    }

    private int reachedTemperature(Room room){
        if(room.equals(Room.KITCHEN)){
            System.out.println("\nL'ambiente cucina ha raggiunto la temperatura di \u001B[32m" + this.kitchenCurrentTemperature + "° C\u001B[0m. Cosa desideri fare?");
            System.out.println("1 -> mantenere la temperatura.\n2 -> spegnere il climatizzatore\n3 -> impostare una temperatura diversa");
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            System.out.print("\nTemperatura cucina: " + this.kitchenCurrentTemperature + "° C");
            System.out.println("\tConsumo elettrico cucina: " + this.kitchenCurrentConsumption + " W");
            switch(choice){
                case 1:
                    System.out.println("Vuoi proseguire con un altro ambiente? (y/n)");
                    String choiceString = scanner.next();
                    if(choiceString.equalsIgnoreCase("y")){
                        kitchenHVACOn = false;
                        start();
                    }
                    else{
                        System.out.println("Grazie per aver usato SMART HOME.");
                        system.terminate();
                        return 0;
                    }
                    break;
                case 2:
                    // kitchenHVACOn = false;
                    if(!this.kitchenHVACOffSelectable){
                        System.out.println("Il climatizzatore è già spento");
                        airConditioningActivating(Room.KITCHEN);
                    }
                    else {
                        this.kitchenHVACOffSelectable = false;
                        this.kitchenDesiredTemperature = kitchenInitialTemperature;
                        this.kitchenSupervisorActor.tell(new SimpleMessage(this.kitchenInitialTemperature, Type.STOP_HVAC, Room.KITCHEN), self());
                    }
                    break;
                case 3:
                    setTemperature(RESET, Room.KITCHEN);
                    break;
                default:
                    break;
            }
            return choice;
        }
        else if(room.equals(Room.BEDROOM)){
            System.out.println("\nL'ambiente camera da letto ha raggiunto la temperatura di \u001B[32m" + this.bedroomCurrentTemperature + "° C\u001B[0m. Cosa desideri fare?");
            System.out.println("1 -> mantenere la temperatura.\n2 -> spegnere il climatizzatore\n3 -> impostare una temperatura diversa");
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            System.out.print("\nTemperatura camera da letto: " + this.bedroomCurrentTemperature + "° C");
            System.out.println("\tConsumo elettrico camera da letto: " + this.bedroomCurrentConsumption + " W");
            switch(choice){
                case 1:
                    System.out.println("Vuoi proseguire con un altro ambiente? (y/n)");
                    String choiceString = scanner.next();
                    if(choiceString.equalsIgnoreCase("y")){
                        bedroomHVACOn = false;
                        start();
                    }
                    else{
                        System.out.println("Grazie per aver usato SMART HOME.");
                        system.terminate();
                        return 0;
                    }
                    break;
                case 2:
                    // bedroomHVACOn = false;
                    if(!this.bedroomHVACOffSelectable){
                        System.out.println("Il climatizzatore è già spento");
                        airConditioningActivating(Room.BEDROOM);
                    }
                    else {
                        this.bedroomHVACOffSelectable = false;
                        this.bedroomDesiredTemperature = bedroomInitialTemperature;
                        this.bedroomSupervisorActor.tell(new SimpleMessage(this.bedroomInitialTemperature, Type.STOP_HVAC, Room.BEDROOM), self());
                    }
                    break;
                case 3:
                    setTemperature(RESET, Room.BEDROOM);
                    break;
                default:
                    break;
            }
            return choice;
        }
        return 0;
    }
    
    static Props props() {
        return Props.create(ServerActor.class);
    }
}
