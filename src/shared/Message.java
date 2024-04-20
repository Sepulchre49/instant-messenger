package shared;

import java.io.Serializable;
import java.util.Date;

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

    private Date timestamp;

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    private Type type;
    private Status status;
    private String content;

    public Message(int sender, Type t, Status s, String msg) {
        this.senderId = sender;
        this.type = t;
        this.status = s;
        this.content = msg;
        this.timestamp = new Date();
    }

    public int getSenderId() {
        return senderId;
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
