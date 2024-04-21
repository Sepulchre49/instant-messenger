package client;

import shared.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
            Message.Type.LOGIN, 
            Message.Status.REQUEST,
            String.format("username: %s password: %s", username, password),
            // Fill these down below.
            -1,
            -1, 
            -1, 
            null
        );

        int senderId = user.getUserId(); 
        Message loginMessage = new Message(
            m.getType(), 
            m.getStatus(), 
            m.getContent(), 
            senderId, 
            -1, // recipientId
            -1, // messageId
            null // conversationId
        );

        write.writeObject(loginMessage);
        Message res = (Message) read.readObject();
        System.out.println(res.getContent());
        return res.getStatus().equals(Message.Status.SUCCESS);
    }

    public boolean logout() throws IOException, ClassNotFoundException {
        int senderId = user.getUserId(); // Example: Get sender ID from user object
        Message logoutMessage = new Message(
            Message.Type.LOGOUT, 
            Message.Status.REQUEST, 
            "Logging out!", 
            senderId, 
            -1, // recipientId
            -1, // messageId
            null // conversationId
        );

        write.writeObject(logoutMessage);
        Message res = (Message) read.readObject();
        System.out.println(res.getContent());
        return res.getStatus() == Message.Status.SUCCESS;
    }

    public void viewConversation(int conversationID) {

    }

    public void sendMessage(Message message) throws IOException, ClassNotFoundException {
        int senderId = user.getUserId(); // Example: Get sender ID from user object
        int recipientId = message.getRecipientId();
        int messageId = message.getMessageId();
        String conversationId = message.getConversationId();

        Message newMessage = new Message(
                message.getType(), 
                message.getStatus(), 
                message.getContent(), 
                senderId, 
                recipientId, 
                messageId, 
                conversationId
        );

        write.writeObject(newMessage);
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

        System.out.println("Username: ");
        String user = scanner.nextLine();

        System.out.println("Password: ");
        String pass = scanner.nextLine();

        if (client.login(user, pass)) {
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
                    client.sendMessage(new Message(
                        Message.Type.TEXT, 
                        Message.Status.REQUEST, 
                        in,
                        // we will fill these in the above sendMessage function.
                        -1,
                        -1, 
                        -1, 
                        null));
                }
            } while (!quit);
        }
    }
}
