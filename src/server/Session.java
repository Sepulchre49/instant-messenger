package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shared.Message;

class LoginException extends Exception {
    public LoginException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

class MessageLoopException extends Exception {
    public MessageLoopException(String msg, Throwable cause) {
        super(msg, cause);
    }
}


class Session implements Runnable {
    private Socket client;
    private String clientAddress;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Server server;
    private ServerUser user;

    public Session(Socket s, Server server) throws IOException {
        this.server = server;
        client = s;
        clientAddress = client.getInetAddress().getHostAddress();
        System.out.println("Connection created with " + clientAddress);

        try {
            in = new ObjectInputStream(client.getInputStream());
            out = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException e) {
            throw new IOException("Error creating the object read/write streams.", e);
        }
    }

    private boolean waitForLogin() throws LoginException {
        int attempts = 3;
        boolean success = false;
        while (!success && attempts > 0) {
            try {
                Message m = (Message) in.readObject();
                if (m.getType() == Message.Type.LOGIN)
                    success = handleLogin(m);
                if (!success)
                    --attempts;
            } catch (IOException e) {
                throw new LoginException("Error reading message from " + client + " in the login loop.", e);
            } catch (ClassNotFoundException e) {
                throw new LoginException("Error trying to cast to Message class in the login loop.", e);
            }
        }
        return success;
    }

    private boolean handleLogin(Message m) {
        boolean success = false;
        String regex = "username:\\s*(\\w+)\\s+password:\\s*(\\w+)";
        Matcher matcher = Pattern.compile(regex).matcher(m.getContent());

        Message res = new Message(
                    0,
                    null,
                    Message.Type.LOGIN, 
                    Message.Status.FAILURE, 
                    "Failed to log in!");

        if (matcher.find()) {
            String username = matcher.group(1);
            String password = matcher.group(2);

            user = server.login(username, password);

            if (user != null) {
                res = new Message(
                        0, // 0 means server is sender
                        null,
                        Message.Type.LOGIN, 
                        Message.Status.SUCCESS, 
                        "Successfully logged in!");
                success = true;
            }
        }

        try {
            out.writeObject(res);
        } catch (IOException e) {
            System.err.println("Error writing login success message to " + clientAddress);
            e.printStackTrace();
        }

        return success;
    }

    private void handleDuplicateLogin() {
        System.out.println("Received duplicate login request from " + clientAddress);
        try {
            out.writeObject(new Message(
                        0,
                        null,
                        Message.Type.LOGIN, 
                        Message.Status.FAILURE, 
                        "Nu uh uh"));
        } catch (IOException e) {
            System.err.println("Error trying to write login failure message to " + client);
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        if (user == null) return;

        try {
            System.out.println("Logging out client at " + clientAddress);
            server.logout(user);
            out.writeObject(new Message(
                        0,
                        new ArrayList<>(user.getUserId()),
                        Message.Type.LOGOUT, 
                        Message.Status.SUCCESS, 
                        "You have been logged out of the server."));
            System.out.println("Successfully logged out client " + clientAddress);
        } catch (IOException e) {
            System.err.println("Error writing logout message to " + clientAddress);
            e.printStackTrace();
        }
    }

    private void terminate() {
        // TODO: Make an special connection termination message
        try {
            out.writeObject(new Message(
                        0,
                        new ArrayList<>(user.getUserId()),
                        Message.Type.LOGOUT,
                        Message.Status.SUCCESS,
                        "Terminating connection"));
        } catch (IOException e) {
            System.out.println("Error sending termination message. Proceeding with termination.");
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                System.err.println("Stupid error trying to close the client socket.");
                e.printStackTrace();
            }
        }
    }

    private void handleText(Message m) {
        server.forward(m);
        try {
            out.writeObject(new Message(
                    0,
                    new ArrayList<>(user.getUserId()),
                    Message.Type.TEXT,
                    Message.Status.RECEIVED,
                    "Message received."));
        } catch (IOException e) {
            System.err.println("Error sending message received acknowledgement to " + clientAddress);
            e.printStackTrace();
        }
    }

    private void doMessageLoop() throws MessageLoopException {
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
                throw new MessageLoopException("Error reading message from " + clientAddress, e);
            } catch (ClassNotFoundException e) {
                throw new MessageLoopException("Error trying to cast to Message class in the message loop.", e);
            }
        } while (!quit);
    }

    @Override
    public void run() {
        try {
            if (waitForLogin()) {
                doMessageLoop();
            }
        } catch (LoginException | MessageLoopException e) {
            e.printStackTrace();
        } finally {
            terminate();
        }
    }
}
