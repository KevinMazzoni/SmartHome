package com.simpleenvironment.ControlPanel;

import java.util.Scanner;

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

    private boolean kitchenHVACOn;

    private int kitchenCurrentTemperature;
	private int bedroomCurrentTemperature;
    private int desiredTemperature;

    private ActorSystem system;
    
    private ActorSelection controlPanelActor;
    private ActorSelection kitchenSupervisorActor;

    public ServerActor(){
        this.kitchenCurrentTemperature = -1;
		this.bedroomCurrentTemperature = -1;
        this.kitchenHVACOn = false;
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
        if(msg.getType().equals(Type.DESIRED_TEMPERATURE))
            this.desiredTemperature = msg.getDesiredTemperature();
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
                    if(msg.isFirstMeasure()) 
                        System.out.print("Temperatura cucina: "/*\u001B[33m"*/ + msg.getTemperature() + "° C\u001B[0m");  
                    else if(msg.getTemperature() > this.kitchenCurrentTemperature && msg.getTemperature() != this.desiredTemperature)
                        System.out.print("Temperatura cucina: \u001B[31m" + msg.getTemperature() + "° C\u001B[0m"); 
                    else if(msg.getTemperature() < this.kitchenCurrentTemperature && msg.getTemperature() != this.desiredTemperature)
                        System.out.print("Temperatura cucina: \u001B[34m" + msg.getTemperature() + "° C\u001B[0m");
                    else {
                        //Qui ho raggiunto la temperatura desiderata; Manda magari messaggio per segnalare fine
                        System.out.print("Temperatura cucina: \u001B[32m" + msg.getTemperature() + "° C\u001B[0m");
                        reachedTemperature("Cucina");
                    }
                    System.out.println("\tConsumo elettrico cucina: \u001B[33m" + msg.getEnergyConsumption() + " W\u001B[0m");
                }
				this.kitchenCurrentTemperature = msg.getTemperature();

                if(!kitchenHVACOn){
                    boolean wantsAirConditioning = airConditioning();
                    if(wantsAirConditioning){
                        kitchenHVACOn = true;
                        this.desiredTemperature = setTemperature();
                        kitchenSupervisorActor.tell(new SimpleMessage(desiredTemperature, Type.DESIRED_TEMPERATURE), self());
                    }
                }

				break;
			case BEDROOM:
				
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
        System.out.print("\nVuoi accendere il condizionatore? (y/n)\t");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.next();
        choice = (answer.equalsIgnoreCase("y")) ? true : false;
        return choice;
    }

    private static int setTemperature(){
        System.out.print("Quale temperatura (°C) vuoi?\u001B[32m\t");
        Scanner scanner = new Scanner(System.in);
        int desiredTemperature = scanner.nextInt();
        System.out.println("\u001B[0m");
        return desiredTemperature;
    }

    private static int reachedTemperature(String environment){
        System.out.print("L'ambiente " + environment + " ha raggiunto la temperatura desiderata. Cosa desideri fare?");
        System.out.println("1 -> mantenere la temperatura desiderata\n2 -> spegnere il climatizzatore\n3 -> impostare una temperatura diversa.");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        
        //Ripartire da quiii (Committa prima se vuoi)

        return choice;
    }
    
    static Props props() {
        return Props.create(ServerActor.class);
    }
}
