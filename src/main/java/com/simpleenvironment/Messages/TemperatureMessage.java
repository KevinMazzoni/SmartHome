package com.simpleenvironment.Messages;

import java.io.Serializable;

public class TemperatureMessage implements Serializable {
    private int temperature;

    public TemperatureMessage(int temperature){
        this.temperature = temperature;
    }

    public int getTemperature(){
        return this.temperature;
    }

}
