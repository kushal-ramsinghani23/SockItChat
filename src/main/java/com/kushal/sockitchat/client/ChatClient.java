package com.kushal.sockitchat.client;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;

public class ChatClient {    
    private Socket socket;
    private DataOutputStream out;
    private String username;

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel userProfilePanel;
    private String userStatus = "Hey there! I'm using SockItChat";

    private JTextField usernameField;
    private JTextField serverIPField;
    private JButton connectButton;

    private JPanel sidebarPanel;
    private JPanel chatPanel;
    private JPanel topBarPanel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton fileButton; // Added instance field for the file attachment button

    // Promoted to instance fields — needed in connectToServer()
    private JFrame frame;
    private final JLabel userProfileName;
    private final JLabel avatarLabel;

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
        
        // Profile bar — initialized using instance field assignment block directly
        userProfilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        userProfilePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
        
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
        sidebarPanel.add(userProfilePanel, BorderLayout.SOUTH);     
        
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
        
        fileButton = new JButton("+"); // A clean modern plus icon text string
        fileButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        fileButton.putClientProperty("JButton.buttonType", "toolBarButton"); // Gives it a clean borderless look

        sendButton = new JButton("Send");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(fileButton);
        buttonPanel.add(sendButton);
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
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


        // Assign the same action to BOTH the button click and the text field Enter key
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        // Assign file chooser behavior to the attachment clip button
        fileButton.addActionListener(e -> sendFile());
        
        setupUserProfilePanel();
    }

    private void connectToServer() {
        String ip = serverIPField.getText().trim();
        this.username = usernameField.getText().trim();

        try {
            // 1. Open socket — blocking call, safe because we moved connectToServer to a background thread
            socket = new Socket(ip, 5000);
            out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // 2. Send username as first message
            out.writeUTF(username);
            out.flush();
            
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
                    // No more while(readUTF != null)
                    // Instead — loop forever, catch EOFException to detect disconnect
                    while(true) {
                        String type = in.readUTF();
                        
                        if (type.equals("TEXT")) {
                            String message = in.readUTF();
                        // Must update Swing components on EDT only
                            SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
                            
                        } else if (type.equals("FILE")) {
                            String meta = in.readUTF();
                            long size = in.readLong();
                            byte[] fileBytes = in.readNBytes((int) size);

                            // Extract actual filename from "username sent file: filename.txt"
                            String filename = meta.substring(meta.lastIndexOf(": ") + 2);

                            // Auto-save to user's Downloads folder
                            File saveDir = new File(System.getProperty("user.home") + "/Downloads/SockItChat");
                            saveDir.mkdirs();
                            File savedFile = new File(saveDir, filename);
                            Files.write(savedFile.toPath(), fileBytes);

                            SwingUtilities.invokeLater(() ->
                                chatArea.append(meta + " — saved to Downloads/SockItChat/\n")
                            );
                        }
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


    private void sendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty() && out != null) {
            try {
                out.writeUTF("TEXT");
                out.writeUTF(text);
                out.flush();
                chatArea.append("You: " + text + "\n");
                messageField.setText("");
            } catch (IOException ex) {
                chatArea.append("Failed to send message.\n");
            }
        }
    }

    private void sendFile() {
        if (out == null) return;
        
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(frame);
        
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                
                out.writeUTF("FILE");
                out.writeUTF(selectedFile.getName());
                out.writeLong(fileBytes.length);
                out.write(fileBytes);
                out.flush();
                
                chatArea.append("You sent: " + selectedFile.getName() + " (" + fileBytes.length + " bytes)\n");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error reading or sending file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
    * Extracted standalone action to build and display the User Profile dialog window.
    * This can be safely called from anywhere within ChatClient.
    */
    public void openUserProfile() {
        
       // Build and show the JDialog modal popup box
       JDialog profileDialog = new JDialog(frame, "My Profile", true); // true = modal
       profileDialog.setSize(300, 350);
       profileDialog.setLocationRelativeTo(frame); // center on parent window frame
       profileDialog.setLayout(new BorderLayout());

       // Main Content layout panel
       JPanel contentPanel = new JPanel(new GridBagLayout());
       contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
       GridBagConstraints gbc = new GridBagConstraints();
       gbc.insets = new Insets(10, 10, 10, 10);
       gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

       // Crisp circular profile avatar rendering
       JLabel largeAvatar = new JLabel(avatarLabel.getText(), JLabel.CENTER) {
           @Override
           protected void paintComponent(Graphics g) {
               Graphics2D g2 = (Graphics2D) g.create();
               g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               g2.setColor(Color.DARK_GRAY);
               g2.fillOval(0, 0, getWidth(), getHeight());
               g2.dispose();
               super.paintComponent(g);
           }
       };
       largeAvatar.setFont(new Font("Segoe UI", Font.BOLD, 24));
       largeAvatar.setForeground(Color.WHITE);
       largeAvatar.setPreferredSize(new Dimension(60, 60));

       gbc.gridy = 0; gbc.anchor = GridBagConstraints.CENTER;
       contentPanel.add(largeAvatar, gbc);

       // Dynamic large text username display
       JLabel largeUsername = new JLabel(userProfileName.getText(), JLabel.CENTER);
       largeUsername.setFont(new Font("Segoe UI", Font.BOLD, 18));
       gbc.gridy = 1;
       contentPanel.add(largeUsername, gbc);

       // Status message information text field
       JTextField statusField = new JTextField(userStatus);
       statusField.putClientProperty("JTextField.placeholderText", "Status");
       gbc.gridy = 2; gbc.weightx = 1.0;
       contentPanel.add(statusField, gbc);

       profileDialog.add(contentPanel, BorderLayout.CENTER);

       // Save action button
       JButton saveButton = new JButton("Save");
       saveButton.putClientProperty("JButton.buttonType", "roundRect");
       saveButton.addActionListener(al -> {
            userStatus = statusField.getText().trim(); // remember for this session
            profileDialog.dispose();
        });
       JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
       bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 15));
       bottomPanel.add(saveButton);
       profileDialog.add(bottomPanel, BorderLayout.SOUTH);

       profileDialog.setVisible(true); // blocks layout thread safely while modal window sits active
   }

    
    /**
    * Attaches the mouse interaction bindings directly to the username and avatar components.
    */
    private void setupUserProfilePanel() {
       // Define a single shared mouse listener to avoid duplicating code structures
       java.awt.event.MouseAdapter profileClickAction = new java.awt.event.MouseAdapter() {
           @Override
           public void mouseClicked(java.awt.event.MouseEvent e) {
               // Trigger the modal profile dialog layout
               openUserProfile();
           }

           @Override
           public void mouseEntered(java.awt.event.MouseEvent e) {
               // Switch pointer cursor to standard hand icon on hover to signify interactability
               Component source = e.getComponent();
               source.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

               // Optional subtle visual cue: turn text underline/bright or tint panel background
               userProfilePanel.setBackground(Color.DARK_GRAY.darker());
           }

           @Override
           public void mouseExited(java.awt.event.MouseEvent e) {
               userProfilePanel.setBackground(UIManager.getColor("Panel.background"));
           }
       };

       // Attach the interaction framework directly onto the specific bottom left labels
       avatarLabel.addMouseListener(profileClickAction);
       userProfileName.addMouseListener(profileClickAction);
   }



    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new ChatClient());
    }
}