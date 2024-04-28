package client;

import shared.Message;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeView extends JFrame {
    private final GUI gui;
    public DefaultListModel<JPanel> conListModel;

    public HomeView(GUI gui) {
        super("Home");

        this.gui = gui;

        setSize(625, 575);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // home icon

        // home label
        JLabel headerLabel = new JLabel("Your Conversations", SwingConstants.LEFT);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 15));
        header.add(headerLabel, BorderLayout.WEST);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        header.add(headerLabel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // add conversation button
        JButton newConButton = new JButton("New");
        newConButton.addActionListener(e -> {
            new UserSelectionDialog(this);
        });
        buttonsPanel.add(newConButton, BorderLayout.EAST);

        // logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            try {
                logout();
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println("Error logging out through the GUI");
                throw new RuntimeException(ex);
            }
        });
        buttonsPanel.add(logoutButton, BorderLayout.EAST);

        // adding buttons to the header
        header.add(buttonsPanel, BorderLayout.EAST);

        // adding the header to the frame
        add(header, BorderLayout.NORTH);

        // conlistmodel and population
        conListModel = new DefaultListModel<>();
        for (Conversation conversation : gui.client.getConversations()) {
            populateConversations(conversation.getId());
        }

        // conversation list
        JList<JPanel> conList = new JList<>(conListModel);
        conList.setCellRenderer(new ConversationCellRenderer());

        conList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        JScrollPane scroll = new JScrollPane(conList);
        add(scroll, BorderLayout.CENTER);

        // clickable list
        conList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    JPanel selected = conList.getSelectedValue();
                    if (selected != null) {
                        JLabel idLabel = (JLabel) selected.getComponent(1);  // ID is the second component
                        JLabel nameLabel = (JLabel) selected.getComponent(3);  // Name is the fourth component

                        String idText = idLabel.getText();   // ID text
                        String nameText = nameLabel.getText();  // Name

                        String idNumberStr = idText.substring(idText.indexOf("ID ") + 3).trim();
                        int RID = Integer.parseInt(idNumberStr);

                        System.out.println("Selected ID: " + RID + ", Name: " + nameText);
                        gui.showConversationView(RID, nameText);
                    }
                }
            }
        });

        setVisible(true);
    }

    private void logout() throws IOException, ClassNotFoundException {
        gui.client.logout();
        System.exit(1);
    }

    private void populateConversations(int conversationId) {
        // list item formatting TODO: must replace dynamic set from ClientUser
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.LINE_AXIS));

        JLabel idLabel = new JLabel("ID " + conversationId);
        JLabel nameLabel = new JLabel("Usernames go here"); // TODO: Add usernames of all users to the convo

        int padding = 10;
        idLabel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));


        itemPanel.add(Box.createHorizontalStrut(10));
        itemPanel.add(idLabel);
        itemPanel.add(Box.createHorizontalStrut(40));
        itemPanel.add(nameLabel);

        conListModel.addElement(itemPanel);
    }

    public class UserSelectionDialog extends JDialog{
        private JList<String> userList;
        private DefaultListModel<String> userModel;
        private JButton createButton;

        public UserSelectionDialog(Frame owner) {
            super(owner, "Select Users for Conversing", true);
            setSize(300, 400);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            userModel = new DefaultListModel<>();
            for(Map.Entry<Integer, String> entry : gui.client.usernameIdMap.entrySet()){
                userModel.addElement(String.format("[UID%s] %s", entry.getKey(), entry.getValue()));
            }

            userList = new JList<>(userModel);
            userList.setCellRenderer(new ConversationCellRenderer());
            userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            JScrollPane scrollPane = new JScrollPane(userList);
            add(scrollPane, BorderLayout.CENTER);

            createButton = new JButton("Create New Conversation");
            createButton.addActionListener(this::createConversation); // create conversation...
            add(createButton, BorderLayout.SOUTH);

            setVisible(true);
        }

        private void createConversation(ActionEvent event) {
            // TODO actually instantiate the conversation
            java.util.List<String> selectedUsers = userList.getSelectedValuesList();
            Set<Integer> participants = new HashSet<>(gui.client.user.getUserId());
            for(String pair : selectedUsers.toArray(new String[0])){
                String[] parts = pair.split("\\] ");
                String uid = parts[0].substring(4);  // Skips the initial "[UID" to start at the number
                participants.add(Integer.parseInt(uid));
                String name = parts[1];  // Removes the ';' at the end

                //System.out.println("Creating conversation for:");
                //System.out.println("UID: " + uid + ", Name: " + name);
            }


            gui.client.outbound.out.add(new Message(
                    gui.client.user.getUserId(),
                    participants,
                    Message.Type.CREATE_CONVERSATION,
                    Message.Status.REQUEST,
                    "",
                    -1));

            boolean done = false;
            do {
                if (!gui.client.inbound.in.isEmpty()) {
                    Message res = gui.client.inbound.in.poll();
                    if (res.getType() == Message.Type.CREATE_CONVERSATION && res.getStatus() == Message.Status.SUCCESS) {
                        int convoId = Integer.parseInt(res.getContent());
                        gui.client.addConversation(convoId, participants);
                        populateConversations(convoId);
                        done = true;
                    } else {
                        gui.client.inbound.in.add(res);
                    }
                }

            } while (!done);

            dispose();
        }
    }

    private static class ConversationCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof JPanel) {
                JPanel panel = (JPanel) value;
                if (isSelected) {
                    panel.setBackground(Color.LIGHT_GRAY);
                } else {
                    panel.setBackground(Color.WHITE);
                }
                return panel;
            }
            return component;
        }
    }
}
