package server;

import java.util.*;

import shared.Message;

public class ConversationLog {
    private Date timeStamp;
    private int senderID;
    private HashSet<Integer> recipientIDs;
    private int messageID;
    private int conversationID;
    private List<Message> messages;

    public ConversationLog(int senderID, Collection<Integer> recipientIDs, int messageID, int conversationID) {
        this.timeStamp = new Date();
        this.senderID = senderID;
        this.recipientIDs = new HashSet<>(recipientIDs);
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

    public Iterator<Integer> getRecipientID() {
        return recipientIDs.iterator();
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

