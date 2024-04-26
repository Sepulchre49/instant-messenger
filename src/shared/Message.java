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

    private int senderId;
    private Set<Integer> receiverIds;
    private Date timestamp;
    private Type type;
    private Status status;
    private String content;

    public Message(int sender, Collection<Integer> recipients, Type t, Status s, String msg) {
        this.senderId = sender;
        this.receiverIds = new HashSet<>();
        this.type = t;
        this.status = s;
        this.content = msg;
        this.timestamp = new Date();

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
}
