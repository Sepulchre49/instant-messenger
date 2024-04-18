package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shared.Message;

class Session implements Runnable {
    private Socket client;
    private String clientAddress;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Server server;
    private ServerUser user;

    public Session(Socket s, Server server) {
        this.server = server;
        client = s;
        clientAddress = client.getInetAddress().getHostAddress();
        System.out.println("Connection created with " + clientAddress);
        try {
            in = new ObjectInputStream(client.getInputStream());
            out = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error creating the object read/write streams.");
            e.printStackTrace();
        }
    }

    private void waitForLogin() {
        boolean isLoggedIn = false;
        do {
            try {
                System.out.println("Received new connection request from " + clientAddress);
                Message m = (Message) in.readObject();
                if (m.getType() == Message.Type.LOGIN)
                    isLoggedIn = handleLogin(m);
            } catch (IOException e) {
                System.out.println("Error reading object from the input stream while waiting for login message from "
                        + clientAddress);
            } catch (ClassNotFoundException e) {
                System.out.println(
                        "Could not find serialized class while waiting for login message from " + clientAddress);
                System.exit(1);
            }
        } while (!isLoggedIn);
    }

    private boolean handleLogin(Message m) throws IOException {
        boolean success = false;
        String regex = "username:\\s*(\\w{3,})\\s+password:\\s*(\\w{6,})";
        Matcher matcher = Pattern.compile(regex).matcher(m.getContent());

        Message res = new Message(
                    Message.Type.LOGIN, 
                    Message.Status.FAILURE, 
                    "Failed to log in!");

        if (matcher.find()) {
            String username = matcher.group(1);
            String password = matcher.group(2);
            String logMsg = String.format("Found username: %s, password: %s", username, password);

            user = server.login(username, password);

            if (user != null) {
                res = new Message(
                        Message.Type.LOGIN, 
                        Message.Status.SUCCESS, 
                        "Successfully logged in!");
                success = true;
            }
        }

        out.writeObject(res);
        return success;
    }

    private void handleDuplicateLogin() {
        System.out.println("Received duplicate login request from " + clientAddress);
        try {
            out.writeObject(new Message(Message.Type.LOGIN, Message.Status.FAILURE, "Nu uh uh"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        try {
            System.out.println("Logging out client at " + clientAddress);
            boolean success = server.logout(user);
            out.writeObject(new Message(
                        Message.Type.LOGOUT, 
                        Message.Status.SUCCESS, 
                        "You have been logged out of the server."));
            out.close();
            in.close();
            client.close();

            System.out.println("Successfully logged out client " + clientAddress);
        } catch (IOException e) {
            System.out.println("Error trying to close connection with " + clientAddress);
            e.printStackTrace();
        }
    }

    private void handleText(Message m) throws IOException {
        server.forward(m);
        out.writeObject(new Message(
                    Message.Type.TEXT,
                    Message.Status.SUCCESS,
                    "Message received."));
    }

    private void doMessageLoop() throws IOException {
        boolean quit = false;
        do {
            // Read a message
            try {
                Message m = (Message) in.readObject();
                switch (m.getType()) {
                    case TEXT:
                        handleText(m);
                        break;
                    case LOGOUT:
                        handleLogout();
                        quit = true;
                        break;
                    case LOGIN:
                        handleDuplicateLogin();
                        break;
                }
            } catch (IOException e) {
                System.out.println("Error while reading messages from " + clientAddress);
                System.out.println("Terminating connection with " + clientAddress);
                handleLogout();
                quit = true;
            } catch (ClassNotFoundException e) {
                System.out.println("Could not find serialized class for message from " + clientAddress);
                quit = true;
            }
        } while (!quit);
    }

    @Override
    public void run() {
        waitForLogin();
        try {
            doMessageLoop();
        } catch (IOException e) {
            System.out.println("An exception occured while processing message loop for client " + clientAddress);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
