package it.unibo.arces.wot.sepa.apps.chat;

public interface IMessageHandler {
	public void onMessageReceived(String userUri,String messageUri,String user,String message);
}
