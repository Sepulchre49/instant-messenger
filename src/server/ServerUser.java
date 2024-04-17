package server;

public class ServerUser {
    private String username, password;
    private boolean isLoggedIn;

    public ServerUser(String username, String password) {
	this.username = username;
	this.password = password;
	this.isLoggedIn = false;
    }

    public boolean authenticate(String password) {
	return this.password.equals(password);
    }

    public String getUsername() {
	return username;
    }

    public boolean isLoggedIn() {
	return isLoggedIn;
    }

    public synchronized void login() {
	this.isLoggedIn = true;
    }

    public synchronized void logout() {
	this.isLoggedIn = false;
    }
}
