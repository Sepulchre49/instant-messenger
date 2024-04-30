package client;

import java.util.Set;

import server.Conversation;
import shared.Message;

public class ClientUser {
    private int userId;
    private String username;

    private int conversationId; // New attribute

    private Set<Conversation> conversations;

    public ClientUser() {

    }
    
    public ClientUser(int userId, String username){ 
        this.userId = userId;
        this.username = username;
    }
    
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void setUsername(String username) {
    	this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    

    public boolean login() {
        return true;
    }
    
    public void receive(Message message) {
            
    }
    
    public void logout() {
            
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public void loadConversation() {
            
    }

}
