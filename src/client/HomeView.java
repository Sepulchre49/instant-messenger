package client;

import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomeView extends JFrame {
    private final GUI gui;

    public HomeView(GUI gui){
        super("Home");

        this.gui = gui;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // home icon

        // Home label
        JLabel headerLabel = new JLabel("Your Conversations");
    }

}
