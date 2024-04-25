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
    private int conversationId; // Add conversationId field

    public Session(Socket s, Server server, int conversationId) throws IOException {
        this.server = server;
        client = s;
        clientAddress = client.getInetAddress().getHostAddress();
        System.out.println("Connection created with " + clientAddress);
        this.conversationId = conversationId; // Initialize conversationId

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
                Message m;
                synchronized (in) {
                    m = (Message) in.readObject();
                }
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
                    Server.SERVER_USER_ID,
                    null,
                    Message.Type.LOGIN, 
                    Message.Status.FAILURE, 
                    "Failed to log in!",
                    conversationId);

        if (matcher.find()) {
            String username = matcher.group(1);
            String password = matcher.group(2);

            user = server.login(username, password);

            if (user != null) {
                res = new Message(
                        Server.SERVER_USER_ID,
                        null,
                        Message.Type.LOGIN, 
                        Message.Status.SUCCESS, 
                        "Successfully logged in!",
                        conversationId);
                success = true;
                user.setOutputStream(out);
            }
        }

        try {
            synchronized (out) {
                out.writeObject(res);
            }
        } catch (IOException e) {
            System.err.println("Error writing login success message to " + clientAddress);
            e.printStackTrace();
        }

        return success;
    }

    private void handleDuplicateLogin() {
        System.out.println("Received duplicate login request from " + clientAddress);
        try {
            synchronized (out) {
                out.writeObject(new Message(
                            Server.SERVER_USER_ID,
                            null,
                            Message.Type.LOGIN, 
                            Message.Status.FAILURE, 
                            "Nu uh uh",
                            conversationId));
            }
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
            synchronized (out) {
                out.writeObject(new Message(
                            Server.SERVER_USER_ID,
                            new ArrayList<>(user.getUserId()),
                            Message.Type.LOGOUT, 
                            Message.Status.SUCCESS, 
                            "You have been logged out of the server.",
                            conversationId));
            }
            System.out.println("Successfully logged out client " + clientAddress);
        } catch (IOException e) {
            System.err.println("Error writing logout message to " + clientAddress);
            e.printStackTrace();
        }
    }

    private void terminate() {
        // TODO: Make an special connection termination message
        try {
            synchronized(out) {
                out.writeObject(new Message(
                            Server.SERVER_USER_ID,
                            new ArrayList<>(user.getUserId()),
                            Message.Type.LOGOUT,
                            Message.Status.SUCCESS,
                            "Terminating connection",
                            conversationId));
            }
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
        // Add conversationId to the message before forwarding
        m.setConversationId(conversationId);
        server.forward(m);
        try {
            synchronized (out) {
                out.writeObject(new Message(
                        Server.SERVER_USER_ID,
                        new ArrayList<>(user.getUserId()),
                        Message.Type.TEXT,
                        Message.Status.RECEIVED,
                        "Message received.",
                        conversationId));
            }
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
                Message m;
                synchronized (in) {
                    m = (Message) in.readObject();
                }
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
