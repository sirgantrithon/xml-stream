package com.sirgantrithon.xml.stream;

import java.util.concurrent.atomic.AtomicInteger;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;

public class XMLClient extends Verticle {

	public void start() {

		HttpServer server = vertx.createHttpServer();
		server.setCompressionSupported(true);

		server.requestHandler(new Handler<HttpServerRequest>() {

			@Override
			public void handle(final HttpServerRequest request) {
				request.response().setChunked(true);
				HttpClient client = vertx.createHttpClient();
				client.setPort(80);
				client.setHost("feeds.arstechnica.com");
				client.getNow("/arstechnica/index?format=xml", new Handler<HttpClientResponse>() {

					@Override
					public void handle(final HttpClientResponse response) {

						final AtomicInteger packetCounter = new AtomicInteger(0);

						final XMLReader xmlReader = new XMLReader(new Handler<Buffer>() {

							@Override
							public void handle(Buffer buffer) {
								request.response().write(buffer + "\n");
								packetCounter.incrementAndGet();
							}

						});
						response.dataHandler(xmlReader).endHandler(new Handler<Void>() {

							@Override
							public void handle(Void nothing) {
								System.out.println("Packets recieved: " + packetCounter.intValue());
								xmlReader.endParsing();
								request.response().end();
							}
						});
					}
				});
			}

		}).listen(8090);
	}
}
