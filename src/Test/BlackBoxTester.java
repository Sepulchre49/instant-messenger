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
		/*Set<ServerUser> participants = new HashSet<>();
		
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
		System.out.println( "Inbox count: "+ user1.getInboxCount());*/
		System.out.println("ServerUser test:");
		testCreateUser();
		testAddConversation();
		testReceiveMessage();
		testLoginandLogout();
		
		
		System.out.println("Converastion Test: ");
		

	}
	
	public static void testCreateUser() {
		ServerUser user= new ServerUser("quang","12345");
		assert user.getUserId() > 0: "User ID should not be less than 0";
		assert user.getUsername().equals("quang"): "Username matched";
		assert !user.isLoggedIn():"User should not be logged in ";
		assert user.getInboxCount()==0: "inbox count should be zero";
		System.out.println("Create User Test passed!");
	}
	
	public static void testAddConversation() {
		ServerUser user = new ServerUser("Bob", "4567");
		Set<ServerUser> participants = new HashSet<>();
		
		participants.add(new ServerUser("Quang", "12345"));
		Conversation conversation = new Conversation(participants);
		user.addConversation(conversation);
		assert user.getConversations().contains(conversation): "Conversation should be added";
		System.out.println("Add Conversation Test Passed!!!");
	}
	
	public static void testReceiveMessage() {
		ServerUser user = new ServerUser("Linda","secret");
		Message receivedMessage = new Message(1,null,Message.Type.TEXT,Message.Status.REQUEST,"Test Receive Message", 1);
		user.receive(receivedMessage);
		assert user.getInboxCount()==1:"Received message should be in the box";
		System.out.println("ReceiveMessage test Passed!!!");
	}
	
	public static void testLoginandLogout() {
		ServerUser user = new ServerUser("Bobby", "12345");
		assert !user.isLoggedIn():"User should not be logged in initially ";
		user.login();
		assert user.isLoggedIn(): "User should be logged in";
		user.logout();
		assert !user.isLoggedIn(): "User should be logged out";
		System.out.println("Login/Logout test passed!!!");
		
		
	}

}
