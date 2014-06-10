package com.sirgantrithon.xml.stream;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

import com.fasterxml.aalto.AsyncInputFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

public class XMLReader implements Handler<Buffer> {

	private static final InputFactoryImpl FACTORY = new InputFactoryImpl();
	private Buffer buff;
	private Handler<Buffer> output;
	private AsyncXMLStreamReader asyncReader;
	private AsyncInputFeeder feeder;

	public XMLReader(Handler<Buffer> outputHandler) {
		this.output = outputHandler;
		this.asyncReader = FACTORY.createAsyncXMLStreamReader();
		this.feeder = asyncReader.getInputFeeder();
	}

	private void parseXml() {
		int type = 0;
		boolean feederFed = false;

		try {
			do {
				while ((type = asyncReader.next()) == AsyncXMLStreamReader.EVENT_INCOMPLETE) {

					if (feederFed) {
						buff = null;
						return;
					}

					feeder.feedInput(buff.getBytes(), 0, buff.length());
					feederFed = true;
				}

				Buffer ret = new Buffer();

				/*
				 * Should return JSonObject of events here...
				 */
				switch (type) {
				case XMLStreamConstants.START_ELEMENT:
					ret.appendString(asyncReader.getLocalName());
					break;
				default:
					ret.appendString("Event: " + type);
					break;
				}

				output.handle(ret);

			} while (type != XMLStreamConstants.END_DOCUMENT);
		} catch (XMLStreamException ex) {
			Buffer ret = new Buffer();
			ret.appendString(ex.toString());
			output.handle(ret);
		}
	}

	@Override
	public void handle(Buffer buffer) {

		if (buff == null) {
			buff = buffer;
		} else {
			buff.appendBuffer(buffer);
		}

		parseXml();
	}

	public void endParsing() {
		feeder.endOfInput();
		try {
			asyncReader.close();
		} catch (XMLStreamException e) {
		}
	}
}