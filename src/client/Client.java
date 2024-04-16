package client;

import shared.Message;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private String host;
	private int port;
	private Socket socket;
	private ObjectOutputStream write;
	private ObjectInputStream read;

	private final ClientUser user = new ClientUser();
	private final GUI clientGUI = new GUI();

    public Client() {
		this.host = "127.0.0.1";
		this.port = 3000;
    }


    public void connectToServer() throws IOException {
		Scanner scanner = new Scanner(System.in); //to be removed after GUI implementation.

		try {
			System.out.println("Enter the host address to connect to: <127.0.0.1>");
			String inputHost = scanner.nextLine();
			if (!inputHost.isEmpty()) {
				this.host = inputHost;
			}

			System.out.println("Enter the port number to connect to: <3000>");
			String inputPort = scanner.nextLine();
			if (!inputPort.isEmpty()) {
				this.port = Integer.parseInt(inputPort);
			}

			this.socket = new Socket(host, port);
			this.write = new ObjectOutputStream(socket.getOutputStream());
			this.read = new ObjectInputStream(socket.getInputStream());

		} catch (IOException e){
			System.err.println("Error in I/O operations: " + e.getMessage());
			e.printStackTrace();

		}
	}

	public boolean login(String username, String password){
//		boolean success = false;
//		Message login = new Message(Message.Type.LOGIN, Message.Status.SENT, "username:" + username + "password: " + password);
//		write.writeObject(login)
//
//		Message loginReceipt = (Message) read.readObject();
//		if (loginReceipt.getType() == loginReceipt.Type.LOGIN && loginReceipt.getStatus() == loginReceipt.Status.SUCCESS){
//			success = true;
//			System.out.println("You've logged in!");
//		} else { System.out.println("Wrong credentials. Try again."); }
//
//		return success;
//
		return false; // STUB: REMOVE
	}

	public boolean logout(){
//		boolean success = false;
//		Message logout = new Message(Message.Type.LOGOUT, Message.Status.SENT, "Logout Request");
//		write.writeObject(logout);

//		Message logoutReceipt = (Message) read.readObject();
//		if (logoutReceipt.getType() == logoutReceipt.Type.LOGOUT && logoutReceipt.getStatus() == logoutReceipt.Status.Success){
//			success = true;
//			read.close();
//			write.close();
//			socket.close();
//			System.out.println("Logging out... See you again soon!");
//		}
//		return success;

		return false; // STUB: REMOVE
	}

	public void viewConversation(int conversationID){
	}

	public void sendMessage(Message message) throws IOException {
//		write.writeObject(message);
//		Message messageReceipt = (Message) read.readObject();
//
//
//
//
	}

	public void receiveMessage(Message message){
//		if (message.getType() == message.Type.TEXT && message.getStatus() == message.Status.RECEIVED){
//			System.out.println("Recipient: " + message.getText());
//		} else { System.out.println("Uh oh... Something wicked comes."); }
	}



	public static void main(String[] args) throws IOException {
		System.out.println("Hello from the client!");
		Scanner scanner = new Scanner(System.in);

		Client client = new Client();
		client.connectToServer();

		System.out.println("Username: ");
		String user =  scanner.nextLine();

		System.out.println("Password: ");
		String pass = scanner.nextLine();

		String line;
//		if(client.login(user, pass)){
//			while (!(line = scanner.nextLine().equalsIgnoreCase("logout"))){
//				Message message = new Message(message.Type.text, message.Status.SENT, line);
//				receiveMessage(message);
//			}
//		}
	}
}
