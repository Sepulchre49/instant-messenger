package client;

import shared.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;


public class ConversationView extends JFrame {
    private final GUI gui;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton backButton;
    private JScrollPane scrollPane;
    private JLabel recipientLabel;

    public ConversationView(GUI gui) {
        // main frame
        super("Conversation");

        this.gui = gui;
        setSize(625, 575);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // back button
        ImageIcon backIcon = new ImageIcon("client/icons/back.png");
        Image scaledBackImage = backIcon.getImage().getScaledInstance(25,25,Image.SCALE_SMOOTH);
        ImageIcon scaledBackIcon = new ImageIcon(scaledBackImage);
        backButton = new JButton(scaledBackIcon);
        backButton.setPreferredSize(new Dimension(25, 25));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("you went backwards");
            }
        });
        header.add(backButton, BorderLayout.WEST);

        // CID :: Recipient's name
        recipientLabel = new JLabel("[CID] [Recipient (s)]", SwingConstants.LEFT);
        recipientLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        header.add(recipientLabel, BorderLayout.CENTER);

        // collation of header
        add(header, BorderLayout.NORTH);

        // chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // message field
        messageField = new JTextField();
        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage();
                } catch (IOException | ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // send button
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage();
                } catch (IOException | ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // footer panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        add(messagePanel, BorderLayout.SOUTH);
        setVisible(true);

        // opening message
        chatArea.append("System: Enter a message, or type '/quit' to quit");
    }

    private void sendMessage() throws IOException, ClassNotFoundException {
        // Get the text from the message field
        String message = messageField.getText().trim();

        if (!message.isEmpty()) {
            // Append the message to the chat area
            chatArea.append("[User]: " + message + "\n");

            gui.client.sendMessage(new Message(
                    0,
                    new ArrayList<>() {{
                        add(1); // TODO : Hardcoded value. Must be replaced with actual recipient.
                    }},
                    Message.Type.TEXT,
                    Message.Status.REQUEST,
                    message
            ));

            // Clear the message field
            messageField.setText("");
        }
    }

    private void updateList(Message message){

    }
}