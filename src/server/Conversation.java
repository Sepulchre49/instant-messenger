package server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import shared.Message;

public class Conversation {
    private int id; // Conversation ID
    private Set<ServerUser> participants;
    private List<Message> messages;
    private File log;

    public Conversation(int id, Set<ServerUser> participants, File log) {
        this.id = id;
        this.participants = participants;
        this.log = log;
        this.messages = new ArrayList<>();
    }

    // Method to add a message to the conversation
    public void addMsg(Message message) {
        if (message.getConversationId() == this.id) {
            this.messages.add(message);
        } else {
            throw new IllegalArgumentException("Message conversationId does not match Conversation's id");
        }
    }

    public List<Message> getAllMsgs() {
        return this.messages;
    }

    public Message getLastMsg() {
        return this.messages.get(this.messages.size() - 1);
    }

    public int getID() {
        return this.id;
    }

    public File getLog() {
        return this.log;
    }
}
