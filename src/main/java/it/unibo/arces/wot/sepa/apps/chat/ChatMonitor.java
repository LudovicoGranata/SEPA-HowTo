package it.unibo.arces.wot.sepa.apps.chat;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.api.ISubscriptionHandler;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.response.Notification;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.pattern.GenericClient;

public class ChatMonitor extends GenericClient implements ISubscriptionHandler {
	protected static final Logger logger = LogManager.getLogger();
	
	private final int totalMessages;
	private int sent = 0;
	private int received = 0;
	private int removed = 0;
	
	private HashMap<String,String> aliases = new HashMap<String,String>();
	
	public ChatMonitor(int users,int messages) throws SEPAProtocolException, SEPAPropertiesException, SEPASecurityException, SEPABindingsException {
		super(new ConfigurationProvider().getJsap(), new ConfigurationProvider().getSecurityManager());
	
		totalMessages = users * (users -1) * messages;
		
		subscribe("SENT", null, this, 5000, "sent");
		subscribe("RECEIVED", null, this, 5000, "received");
	}
	
	public void monitor() throws InterruptedException {
		while(sent < totalMessages || received < totalMessages || removed < totalMessages) {
			synchronized (this) {
				wait();
			}
		}
	}

	@Override
	public void onSemanticEvent(Notification notify) {
		if(aliases.get(notify.getSpuid()).equals("sent")) {
			for(Bindings bindings : notify.getARBindingsResults().getRemovedBindings().getBindings()) {
				removed++;
				logger.info("Message removed: "+bindings.getValue("message")+" ("+removed+"/"+totalMessages+")");
			}
			for(Bindings bindings : notify.getARBindingsResults().getAddedBindings().getBindings()) {
				sent++;
				logger.info("Message sent: "+bindings.getValue("message")+" ("+sent+"/"+totalMessages+")");
			}
		}
		else {
			for(Bindings bindings : notify.getARBindingsResults().getRemovedBindings().getBindings()) {
				received++;
				logger.info("Message received: "+bindings.getValue("message")+" ("+received+"/"+totalMessages+")");
			}
		}
		
		synchronized (this) {
			notify();
		}
	}

	@Override
	public void onBrokenConnection() {
		logger.warn("Broken connection");
	}

	@Override
	public void onError(ErrorResponse errorResponse) {
		logger.error(errorResponse);
	}

	@Override
	public void onSubscribe(String spuid, String alias) {
		aliases.put(spuid,alias);	
	}

	@Override
	public void onUnsubscribe(String spuid) {
		aliases.remove(spuid);
	}

}
