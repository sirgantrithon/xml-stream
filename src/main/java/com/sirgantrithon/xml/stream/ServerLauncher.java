package com.sirgantrithon.xml.stream;

import org.vertx.java.platform.Verticle;

public class ServerLauncher extends Verticle {
	
	public void start() {
		container.deployVerticle("com.sirgantrithon.xml.stream.XMLStreamServer");
	}

}
