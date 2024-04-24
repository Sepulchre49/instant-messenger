package client;

import shared.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.*;

public class Client {
    private String host;
    private int port;
    private Socket socket;
    private Scanner scanner;
    private ObjectInputStream read;
    private ObjectOutputStream write;

    private final ClientUser user = new ClientUser();
    private final GUI clientGUI = new GUI();

    public Client() {
        this.host = "127.0.0.1";
        this.port = 3000;

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
                            Message.Type.TEXT,
                            Message.Status.REQUEST,
                            in,
                            user.getUserId(),
                            new ArrayList<>() {{
                                add(1); // TODO : Hardcoded value. Must be replaced with actual recipient.
                            }},
                            -1
                    ));
                }
            } while (!quit);
        }
    }

    public void connectToServer() throws IOException {
        Scanner scanner = new Scanner(System.in); //to be removed after GUI implementation.

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
        Message loginMessage = new Message(
                Message.Type.LOGIN,
                Message.Status.REQUEST,
                String.format("username: %s password: %s", username, password),
                0,
                null,
                -1
        );

        write.writeObject(loginMessage);
        Message res = (Message) read.readObject();
        return res.getType() == Message.Type.LOGIN && res.getStatus() == Message.Status.SUCCESS;
    }

    public boolean logout() throws IOException, ClassNotFoundException {
        Message logoutMessage = new Message(
            Message.Type.LOGOUT,
            Message.Status.REQUEST,
            "Logging out!",
            user.getUserId(),
            null, // recipientId
            -1    // conversationId
        );
        write.writeObject(logoutMessage);
        // TODO: This is throwing a StreamCorruptionException: invalid type code FF.
        // Doesn't affect usage because it is just during the logout phase, but would like to fix in the future.
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

    private static class InQueue implements Runnable {
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
                    System.out.println(message.getContent());

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

    private static class OutQueue implements Runnable {
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
        Scanner scanner = new Scanner(System.in);

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
            e.printStackTrace();
            System.exit(1);

        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Error in auth.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
