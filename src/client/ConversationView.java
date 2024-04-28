package client;

import shared.Message;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;


public class ConversationView extends JFrame {
    private final GUI gui;
//    public JTextArea chatArea;
    public JTextPane chatArea;

    /*The less important variables*/
    private JButton backButton;
    private JTextField messageField;
    private JButton sendButton;

    private JScrollPane scrollPane;
    private JLabel conversationLabel;
    private Conversation conversation;

    public ConversationView(GUI gui, Conversation conversation) throws BadLocationException {
        // main frame
        super("Conversation");

        this.gui = gui;
        this.conversation = conversation;

        setSize(625, 575);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // back button
        ImageIcon backIcon = new ImageIcon("client/icons/back.png");
        Image scaledBackImage = backIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        ImageIcon scaledBackIcon = new ImageIcon(scaledBackImage);
        backButton = new JButton(scaledBackIcon);
        backButton.setPreferredSize(new Dimension(25, 25));

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                conversation.setConversationView(null);
                gui.showHomeView();
            }
        });
        header.add(backButton, BorderLayout.WEST);

        // CID :: Recipient's name
        Integer[] receiverArray = conversation.getParticipants().toArray(new Integer[0]);
        StringBuilder recipientsText = new StringBuilder();

        for (int i = 0; i < receiverArray.length; i++) {
            String name = gui.client.usernameIdMap.get(receiverArray[i]);
            if (name != null) {
                recipientsText.append(name);
                if (i < receiverArray.length - 1) {
                    recipientsText.append(", ");
                }
            }
        }

        conversationLabel = new JLabel(String.format("[Conversation: %d] %s", conversation.getId(), recipientsText.toString()), SwingConstants.LEFT);
        conversationLabel.setFont(new Font("Arial", Font.BOLD, 15));
        conversationLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        header.add(conversationLabel, BorderLayout.CENTER);

        // collation of header
        add(header, BorderLayout.NORTH);

        // chat area and styles
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        conversation.setConversationView(this);

        for (Message m : conversation.getMessages()) {
            updateChatArea(m);
        }

        StyledDocument doc = chatArea.getStyledDocument();

        Style systemStyle = chatArea.addStyle("System", null);
        StyleConstants.setForeground(systemStyle, Color.LIGHT_GRAY);
        StyleConstants.setItalic(systemStyle,true);
        StyleConstants.setBold(systemStyle,true);

        scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // message field
        messageField = new JTextField();
        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage();
                } catch (IOException | ClassNotFoundException | BadLocationException ex) {
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
                } catch (IOException | ClassNotFoundException | BadLocationException ex) {
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
        doc.insertString(doc.getLength(), "[System] You can now send messages.\n", systemStyle);
    }

    private void sendMessage() throws IOException, ClassNotFoundException, BadLocationException {
        String message = messageField.getText().trim();

        if (message.equals("/quit")) {
            gui.logoutResult(gui.client.logout());
            return;
        }

        Message m = new Message(
                gui.client.user.getUserId(),
                new ArrayList<>(conversation.getParticipants()),
                Message.Type.TEXT,
                Message.Status.REQUEST,
                message,
                conversation.getId()
        );

        if (!message.isEmpty()) {
            gui.client.sendMessage(m);

            conversation.addMessage(m);

            // Clear the message field
            messageField.setText("");
        }
    }

    public void updateChatArea(Message message) throws BadLocationException {
        if (message.getType() == Message.Type.TEXT && !(message.getStatus() == Message.Status.RECEIVED)) {
            String timestamp = message.getTimestamp().toString();
            String[] parts = timestamp.split(" ");
            String truncatedTimestamp = parts[3];

            StyledDocument doc = chatArea.getStyledDocument();

            Style timestampStyle = chatArea.addStyle("Timestamp", null);
            StyleConstants.setForeground(timestampStyle, Color.GRAY);
            StyleConstants.setItalic(timestampStyle, true);

            Style nameSenderStyle = chatArea.addStyle("SenderName", null);
            StyleConstants.setForeground(nameSenderStyle, Color.RED);
            StyleConstants.setBold(nameSenderStyle, true);

            Style nameReciepientStyle = chatArea.addStyle("RecipientName", null);
            StyleConstants.setForeground(nameReciepientStyle, Color.BLUE);
            StyleConstants.setBold(nameReciepientStyle, true);

            Style messageStyle = chatArea.addStyle("Message", null);

            // creating the actual damn message
            doc.insertString(doc.getLength(), String.format("[%s] ",truncatedTimestamp), timestampStyle);
            doc.insertString(doc.getLength(), String.format("[UID%s] ", message.getSenderId()), timestampStyle);

            if (message.getSenderId() == gui.client.user.getUserId()) {
                doc.insertString(doc.getLength(), String.format("%s: ", gui.client.usernameIdMap.get(message.getSenderId())), nameSenderStyle);
            } else {
                doc.insertString(doc.getLength(), String.format("%s: ", gui.client.usernameIdMap.get(message.getSenderId())), nameReciepientStyle);
            }

            doc.insertString(doc.getLength(), message.getContent() + "\n", messageStyle);

        }
    }
}