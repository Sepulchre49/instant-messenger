package Test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import shared.Message;

public class MessageTester {
	
	private Message msg;
	
	
	@Before 
	public void setUp() {
		Set<Integer> recipients = new HashSet<>(Arrays.asList(1,2,3));
		msg = new Message(0,recipients, Message.Type.TEXT,Message.Status.REQUEST,"Hello", 1);
	}
	
	@Test
	public void testConstructor() {
		assertEquals(0,msg.getSenderId());
		assertEquals(new HashSet<>(Arrays.asList(1,2,3)),msg.getReceiverIds());
		assertNotNull(msg.getTimestamp());
		assertEquals(Message.Type.TEXT,msg.getType());
		assertEquals(Message.Status.REQUEST,msg.getStatus());
		assertEquals("Hello", msg.getContent());
	}

	
	@Test
	public void testConstructorWithNullRecipients() {
		Message nullRepcipientsMessage = new Message(0,null,Message.Type.TEXT,Message.Status.REQUEST,"This is content testConstructorWithNullRecipients", 1);
		assertNotNull(nullRepcipientsMessage.getReceiverIds());
		assertEquals(0,nullRepcipientsMessage.getReceiverIds().size());
	}
	
	@Test
	public void testGetSenderId() {
		assertEquals(0,msg.getSenderId());
	}
	@Test 
	public void testGetReceiverId() {
		assertEquals(new HashSet<>(Arrays.asList(1,2,3)),msg.getReceiverIds());
	}
	
	@Test
	public void testGetTimeStamp() {
		assertNotNull(msg.getTimestamp());
	}
	
	@Test
	public void testGetType() {
		assertEquals(Message.Type.TEXT,msg.getType());
	}
	
	@Test
	public void testGetStatus() {
		assertEquals(Message.Status.REQUEST,msg.getStatus());
	}
	
	@Test
	public void testGetContent() {
		assertEquals("Hello",msg.getContent());
	}
}
