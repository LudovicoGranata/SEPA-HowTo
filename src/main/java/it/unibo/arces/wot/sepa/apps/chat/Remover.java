package it.unibo.arces.wot.sepa.apps.chat;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;

class Remover extends ChatAggregator {	
	
	public Remover(String userUri) throws SEPAProtocolException, SEPAPropertiesException, SEPASecurityException, SEPABindingsException {
		super("RECEIVED", "REMOVE");
		
		this.setSubscribeBindingValue("sender", new RDFTermURI(userUri));
	}

	@Override
	public void onAddedResults(BindingsResults results) {
		super.onAddedResults(results);
		
		for (Bindings bindings : results.getBindings()) {
			logger.debug("RECEIVED: "+bindings.getValue("message"));
			
			try {
				this.setUpdateBindingValue("message", bindings.getRDFTerm("message"));
				update();
				
			} catch (SEPASecurityException | SEPAProtocolException | SEPAPropertiesException | SEPABindingsException e) {
				logger.error(e.getMessage());
			}
		}

	}
}
