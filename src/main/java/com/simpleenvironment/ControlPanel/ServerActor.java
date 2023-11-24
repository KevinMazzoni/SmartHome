package com.simpleenvironment.ControlPanel;

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
        // userInputActor = getContext().actorOf(UserInputActor.props(), "userInputActor");
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
        // System.out.println("MEssage type: " + messageType);
        if(msg.getType().equals(Type.INFO_ACTOR_SYSTEM))
            this.system = msg.getActorSystem();
        if(msg.getType().equals(Type.INFO_CONTROLPANEL))
            this.controlPanelActor = msg.getControlPanelRef();
        if(msg.getType().equals(Type.INFO_USER_INPUT))
            this.userInputActor = msg.getSiblingActor();
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
        if(msg.getType().equals(Type.INPUT_REACHED_TEMPERATURE))
            inputReachedTemperatureReceived(msg);
        if(msg.getType().equals(Type.INPUT_CONTINUE))
            inputContinueReceived(msg);
        if(msg.getType().equals(Type.KITCHEN_ON)){
            // System.out.println("Kitchen is ON!!!");
            this.kitchenRunning = true;
        }
        if(msg.getType().equals(Type.BEDROOM_ON)){
            // System.out.println("Bedroom is ON!!!");
            this.bedroomRunning = true;
        }
        if(msg.getType().equals(Type.KITCHEN_OFF)){
            // System.out.println("Kitchen is OFF!!!");
            this.kitchenRunning = false;
            this.kitchenCurrentConsumption = 0;
            this.kitchenHVACOn = false;
            this.kitchenEnvironment = true;
            if(workingOnKitchen){
                // System.out.println("\u001B[31mLa cucina ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
                this.workingOnKitchen = false;
                start();
            }
        }
        if(msg.getType().equals(Type.BEDROOM_OFF)){
            // System.out.println("Bedroom is OFF!!!");
            this.bedroomRunning = false;
            this.bedroomCurrentConsumption = 0;
            this.bedroomHVACOn = false;
            this.bedroomEnvironment = true;
            if(workingOnBedroom){
                // System.out.println("\u001B[31mLa camera da letto ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
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
        
        if(environment == KITCHEN && this.kitchenRunning){
            this.workingOnKitchen = true;
            kitchenSupervisorActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2553/user/KitchenSupervisorActor");
            kitchenSupervisorActor.tell(new SimpleMessage("INFO_TEMPERATURE", Type.INFO_TEMPERATURE), ActorRef.noSender());
        }

        if(environment == BEDROOM && this.bedroomRunning){
            this.workingOnBedroom = true;
            bedroomSupervisorActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2554/user/BedroomSupervisorActor");
            bedroomSupervisorActor.tell(new SimpleMessage("INFO_TEMPERATURE", Type.INFO_TEMPERATURE), ActorRef.noSender());
        }

        if(environment != KITCHEN && environment != BEDROOM){
            System.out.println("Seleziona un ambiente valido per favore.");
            start();
        }
    }

    private void inputHVACReceived(SimpleMessage msg){
        // System.out.println("INPUT_HVAC Received");
        boolean wantsAirConditioning = msg.getHVACChoice();

        if(msg.getRoom().equals(Room.KITCHEN)){
            if(!this.kitchenRunning){
                System.out.println("\u001B[31mLa cucina ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
                return;
            }
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
            if(!this.bedroomRunning){
                System.out.println("\u001B[31mLa camera da letto ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
                return;
            }
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
            if(!this.kitchenRunning){
                System.out.println("\u001B[31mLa cucina ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
                return;
            }
            if(msg.getResettingChoice())
                this.kitchenSupervisorActor.tell(new SimpleMessage(kitchenDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.KITCHEN), self());
            this.kitchenDesiredTemperature = msg.getDesiredTemperature();
            
            //Dubbio questo sotto
            if(this.kitchenDesiredTemperature == this.kitchenCurrentTemperature){
                //Gestisco caso limite in cui venga impostata una temperatura al climatizzatore == temperatura iniziale 
                // if(kitchenHVACOn && this.kitchenInitialTemperature == this.kitchenDesiredTemperature)
                //     kitchenHVACOn = false;
                // else if (this.kitchenInitialTemperature == this.kitchenDesiredTemperature)
                //     kitchenHVACOn = true;
                if(this.kitchenInitialTemperature == this.kitchenDesiredTemperature)
                    kitchenHVACOn = false;
                else
                    kitchenHVACOn = true;

                this.userInputActor.tell(new SimpleMessage(Room.KITCHEN, this.kitchenCurrentTemperature, this.kitchenCurrentConsumption, this.kitchenHVACOn, Type.INPUT_REACHED_TEMPERATURE), self());
            }
            else{
                this.kitchenHVACOn = true;
                this.kitchenSupervisorActor.tell(new SimpleMessage(kitchenDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.KITCHEN), self());
            }
        }
        else if(msg.getRoom().equals(Room.BEDROOM)){
            if(!this.bedroomRunning){
                System.out.println("\u001B[31mLa camera da letto ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
                return;
            }
            if(msg.getResettingChoice())
                this.bedroomSupervisorActor.tell(new SimpleMessage(bedroomDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.BEDROOM), self());
            this.bedroomDesiredTemperature = msg.getDesiredTemperature();
            
            //Dubbio questo sotto
            if(this.bedroomDesiredTemperature == this.bedroomCurrentTemperature){
                if((this.bedroomInitialTemperature == this.bedroomDesiredTemperature))
                    this.bedroomHVACOn = false;
                else
                    this.bedroomHVACOn = true;

                this.userInputActor.tell(new SimpleMessage(Room.BEDROOM, this.bedroomCurrentTemperature, this.bedroomCurrentConsumption, this.bedroomHVACOn, Type.INPUT_REACHED_TEMPERATURE), self());
            }
            else{
                this.bedroomHVACOn = true;
                this.bedroomSupervisorActor.tell(new SimpleMessage(bedroomDesiredTemperature, Type.DESIRED_TEMPERATURE, Room.BEDROOM), self());
            }
        }
        // return 0;
    }

    private void inputReachedTemperatureReceived(SimpleMessage msg){
        if(msg.getRoom().equals(Room.KITCHEN)){
            if(!this.kitchenRunning){
                System.out.println("\u001B[31mLa cucina ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
                return;
            }
            switch(msg.getChoice()){
                case 1:
                    this.userInputActor.tell(new SimpleMessage("INPUT_CONTINUE", Type.INPUT_CONTINUE, Room.KITCHEN), self());
                    break;
                case 2:
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
                    // this.kitchenHVACOn = true;
                    setTemperature(RESET, Room.KITCHEN);
                    break;
                default:
                    break;
            }
        }
        else if(msg.getRoom().equals(Room.BEDROOM)){
            if(!this.bedroomRunning){
                System.out.println("\u001B[31mLa camera da letto ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
                return;
            }
            switch(msg.getChoice()){
                case 1:
                    this.userInputActor.tell(new SimpleMessage("INPUT_CONTINUE", Type.INPUT_CONTINUE, Room.BEDROOM), self());
                    break;
                case 2:
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
                    // this.bedroomHVACOn = true;
                    setTemperature(RESET, Room.BEDROOM);
                    break;
                default:
                    break;
            }
        }
    }

    private void inputContinueReceived(SimpleMessage msg){
        if(msg.getRoom().equals(Room.KITCHEN)){
            if(!this.kitchenRunning){
                System.out.println("\u001B[31mLa cucina ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
                return;
            }
            if(msg.getStringChoice().equalsIgnoreCase("y")){
                // kitchenHVACOn = false;
                this.workingOnKitchen = false;
                start();
            }
            else{
                System.out.println("Grazie per aver usato SMART HOME.");
                system.terminate();
                return;
            }
        }
        else if(msg.getRoom().equals(Room.BEDROOM)){
            if(!this.bedroomRunning){
                System.out.println("\u001B[31mLa camera da letto ha smesso improvvisamente di funzionare! Prova con un altro ambiente\u001B[0m");
                return;
            }
            if(msg.getStringChoice().equalsIgnoreCase("y")){
                // kitchenHVACOn = false;
                this.workingOnBedroom = false;
                start();
            }
            else{
                System.out.println("Grazie per aver usato SMART HOME.");
                system.terminate();
                return;
            }
        }
    }

    private void start() {
        this.userInputActor.tell(new SimpleMessage((this.bedroomCurrentConsumption + this.kitchenCurrentConsumption), Type.INPUT_ENVIRONMENT), self());
    }

    private void airConditioning(Room room){
        userInputActor.tell(new SimpleMessage(0, Type.INPUT_HVAC, room), self());
    }

    private void airConditioningActivating(Room room){
        airConditioning(room);
    }

    private void setTemperature(boolean resetting, Room room){
        this.userInputActor.tell(new SimpleMessage(resetting, Type.INPUT_TEMPERATURE, room), self());
    }

    private int reachedTemperature(Room room){
        if(room.equals(Room.KITCHEN)){
            this.userInputActor.tell(new SimpleMessage(room, this.kitchenCurrentTemperature, this.kitchenCurrentConsumption, this.kitchenHVACOn, Type.INPUT_REACHED_TEMPERATURE), self());
        }
        else if(room.equals(Room.BEDROOM)){
            this.userInputActor.tell(new SimpleMessage(room, this.bedroomCurrentTemperature, this.bedroomCurrentConsumption, this.bedroomHVACOn, Type.INPUT_REACHED_TEMPERATURE), self());
        }
        return 0;
    }
    
    static Props props() {
        return Props.create(ServerActor.class);
    }
}
