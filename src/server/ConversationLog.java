package server;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import shared.Message;

public class ConversationLog {
    private Date timeStamp;
    private int senderID;
    private int recipientID;
    private int messageID;
    private int conversationID;
    private List<Message> messages;

    public ConversationLog(int senderID, int recipientID, int messageID, int conversationID) {
        this.timeStamp = new Date();
        this.senderID = senderID;
        this.recipientID = recipientID;
        this.messageID = messageID;
        this.conversationID = conversationID;
        this.messages = new ArrayList<>();
    }

    public Date getTimeStamp() {
        return this.timeStamp;
    }

    public int getSenderID() {
        return this.senderID;
    }

    public int getRecipientID() {
        return this.recipientID;
    }

    public int getMessageID() {
        return this.messageID;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void deleteMessage(Message message) {
        this.messages.remove(message);
    }
}

