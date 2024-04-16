package client;

import shared.Message;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private final Scanner scanner = new Scanner(System.in); //to be removed after GUI implementation.
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
		return false;
	}

	public boolean logout(){
		return false;
	}

	public void viewConversation(int conversationID){

	}

	public void sendMessage(Message message) throws IOException{

	}

	public void receiveMessage(Message message){

	}



	public static void main(String[] args) throws IOException {
		System.out.println("Hello from the client!");

		Client client = new Client();
		client.connectToServer();
	}
}
