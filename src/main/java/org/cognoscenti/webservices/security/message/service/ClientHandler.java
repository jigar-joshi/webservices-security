package org.cognoscenti.webservices.security.message.service;

import java.util.Set;
import java.util.HashSet;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.InputStream;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessorFactory;
import com.sun.xml.wss.XWSSProcessor;
import com.sun.xml.wss.XWSSecurityException;

public class ClientHandler implements SOAPHandler<SOAPMessageContext> {
	
	private XWSSProcessor xwssProcessor;

	public ClientHandler() {
		
		XWSSProcessorFactory xwssProcessorFactory = null;
		
		try {
			
			xwssProcessorFactory = XWSSProcessorFactory.newInstance();
			
		} catch (XWSSecurityException e) { throw new RuntimeException(e); }
		
		try {
			
			InputStream config = getClass().getClassLoader().getResourceAsStream("META-INF/client.xml");
			xwssProcessor = xwssProcessorFactory.createProcessorForSecurityConfiguration(config, new Prompter());
			config.close();
			
		} catch (Exception e) { throw new RuntimeException(e); }
		
	}
	
	public Set<QName> getHeaders() {
		
		String uri = "http://docs.oasis-open.org/wss/2004/01/" + 
					 "oasis-200401-wss-wssecurity-secext-1.0.xsd";
		
		QName securityHeader = new QName(uri, "Security", "wsse");
		HashSet<QName> headers = new HashSet<QName>();
		headers.add(securityHeader);
		
		return headers;
		
	}

	public boolean handleMessage(SOAPMessageContext soapMessageContext) {
		
		Boolean outboundProperty = (Boolean) soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		SOAPMessage soapMessage = soapMessageContext.getMessage();

		if (outboundProperty.booleanValue()) {
			
			ProcessingContext processingContext = null;
			
			try {
				
				processingContext = xwssProcessor.createProcessingContext(soapMessage);
				processingContext.setSOAPMessage(soapMessage);
				SOAPMessage secureMessage = xwssProcessor.secureOutboundMessage(processingContext);
				soapMessageContext.setMessage(secureMessage);
				
			} catch (XWSSecurityException e) { throw new RuntimeException(e); }
			
		}
		
		return true;
		
	}

	public boolean handleFault(SOAPMessageContext soapMessageContext) { return true; }

	public void close(MessageContext soapMessageContext) { }

}
