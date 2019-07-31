package it.unibo.arces.wot.sepa.apps.chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.sparql.ARBindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.pattern.Aggregator;

/**
 * This abstract class provides a default management of a subscription. If the socket gets broken, the client tries to subscribe again.
 * 
 * */
public abstract class ChatAggregator extends Aggregator {
	protected static final Logger logger = LogManager.getLogger();

	private boolean joined = false;
	
	public ChatAggregator(String subscribeID, String updateID)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException {
		super(new ConfigurationProvider().getJsap(), subscribeID, updateID, new ConfigurationProvider().getSecurityManager());
	}

	public void joinChat() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException,
			InterruptedException, SEPABindingsException {
		logger.debug("Join the chat");
		while (!joined) {
			subscribe(5000);
			synchronized (this) {
				wait(5000);
			}
		}
		logger.debug("Joined");
	}

	public void leaveChat()
			throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException, InterruptedException {
		logger.debug("Leave the chat");
		while (joined) {
			unsubscribe(5000);
			synchronized (this) {
				wait(5000);
			}
		}
		logger.info("Leaved");
	}
	
	@Override
	public void onBrokenConnection() {
		logger.warn("onBrokenConnection");
		
		joined = false;
		
		try {
			joinChat();
		} catch (SEPASecurityException  | SEPAPropertiesException | SEPAProtocolException | InterruptedException | SEPABindingsException e2) {
		}
	}

	@Override
	public void onError(ErrorResponse errorResponse) {
		logger.error("onError: "+errorResponse);
	}

	@Override
	public void onSubscribe(String spuid, String alias) {
		logger.debug("onSubscribe");
		joined = true;
		synchronized(this) {
			notify();
		}
	}

	@Override
	public void onUnsubscribe(String spuid) {
		logger.debug("onUnsubscribe");
		joined = false;
		synchronized(this) {
			notify();
		}
	}
	
	@Override
	public void onResults(ARBindingsResults results) {
		logger.debug("onResults");
	}

	@Override
	public void onRemovedResults(BindingsResults results) {
		logger.debug("onRemovedResults");
	}
	
	@Override
	public void onAddedResults(BindingsResults results) {
		logger.debug("onAddedResults");
	}
	
	@Override
	public void onFirstResults(BindingsResults results) {
		logger.debug("onFirstResults");
	}

}
