package it.unibo.arces.wot.sepa.apps.chat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.security.ClientSecurityManager;
import it.unibo.arces.wot.sepa.commons.sparql.ARBindingsResults;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import it.unibo.arces.wot.sepa.pattern.Consumer;
import it.unibo.arces.wot.sepa.pattern.JSAP;

public class Users extends Consumer {
	private static final Logger logger = LogManager.getLogger();
	
	private HashMap<String, String> usersList = new HashMap<String, String>();
	private boolean joined = false;
	private boolean usersRetrieved = false;

	public Users(JSAP jsap,ClientSecurityManager sm) throws SEPAProtocolException, SEPAPropertiesException, SEPASecurityException {
		super(jsap, "USERS",sm);
	}

	public void joinChat() throws SEPASecurityException, IOException, SEPAPropertiesException, SEPAProtocolException, InterruptedException, SEPABindingsException {
		logger.debug("Joining...");
		while (!joined) {
			subscribe(5000);
			synchronized(this) {
				wait(1000);
			}
		}
		logger.debug("Joined");
		
		logger.debug("Retrive users...");
		while (!usersRetrieved) {
			synchronized(this) {
				wait(1000);
			}
		}
		logger.debug("Users retrieved");
	}

	public void leaveChat() throws SEPASecurityException, IOException, SEPAPropertiesException, SEPAProtocolException, InterruptedException {
		while (joined) {
			unsubscribe(5000);
			synchronized(this) {
				wait(1000);
			}
		}
	}

	public Set<String> getUsers() {
		synchronized (usersList) {
			return usersList.keySet();
		}
	}

	public String getUserName(String user) {
		synchronized (usersList) {
			return usersList.get(user);
		}
	}

	@Override
	public void onSubscribe(String spuid, String alias) {
		super.onSubscribe(spuid, alias);
		
		synchronized(this) {
			joined = true;
			notify();
		}
	}

	@Override
	public void onUnsubscribe(String spuid) {
		super.onUnsubscribe(spuid);
		
		synchronized(this) {
			joined = false;
			notify();
		}
	}
	
	@Override
	public void onFirstResults(BindingsResults results) {
		super.onFirstResults(results);
		
		synchronized (usersList) {
			for (Bindings bindings : results.getBindings()) {
				usersList.put(bindings.getValue("user"), bindings.getValue("userName"));
				logger.debug("Add user: "+bindings.getValue("userName"));
			}
		}
		
		synchronized(this) {
			usersRetrieved = true;
			notify();
		}
	}
	
	@Override
	public void onResults(ARBindingsResults results) {
		super.onResults(results);
		
		synchronized (usersList) {
			for (Bindings bindings : results.getRemovedBindings().getBindings()) {
				usersList.remove(bindings.getValue("user"));
				logger.debug("Remove user: "+bindings.getValue("user"));
			}
			for (Bindings bindings : results.getAddedBindings().getBindings()) {
				usersList.put(bindings.getValue("user"), bindings.getValue("userName"));
				logger.debug("Add user: "+bindings.getValue("userName"));
			}
		}
	}	
}
