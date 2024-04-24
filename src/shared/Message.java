package shared;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Message implements Serializable {
    public enum Type {
        LOGIN,
        LOGOUT,
        TEXT
    }

    public enum Status {
        REQUEST,
        SUCCESS,
        FAILURE,
        ERROR,
        INVALID_ARGUMENT,
        RECEIVED
    }

    private static int count = 1;
    private int senderId;
    private Set<Integer> receiverIds;
    private Date timestamp;
    private Type type;
    private Status status;
    private String content;
    private int messageId;
    private int conversationId;

    public Message(Type t, Status s, String msg, int senderId, Collection<Integer> recipients, int conversationId) {
        this.senderId = senderId;
        this.receiverIds = new HashSet<>();
        this.type = t;
        this.status = s;
        this.content = msg;
        this.timestamp = new Date();
        this.conversationId = conversationId;

        if (recipients != null) {
            for (int id : recipients) {
                receiverIds.add(id);
            }
        }
    }

    public int getSenderId() {
        return senderId;
    }

    public Set<Integer> getReceiverIds() {
        return receiverIds;
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

    public int getMessageId() {
        return messageId;
    }

    public int getConversationId() {
        return conversationId;
    }
}
