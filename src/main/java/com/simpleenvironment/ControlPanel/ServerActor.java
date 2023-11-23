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
    private static final int LIVINGROOM = 3;

    private boolean kitchenHVACOn;
    private boolean kitchenHVACOffSelectable;

    private boolean bedroomHVACOn;
    private boolean bedroomHVACOffSelectable;

    private boolean kitchenEnvironment;
    private boolean bedroomEnvironment;

    private boolean kitchenRunning = false;
	private boolean bedroomRunning = false;

    private boolean workingOnKitchen = false;
    private boolean workingOnBedroom = false;

    private int kitchenInitialTemperature;
    private int bedroomInitialTemperature;

    private int kitchenCurrentTemperature;
	private int bedroomCurrentTemperature;

    private int kitchenCurrentConsumption;
    private int bedroomCurrentConsumption;
    
    private int kitchenDesiredTemperature;
    private int bedroomDesiredTemperature;

    private int environment;

    private ActorSystem system;

    private ActorRef userInputActor;
    
    private ActorSelection controlPanelActor;
    private ActorSelection kitchenSupervisorActor;
    private ActorSelection bedroomSupervisorActor;
    private ActorSelection livingroomSupervisorActor;

    private static final boolean RESET = true;
    private static final boolean SET = false;

    public ServerActor(){
        this.kitchenCurrentTemperature = -1;
		this.bedroomCurrentTemperature = -1;

        this.kitchenCurrentConsumption = 0;
		this.bedroomCurrentConsumption = 0;

        this.kitchenHVACOn = false;
        this.bedroomHVACOn = false;

        this.kitchenEnvironment = true;
        this.bedroomEnvironment = true;
    }

    @Override
    public void preStart() throws Exception {
        // Crea e avvia l'attore UserInputActor
        userInputActor = getContext().actorOf(UserInputActor.props(), "userInputActor");
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
        Type messageType = msg.getType();
        System.out.println("MEssage type: " + messageType);
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
        if(msg.getType().equals(Type.TV_CONSUMPTION))
            System.out.println("Ricevuto TV_CONSUMPTION: " + msg.getEnergyConsumption());
        if(msg.getType().equals(Type.INPUT_ENVIRONMENT))
            inputEnvironmentReceived(msg);
        if(msg.getType().equals(Type.INPUT_HVAC))
            inputHVACReceived(msg);
        if(msg.getType().equals(Type.INPUT_TEMPERATURE))
            inputTemperatureReceived(msg);
        if(msg.getType().equals(Type.KITCHEN_ON)){
            System.out.println("Kitchen is ON!!!");
            this.kitchenRunning = true;
        }
        if(msg.getType().equals(Type.BEDROOM_ON)){
            System.out.println("Bedroom is ON!!!");
            this.bedroomRunning = true;
        }
        if(msg.getType().equals(Type.KITCHEN_OFF)){
            System.out.println("Kitchen is OFF!!!");
            this.kitchenRunning = false;
            this.kitchenCurrentConsumption = 0;
            if(workingOnKitchen){
                System.out.println("\u001B[31mLa cucina ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
                this.workingOnKitchen = false;
                start();
            }
        }
        if(msg.getType().equals(Type.BEDROOM_OFF)){
            System.out.println("Bedroom is OFF!!!");
            this.bedroomRunning = false;
            this.bedroomCurrentConsumption = 0;
            if(workingOnBedroom){
                System.out.println("\u001B[31mLa camera da letto ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
                this.workingOnBedroom = false;
                start();
            }
        }
        
    }

    void onTemperatureMessage(TemperatureMessage msg) throws Exception {

        // System.out.println("ControlPanelActor ha ricevuto il TemperatureMessage: " + msg.getTemperature() + " da " + getSender());
		switch(msg.getRoom()){
			case KITCHEN:
				if(msg.isFirstMeasure() || msg.getTemperature() != this.kitchenCurrentTemperature){
                    if(msg.isFirstMeasure()) {
                        if(kitchenEnvironment){
                            this.kitchenInitialTemperature = msg.getTemperature();
                            kitchenEnvironment = false;
                        }
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

                if(msg.isFirstMeasure() && !kitchenHVACOn){
                    airConditioningActivating(Room.KITCHEN);
                }
                else{
                    if(msg.isFirstMeasure())
                        reachedTemperature(Room.KITCHEN);
                }

				break;
			case BEDROOM:
				if(msg.isFirstMeasure() || msg.getTemperature() != this.bedroomCurrentTemperature){
                    if(msg.isFirstMeasure()) {
                        if(bedroomEnvironment){
                            this.bedroomInitialTemperature = msg.getTemperature();
                            bedroomEnvironment = false;
                        }
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

                if(msg.isFirstMeasure() && !bedroomHVACOn){
                    airConditioningActivating(Room.BEDROOM);
                }
                else{
                    if(msg.isFirstMeasure())
                        reachedTemperature(Room.BEDROOM);
                }

				break;
			default:
				break;
		}
    }

    private void inputEnvironmentReceived(SimpleMessage msg){

        // System.out.println("INPUT RECEIVED! Choice: " + msg.getEnvironmentChoice());
        
        this.environment = msg.getEnvironmentChoice();

        if(environment == KITCHEN && !this.kitchenRunning || environment == BEDROOM && !this.bedroomRunning){
            System.out.println("\u001B[31mL'ambiente selezionato ha subìto un crash improvviso! Prova con un altro ambiente\u001B[0m");
            // return;
            start();
        }
        
        if(environment == KITCHEN){
            this.workingOnKitchen = true;
            kitchenSupervisorActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2553/user/KitchenSupervisorActor");
            kitchenSupervisorActor.tell(new SimpleMessage("INFO_TEMPERATURE", Type.INFO_TEMPERATURE), ActorRef.noSender());
        }

        if(environment == BEDROOM){
            this.workingOnBedroom = true;
            bedroomSupervisorActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2554/user/BedroomSupervisorActor");
            bedroomSupervisorActor.tell(new SimpleMessage("INFO_TEMPERATURE", Type.INFO_TEMPERATURE), ActorRef.noSender());
        }

        if(environment == LIVINGROOM){
            livingroomSupervisorActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2555/user/LivingroomSupervisorActor");
            livingroomSupervisorActor.tell(new SimpleMessage("INFO_CONSUMPTION_TELEVISION", Type.INFO_CONSUMPTION), ActorRef.noSender());
        }
    }

    private void inputHVACReceived(SimpleMessage msg){
        // System.out.println("INPUT_HVAC Received");
        boolean wantsAirConditioning = msg.getHVACChoice();

        if(msg.getRoom().equals(Room.KITCHEN)){
            // System.out.println("STO PER FARE LA SCELTA!");
            if(wantsAirConditioning){
                kitchenHVACOn = true;
                this.kitchenHVACOffSelectable = true;
                /*this.kitchenDesiredTemperature = */setTemperature(SET, Room.KITCHEN);
                // this.kitchenSupervisorActor.tell(new SimpleMessage(kitchenDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.KITCHEN), self());
            }
            else{
                System.out.println();
                start();
            }
        }
        else if(msg.getRoom().equals(Room.BEDROOM)){
            if(wantsAirConditioning){
                bedroomHVACOn = true;
                this.bedroomHVACOffSelectable = true;
                /*this.bedroomDesiredTemperature = */setTemperature(SET, Room.BEDROOM);
                // this.bedroomSupervisorActor.tell(new SimpleMessage(bedroomDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.BEDROOM), self());
            }
            else{
                System.out.println();
                start();
            }
        }
    }

    private void inputTemperatureReceived(SimpleMessage msg){
        //TODO
        if(msg.getRoom().equals(Room.KITCHEN)){
            if(msg.getResettingChoice())
                this.kitchenSupervisorActor.tell(new SimpleMessage(kitchenDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.KITCHEN), self());
            this.kitchenDesiredTemperature = msg.getDesiredTemperature();
            
            //Dubbio questo sotto
            this.kitchenSupervisorActor.tell(new SimpleMessage(kitchenDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.KITCHEN), self());
        }
        else if(msg.getRoom().equals(Room.BEDROOM)){
            if(msg.getResettingChoice())
                this.bedroomSupervisorActor.tell(new SimpleMessage(bedroomDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.BEDROOM), self());
            this.bedroomDesiredTemperature = msg.getDesiredTemperature();
            
            //Dubbio questo sotto
            this.bedroomSupervisorActor.tell(new SimpleMessage(bedroomDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.BEDROOM), self());
        }
        // return 0;
    }

    private void start() {
        // environment = showCli();

        this.userInputActor.tell(new SimpleMessage((this.bedroomCurrentConsumption + this.kitchenCurrentConsumption), Type.INPUT_ENVIRONMENT), self());

        // System.out.println("\nCONSUMO TOTALE ATTUALE: \u001B[33m" + (this.bedroomCurrentConsumption + this.kitchenCurrentConsumption) + " W\u001B[0m\n");
        // System.out.println("ENVIRONMENT: " + environment);

        // while((environment == KITCHEN && !this.kitchenRunning) || (environment == BEDROOM && !this.bedroomRunning)){
        //     System.out.println("L'ambiente ha subìto un crash improvviso, è consigliato scegliere un altro ambiente.");
        //     environment = showCli();
        // }

        // if(environment == KITCHEN && !this.kitchenRunning){
        //     System.out.println("La cucina ha subìto un crash improvviso!");
        //     return;
        // }
        
        // if(environment == KITCHEN){
        //     kitchenSupervisorActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2553/user/KitchenSupervisorActor");
        //     kitchenSupervisorActor.tell(new SimpleMessage("INFO_TEMPERATURE", Type.INFO_TEMPERATURE), ActorRef.noSender());
        // }

        // if(environment == BEDROOM){
        //     bedroomSupervisorActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2554/user/BedroomSupervisorActor");
        //     bedroomSupervisorActor.tell(new SimpleMessage("INFO_TEMPERATURE", Type.INFO_TEMPERATURE), ActorRef.noSender());
        // }

        // if(environment == LIVINGROOM){
        //     livingroomSupervisorActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2555/user/LivingroomSupervisorActor");
        //     livingroomSupervisorActor.tell(new SimpleMessage("INFO_CONSUMPTION_TELEVISION", Type.INFO_CONSUMPTION), ActorRef.noSender());
        // }
    }

    private int showCli() {
        // System.out.println("\nCONSUMO TOTALE ATTUALE: \u001B[33m" + (this.bedroomCurrentConsumption + this.kitchenCurrentConsumption) + " W\u001B[0m\n");
        // System.out.println("Scegli la stanza: \n1. Cucina\n2. Camera da letto\n3. Salotto");
        // Scanner scanner = new Scanner(System.in);
        // int choice = scanner.nextInt();
        // System.out.println("\n");
        // return choice;
        
        System.out.println("\nCONSUMO TOTALE ATTUALE: \u001B[33m" + (this.bedroomCurrentConsumption + this.kitchenCurrentConsumption) + " W\u001B[0m\n");
        System.out.println("Scegli la stanza: \n1. Cucina\n2. Camera da letto\n3. Salotto");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> future = executor.submit(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                return Integer.parseInt(reader.readLine());
            } catch (IOException | NumberFormatException e) {
                System.out.println("Errore durante la lettura dell'input. Assicurati di inserire un numero valido.");
                return -1; // Valore non valido per indicare un errore
            }
        });

        // Chiudi l'ExecutorService per evitare perdite di risorse
        executor.shutdown();

        try {
            // Attendi che l'input venga letto, con un timeout di 5 secondi (puoi impostare il timeout desiderato)
            return future.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println("Timeout o errore durante la lettura dell'input.");
            e.printStackTrace();
            return -1; // Valore non valido per indicare un errore
        }
    }

    private void airConditioning(Room room){
        // boolean choice;
        // System.out.print("\nVuoi accendere il condizionatore? (y/n)\t");
        // Scanner scanner = new Scanner(System.in);
        // String answer = scanner.next();
        // System.out.println("ANSWERRR: " + answer);
        // choice = (answer.equalsIgnoreCase("y")) ? true : false;
        // if(room.equals(Room.KITCHEN))
        //     this.kitchenHVACOn = choice;
        // else if(room.equals(Room.BEDROOM))
        //     this.bedroomHVACOn = choice;
        // return choice;

        userInputActor.tell(new SimpleMessage(0, Type.INPUT_HVAC, room), self());

        // new Thread(new Runnable() {
        //     @Override
        //     public void run() {
        //         boolean choice;

        //         try {
        //             Scanner scanner = new Scanner(System.in);
        //             System.out.println("\nVuoi accendere il condizionatore? (1 -> y / 0 -> n)");

        //             while (!scanner.hasNextInt()) {
        //                 System.out.println("Input non valido. Utilizza 1 per 'y' e 0 per 'n'.");
        //                 scanner.next();  // Consuma l'input non valido
        //             }

        //             int answer = InputReader.readInt();
        //             System.out.println("ANSWERRR: " + answer);
        //             choice = (answer == 1);

        //         } catch (Exception e) {
        //             System.out.println("Errore durante la lettura dell'input.");
        //             e.printStackTrace();
        //             choice = false;
        //         }

        //         // Usa 'choice' come necessario
        //     }
        // }).start();



    }

    private void airConditioningActivating(Room room){
        // if(room.equals(Room.KITCHEN)){
        //     boolean wantsAirConditioning = airConditioning(room);
        //     System.out.println("STO PER FARE LA SCELTA!");
        //     if(wantsAirConditioning){
        //         // kitchenHVACOn = true;
        //         this.kitchenHVACOffSelectable = true;
        //         this.kitchenDesiredTemperature = setTemperature(SET, Room.KITCHEN);
        //         this.kitchenSupervisorActor.tell(new SimpleMessage(kitchenDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.KITCHEN), self());
        //     }
        //     else{
        //         System.out.println();
        //         start();
        //     }
        // }
        // else if(room.equals(Room.BEDROOM)){
        //     boolean wantsAirConditioning = airConditioning(room);
        //     if(wantsAirConditioning){
        //         // bedroomHVACOn = true;
        //         this.bedroomHVACOffSelectable = true;
        //         this.bedroomDesiredTemperature = setTemperature(SET, Room.BEDROOM);
        //         this.bedroomSupervisorActor.tell(new SimpleMessage(bedroomDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.BEDROOM), self());
        //     }
        //     else{
        //         System.out.println();
        //         start();
        //     }
        // }
        airConditioning(room);
    }

    private void setTemperature(boolean resetting, Room room){
        this.userInputActor.tell(new SimpleMessage(resetting, Type.INPUT_TEMPERATURE, room), self());
        // if(room.equals(Room.KITCHEN)){
        //     System.out.print("Quale temperatura (°C) vuoi?\u001B[32m\t");
        //     Scanner scanner = new Scanner(System.in);
        //     this.kitchenDesiredTemperature = scanner.nextInt();
        //     System.out.println("\u001B[0m");
        //     if(resetting)
        //         this.kitchenSupervisorActor.tell(new SimpleMessage(kitchenDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.KITCHEN), self());
        //     return kitchenDesiredTemperature;
        // }
        // else if(room.equals(Room.BEDROOM)){
        //     System.out.print("Quale temperatura (°C) vuoi?\u001B[32m\t");
        //     Scanner scanner = new Scanner(System.in);
        //     this.bedroomDesiredTemperature = scanner.nextInt();
        //     System.out.println("\u001B[0m");
        //     if(resetting)
        //         this.bedroomSupervisorActor.tell(new SimpleMessage(bedroomDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.BEDROOM), self());
        //     return bedroomDesiredTemperature;
        // }
        // return 0;
    }

    private int reachedTemperature(Room room){
        if(room.equals(Room.KITCHEN)){
            System.out.println("\nL'ambiente cucina ha raggiunto la temperatura di \u001B[32m" + this.kitchenCurrentTemperature + "° C\u001B[0m. Cosa desideri fare?");
            if(kitchenHVACOn)
                System.out.println("1 -> mantenere la temperatura.\n2 -> spegnere il climatizzatore\n3 -> impostare una temperatura diversa");
            else
                System.out.println("1 -> mantenere la temperatura.\n3 -> impostare una temperatura diversa");
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            System.out.print("\nTemperatura cucina: " + this.kitchenCurrentTemperature + "° C");
            System.out.println("\tConsumo elettrico cucina: " + this.kitchenCurrentConsumption + " W");
            switch(choice){
                case 1:
                    System.out.print("Vuoi proseguire con un altro ambiente? (y/n)");
                    String choiceString = scanner.next();
                    if(choiceString.equalsIgnoreCase("y")){
                        // kitchenHVACOn = false;
                        this.workingOnKitchen = false;
                        start();
                    }
                    else{
                        System.out.println("Grazie per aver usato SMART HOME.");
                        system.terminate();
                        return 0;
                    }
                    break;
                case 2:
                    //Così era il commit prima
                    // kitchenHVACOn = false;
                    // if(!this.kitchenHVACOffSelectable){
                    //     System.out.println("Il climatizzatore è già spento");
                    //     airConditioningActivating(Room.KITCHEN);
                    // }
                    // else {
                    //     this.kitchenHVACOffSelectable = false;
                    //     this.kitchenDesiredTemperature = kitchenInitialTemperature;
                    //     this.kitchenSupervisorActor.tell(new SimpleMessage(this.kitchenInitialTemperature, Type.STOP_HVAC, Room.KITCHEN), self());
                    // }
                    if(this.kitchenHVACOn){
                        this.kitchenHVACOn = false;
                        this.kitchenDesiredTemperature = kitchenInitialTemperature;
                        this.kitchenSupervisorActor.tell(new SimpleMessage(this.kitchenInitialTemperature, Type.STOP_HVAC, Room.KITCHEN), self());
                    }
                    else{
                        System.out.println("Il climatizzatore è già spento");
                        airConditioningActivating(Room.KITCHEN);
                    }
                    break;
                case 3:
                    this.kitchenHVACOn = true;
                    setTemperature(RESET, Room.KITCHEN);
                    break;
                default:
                    break;
            }
            return choice;
        }
        else if(room.equals(Room.BEDROOM)){
            System.out.println("\nL'ambiente camera da letto ha raggiunto la temperatura di \u001B[32m" + this.bedroomCurrentTemperature + "° C\u001B[0m. Cosa desideri fare?");
            if(bedroomHVACOn)
                System.out.println("1 -> mantenere la temperatura.\n2 -> spegnere il climatizzatore\n3 -> impostare una temperatura diversa");
            else
                System.out.println("1 -> mantenere la temperatura.\n3 -> impostare una temperatura diversa");
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            System.out.print("\nTemperatura camera da letto: " + this.bedroomCurrentTemperature + "° C");
            System.out.println("\tConsumo elettrico camera da letto: " + this.bedroomCurrentConsumption + " W");
            switch(choice){
                case 1:
                    System.out.println("Vuoi proseguire con un altro ambiente? (y/n)");
                    String choiceString = scanner.next();
                    if(choiceString.equalsIgnoreCase("y")){
                        this.workingOnBedroom = false;
                        // bedroomHVACOn = false;
                        start();
                    }
                    else{
                        System.out.println("Grazie per aver usato SMART HOME.");
                        system.terminate();
                        return 0;
                    }
                    break;
                case 2:
                    //Commit precedente
                    // bedroomHVACOn = false;
                    // if(!this.bedroomHVACOffSelectable){
                    //     System.out.println("Il climatizzatore è già spento");
                    //     airConditioningActivating(Room.BEDROOM);
                    // }
                    // else {
                    //     this.bedroomHVACOffSelectable = false;
                    //     this.bedroomDesiredTemperature = bedroomInitialTemperature;
                    //     this.bedroomSupervisorActor.tell(new SimpleMessage(this.bedroomInitialTemperature, Type.STOP_HVAC, Room.BEDROOM), self());
                    // }
                    if(this.bedroomHVACOn){
                        this.bedroomHVACOn = false;
                        this.bedroomDesiredTemperature = bedroomInitialTemperature;
                        this.bedroomSupervisorActor.tell(new SimpleMessage(this.bedroomInitialTemperature, Type.STOP_HVAC, Room.BEDROOM), self());
                    }
                    else{
                        System.out.println("Il climatizzatore è già spento");
                        airConditioningActivating(Room.BEDROOM);
                    }
                    break;
                case 3:
                    this.bedroomHVACOn = true;
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
