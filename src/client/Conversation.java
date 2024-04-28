package client;

import shared.Message;

import javax.swing.text.BadLocationException;
import java.util.*;

public class Conversation {
    private final int id;
    private final Set<Integer> participants;
    private ArrayList<Message> messages;
    private ConversationView conversationView;

    public Conversation(int id, Set<Integer> participants) {
        this.id = id;
        this.participants = participants;
        this.messages = new ArrayList<>();
    }
    public Conversation(int id, Set<Integer> participants, ArrayList<Message> messages) {
        this.id = id;
        this.participants = participants;
        this.messages = messages;
    }

    public void addMessage(Message m) throws BadLocationException {
        messages.add(m);
        if (conversationView != null)
            conversationView.updateChatArea(m);
    }

    public int getId() {
        return id;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public Set<Integer> getParticipants() {
        return participants;
    }

    public void setConversationView(ConversationView view) {
        conversationView = view;
    }

    public ConversationView getConversationView() {
        return conversationView;
    }
}
