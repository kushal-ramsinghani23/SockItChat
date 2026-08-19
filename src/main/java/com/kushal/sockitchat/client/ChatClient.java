package com.kushal.sockitchat.client;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    private Socket socket;
    private PrintWriter out;
    private String username;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JTextField usernameField;
    private JTextField serverIPField;
    private JButton connectButton;

    private JPanel sidebarPanel;
    private JPanel chatPanel;
    private JPanel topBarPanel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    // Promoted to instance fields — needed in connectToServer()
    private JFrame frame;
    private JLabel userProfileName;
    private JLabel avatarLabel;

    public ChatClient() {
        frame = new JFrame("SockItChat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 550);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- Login Panel ---
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("SockItChat", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        loginPanel.add(new JLabel("Server IP:"), gbc);
        serverIPField = new JTextField("localhost", 15);
        gbc.gridx = 1;
        loginPanel.add(serverIPField, gbc);

        connectButton = new JButton("Connect to Chatroom");
        connectButton.putClientProperty("JButton.buttonType", "roundRect");
        connectButton.setBackground(new Color(37, 211, 102));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        loginPanel.add(connectButton, gbc);

        mainPanel.add(loginPanel, "login");

        // --- Sidebar ---
        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.DARK_GRAY));

        DefaultListModel<String> contactModel = new DefaultListModel<>();
        contactModel.addElement("Global Chatroom");
        JList<String> contactList = new JList<>(contactModel);

        JPanel contactHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        JLabel contactsTitle = new JLabel("Contacts");
        contactsTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contactHeaderPanel.add(contactsTitle);
        sidebarPanel.add(contactHeaderPanel, BorderLayout.NORTH);
        sidebarPanel.add(new JScrollPane(contactList), BorderLayout.CENTER);

        // Profile bar — instance fields
        JPanel userProfilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        userProfilePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));

        avatarLabel = new JLabel("?", JLabel.CENTER);
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(Color.DARK_GRAY);
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setPreferredSize(new Dimension(32, 32));

        userProfileName = new JLabel("Connecting...");
        userProfileName.setFont(new Font("Segoe UI", Font.BOLD, 13));

        userProfilePanel.add(avatarLabel);
        userProfilePanel.add(userProfileName);
        sidebarPanel.add(userProfilePanel, BorderLayout.SOUTH);

        // --- Chat Panel ---
        chatPanel = new JPanel(new BorderLayout());

        topBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        topBarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
        JLabel activeContactLabel = new JLabel("Global Chatroom");
        activeContactLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topBarPanel.add(activeContactLabel);
        chatPanel.add(topBarPanel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messageField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        JPanel fullChatWindow = new JPanel(new BorderLayout());
        fullChatWindow.add(sidebarPanel, BorderLayout.WEST);
        fullChatWindow.add(chatPanel, BorderLayout.CENTER);
        mainPanel.add(fullChatWindow, "chat");

        frame.add(mainPanel);
        frame.setVisible(true);

        // --- Connect Button ---
        connectButton.addActionListener(e -> {
            String typedUsername = usernameField.getText().trim();
            if (!typedUsername.isEmpty()) {
                // Disable to prevent double clicks
                connectButton.setEnabled(false);
                // Background thread — prevents EDT freeze during socket connection
                new Thread(() -> connectToServer()).start();
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a username.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        
        // 1. Define the sending action logic
        ActionListener sendAction = e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty() && out != null) {
                out.println(message);
                // Show own message locally
                chatArea.append("You: " + message + "\n");
                messageField.setText("");
            }
        };

        // 2. Assign the same action to BOTH the button click and the text field Enter key
        sendButton.addActionListener(sendAction);
        messageField.addActionListener(sendAction);

    }

    private void connectToServer() {
        String ip = serverIPField.getText().trim();
        this.username = usernameField.getText().trim();

        try {
            // 1. Open socket — blocking call, safe on background thread
            socket = new Socket(ip, 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 2. Send username as first message
            out.println(username);

            // 3. Switch to chat screen on EDT
            SwingUtilities.invokeLater(() -> {
                cardLayout.show(mainPanel, "chat");
                frame.setTitle("SockItChat — " + username);
                userProfileName.setText(username);
                avatarLabel.setText(String.valueOf(username.charAt(0)).toUpperCase());
            });

            // 4. Reader thread — listens for incoming messages forever
            Thread readerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        final String msg = message;
                        // Must update Swing components on EDT only
                        SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n"));
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() ->
                        chatArea.append("** Disconnected from server **\n"));
                }
            });
            readerThread.setDaemon(true); // dies when main app closes
            readerThread.start();

        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame,
                    "Could not connect to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
                connectButton.setEnabled(true); // re-enable so user can retry
            });
        }
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new ChatClient());
    }
}