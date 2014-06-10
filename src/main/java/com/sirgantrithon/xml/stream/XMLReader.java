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
	private Handler<Buffer> output;
	private AsyncXMLStreamReader asyncReader;
	private AsyncInputFeeder feeder;

	public XMLReader(Handler<Buffer> outputHandler) {
		this.output = outputHandler;
		this.asyncReader = FACTORY.createAsyncXMLStreamReader();
		this.feeder = asyncReader.getInputFeeder();
	}

	@Override
	public void handle(Buffer buffer) {
		int type = 0;
		boolean feederFed = false;

		do {
			try {
				while ((type = asyncReader.next()) == AsyncXMLStreamReader.EVENT_INCOMPLETE) {

					// If we have already fed the buffer to the feeder, we have processed all of the data we have
					// available, so return

					if (feederFed) {
						return;
					}

					feeder.feedInput(buffer.getBytes(), 0, buffer.length());
					feederFed = true;
				}
			} catch (XMLStreamException e) {
				throw new RuntimeException("Exception while parsing XML", e);
			}

			switch (type) {
			case XMLStreamConstants.ATTRIBUTE:
				output.handle(new Buffer("ATTRIBUTE"));
				break;
			case XMLStreamConstants.CDATA:
				output.handle(new Buffer("CDATA: " + asyncReader.getText()));
				break;
			case XMLStreamConstants.CHARACTERS:
				output.handle(new Buffer("CHARACTERS: " + asyncReader.getText()));
				break;
			case XMLStreamConstants.COMMENT:
				output.handle(new Buffer("COMMENT"));
				break;
			case XMLStreamConstants.DTD:
				output.handle(new Buffer("DTD"));
				break;
			case XMLStreamConstants.END_DOCUMENT:
				output.handle(new Buffer("END_DOCUMENT"));
				break;
			case XMLStreamConstants.END_ELEMENT:
				output.handle(new Buffer("END_ELEMENT"));
				break;
			case XMLStreamConstants.ENTITY_DECLARATION:
				output.handle(new Buffer("ENTITY_DECLARATION"));
				break;
			case XMLStreamConstants.ENTITY_REFERENCE:
				output.handle(new Buffer("ENTITY_REFERENCE"));
				break;
			case XMLStreamConstants.NAMESPACE:
				output.handle(new Buffer("NAMESPACE"));
				break;
			case XMLStreamConstants.NOTATION_DECLARATION:
				output.handle(new Buffer("NOTATION_DECLARATION"));
				break;
			case XMLStreamConstants.PROCESSING_INSTRUCTION:
				output.handle(new Buffer("PROCESSING_INSTRUCTION"));
				break;
			case XMLStreamConstants.SPACE:
				output.handle(new Buffer("SPACE"));
				break;
			case XMLStreamConstants.START_DOCUMENT:
				output.handle(new Buffer("START_DOCUMENT"));
				break;
			case XMLStreamConstants.START_ELEMENT:
				output.handle(new Buffer("START_ELEMENT, attributes: " + asyncReader.getAttributeCount()));
				break;
			default:
				output.handle(new Buffer("Unknown Event: " + type));
				break;
			}

		} while (type != XMLStreamConstants.END_DOCUMENT);

		output.handle(new Buffer("END_DOCUMENT out of the loop"));

	}

	public void endParsing() {
		feeder.endOfInput();
		try {
			asyncReader.close();
		} catch (XMLStreamException e) {
		}
	}
}