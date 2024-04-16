package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int DEFAULT_PORT = 3000;
    private static final int MAX_THREADS = 20;

    private ServerSocket socket;
    private Map<String, ServerUser> users;
    private Map<String, ServerUser> activeUsers;

    public Server(int port) throws IOException {
	socket = new ServerSocket(port);
	users = new HashMap<>();
	activeUsers = new HashMap<>();

	ExecutorService tp = Executors.newFixedThreadPool(MAX_THREADS);
	tp.execute(new MessageQueueWriter());

	System.out.println("Now listening on port " + port + "...");
	while (true) {
	    Socket clientSocket = socket.accept();
	    tp.execute(new Session(clientSocket, this));
	}
    }

    public synchronized boolean login(String username, String password) {
	boolean success = false;
	if (users.containsKey(username)) {
	    ServerUser user = users.get(username);
	    if (user.authenticate(password)) {
		activeUsers.put(username, user);
		System.out.println("Successfully logged in user " + username);
		success = true;
	    } else {
		System.out.println("Authentication error for user " + username);
	    }
	}
	System.out.println("User " + username + " does not exist on the system.");
	return success;
    }

    public synchronized boolean logout(ServerUser user) {
	boolean success = false;
	if (activeUsers.containsKey(user.getUsername())) {
	    user.logout();
	    activeUsers.remove(user.getUsername());
	    success = true;
	} else {
	    System.out.println("Failed to log out user " + user.getUsername() + ". Already signed out.");
	}
	return success;
    }

    public static void main(String[] args) throws IOException {
	Server s = new Server(DEFAULT_PORT);
    }

    private static class MessageQueueWriter implements Runnable {
	@Override
	public void run() {
	    // Repeatedly check each client's message queue;
	    System.out.println("Hello from the MessageQueueWriter!");
	}
    }
}
