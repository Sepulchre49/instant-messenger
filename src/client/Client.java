package client;

import server.ServerUser;
import shared.Message;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable {
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


    public void connectToServer() {
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
        boolean success = false;
        Message login = new Message(Message.Type.LOGIN, Message.Status.REQUEST, "username:" + username + "password: " + password);
        write.writeObject(login);

        Message loginReceipt = (Message) read.readObject();
        if (loginReceipt.getType() == Message.Type.LOGIN && loginReceipt.getStatus() == Message.Status.SUCCESS) {
            success = true;
            System.out.println("You've logged in!");
        } else {
            System.out.println("Wrong credentials. Try again.");
        }

        return success;

    }

    public boolean logout() throws IOException, ClassNotFoundException {
        boolean success = false;
        Message logout = new Message(Message.Type.LOGOUT, Message.Status.REQUEST, "Logout Request");
        write.writeObject(logout);

        Message logoutReceipt = (Message) read.readObject();
        if (logoutReceipt.getType() == Message.Type.LOGOUT && logoutReceipt.getStatus() == Message.Status.SUCCESS) {
            success = true;
            read.close();
            write.close();
            socket.close();
            System.out.println("Logging out... See you again soon!");
        }
        return success;
    }

    public void viewConversation(int conversationID) {
    }

    public void sendMessage(Message message) throws IOException, ClassNotFoundException {
        write.writeObject(message);
    }

    public void receiveMessages() throws IOException, ClassNotFoundException {
        Message message = (Message) read.readObject();
        if (message != null) {
            switch (message.getType()) {
                case TEXT:
                    if (message.getStatus() == Message.Status.SUCCESS) {
                        System.out.println("Message from the server: " + message.getContent());
                    }
                    break;

                default:
                    System.out.println("Received an unknown type of message! Sorry!");
                    break;
            }
        } else {
            System.out.println("Received a null message.");
        }
    }

    @Override
    public void run() { // Listens for messages on a separate thread. It's epic.
        System.out.println("Thread has started!");
        try {
            while (true) {
                receiveMessages();
            }
        } catch (IOException e) {
            System.out.println("IOException in receiveMessage!");
            e.printStackTrace();

        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in receiveMessage!");
            e.printStackTrace();
        }
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
            String message;

            Thread listener = new Thread(client); // Will now listen for messages.
            listener.start();

            System.out.println("You can now print messages! Type 'logout' to logout!");
            while (!(message = scanner.nextLine()).equalsIgnoreCase("logout")) {
                Message textMessage = new Message(Message.Type.TEXT, Message.Status.REQUEST, message);
                client.sendMessage(textMessage);
            }

            client.logout();
        } else {
            System.out.println("Uh oh, something's gone terribly wrong with logging in/out!");
        }
    }
}