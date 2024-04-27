package Test;

import client.Client;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;


public class ClientTester {
	@Test
	public void constructor() {
        assertNotNull(new Client());
	}
}
