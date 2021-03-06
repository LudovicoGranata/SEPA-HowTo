package it.unibo.arces.wot.sepa.apps.chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.security.ClientSecurityManager;
import it.unibo.arces.wot.sepa.pattern.JSAP;
import it.unibo.arces.wot.sepa.pattern.Producer;

/**
 * Delete all the registered users and messages. Message logs are not delete as they belong to a different graph.
 * */
public class DeleteAll extends Producer {
	private static final Logger logger = LogManager.getLogger();
	
	public DeleteAll(JSAP jsap, ClientSecurityManager sm) throws SEPAProtocolException, SEPAPropertiesException, SEPASecurityException {
		super(jsap, "DELETE_ALL",sm);
	}
	
	public void clean() {
		logger.info("Delete all");
		try {
			update();
		} catch (SEPASecurityException | SEPAPropertiesException | SEPABindingsException | SEPAProtocolException e) {
			logger.error(e.getMessage());
		}
	}
}
