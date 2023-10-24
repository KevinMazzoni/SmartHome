package com.simpleenvironment;

import java.io.Serializable;

public class SimpleMessage implements Serializable {
    
    private String message;
    private Type type;

    public SimpleMessage(String message, Type type){
        this.message = message;
        this.type = type;
    }

    public String getMessage(){
        return this.message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public Type getType(){
        return this.type;
    }
}
