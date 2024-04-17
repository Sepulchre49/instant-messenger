package client;
import java.util.Set;

public class ClientUser {
	private int userId;
	private String username;
	private Set<Conversation> convrsations;
	
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
	
	public boolean login() {
		return true;
	}
	
	public void receive(Message message) {
		
	}
	
	public void logout() {
		
	}
	
	public void loadConversation() {
		
	}

}

