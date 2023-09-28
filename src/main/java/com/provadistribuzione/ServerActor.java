package com.provadistribuzione;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.File;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;

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

        // return receiveBuilder()
        //         .matchAny(message -> {
        //             if (message instanceof String) {
        //                 String content = (String) message;
        //                 System.out.println("Server ha ricevuto il messaggio: " + content);
        //             } else {
        //                 System.out.println("UNHANDLEDDDDDDDDDDDDDDDD!!!!!!");
        //                 unhandled(message);
        //             }
        //         })
        //         .build();

    }

    public static void main(String[] args) {
        
        //Qui valido
        System.out.println("Il ServerActor sta eseguendo il main");
        System.out.println("PORTAAAAAAA: " + System.getenv("PORT"));
        
        
        //Qui non valido
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
        
        //Qui valido
        Config config = ConfigFactory.load();
        ActorSystem system = ActorSystem.create("Server", config);
        system.actorOf(ServerActor.props(), "ServerActor");
        System.out.println("Il ServerActor ha terminato di eseguire il main");

        //Una prova
        // System.out.println("Il ServerActor sta eseguendo il main");

        // System.out.println("PORTAAAAAAA: " + System.getenv("PORT"));
        // Config config = ConfigFactory.load();
        // ActorSystem system = ActorSystem.create("Server", config);
        // system.actorOf(ServerActor.props(), "ServerActor");

        // // Tentativo di connessione al client
        // ActorSelection clientSelection = system.actorSelection("akka://ClientSystem@127.0.0.1:2552/user/ClientActor");

        // int maxRetries = 10; // Numero massimo di tentativi
        // int retries = 0;
        // boolean connected = false;

        // while (retries < maxRetries && !connected) {
        //     try {
        //         clientSelection.resolveOne(Duration.create(5, TimeUnit.SECONDS)).toCompletableFuture().get();
        //         connected = true;
        //         System.out.println("Connessione al client riuscita!");
        //     } catch (Exception e) {
        //         retries++;
        //         System.out.println("Tentativo di connessione al client #" + retries + " fallito. Riproverà tra 5 secondi...");
        //         try {
        //             Thread.sleep(5000); // Attendere per 5 secondi prima di un nuovo tentativo
        //         } catch (InterruptedException ex) {
        //             Thread.currentThread().interrupt();
        //         }
        //     }
        // }

        // if (!connected) {
        //     System.out.println("Impossibile connettersi al client dopo " + maxRetries + " tentativi. Uscita.");
        //     system.terminate();
        //     return;
        // }

        // System.out.println("Il ServerActor ha terminato di eseguire il main");

        //Un'altra prova
        // System.out.println("Il ServerActor sta eseguendo il main");

        // System.out.println("PORTAAAAAAA: " + System.getenv("PORT"));
        // Config config = ConfigFactory.load();
        // ActorSystem system = ActorSystem.create("Server", config);
        // system.actorOf(ServerActor.props(), "ServerActor");

        // System.out.println("Il ServerActor ha terminato di eseguire il main");

    }

    static Props props() {
		return Props.create(ServerActor.class);
	}
}
