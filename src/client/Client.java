package client;

import server.ServerUser;
import shared.Message;

import javax.swing.text.BadLocationException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Client {
    private String host;
    private int port;
    private Socket socket;
    private Scanner scanner;
    public ObjectInputStream read;
    public ObjectOutputStream write;
    public InQueue inbound;
    public OutQueue outbound;

    public Map<Integer,String> usernameIdMap;


    private Map<Integer, Conversation> conversationMap;

    public ClientUser user;
    public static GUI gui;

    public Client() {
        gui = null;
        this.host = "127.0.0.1";
        this.port = 3000;

        usernameIdMap = new HashMap<>();
        conversationMap = new HashMap<>();

        scanner = new Scanner(System.in);
    }

    public void connectToServer() throws IOException {
        try {
            this.socket = new Socket(this.host, this.port);
            this.write = new ObjectOutputStream(socket.getOutputStream());
            this.read = new ObjectInputStream(socket.getInputStream());

        } catch (IOException e) {
            System.err.println("Error in I/O operations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean login(String username, String password) throws IOException, ClassNotFoundException {
        Message m = new Message(
                0,
                null,
                Message.Type.LOGIN,
                Message.Status.REQUEST,
                String.format("username: %s password: %s", username, password),
                -1 // ConversationId: fix this when the conversation endpoint is up
        );

        write.writeObject(m);
        Message res = (Message) read.readObject();
        boolean success = res.getType() == Message.Type.LOGIN && res.getStatus() == Message.Status.SUCCESS;
        if (success) {
            user = new ClientUser(res.getReceiverIds().get(0), username);
            decodeLoginPayload(res.getContent());
            System.out.println("Login successful");

            outbound = new OutQueue(write);
            Thread outThread = new Thread(outbound);
            outThread.start();

            inbound = new InQueue(read);
            Thread inThread = new Thread(inbound);
            inThread.start();
        }
        return success;
    }

    public void decodeLoginPayload(String content) {

        // Matches conversation id followed by the ids of all convo participants
        Pattern conversationPattern = Pattern.compile("(\\d+)((?:\\s+\\d+)*)");
        // Matches a pair of username, id
        Pattern userPattern = Pattern.compile("(\\d+)\\s+(\\p{Alpha}+)");

        for (String line : content.lines().toList()) {
            Matcher conversationMatch = conversationPattern.matcher(line);
            Matcher userMatch = userPattern.matcher(line);

            if (conversationMatch.matches()) {
                int conversationId = Integer.parseInt(conversationMatch.group(1));
                Set<Integer> participants = Arrays.stream(conversationMatch.group(2).strip().split("\\s+"))
                        .map(Integer::parseInt)
                        .collect(Collectors.toSet());
                participants.remove(user.getUserId());
                conversationMap.put(conversationId, new Conversation(conversationId, participants));
                System.out.printf("Adding conversation %d\n", conversationId);
            } else if (userMatch.matches()) {
                int userId = Integer.parseInt(userMatch.group(1));
                String username = userMatch.group(2);
                usernameIdMap.put(userId, username);
                System.out.printf("Adding user: %d %s\n", userId, username);
            }
        }
    }

    public boolean logout() throws IOException, ClassNotFoundException {
        boolean success = false;
        Message response;
        Message m = new Message(
                user.getUserId(),
                null,
                Message.Type.LOGOUT,
                Message.Status.REQUEST,
                "Logging out!",
                -1 // ConversationID: fix when conversation endpoint is up
        );

        write.writeObject(m);
        do {
            if (!inbound.in.isEmpty()) {
                Message res = inbound.in.poll();

                if (res.getType() == Message.Type.LOGOUT && res.getStatus() == Message.Status.SUCCESS) {
                    System.out.println("Successfully logged out!");
                    outbound.quit();
                    inbound.quit();
                    success = true;
                } else {
                    inbound.in.add(res);
                }
            }
        } while (!success);
        return success;
    }

    public Conversation getConversation(int id) {
        return conversationMap.get(id);
    }

    public Collection<Conversation> getConversations() {
        return conversationMap.values();
    }

    public Conversation addConversation(int id, Set<Integer> participants) {
        Conversation conversation = new Conversation(id, participants);
        conversationMap.put(id, conversation);
        return conversation;
    }

    public void viewConversation(int conversationID) {

    }

    public void sendMessage(Message message) throws IOException, ClassNotFoundException {
        outbound.out.add(message);
    }

    public void receiveMessage(Message message) {

    }

    class InQueue implements Runnable {
        public Queue<Message> in = new ConcurrentLinkedQueue<>();
        private final ObjectInputStream read;
        private volatile boolean quit = false;

        public InQueue(ObjectInputStream read) throws IOException {
            this.read = read;
        }

        @Override
        public void run() {
            while (!quit) {
                try {
                    Message message = (Message) read.readObject();
                    in.add(message);

                    if(gui == null) {
                        System.out.println(message.getContent());
                    } else{
                        if (message.getType() == Message.Type.TEXT && message.getStatus() == Message.Status.REQUEST) {
                            conversationMap.get(message.getConversationId()).addMessage(message);
                        } else if (message.getType() == Message.Type.CREATE_CONVERSATION && message.getStatus() == Message.Status.SUCCESS) {
                            HashSet<Integer> participants = new HashSet<>(message.getReceiverIds());
                            participants.remove(user.getUserId());
                            addConversation(message.getConversationId(), participants);
                            if (gui.homeView != null) {
                                gui.homeView.populateConversations(message.getConversationId(), message.getReceiverIds());
                            }
                        }
                    }

                } catch (IOException | ClassNotFoundException e) {
                    if (!quit) {
                        System.out.println("Error in InQueue: " + e.getMessage());
                        quit = true;
                    }
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void handleMessages(){

        }

        public void quit() {
            quit = true;
            Thread.currentThread().interrupt();
            try {
                read.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class OutQueue implements Runnable {
        public BlockingQueue<Message> out = new LinkedBlockingQueue<>();
        private final ObjectOutputStream write;
        private volatile boolean quit = false;

        public OutQueue(ObjectOutputStream write) throws IOException {
            this.write = write;
        }

        public void run() {
            while (!quit) {
                try {
                    Message message = out.poll();
                    if (message != null) {
                        write.writeObject(message);
                        write.flush();
                        System.out.println("Sent message: " + message.getContent());
                    } else {
                        try {
                            Thread.sleep(100); // Need to snooze for a bit, just in case...
                        } catch (InterruptedException e) {
                            quit = true;
                        }
                    }
                } catch (IOException e) {
                    if (!quit) {
                        System.out.println("Error in OutQueue: " + e.getMessage());
                        quit = true;
                    }
                }
            }
        }

        public void quit() {
            quit = true;
            Thread.currentThread().interrupt();
            try {
                write.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
