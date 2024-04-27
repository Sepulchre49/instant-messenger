package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
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
    private int conversationCounter = 0;

    private ServerSocket socket;
    private Map<String, Integer> usernames;
    private Map<Integer, ServerUser> users;
    private Set<Integer> activeUsers;

    public Server(int port) throws ServerInitializationException {
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            throw new ServerInitializationException("Error creating the ServerSocket.", e);
        }

        usernames = new HashMap<>();
        users = new HashMap<>();
        activeUsers = new HashSet<>();

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
                // Assuming you have some logic to determine the conversationId for each session
                int conversationId = determineConversationId(); // Implement this method according to your requirements
                tp.execute(new Session(clientSocket, this, conversationId)); // Pass conversationId
            } catch (IOException e) {
                System.err.println("Error accepting a new connection.");
                e.printStackTrace();
            }
        }
    }

    // Method to determine conversation ID
    private synchronized int determineConversationId() {
        return ++conversationCounter;
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

    public String getUserList() {
        String list = "";
        for (ServerUser user: users.values())
            list += String.format("%d %s\n", user.getUserId(), user.getUsername());

        return list;
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

            // Create a logout message
            Message logoutMessage = new Message(
                    user.getUserId(),
                    null,
                    Message.Type.LOGOUT,
                    Message.Status.REQUEST,
                    "Logging out!",
                    -1);

            // Log the logout message
            log(logoutMessage);
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
        // Get relevant message details
        int senderID = msg.getSenderId();
        List<Integer> receiverIDs = msg.getReceiverIds(); // Change to List
        int messageID = msg.hashCode(); // Generate a unique ID for each message
        int conversationID = msg.getConversationId();
        Set<ServerUser> participants = new HashSet<>();

        // Initialize receiver ID with a default value
        int receiverID = -1;
        if (!receiverIDs.isEmpty()) {
            receiverID = receiverIDs.get(0); // Get the first receiver ID
        }

        // Populate participants
        for (int id : receiverIDs) {
            ServerUser participant = users.get(id);
            if (participant != null) {
                participants.add(participant);
            }
        }

        String currentDirectory = System.getProperty("user.dir");
        String filePath = currentDirectory + File.separator + "conversation_log_" + conversationID + ".log";
        File logFile = new File(filePath);

        // Create a new instance of ConversationLog
        ConversationLog conversationLog = new ConversationLog(senderID, receiverID, messageID, conversationID, participants);

        // Add the message to the log
        conversationLog.addMessage(msg);

        // Write the log to the .log file
        conversationLog.writeLogToFile(logFile);
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
                        System.out.println("Delivering message to " + user.getUsername());
                        user.deliver();
                    }
                }
            }
        }
    }
}
