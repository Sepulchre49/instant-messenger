package client;

import shared.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream write;
    private ObjectInputStream read;

    private final ClientUser user = new ClientUser();
    private final GUI clientGUI = new GUI();

    public Client() {
        this.host = "127.0.0.1";
        this.port = 3000;
    }


    public void connectToServer() throws IOException {
        Scanner scanner = new Scanner(System.in); //to be removed after GUI implementation.

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
        System.out.println(res.getContent());
        return (res.getType().equals(Message.Type.LOGIN) && res.getStatus().equals(Message.Status.SUCCESS));
    }

    public boolean logout() throws IOException, ClassNotFoundException {
        write.writeObject(new Message( 
                    user.getUserId(), 
                    null,
                    Message.Type.LOGOUT, 
                    Message.Status.REQUEST, 
                    "Logging out!"));

        Message res = (Message) read.readObject();
        System.out.println(res.getContent());
        return res.getStatus() == Message.Status.SUCCESS;
    }

    public void viewConversation(int conversationID) {

    }

    public void sendMessage(Message message) throws IOException, ClassNotFoundException {
        write.writeObject(message);
        Message res = (Message) read.readObject();
        System.out.println(res.getContent());
    }

    public void receiveMessage(Message message) {

    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Hello from the client!");
        Scanner scanner = new Scanner(System.in);

        Client client = new Client();
        client.connectToServer();

        int attempts = 3;
        boolean success = false;
        while (!success && attempts > 0) {
            System.out.println("Username: ");
            String user = scanner.nextLine();

            System.out.println("Password: ");
            String pass = scanner.nextLine();

            success = client.login(user, pass);
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
                    
                    if (client.logout()) {
                        System.out.println("(Client) Successfully logged out.");
                    } else {
                        System.out.println("Error logging out.");
                    }
                    quit = true;
                } else {
                    ArrayList<Integer> recipients = new ArrayList<>() {{add(5);}};
                    client.sendMessage(new Message(
                                0,
                                new ArrayList<>() {{add(5);}},
                                Message.Type.TEXT,
                                Message.Status.REQUEST, 
                                in));
                }
            } while (!quit);
        }
    }
}
