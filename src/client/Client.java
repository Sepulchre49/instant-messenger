package client;

import shared.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.HashMap; //new
import java.util.Map; //new

public class Client {
    private String host;
    private int port;
    private Socket socket;
    private Scanner scanner;
    public ObjectInputStream read;
    public ObjectOutputStream write;
    
    private Map<String, Integer> usernameIdMap; //new

    private final ClientUser user = new ClientUser();
    public static GUI gui;

    public Client() {
        gui = null;
        this.host = "127.0.0.1";
        this.port = 3000;
        
        usernameIdMap = new HashMap<>(); //new

        scanner = new Scanner(System.in);
    }

    public void doMessageReadLoop() throws IOException, ClassNotFoundException, ExecutionException, InterruptedException {
        int attempts = 3;
        boolean success = false;
        while (!success && attempts > 0) {
            System.out.println("Username: ");
            String user = scanner.nextLine();

            System.out.println("Password: ");
            String pass = scanner.nextLine();

            success = login(user,pass);
            --attempts;
        }

        if (success) {
            OutQueue outQueue = new OutQueue(write);
            Thread outThread = new Thread(outQueue);
            outThread.start();

            InQueue inQueue = new InQueue(read);
            Thread inThread = new Thread(inQueue);
            inThread.start();

            System.out.println("Successfully logged in.");

            boolean quit = false;
            do {
                System.out.println("Enter a message, or type 'logout' to quit: ");
                String in = scanner.nextLine();
                if (in.equals("logout")) {
                    System.out.println("Logging out...");


                    if (logout()) {
                        System.out.println("(Client) Successfully logged out.");
                    } else {
                        System.out.println("Error logging out.");
                    }
                    quit = true;
                } else {
                    sendMessage(new Message(
                            0,
                            new ArrayList<>() {{
                                add(1); // TODO : Hardcoded value. Must be replaced with actual recipient.
                            }},
                            Message.Type.TEXT,
                            Message.Status.REQUEST,
                            in));
                }
            } while (!quit);
        }
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
                String.format("username: %s password: %s", username, password));

        write.writeObject(m);
        Message res = (Message) read.readObject();
        if(res.getType() == Message.Type.LOGIN && res.getStatus() == Message.Status.SUCCESS) {
        	decodeAndStoreUsernames(res.getContent()); //new
        	System.out.println("Login Successful"); 
        	return true;
        } else {
        	System.out.println("Error: Login Failed");
        	return false;
        } //new
        /*System.out.println(res.getContent());
        return res.getType() == Message.Type.LOGIN && res.getStatus() == Message.Status.SUCCESS;*/
    }
    
    public void decodeAndStoreUsernames(String content) { //new
    	String[] usernameIdpairs = content.split("\\n");
    	for(String pair : usernameIdpairs) {
    		String[] usernameIdData = pair.split(":");
    		if(usernameIdData.length == 2) {
    			String username = usernameIdData[0];
    			int id = Integer.parseInt(usernameIdData[1]);
    			usernameIdMap.put(username, id);
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
                "Logging out!");

        write.writeObject(m);
        // TODO: This throws a StreamCorruptionException: invalid type code: FF
        // Doesnt affect usage because it happens after logout phase, but would like to fix this in the future
        Message res = (Message) read.readObject();
        return res.getType() == Message.Type.LOGOUT && res.getStatus() == Message.Status.SUCCESS;
    }

    public void viewConversation(int conversationID) {

    }

    public void sendMessage(Message message) throws IOException, ClassNotFoundException {
        OutQueue.out.add(message);
    }

    public void receiveMessage(Message message) {

    }

    static class InQueue implements Runnable {
        public static Queue<Message> in = new ConcurrentLinkedQueue<>();
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
                        gui.updateChatArea(message);
                    }

                } catch (IOException | ClassNotFoundException e) {
                    if (!quit) {
                        System.out.println("Error in InQueue: " + e.getMessage());
                        quit = true;
                    }
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

    static class OutQueue implements Runnable {
        public static BlockingQueue<Message> out = new LinkedBlockingQueue<>();
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
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Hello from the client!");

        Client client = new Client();

        try {
            client.connectToServer();
        } catch (IOException e) {
            System.out.println("Error connecting to server.");
            System.exit(1);
        }

        try {
            client.doMessageReadLoop();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error in message loop.");
            System.exit(1);

        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Error in auth.");
            System.exit(1);
        }
    }
}
