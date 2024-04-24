package server;

import java.net.Socket;
import java.util.Queue;
import java.util.Set;

import shared.Message;
// The server will provide IT users with access to all conversations in the system.

// IT users will also be able to act as normal user, sending messages and participating in conversations.
public class ITUser extends ServerUser {

	public ITUser(String username, String password) {
		super(username,  password);

	}

	public void snoop(Conversation conversation) {
		/*System.out.println("Snooping conversation...");
		for(conversation.getConversation()) {
			System.out.println(conversation);
		}*/
	}

}