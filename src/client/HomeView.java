package client;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.IOException;

public class HomeView extends JFrame {
    private final GUI gui;
    public DefaultListModel<JPanel> conListModel;

    public HomeView(GUI gui){
        super("Home");

        this.gui = gui;

        setSize(625, 575);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // home icon

        // home label
        JLabel headerLabel = new JLabel("Your Conversations", SwingConstants.LEFT);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 15));
        header.add(headerLabel, BorderLayout.WEST);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
        header.add(headerLabel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // add conversation button
        JButton newConButton = new JButton("New");
        newConButton.addActionListener(e -> {
            newConversation();
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

        // conlistmodel
        conListModel = new DefaultListModel<>();
        populateConversations("001", "Recipient Name");
        populateConversations("002", "someguy123");
        populateConversations("020", "xXx_destroyer_xXx");

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
                    System.out.println(conList.getSelectedIndex());
                    gui.showConversationView(-1); // TODO : Replace with real dynamic ID
                }
            }
        });

        setVisible(true);
    }

    private void logout() throws IOException, ClassNotFoundException {
        gui.client.logout();
        System.exit(1);
    }

    private void newConversation() {
    }

    private void populateConversations(String ID, String name) {
        // list item formatting TODO: must replace dynamic set from ClientUser
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.LINE_AXIS));

        JLabel idLabel = new JLabel("ID " + ID);
        JLabel nameLabel = new JLabel(name);

        int padding = 10;
        idLabel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));


        itemPanel.add(Box.createHorizontalStrut(10));
        itemPanel.add(idLabel);
        itemPanel.add(Box.createHorizontalStrut(40));
        itemPanel.add(nameLabel);

        conListModel.addElement(itemPanel);
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
