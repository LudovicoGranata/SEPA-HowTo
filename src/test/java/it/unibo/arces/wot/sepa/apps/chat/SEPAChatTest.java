package it.unibo.arces.wot.sepa.apps.chat;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import it.unibo.arces.wot.sepa.apps.chat.DeleteAll;
import it.unibo.arces.wot.sepa.apps.chat.UserRegistration;
import it.unibo.arces.wot.sepa.apps.chat.Users;
import it.unibo.arces.wot.sepa.apps.chat.client.BasicClient;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;

public class SEPAChatTest {
	private static int N_CLIENTS = 2;
	private static int BASE = 0;
	private static int MESSAGES = 10;

	private static Users users;
	private static List<ChatClient> clients = new ArrayList<ChatClient>();

	private static ChatMonitor monitor;

	private static ConfigurationProvider cfg;

	@BeforeClass
	public static void init() throws SEPASecurityException, SEPAPropertiesException, SEPAProtocolException {
		cfg = new ConfigurationProvider();

		BASE = cfg.getJsap().getExtendedData().get("base").getAsInt();
		N_CLIENTS = cfg.getJsap().getExtendedData().get("clients").getAsInt();
		MESSAGES = cfg.getJsap().getExtendedData().get("messages").getAsInt();

		deleteAllClients();
		registerClients();

		users = new Users(cfg.getJsap(), cfg.getSecurityManager());
	}

	@Test // (timeout = 5000)
	public void basicChatTest() throws SEPAProtocolException, SEPAPropertiesException, SEPASecurityException,
			InterruptedException, IOException, SEPABindingsException {

		monitor = new ChatMonitor(N_CLIENTS, MESSAGES);

		users.joinChat();

		for (String user : users.getUsers()) {
			ChatClient client = new BasicClient(user, users, MESSAGES);
			clients.add(client);

		}

		for (ChatClient client : clients) {
			Thread th = new Thread(client);
			th.start();
		}

		monitor.monitor();
	}

	private static void deleteAllClients()
			throws SEPAProtocolException, SEPAPropertiesException, SEPASecurityException {
		DeleteAll client = new DeleteAll(cfg.getJsap(), cfg.getSecurityManager());
		client.clean();
		try {
			client.close();
		} catch (IOException e) {
			assertFalse("deleteAllClients", true);
		}
	}

	private static void registerClients() throws SEPAProtocolException, SEPAPropertiesException, SEPASecurityException {
		// Register chat BOTS
		UserRegistration registration = new UserRegistration(cfg.getJsap(), cfg.getSecurityManager());
		for (int i = BASE; i < BASE + N_CLIENTS; i++) {
			registration.register("ChatBot" + i);
		}
		try {
			registration.close();
		} catch (IOException e) {
			assertFalse("registerClients", true);
		}
	}
}
