package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginView extends JFrame implements ActionListener {
    private final GUI gui;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;

    public LoginView(GUI gui) {
        super("Messenger Login");
        this.gui = gui;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 150);
        setLocationRelativeTo(null);

        // Padding
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding around all components
        add(panel);

        // Components
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        loginButton = new JButton("Login");

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(new JLabel()); // Spacer
        panel.add(loginButton);

        // Add action listener to the button
        loginButton.addActionListener(this);

        // "Enter" button functionality
        getRootPane().setDefaultButton(loginButton);

        // Show the frame
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            boolean success;

            try {
                success = gui.client.login(username, password);
            } catch (IOException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }

            gui.loginResult(success);
        }
    }
}
