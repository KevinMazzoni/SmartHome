package com.simpleenvironment;

import java.io.Serializable;

public class TemperatureMessage implements Serializable {
    private double temperature;

    public TemperatureMessage(double temperature){
        this.temperature = temperature;
    }

    public double getTemperature(){
        return this.temperature;
    }
}
