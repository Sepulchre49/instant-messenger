package Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import server.Conversation;
import server.ServerUser;
import shared.Message;
import shared.Message.Status;
import shared.Message.Type;

public class ConversationTest {
	public Conversation conversation;
	private Set<ServerUser> participants;
	private List<Message>messages;
	private File log;
	private int id=1;
	private int sender= id++;
	Collection<Integer> recipientIds = new ArrayList<>();
	Type type= Type.TEXT;
	Status stat= Status.REQUEST;

	
	@Test
	public void ConstructorTest() {
		Set<ServerUser> participants = new HashSet<>();
		participants.add(new ServerUser("user1", "12345"));
		participants.add(new ServerUser("user2", "456789"));
		File log= new File("conversation_log.txt");
		conversation = new Conversation(participants);
	}
	
	@Test
	public void addMsgTest() {
		//Create sample conversation 
		Set<ServerUser> participants = new HashSet<>();
		File logFile = new File("conversation_log.txt");
		Conversation conversation = new Conversation(participants);
		//Add Message 
		Message msg1= new Message(1,recipientIds,Message.Type.TEXT, Message.Status.SUCCESS,"Hello", conversation.getID());
		conversation.addMsg(msg1);
		//verify the message 
		List<Message> allMessage = conversation.getAllMsgs();
		assertEquals(msg1,allMessage.get(0));
	
	}
	
	@Test
	public void getAllMsgTest() {
		Set<ServerUser> participants = new HashSet<>();
		Conversation conversation = new Conversation(participants);
		
		//Add more message
		Message msg2= new Message(2,recipientIds,Message.Type.TEXT, Message.Status.SUCCESS,"How are you doing?", 1);
		Message msg3= new Message(3,recipientIds,Message.Type.TEXT, Message.Status.SUCCESS,"I am good , Thanks!", 1);
		conversation.addMsg(msg2);
		conversation.addMsg(msg3);
		
		//Verify Message
		List<Message> allMessages= conversation.getAllMsgs();
		assertTrue(allMessages.contains(msg2));
		assertTrue(allMessages.contains(msg3));
		
	}
	
	@Test
	public void getLastMsg() {
		Set<ServerUser> participants = new HashSet<>();
		File logFile = new File("conversation_log.txt");
		Conversation conversation = new Conversation(participants);
		
		//Add more messages
		Message msg2= new Message(2,recipientIds,Message.Type.TEXT, Message.Status.SUCCESS,"How are you doing?", conversation.getID());
		Message msg3= new Message(3,recipientIds,Message.Type.TEXT, Message.Status.SUCCESS,"I am good , Thanks!", conversation.getID());
		conversation.addMsg(msg2);
		conversation.addMsg(msg3);
		
		//Verify the last message
		Message lastMsg= conversation.getLastMsg();
		assertEquals(msg3,lastMsg);
	}
	
	@Test
	public void getIDTest() {
		Set<ServerUser> participants = new HashSet<>();
		Conversation conversation = new Conversation(participants);
		// verify conversation ID
		assertEquals(3,conversation.getID());
	}
	
	@Test
	public void getLogTest() {
		Set<ServerUser> participants = new HashSet<>();
		Conversation conversation = new Conversation(participants);
		File log = new File(conversation.getID() + ".log");
		//verify log file
		assertEquals(log, conversation.getLog());
		
	}
}
