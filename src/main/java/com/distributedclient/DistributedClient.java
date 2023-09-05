package com.distributedclient;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;


import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DistributedClient{

	public static void main(String[] args){

		Config clientConfig = ConfigFactory.parseFile(new File("client-conf"));
		ActorSystem sys = ActorSystem.create("Client", clientConfig);
		ActorRef supervisor = sys.actorOf(ClientSupervisorActor.props(), "ClientSupervisorActor");

		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
		scala.concurrent.Future<Object> waitingForClientActor = ask(supervisor, Props.create(ClientActor.class), 5000);
		
		try {
			ActorRef clientActor = (ActorRef) waitingForClientActor.result(timeout, null);

			String serverAddr = "akka.tcp://Server@127.0.0.1:6123/user/serverActor";

			clientActor.tell(new SimpleMessage(serverAddr), ActorRef.noSender());

			System.in.read();

		} catch (TimeoutException | InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		sys.terminate();
	}

}
