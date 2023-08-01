package com.environment;

public class TemperatureMessage {

	private double temperature;
	private int messageType;
	
	public double getTemperature() {
		return temperature;
	}

	public int getType() {
		return messageType;
	}

	public TemperatureMessage(double temperature, int messageType) {
		this.temperature = temperature;
		this.messageType = messageType;
	}
	
}
