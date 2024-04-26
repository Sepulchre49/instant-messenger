package client;
import java.util.Set;

import server.Conversation;
import shared.Message;

public class ClientUser {
    private int userId;
    private String username;
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
    
    public String getUsername() {
        return username;
    }
    
    public void loadConversation() {
            
    }

}

