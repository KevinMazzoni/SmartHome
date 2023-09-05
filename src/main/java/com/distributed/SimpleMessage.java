package com.distributed;

public class SimpleMessage {
    private String serverAddr;

    public SimpleMessage(String serverAddr){
        this.serverAddr = serverAddr;
    }

    public String getServerAddr(){
        return serverAddr;
    }
}
