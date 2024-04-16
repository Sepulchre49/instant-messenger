package client;

import shared.Message;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private String host = "127.0.0.1";
	private int port = 5000;
	private final ClientUser user = new ClientUser();


	public void connectToServer(){
		try (Scanner scanner = new Scanner(System.in)){ // The instantiation of scanner
			System.out.println("Enter the host address to connect to: <127.0.0.1>");
			String inputHost = scanner.nextLine(); // To be replaced with an automatic scan of the input

			if (!inputHost.isEmpty()) {
				host = inputHost;
			}

			System.out.println("Enter the port number to connect to: <5000>");
			String inputPort = scanner.nextLine();

			if(!inputPort.isEmpty()){
				port = Integer.parseInt(inputPort);
			}

			// The attemption to connect to the server and handle comms. This has to work! PLEASE!!!!
			try (Socket socket = new Socket(host, port)){ // If this works, the socket will instantiate the write and read
				ObjectOutputStream write = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream read = new ObjectInputStream(socket.getInputStream());

				/*
				* do{
				* String usernameInput = scanner.nextLine();
				* String passwordInput = scanner.nextLine();
				*
				* } while(!login(usernameInput, passwordInput);)
				* */

				System.out.println("Connected to " + host + ":" + port);

			}

		} catch (IOException e){
			System.err.println("Error in I/O operations: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean login(String username, String password) throws IOException{
		return false; // STUB
	}

	public boolean logout(){
		//if button from GUI is pressed epic style:
		//writer.writeObject(logout)
		//print("Logging you out")
		//

		return false; // STUB
	}

	public void viewConversation(int conversationID){

	}

	public void sendMessage(Message message, ObjectOutputStream write, ObjectInputStream read) throws IOException{

	}

	public void receiveMessage(Message message){

	}



	public static void main(String[] args) {
		System.out.println("Hello from the client!");

		Client client = new Client();
		client.connectToServer();
	}
}
