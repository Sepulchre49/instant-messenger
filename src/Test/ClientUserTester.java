package Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;

import client.ClientUser;
import shared.Message;

public class ClientUserTester {
	@Test
	public void constructorTest() {
		int userId= 1;
		String username=  "tester";
		ClientUser user = new ClientUser(userId,username);
		
		assertEquals(userId, user.getUserId());
		assertEquals(username,user.getUsername());
	}
	
	@Test
	public void constructorInvalidArgument() {
		int userId =-1;
		String username =null;
		ClientUser user= new ClientUser(userId,username);
		
		assertEquals(userId, user.getUserId());
		assertNull(user.getUsername());
	}
	
	@Test
	public void getterAndSetterTest() {
		int userId =1;
		String username = "Tester";
		ClientUser user = new ClientUser();
		user.setUserId(userId);
		user.setUsername(username);
		assertEquals ( userId, user.getUserId());
		assertEquals(username,user.getUsername());
		
	}
	
	@Test
	public void loginTest() {
		ClientUser user= new ClientUser();
		assertTrue(user.login());
	}
	
	@Test 
	public void logoutTest() {
		ClientUser user = new ClientUser();
		user.logout();
	}
	
	@Test
	public void setterAndGetterConversationTest() {
		int conversationId= 123;
		ClientUser user = new ClientUser();
		user.setConversationId(conversationId);
		assertEquals(conversationId, user.getConversationId());
	}

}
