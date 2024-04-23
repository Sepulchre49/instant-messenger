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

public class Client {
    private String host;
    private int port;
    private Socket socket;
    private Scanner scanner;

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
                    ArrayList<Integer> recipients = new ArrayList<>() {{
                        add(5);
                    }};
                    sendMessage(new Message(
                            0,
                            new ArrayList<>() {{
                                add(5);
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
            System.out.println("Enter the host address to connect to: <127.0.0.1>");
            String inputHost = scanner.nextLine();
            if (!inputHost.isEmpty()) {
                this.host = inputHost;
            }

            System.out.println("Enter the port number to connect to: <3000>");
            String inputPort = scanner.nextLine();
            if (!inputPort.isEmpty()) {
                this.port = Integer.parseInt(inputPort);
            }

            this.socket = new Socket(host, port);

            OutQueue out = new OutQueue(socket);
            Thread outThread = new Thread(out);
            outThread.start();

            InQueue in = new InQueue(socket);
            Thread inThread = new Thread(in);
            inThread.start();

        } catch (IOException e) {
            System.err.println("Error in I/O operations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean login(String username, String password) throws IOException, ClassNotFoundException {
        Message response = null;
        Message m = new Message(
                0,
                null,
                Message.Type.LOGIN,
                Message.Status.REQUEST,
                String.format("username: %s password: %s", username, password));

        OutQueue.out.add(m);

        while(response == null) {
            response = InQueue.in.poll();
        }

        return response.getStatus() == Message.Status.SUCCESS;
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

        OutQueue.out.add(m);

        while(!success){
            response = InQueue.in.poll();

            assert response != null;
            if(response.getType() == Message.Type.LOGOUT && response.getStatus() == Message.Status.SUCCESS){
                success = true;
            }
        }
        return success;
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

        public InQueue(Socket socket) throws IOException {
            this.read = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            while (!quit) {
                try {
                    Message message = (Message) read.readObject();
                    in.add(message);


                } catch (IOException | ClassNotFoundException e) {
                    if (!quit) {
                        System.out.println("Error in InQueue: " + e.getMessage());
                        quit = true;
                    }
                }
            }
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

        public OutQueue(Socket socket) throws IOException {
            this.write = new ObjectOutputStream(socket.getOutputStream());
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
