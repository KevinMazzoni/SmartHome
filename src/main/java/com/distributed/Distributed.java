package com.distributed;

import java.io.File;
import java.util.concurrent.TimeoutException;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Distributed {

	public static void main(String[] args){
		// Config serverConfig = ConfigFactory.parseFile(new File("conf"));
		Config customConfig = ConfigFactory.load("serverconf.conf");
		ActorSystem sys = ActorSystem.create("Server", customConfig);
		
		// ActorRef supervisor = sys.actorOf(ServerActor.props(), "ServerActor");
		ActorRef supervisor = sys.actorOf(Props.create(ServerActor.class), "ServerActor");

		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
		scala.concurrent.Future<Object> waitingForControlPanel = ask(supervisor, Props.create(ControlPanelActor.class), 5000);
		
		try {
			ActorRef controlPanel = (ActorRef) waitingForControlPanel.result(timeout, null);

		} catch (TimeoutException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
