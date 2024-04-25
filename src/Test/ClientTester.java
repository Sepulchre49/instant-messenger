package Test;

import client.Client;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;


public class ClientTester {
	
	@Test
	public void testConnectionandMessageLoop() {
		Client client = new Client();
		
		try {
			client.connectToServer();
		} catch(IOException e) {
			fail("Error: Not Able To Connect To Server");
		}
		
		try {
			client.doMessageReadLoop();
		}catch(IOException | ClassNotFoundException e) {
			fail("Error: Error In Message Loop");
		}catch(Exception e) {
			fail("Error: Unexpected Error: " + e.getMessage());
		}
	}

}
