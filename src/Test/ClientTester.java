package Test;

import client.Client;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;


public class ClientTester {
	@Test
	public void constructor() {
        assertNotNull(new Client("127.0.0.1", 3000));
	}
}
