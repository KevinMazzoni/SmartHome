package com.provadistribuzione;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.File;

import com.environment.HVACActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ServerActor extends AbstractActor {
    // public static final String stringConfig = "akka { actor { provider = 'akka.remote.RemoteActorRefProvider'} remote {enabled-transports = ['akka.remote.netty.tcp'] netty.tcp {hostname = '127.0.0.1' port = 6123}}}";

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    System.out.println("Server ha ricevuto il messaggio: " + message);
                })
                .build();
    }

    public static void main(String[] args) {
        System.out.println("Il ServerActor sta eseguendo il main");
        // Config config = ConfigFactory.load("application.conf");
        // File conf = new File("application.conf");
        // Config config = ConfigFactory.parseFile(conf);
        // Config config = ConfigFactory.load();
        // Config config = ConfigFactory.load("kghgchchjfc");
        // String configString = config.getString("application.conf");
        // boolean isResolved = config.isResolved();
        // boolean fileIsPresent = config.hasPath("./provadistribuzione");
        // System.out.println("File is present at that path: " + " ConfigString is: " + " isResolved: " + isResolved);
        // Config config = ConfigFactory.parseFile(new File("./application.conf"));
        // File applicationFile = new File("src/main/java/com/provadistribuzione/application.conf");
        // Config config = ConfigFactory.parseFile(applicationFile);
        // System.out.println("L'application.conf file è: " + applicationFile.getName() + " il path è: " + applicationFile.getAbsolutePath());
        Config config = ConfigFactory.load();
        ActorSystem system = ActorSystem.create("Server", config);
        system.actorOf(ServerActor.props(), "ServerActor");

        System.out.println("Il ServerActor ha terminato di eseguire il main");
    }

    static Props props() {
		return Props.create(ServerActor.class);
	}
}
