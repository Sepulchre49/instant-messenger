package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ConversationView extends JFrame{
    private JFrame panel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton backButton;
    private JScrollPane scrollPane;
    private JLabel recipientLabel;

    public ConversationView(){
        // main frame
        super("Conversation");
        setSize(625, 575);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // back button
        backButton = new JButton("<");
        backButton.setPreferredSize(new Dimension(45,45));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("you went backwards");
            }
        });
        header.add(backButton, BorderLayout.WEST);

        // CID :: Recipient's name
        recipientLabel = new JLabel("[CID] [Recipient (s)]", SwingConstants.LEFT);
        recipientLabel.setBorder(BorderFactory.createEmptyBorder(0, 10 ,0, 0));
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
                sendMessage();
            }
        });

        // send button
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // footer panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        add(messagePanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void sendMessage(){
        // Get the text from the message field
        String message = messageField.getText().trim();

        if (!message.isEmpty()) {
            // Append the message to the chat area
            chatArea.append("[User]: " + message + "\n");

            // Clear the message field
            messageField.setText("");
        }
    }

    public static void main(String[] args){ // Another testing function. I really want to drink a soda
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ConversationView();
            }
        });
    }
}