package it.unibo.arces.wot.sepa.apps.chat;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.QueryResponse;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparql.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.GenericClient;

public class UpdateQueryTest {
	private static final Logger logger = LogManager.getLogger();

	private static int clients;

	private static ConfigurationProvider cfg;
	
	private GenericClient client;
	
	@BeforeClass
	public static void init() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException {
		cfg = new ConfigurationProvider();

		clients = cfg.getJsap().getExtendedData().get("clients").getAsInt();
	}
	
	//@Test(timeout = 5000)
	@Test
	public void test() throws SEPAProtocolException, SEPASecurityException, SEPAPropertiesException, IOException {
		client = new GenericClient(cfg.getJsap(),cfg.getSecurityManager());
		
		run();
	}

	public void run() {
		deleteAll();

		for (int i = 0; i < clients; i++) {
			registerUser("User"+i);
		}
		
		List<String> users = users();
		
		// 1 - SEND
		for (String receiver : users) send(users.get(0),receiver,"Message");
		
		// 2 - SENT
		List<String> messages = new ArrayList<String>();
		for (String receiver : users) {
			List<String> msg = sent(receiver);
			messages.addAll(msg);
		}
		
		// 3 - SET RECEIVED
		for(String message : messages) setReceived(message);
		
		// 4 - RECEIVED
		messages.clear();
		List<String> msg = received(users.get(0));
		messages.addAll(msg);
		
		// 5 - REMOVE
		for(String message : messages) remove(message);
	}

	public void send(String sender, String receiver, String text) {
		Bindings bindings = new Bindings();
		bindings.addBinding("sender", new RDFTermURI(sender));
		bindings.addBinding("receiver", new RDFTermURI(receiver));
		bindings.addBinding("text", new RDFTermLiteral(text));

		long start = new Date().toInstant().toEpochMilli();
		try {
			client.update("SEND", bindings,5000);
		} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException | SEPABindingsException e) {
			assertFalse("SEND FAILED "+e.getMessage(), true);
		}
		long stop = new Date().toInstant().toEpochMilli();
		
		logger.info("SEND "+(stop-start));
	}

	public void setReceived(String message) {
		Bindings bindings = new Bindings();
		bindings.addBinding("message", new RDFTermURI(message));

		long start = new Date().toInstant().toEpochMilli();
		try {
			client.update("SET_RECEIVED", bindings,5000);
		} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException | SEPABindingsException e) {
			assertFalse("SET_RECEIVED " +e.getMessage(), true);
		}
		long stop = new Date().toInstant().toEpochMilli();
		
		logger.info("SET_RECEIVED "+(stop-start));
	}

	public void remove(String message) {
		Bindings bindings = new Bindings();
		bindings.addBinding("message", new RDFTermURI(message));
		
		long start = new Date().toInstant().toEpochMilli();
		try {
			client.update("REMOVE", bindings,5000);
		} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException | SEPABindingsException e) {
			assertFalse("REMOVE "+e.getMessage(), true);
		}
		long stop = new Date().toInstant().toEpochMilli();
		
		logger.info("REMOVE "+(stop-start));
	}

	public void deleteAll() {
		try {
			client.update("DELETE_ALL", null,5000);
		} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException | SEPABindingsException e) {
			assertFalse("DELETE_ALL "+e.getMessage(), true);
		}
	}

	public void registerUser(String userName) {
		Bindings bindings = new Bindings();
		bindings.addBinding("userName", new RDFTermLiteral(userName));

		try {
			client.update("REGISTER_USER", bindings,5000).isUpdateResponse();
		} catch (SEPAProtocolException | SEPASecurityException | IOException | SEPAPropertiesException | SEPABindingsException e) {
			assertFalse("REGISTER_USER "+e.getMessage(), true);
		}
	}

	public List<String> sent(String receiver) {
		Bindings bindings = new Bindings();
		bindings.addBinding("receiver", new RDFTermURI(receiver));
		
		ArrayList<String> list = new ArrayList<String>();
			
		long start = new Date().toInstant().toEpochMilli();
		Response ret;
		try {
			ret = client.query("SENT", bindings,5000);
		} catch (SEPAProtocolException | SEPASecurityException  | SEPAPropertiesException | SEPABindingsException e) {
			assertFalse("SENT "+e.getMessage(), true);
			return list;
		}
		long stop = new Date().toInstant().toEpochMilli();
		
		logger.info("SENT "+(stop-start));
		
		if (ret.isError()) return list;
		
		QueryResponse results = (QueryResponse) ret;
		for (Bindings result : results.getBindingsResults().getBindings()) {
			list.add(result.getValue("message"));
		}
		
		return list;
	}

	public List<String> received(String sender) {
		Bindings bindings = new Bindings();
		bindings.addBinding("sender", new RDFTermURI(sender));

		ArrayList<String> list = new ArrayList<String>();

		long start = new Date().toInstant().toEpochMilli();
		Response ret;
		try {
			ret = client.query("RECEIVED", bindings,5000);
		} catch (SEPAProtocolException | SEPASecurityException  | SEPAPropertiesException | SEPABindingsException e) {
			assertFalse("RECEIVED "+e.getMessage(), true);
			return list;
		}
		long stop = new Date().toInstant().toEpochMilli();
		
		logger.info("RECEIVED "+(stop-start));
		
		if (ret.isError()) return list;
		
		QueryResponse results = (QueryResponse) ret;
		for (Bindings result : results.getBindingsResults().getBindings()) {
			list.add(result.getValue("message"));
		}
		
		return list;
	}

	public List<String> users() {
		ArrayList<String> list = new ArrayList<String>();
		
		Response ret;
		try {
			ret = client.query("USERS", null,5000);
		} catch (SEPAProtocolException | SEPASecurityException  | SEPAPropertiesException | SEPABindingsException e) {
			assertFalse("USERS "+e.getMessage(), true);
			return list;
		}
		
		if (ret.isError()) return list;
		
		QueryResponse results = (QueryResponse) ret;
		for (Bindings bindings : results.getBindingsResults().getBindings()) {
			list.add(bindings.getValue("user"));
		}
		
		return list;
	}
}
