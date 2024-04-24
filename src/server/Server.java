package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shared.Message;

class ServerInitializationException extends Exception {
    public ServerInitializationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

public class Server {
    public static final int SERVER_USER_ID = 0;
    private static final int DEFAULT_PORT = 3000;
    private static final int MAX_THREADS = 20;

    private ServerSocket socket;
    private Map<String, Integer> usernames;
    private Map<Integer, ServerUser> users;
    private Set<Integer> activeUsers;
    private Map<Integer, Conversation> conversations;

    public Server(int port) throws ServerInitializationException {
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            throw new ServerInitializationException("Error creating the ServerSocket.", e);
        }

        usernames = new HashMap<>();
        users = new HashMap<>();
        activeUsers = new HashSet<>();
        conversations = new HashMap<>();
        
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

                ServerUser user = new ServerUser(username, password);

                int userId = user.getUserId();

                usernames.put(username, userId);
                users.put(userId, user);
            } else {
                System.out.println("Rejecting username/password " + line + "...");
            }
        }

        scanner.close();
    }

    public synchronized ServerUser login(String username, String password) {
        if (usernames.containsKey(username)) {
            int id = usernames.get(username);
            ServerUser user = users.get(id);
            if (user.authenticate(password)) {
                activeUsers.add(user.getUserId());
                System.out.println("Successfully logged in user " + username + " w/ id " + user.getUserId());
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

        if (activeUsers.contains(user.getUserId())) {
            user.logout();
            activeUsers.remove(user.getUserId());
        } else {
            System.out.println("Failed to log out user " + user.getUsername() + ". Already signed out.");
        }
    }

    public void forward(Message msg) {
        System.out.println("Forwarded message received by server."); 

        for (int id : msg.getReceiverIds()) {
            System.out.println("Recipient id: " + id);
            ServerUser recipient = users.get(id);
            System.out.println("Adding msg to " + recipient.getUsername() + "'s message queue");
            recipient.receive(msg);
        }

        log(msg);
    }

    private void log(Message msg) {

        int conversationID = msg.getConversationId();
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
        ConversationLog conversationLog = new ConversationLog(msg.getSenderId(), msg.getReceiverIds(), msg.getMessageId(), conversation.getID());
        conversationLog.addMessage(msg);

        File logFile = conversation.getLog();
        try {
            // If the log file doesn't exist, create one
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            // Write ConversationLog to the log file
            FileOutputStream fileOutputStream = new FileOutputStream(logFile, true); // Append mode
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(conversationLog);
            objectOutputStream.close();
            fileOutputStream.close();

            System.out.println("Conversation log written to file: " + logFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error writing conversation log to file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Server s = new Server(DEFAULT_PORT);
        } catch (ServerInitializationException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private class MessageQueueWriter implements Runnable {
        private boolean quit;

        public MessageQueueWriter() {
            quit = false;
        }

        public void quit() {
            quit = true;
        }

        @Override
        public void run() {
            while (!quit) {
                for (int id : activeUsers) {
                    ServerUser user = users.get(id);
                    if (user != null && user.getInboxCount() > 0) {
                        System.out.println("Delivering message to user " + user.getUsername());
                        user.deliver();
                    }
                }
            }
        }
    }
}
