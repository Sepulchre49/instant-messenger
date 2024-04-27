package client;

import shared.Message;

import javax.swing.*;
import java.util.*;

public class Conversation {
    private final int id;
    private final Set<Integer> participants;
    private ArrayList<Message> messages;
    private JTextArea chatArea;

    public Conversation(int id, Set<Integer> participants) {
        this.id = id;
        this.participants = participants;
        this.messages = new ArrayList<>();
    }
    public Conversation(int id, Set<Integer> participants, ArrayList<Message> messages) {
        this.id = id;
        this.participants = participants;
        this.messages = messages;
    }

    public void addMessage(Message m, Client client) {
        messages.add(m);
        if (chatArea != null)
            updateChatArea(m, client);
    }

    public int getId() {
        return id;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public Set<Integer> getParticipants() {
        return participants;
    }

    public void setChatArea(JTextArea chatArea) {
        this.chatArea = chatArea;
    }

    public JTextArea getChatArea() {
        return chatArea;
    }

    private void updateChatArea(Message message, Client client) {
        if (message.getType() == Message.Type.TEXT && !(message.getStatus() == Message.Status.RECEIVED)) {
            String timestamp = message.getTimestamp().toString();
            String[] parts = timestamp.split(" ");
            String truncatedTimestamp = parts[3];

            chatArea.append(String.format("[%s] [UID%s] %s: %s\n",
                    truncatedTimestamp,
                    message.getSenderId(),
                    client.usernameIdMap.get(message.getSenderId()),
                    message.getContent()));
        }
    }

}
