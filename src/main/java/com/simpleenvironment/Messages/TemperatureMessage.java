package com.simpleenvironment.Messages;

import java.io.Serializable;

public class TemperatureMessage implements Serializable {
    private int temperature;
    private Room room;
    private boolean isFirstMeasure;

    public TemperatureMessage(int temperature, Room room, boolean isFirstMeasure){
        this.temperature = temperature;
        this.room = room;
        this.isFirstMeasure = isFirstMeasure;
    }

    public int getTemperature(){
        return this.temperature;
    }

    public Room getRoom(){
        return this.room;
    }

    public boolean isFirstMeasure(){
        return isFirstMeasure;
    }

}
