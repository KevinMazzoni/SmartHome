package com.simpleenvironment.Messages;

import java.io.Serializable;

public class TemperatureMessage implements Serializable {
    private int temperature;
    private int energyConsumption;
    private Room room;
    private boolean isFirstMeasure;

    public TemperatureMessage(int temperature, int energyConsumption, Room room, boolean isFirstMeasure){
        this.temperature = temperature;
        this.energyConsumption = energyConsumption;
        this.room = room;
        this.isFirstMeasure = isFirstMeasure;
    }

    public int getTemperature(){
        return this.temperature;
    }

    public Room getRoom(){
        return this.room;
    }

    public int getEnergyConsumption(){
        return this.energyConsumption;
    }

    public boolean isFirstMeasure(){
        return isFirstMeasure;
    }

}
