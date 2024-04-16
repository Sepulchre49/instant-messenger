package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.Message;

public class Client {
    public static void main(String[] args) {
	System.out.println("Hello from the client!");
	
	Message loginRequest = new Message(Message.Type.LOGIN, Message.Status.REQUEST, "username: caleb password: letmein");

	try {
	    Socket s = new Socket("localhost", 3000);
	    ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
	    ObjectInputStream in = new ObjectInputStream(s.getInputStream());

	    out.writeObject(loginRequest);
	    Message res = (Message) in.readObject();
	    System.out.println(res.getContent());

	    in.close();
	    out.close();
	    s.close();
	} catch (IOException | ClassNotFoundException e) {
	    e.printStackTrace();
	}
    }
}
