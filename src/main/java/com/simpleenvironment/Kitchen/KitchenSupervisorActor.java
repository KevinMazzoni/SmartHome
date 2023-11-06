package com.simpleenvironment.Kitchen;

import java.time.Duration;

import com.simpleenvironment.Messages.TemperatureMessage;
import com.simpleenvironment.Messages.Appliance;
import com.simpleenvironment.Messages.Room;
import com.simpleenvironment.Messages.SimpleMessage;
import com.simpleenvironment.Messages.Type;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

public class KitchenSupervisorActor extends AbstractActor {

	private ActorRef kitchenTemperatureSensorActor;
	private ActorRef kitchenHVACActor;

	private ActorSelection controlPanelActor;
	private boolean temperatureSensorOn;
	private boolean HVACOn;

	private static final boolean ON = true;
	private static final boolean OFF = false;

	private int sensorConsumption;
	private int HVACConsumption;

     // #strategy
    private static SupervisorStrategy strategy =
        new OneForOneStrategy(
            1, // Max no of retries
            Duration.ofMinutes(1), // Within what time period
            DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
                .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
      return strategy;
    }

	public KitchenSupervisorActor() {
		this.temperatureSensorOn = OFF;
		this.HVACOn = OFF;
		this.sensorConsumption = 0;
		this.HVACConsumption = 0;
	}

	@Override
	public Receive createReceive() {
		// Creates the child actor within the supervisor actor context
		return receiveBuilder()
		          	.match(
		            	Props.class,
		              	props -> {
		                	getSender().tell(getContext().actorOf(props), getSelf());
		             	})
					// .match(TemperatureMessage.class, this::onTemperatureMessage)
					.match(SimpleMessage.class, this::onSimpleMessage)
					.match(TemperatureMessage.class, this::onTemperatureMessage)
				  	.match(String.class, message -> {
                    	System.out.println("KitchenSupervisorActor ha ricevuto il messaggio: " + message);
                	})
		          	.build();
	}

	void onTemperatureMessage(TemperatureMessage msg){
		System.out.println("Sono il kitchenSupervisorActor! Ho ricevuto un TemperatureMessage con temperatura: " + msg.getTemperature());
		// System.out.println("temperature sensor on: " + this.temperatureSensorOn);
		// System.out.println("HVAC on: " /*+ this.HVACOn*/);
		if(!temperatureSensorOn && msg.getAppliance().equals(Appliance.TEMPERATURE_SENSOR)){
			this.temperatureSensorOn = ON;
			this.sensorConsumption = msg.getEnergyConsumption();
		}
		if(/*!HVACOn && */msg.getAppliance().equals(Appliance.HVAC)){
			// this.HVACOn = ON;
			this.HVACConsumption = msg.getEnergyConsumption();
			this.kitchenTemperatureSensorActor.tell(new TemperatureMessage(msg.getTemperature(), this.HVACConsumption, Room.KITCHEN, Appliance.HVAC, true), self());
		}
		if(msg.getAppliance().equals(Appliance.TEMPERATURE_SENSOR)){
			TemperatureMessage toSend = new TemperatureMessage(msg.getTemperature(), (this.sensorConsumption + this.HVACConsumption), Room.KITCHEN, Appliance.KITCHEN_SUPERVISOR, msg.isFirstMeasure());
			controlPanelActor.tell(toSend, self());
		}
	}

	void onSimpleMessage(SimpleMessage msg) throws Exception {

		switch(msg.getType()){
			case INFO:
				System.out.println("Ho ricevuto un INFO message");
				break;
			case INFO_CHILD:
				if(msg.getAppliance().equals(Appliance.TEMPERATURE_SENSOR)){
					this.kitchenTemperatureSensorActor = msg.getChildActor();
					System.out.println("Sono il KitchenSupervisorActor, setto il child kitchenTemperatureSensorActor a: " + this.kitchenTemperatureSensorActor);
				}
				if(msg.getAppliance().equals(Appliance.HVAC)){
					this.kitchenHVACActor = msg.getChildActor();
					System.out.println("Sono il KitchenSupervisorActor, setto il child kitchenHVACActor a: " + this.kitchenHVACActor);
				}
				break;
			case INFO_CONTROLPANEL:
				this.controlPanelActor = msg.getControlPanelRef();
				break;
			case INFO_TEMPERATURE:
				this.kitchenTemperatureSensorActor.tell(new SimpleMessage("Prova tell", Type.INFO_TEMPERATURE), self());
				break;
			case DESIRED_TEMPERATURE:
				// this.HVACOn = false;
				//Ripartire da qui, gestire un DESIRED_TEMPERATURE da parte del HVACActor
				System.out.println("Sono il KitchenSupervisorActor, ho ricevuto un DESIRED TEMPERATURE a " + msg.getDesiredTemperature());
				this.kitchenHVACActor.tell(msg, kitchenHVACActor);
				//Qui magari mandare un messaggio al ControlPanelActor al corretto avvio del HVAC
				
				// this.kitchenTemperatureSensorActor.tell(new TemperatureMessage(msg.getDesiredTemperature(), this.HVACConsumption, Room.KITCHEN, Appliance.HVAC, true), self());
				
				// this.kitchenTemperatureSensorActor.tell(new SimpleMessage("Prova invio Simple", Type.INFO), self());
				// this.kitchenTemperatureSensorActor.tell(new TemperatureMessage(msg.getDesiredTemperature(), this.HVACConsumption, Room.KITCHEN, Appliance.HVAC, true), self());
				break;
			case STOP_HVAC:
				// this.HVACOn = false;
				this.kitchenHVACActor.tell(msg, self());
				// this.kitchenTemperatureSensorActor.tell(new TemperatureMessage(msg.getDesiredTemperature(), this.HVACConsumption, Room.KITCHEN, Appliance.HVAC, true), self());
				break;
			default:
				break;
		}

	}

	static Props props() {
		return Props.create(KitchenSupervisorActor.class);
	}
}
