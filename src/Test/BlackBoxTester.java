package Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import server.Conversation;
import server.ServerUser;
import shared.Message;

public class BlackBoxTester {

	public static void main(String[] args) {
		System.out.println("ServerUser Black Box Tester: ");
		Set<ServerUser> participants = new HashSet<>();
		
		participants.add(new ServerUser("user1", "12345"));
		Conversation conversation = new Conversation(participants);
		System.out.println("Create a new user: ");
		//Create a new user
		ServerUser user1= new ServerUser("quang","12345");
		
		//add conversation 
		System.out.println("Add conversation: ");
		user1.addConversation(conversation);
		
		//Receiving a Message 
		System.out.println("Receiving a message:");
		Message message = new Message(1,null,Message.Type.TEXT,Message.Status.REQUEST,"Test Receive Message", 1);
		user1.receive(message);
		
		//logging in 
		user1.login();
		
		//logging out
		user1.logout();
		
		//print user information 
		System.out.println("Show the user information:");
		System.out.println("User ID: "+ user1.getUserId());
		System.out.println( "Username: "+ user1.getUsername());
		System.out.println("Is logged in: "+ user1.isLoggedIn());
		System.out.println( "Inbox count: "+ user1.getInboxCount());
		

	}

}
