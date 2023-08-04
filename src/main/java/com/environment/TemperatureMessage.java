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

	public TemperatureMessage(int messageType, double temperature) {
		this.temperature = temperature;
		this.messageType = messageType;
	}

	public TemperatureMessage(int messageType) {
		this.temperature = 0;
		this.messageType = messageType;
	}
	
}
