package server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import shared.Message;

public class ServerUser {
    private static int count = 1;
    private int userId;
    private String username, password;
    private boolean isLoggedIn;
    private Queue<Message> messageQueue;
    private Socket connection;
    private ObjectOutputStream out;

    public ServerUser(String username, String password) {
        // Make sure that the user id will never ever be equal to the id reserved by the server
        if (count == Server.SERVER_USER_ID)
            count++;

        this.userId = count++;
        this.username = username;
        this.password = password;
        this.isLoggedIn = false;
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }
    
    public void setOutputStream(ObjectOutputStream out) {
        this.out = out;
    }

    public int getInboxCount() {
        return messageQueue.size();
    }

    public void deliver() {
        Message m = messageQueue.poll();
        if (m != null) {
            try {
                synchronized (out) {
                    out.writeObject(m);
                    out.flush();
                }
                System.out.println("Message delivered.");
            } catch (IOException e) {
                System.err.println("Failed delivering message to user " + userId);
                e.printStackTrace();
            }
        }
    }

    public int getUserId() {
        return userId;
    }

    public boolean authenticate(String password) {
	return this.password.equals(password);
    }

    public String getUsername() {
	return username;
    }

    public boolean isLoggedIn() {
	return isLoggedIn;
    }

    public void receive(Message m) {
        messageQueue.offer(m);
        System.out.println("Successfully added message to " + username + "'s message queue");
    }

    public synchronized void login() {
	this.isLoggedIn = true;
    }

    public synchronized void logout() {
	this.isLoggedIn = false;
    }

    public Socket getConnection() {
        return connection;
    }

    public void setConnection(Socket connection) {
        this.connection = connection;
    }
}
