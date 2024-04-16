package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import shared.Message;

class Session implements Runnable {
    private Socket client;
    private String clientAddress;
    private boolean isLoggedIn;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Session(Socket s) {
	client = s;
	clientAddress = client.getInetAddress().getHostAddress();
	isLoggedIn = false;
	System.out.println("Connection created with " + clientAddress);
	try {
	    in = new ObjectInputStream(client.getInputStream());
	    out = new ObjectOutputStream(client.getOutputStream());
	} catch (IOException e) {
	    System.out.println("Error creating the object read/write streams.");
	    e.printStackTrace();
	}
    }

    private void waitForLogin() {
	do {
	    try {
		System.out.println("Received new message from " + clientAddress);
		Message m = (Message) in.readObject();
		if (m.getType() == Message.Type.LOGIN)
		    handleLogin();
	    } catch (IOException e) {
		System.out.println("Error reading object from the input stream while waiting for login message from " + clientAddress);
		e.printStackTrace();
	    } catch (ClassNotFoundException e) {
		System.out.println("Could not find serialized class while waiting for login message from " + clientAddress);
		e.printStackTrace();
	    }
	} while (!isLoggedIn);
    }
    
    private void handleLogin() {
	isLoggedIn = true;
	try {
	    out.writeObject(new Message(Message.Type.LOGIN, Message.Status.SUCCESS, "You successfully logged in!"));
	    System.out.println(clientAddress + " has successfully logged in");
	} catch (IOException e) {
	    System.out.println("Error sending login success message to " + clientAddress);
	    e.printStackTrace();
	}
    }

    private void handleDuplicateLogin() {
	System.out.println("Received duplicate login request from " + clientAddress);
	try {
	    out.writeObject(new Message(Message.Type.LOGIN, Message.Status.FAILURE, "Nu uh uh"));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void handleLogout() {
	try {
	    System.out.println("Received logout request from " + clientAddress);
	    logout();
	    System.out.println("Successfully logged out client " + clientAddress);
	} catch (IOException e) {
	    System.out.println("Error trying to close connection with " + clientAddress);
	    e.printStackTrace();
	}
    }

    private void logout() throws IOException {
	out.writeObject(new Message(Message.Type.LOGOUT, Message.Status.SUCCESS, "You have been logged out of the server."));
	isLoggedIn = false;
	client.close();
    }

    private void handleText(String text) { 
	try {
	    System.out.println("Received text message from " + clientAddress);
	    out.writeObject(new Message(Message.Type.TEXT, Message.Status.SUCCESS, text.toUpperCase()));
	} catch (IOException e) {
	    System.out.println("Error responding to text message from " + clientAddress);
	    e.printStackTrace();
	}
    }

    private void doMessageLoop() throws IOException {
	do {
	    // Read a message
	    try {
		Message m = (Message) in.readObject();
		switch (m.getType()) {
		    case TEXT:
			handleText(m.getContent());
			break;
		    case LOGOUT:
			handleLogout();
			break;
		    case LOGIN:
			handleDuplicateLogin();
			break;
		}
	    } catch (IOException e) {
		System.out.println("Error while reading messages from " + clientAddress);
		e.printStackTrace();
		System.out.println("Terminating connection with " + clientAddress);
		logout();
	    } catch (ClassNotFoundException e) {
		System.out.println("Could not find serialized class for message from " + clientAddress);
	    }
	} while (isLoggedIn);
    }

    @Override
    public void run() {
	waitForLogin();
	try {
	    doMessageLoop();
	} catch (IOException e) {
	    System.out.println("An exception occured while trying to logout client " + clientAddress + " in response to an invalid state in the message loop.");
	    e.printStackTrace();
	}
    }
}
