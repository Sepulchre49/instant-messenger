package shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Message implements Serializable {
    public enum Type {
        LOGIN,
        LOGOUT,
        TEXT,
        CREATE_CONVERSATION
    }

    public enum Status {
        REQUEST,
        SUCCESS,
        FAILURE,
        ERROR,
        INVALID_ARGUMENT,
        RECEIVED
    }

    private int senderId;
    private Set<Integer> receiverIds;
    private Date timestamp;
    private Type type;
    private Status status;
    private String content;
    private int conversationId; // New field to hold Conversation ID

    public Message(int sender, Collection<Integer> recipients, Type t, Status s, String msg, int conversationId) {
        this.senderId = sender;
        this.receiverIds = new HashSet<>();
        this.type = t;
        this.status = s;
        this.content = msg;
        this.timestamp = new Date();
        this.conversationId = conversationId; // Initialize Conversation ID

        if (recipients != null) {
            for (int id : recipients) {
                receiverIds.add(id);
            }
        }
    }

    public int getSenderId() {
        return senderId;
    }

    public ArrayList<Integer> getReceiverIds() {
        return new ArrayList<>(receiverIds);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Type getType() {
        return type;
    }

    public Status getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }

    public int getConversationId() {
        return conversationId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Type: ").append(type).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Sender ID: ").append(senderId).append("\n");
        sb.append("Receiver IDs: ").append(receiverIds).append("\n");
        sb.append("Content: ").append(content).append("\n");
        return sb.toString();
    }
}
