package server;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import shared.Message;

public class ConversationLog {
	private Date timeStamp;
	private int senderID;
	private int recipientID;
	private int messageID;
	private int conversationID;
	private List<Message> messages;
	private Set<ServerUser> participants;

	public ConversationLog(int senderID, int recipientID, int messageID, int conversationID,
			Set<ServerUser> participants) {
		this.timeStamp = new Date();
		this.senderID = senderID;
		this.recipientID = recipientID;
		this.messageID = messageID;
		this.conversationID = conversationID;
		this.messages = new ArrayList<>();
		this.participants = participants;
	}

	public Date getTimeStamp() {
		return this.timeStamp;
	}

	public int getSenderID() {
		return this.senderID;
	}

	public int getRecipientID() {
		return this.recipientID;
	}

	public int getMessageID() {
		return this.messageID;
	}

	public int getConversationID() {
		return this.conversationID;
	}

	public void addMessage(Message message) {
		this.messages.add(message);
	}

	public void deleteMessage(Message message) {
		this.messages.remove(message);
	}

	public void writeLogToFile(File file) {
		String filename = file.getAbsolutePath(); // Convert File object to string path
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));

			writer.write("Timestamp: " + this.getTimeStamp().toString());
			writer.newLine();
			writer.write("Sender ID: " + this.getSenderID());
			writer.newLine();
			writer.write("Recipient ID: " + this.getRecipientID());
			writer.newLine();
			writer.write("Message ID: " + this.getMessageID());
			writer.newLine();
			writer.write("Conversation ID: " + this.getConversationID());
			writer.newLine();

			writer.write("Participants: ");
			for (ServerUser participant : this.participants) {
				writer.write(participant.getUsername() + ", ");
			}
			writer.newLine();

			for (Message message : this.messages) {
				// Check if the message is not a login or logout request
				if (message.getType() != Message.Type.LOGIN && message.getType() != Message.Type.LOGOUT) {
					writer.write("Message: " + message.toString());
					writer.newLine();
				}
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
