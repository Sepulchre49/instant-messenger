package Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Before;
import org.junit.Test;

import server.ServerUser;
import shared.Message;

public class ServerUserTester {
	private ServerUser user;
	private Queue<Message>messageQueue;
	
	@Before
	public void setUp() {
		user= new ServerUser("testUser","testPassword");
		messageQueue = new ConcurrentLinkedQueue<>();
		user.setOutputStream(null);
	}
	
	@Test 
	public void testConstructor() {
		assertEquals("testUser",user.getUsername());
		assertFalse(user.isLoggedIn());
		assertEquals(0,user.getInboxCount());
	}
	
	@Test
	public void testAuthentication() {
		assertTrue(user.authenticate("testPassword"));
		assertFalse(user.authenticate("wrong password"));
	}
	
	@Test
	public void testLogin() {
		assertFalse(user.isLoggedIn());
		user.login();
	}
	@Test
	public void testLogout() {
		assertFalse(user.isLoggedIn());
		user.logout();
		
	}
	
	@Test
	public void testReceive() {
		Message msg = new Message(1,null,Message.Type.TEXT,Message.Status.REQUEST,"Test Receive Message", 1);
		user.receive(msg);
		assertEquals(1,user.getInboxCount());
	}
	
	@Test
	public void testSendMessage() throws IOException {
		Message msg = new Message(1,null,Message.Type.TEXT,Message.Status.REQUEST,"Test Send Message", 1);
		user.receive(msg);
		ObjectOutputStream out= new ObjectOutputStream(System.out);
		user.setOutputStream(out);
		assertEquals(1,user.getInboxCount());
		user.deliver();
		assertEquals(0,user.getInboxCount());
	}
	
	@Test
	public void testMessageQueue() {
		assertEquals(0,user.getInboxCount());
		user.deliver();
		assertEquals(0,user.getInboxCount());
		
	}
		@Test
	public void testConnection() {
		assertNull(user.getConnection());
		Socket ss = new Socket();
		user.setConnection(ss);
		
	}
	
	@Test
	public void testConnectionNull() {
		user.setConnection(null);
		assertNull(user.getConnection());
	}
	

}
