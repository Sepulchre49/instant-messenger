package server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import shared.Message;

public class Conversation {
    private int id;
    private Set<ServerUser> participants;
    private List<Message> messages;
    private File log;

    public Conversation(int id, Set<ServerUser> participants, File log) {
        this.id = id;
        this.participants = participants;
        this.log = log;
        this.messages = new ArrayList<>();
    }

    public void addMsg(Message message) {
        this.messages.add(message);
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
