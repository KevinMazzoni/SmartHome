package com.simpleenvironment.ControlPanel;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class ServerActor extends AbstractActor {

    private static boolean isClientRunning = false;

    public ServerActor(){
        
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    System.out.println("Server ha ricevuto il messaggio: " + message);
                })
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

    static Props props() {
        return Props.create(ServerActor.class);
    }
}
