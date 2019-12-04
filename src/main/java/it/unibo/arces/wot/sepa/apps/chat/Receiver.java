package it.unibo.arces.wot.sepa.apps.chat;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;

class Receiver extends ChatAggregator {
	private final IMessageHandler handler;
	
	public Receiver(String userUri,IMessageHandler handler)
			throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, SEPABindingsException {
		super("SENT", "SET_RECEIVED");

		this.setSubscribeBindingValue("receiver", new RDFTermURI(userUri));
		
		this.handler = handler;
	}

	@Override
	public void onAddedResults(BindingsResults results) {
		super.onAddedResults(results);
		
		logger.debug("onAddedResults");

		for (Bindings bindings : results.getBindings()) {
			logger.debug("SENT " + bindings.getValue("message"));
			
			handler.onMessageReceived(bindings.getValue("sender"), bindings.getValue("message"), bindings.getValue("name"), bindings.getValue("text"));
			
			try {
				this.setUpdateBindingValue("message", new RDFTermURI(bindings.getValue("message")));
				update();
				
			} catch (SEPASecurityException | SEPAProtocolException | SEPAPropertiesException | SEPABindingsException e) {
				logger.error(e.getMessage());
			}
		}
	}
}
