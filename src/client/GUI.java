package client;

import shared.Message;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintStream;

public class GUI {
    public final Client client;
    private final LoginView loginView;
    public ConversationView conversationView;
    public HomeView homeView;

    public GUI(Client client) {
        this.client = client;
        loginView = new LoginView(this);
        loginView.setVisible(true);
    }

    public void loginResult(boolean success) throws IOException {
        if (success) {
            Client.OutQueue outQueue = new Client.OutQueue(client.write);
            Thread outThread = new Thread(outQueue);
            outThread.start();

            Client.InQueue inQueue = new Client.InQueue(client.read);
            Thread inThread = new Thread(inQueue);
            inThread.start();

            System.out.println("Successfully logged in.");
            showHomeView(this);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Login Failed",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void logoutResult(boolean success) throws IOException {
        if(success){
            conversationView.setVisible(false);
            System.exit(1);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Logout Failed",
                    "Logout Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showConversationView(int ID){
        if (homeView != null){
            homeView.setVisible(false);
        }


        conversationView = new ConversationView(this, ID);
        conversationView.setVisible(true);
    }

    public void showHomeView(GUI gui){
        if (loginView != null) {
            loginView.setVisible(false);
            loginView.dispose();
        }

        if (conversationView != null) {
            conversationView.setVisible(false);
        }

        homeView = new HomeView(gui);
        homeView.setVisible(true);
    }

    public void updateChatArea(Message message){
        if (message.getType() == Message.Type.TEXT && message.getStatus() == Message.Status.SUCCESS){
            conversationView.chatArea.append( "[Recipient]: " + message.getContent() + "\n");
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Client client = new Client();

                try {
                    client.connectToServer();
                } catch (IOException e) {
                    System.out.println("Error connecting to server.");
                    System.exit(1);
                }

                Client.gui = new GUI(client);
            }
        });
    }
}
