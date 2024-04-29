package client;

import shared.Message;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class GUI {
	public final Client client;
	private final LoginView loginView;
	public ConversationView conversationView;
	public HomeView homeView;
	public Map<Integer, List<Message>> messageMap;

	public GUI(Client client) {
		this.client = client;
		loginView = new LoginView(this);
		loginView.setVisible(true);
	}

	public void loginResult(boolean success) throws IOException {
		if (success) {
			System.out.println("Successfully logged in.");
			showHomeView();
		} else {
			JOptionPane.showMessageDialog(null, "Login Failed", "Login Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void logoutResult(boolean success) throws IOException {
		if (success) {
			conversationView.setVisible(false);
			System.exit(0);
		} else {
			JOptionPane.showMessageDialog(null, "Logout Failed", "Logout Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void showConversationView(int convoId, String rName) throws BadLocationException {
		if (homeView != null) {
			homeView.setVisible(false);
		}

		conversationView = new ConversationView(this, client.getConversation(convoId));
		conversationView.setVisible(true);
	}

	public void showHomeView() {
		if (loginView != null) {
			loginView.setVisible(false);
			loginView.dispose();
		}

		if (conversationView != null) {
			conversationView.setVisible(false);
		}

		homeView = new HomeView(this);
		homeView.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				boolean connected = false;
				Client client = null;

				while (!connected) {
					JTextField addressField = new JTextField();
					JTextField portField = new JTextField();

					Object[] message = { "Server address:", addressField, "Port number:", portField };

					int option = JOptionPane.showConfirmDialog(null, message, "Enter Server Information",
							JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						String serverAddress = addressField.getText();
						int portNumber;
						try {
							portNumber = Integer.parseInt(portField.getText());
						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null, "Invalid port number. Please enter a valid number.",
									"Input Error", JOptionPane.ERROR_MESSAGE);
							continue;
						}

						client = new Client(serverAddress, portNumber);

						try {
							client.connectToServer(serverAddress, portNumber);
							connected = true;
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null, "Error connecting to server. Please try again.",
									"Connection Error", JOptionPane.ERROR_MESSAGE);
							// Here you can add code to terminate the old connection attempt
						}
					} else {
						// User clicked cancel or closed the dialog
						break;
					}
				}

				if (connected) {
					Client.gui = new GUI(client);
				}
			}
		});
	}

}