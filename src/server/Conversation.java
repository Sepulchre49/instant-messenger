package server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import shared.Message;

public class Conversation {
    private int count = 1;
    private int id; // Conversation ID
    private Set<ServerUser> participants;
    private List<Message> messages;
    private File log;

    public Conversation(Set<ServerUser> participants, File log) {
        this.id = count++;
        this.participants = participants;
        this.log = log;
        this.messages = new ArrayList<>();

        // Add this conversationId to each ServerUser's conversations set.
        for (ServerUser participant : participants)
            participant.addConversation(this);
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

    public Set<ServerUser> getParticipants() {
        return participants;
    }
}
