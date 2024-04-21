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

public class Server {
    private static final int DEFAULT_PORT = 3000;
    private static final int MAX_THREADS = 20;

    private ServerSocket socket;
    private Map<String, ServerUser> users;
    private Map<String, ServerUser> activeUsers;
    private Map<String, Conversation> conversations;

    public Server(int port) throws IOException {
	socket = new ServerSocket(port);
	users = new HashMap<>();
	activeUsers = new HashMap<>();
        
        try {
            init_users();
        } catch (FileNotFoundException e) {
            System.out.println("Could not find users.txt file");
            e.printStackTrace();
        }

	ExecutorService tp = Executors.newFixedThreadPool(MAX_THREADS);
	tp.execute(new MessageQueueWriter());

	System.out.println("Now listening on port " + port + "...");
	while (true) {
	    Socket clientSocket = socket.accept();
	    tp.execute(new Session(clientSocket, this));
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
		System.out.println("Authentication error for user " + username);
                return null;
	    }
	} else {
            System.out.println("User " + username + " does not exist on the system.");
            return null;
        }
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

    public void forward(Message msg) {
        System.out.println("Forwarded message received by server."); 
        // TODO: Read the message recepients, write to each recipients message queue
        log(msg);
    }

    private void log(Message msg) {
    	
        String conversationID = msg.getConversationId();
        Conversation conversation = conversations.get(conversationID);
        if (conversation != null) {
            conversation.addMsg(msg); // Add message to conversation
            System.out.println("Message logged for conversation " + conversationID);
            // Also, log the message into the ConversationLog
            logToConversationLog(msg, conversation);
        } else {
            System.out.println("Conversation " + conversationID + " not found.");
        }
        
    }
    
    private void logToConversationLog(Message msg, Conversation conversation) {
        // Log message details to ConversationLog
        ConversationLog conversationLog = new ConversationLog(msg.getSenderId(), msg.getRecipientId(), msg.getMessageId(), conversation.getID());
        conversationLog.addMessage(msg);
        // Assuming you want to log to a file, you can implement file writing here
        // Example: conversation.getLog().write(conversationLog);
    }

    public static void main(String[] args) throws IOException {
	Server s = new Server(DEFAULT_PORT);
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
