package server;

import shared.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ServerInitializationException extends Exception {
    public ServerInitializationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

public class Server {
    private static final int DEFAULT_PORT = 3000;
    private static final int MAX_THREADS = 20;

    private ServerSocket socket;
    private Map<String, ServerUser> users;
    private Map<String, ServerUser> activeUsers;

    public Server(int port) throws ServerInitializationException {
        try {
	    socket = new ServerSocket(port);
        } catch (IOException e) {
            throw new ServerInitializationException("Error creating the ServerSocket.", e);
        }

	users = new HashMap<>();
	activeUsers = new HashMap<>();
        
        try {
            init_users();
        } catch (FileNotFoundException e) {
            throw new ServerInitializationException("Could not find users.txt file. No users could be loaded.", e);
        }

	ExecutorService tp = Executors.newFixedThreadPool(MAX_THREADS);
	tp.execute(new MessageQueueWriter());

	System.out.println("Now listening on port " + port + "...");
	while (true) {
            try {
	        Socket clientSocket = socket.accept();
	        tp.execute(new Session(clientSocket, this));
            } catch (IOException e) {
                System.err.println("Error accepting a new connection.");
                e.printStackTrace();
            }
	}
    }

    private void init_users() throws FileNotFoundException {
        File user_db = new File("users.txt");
        Scanner scanner = new Scanner(user_db);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher matcher = Pattern.compile("(\\w+)\\s+(\\w+)").matcher(line);
            
            if (matcher.find()) {
                String username = matcher.group(1);
                String password = matcher.group(2);

                users.put(username, new ServerUser(username, password));
            } else {
                System.out.println("Rejecting username/password " + line + "...");
            }
        }

        scanner.close();
    }

    public synchronized ServerUser login(String username, String password) {
	if (users.containsKey(username)) {
	    ServerUser user = users.get(username);
	    if (user.authenticate(password)) {
		activeUsers.put(username, user);
		System.out.println("Successfully logged in user " + username);
                return user;
	    } else {
		System.out.println("Authentication failure for user " + username);
                return null;
	    }
	} else {
            System.out.println("User " + username + " does not exist on the system.");
            return null;
        }
    }

    public synchronized void logout(ServerUser user) {
        if (user == null) {
            System.out.println("Cannot log out null user.");
            return;
        }

	if (activeUsers.containsKey(user.getUsername())) {
	    user.logout();
	    activeUsers.remove(user.getUsername());
	} else {
	    System.out.println("Failed to log out user " + user.getUsername() + ". Already signed out.");
	}
    }

    public void forward(Message msg) {
        System.out.println("Forwarded message received by server."); 
        // TODO: Read the message recepients, write to each recipients message queue
        log(msg);
    }

    private void log(Message msg) {
        // TODO: Implement logging
    }

    public static void main(String[] args) {
        try {
	    Server s = new Server(DEFAULT_PORT);
        } catch (ServerInitializationException e) {
            e.printStackTrace(); 
            System.exit(1);
        }
    }

    private static class MessageQueueWriter implements Runnable {
	@Override
	public void run() {
	    // TODO: Repeatedly check each active user's message queue; 
            // write one message per users queue per iteration
	    System.out.println("Hello from the MessageQueueWriter!");
	}
    }
}
