package shared;

import java.io.Serializable;

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
        INVALID_ARGUMENT
    }

    private Type type;
    private Status status;
    private String content;
    private int senderId;
    private int recipientId;
    private int messageId;
    private String conversationId;

    public Message(Type t, Status s, String msg, int senderId, int recipientId, int messageId, String conversationId) {
        this.type = t;
        this.status = s;
        this.content = msg;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.messageId = messageId;
        this.conversationId = conversationId;
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

    public int getSenderId() {
        return senderId;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getConversationId() {
        return conversationId;
    }
}
