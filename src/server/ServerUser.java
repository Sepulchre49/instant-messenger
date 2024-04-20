package server;

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
        messageQueue.add(m);
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
