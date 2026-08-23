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
    private String currentStatus = "Hey there! I'm using SockItChat";

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
    
    private java.awt.image.BufferedImage profileImage = null;

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
        
        // --- Inside ChatClient Constructor ---
        avatarLabel = new JLabel("?", JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                if (profileImage != null) {
                    // Mirror the clipping mask behavior straight onto the primary sidebar container viewport
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                    g2.drawImage(profileImage, 0, 0, getWidth(), getHeight(), null);
                } else {
                    // Fallback to standard baseline flat graphic layout behavior if instance memory is empty
                    g2.setColor(Color.DARK_GRAY);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    super.paintComponent(g2); // Draws the text character string automatically
                }
                g2.dispose();
            }
        };
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatarLabel.setOpaque(false); // Changed to false so the square background bounding area box doesn't overlap the circular clip vector
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

            // 2. Send username 
            out.writeUTF(username);
            
            // 3. Send status
            out.writeUTF(currentStatus);
            
            // 4. Convert and send profile image if it exists
            if (this.profileImage != null) {
                out.writeBoolean(true); // Tell server an image IS coming

                // Convert BufferedImage to raw byte array
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(this.profileImage, "png", baos);
                byte[] imageBytes = baos.toByteArray();

                out.writeLong(imageBytes.length); // Send size boundary
                out.write(imageBytes); // Send raw payload
            } else {
                out.writeBoolean(false); // Tell server NO image is coming
            }

            out.flush(); // Flush all registration packet blocks together
            
            // 5. Switch to chat screen on EDT
            SwingUtilities.invokeLater(() -> {
                cardLayout.show(mainPanel, "chat");
                frame.setTitle("SockItChat — " + username);
                userProfileName.setText(username);
                avatarLabel.setText(String.valueOf(username.charAt(0)).toUpperCase());
            });

            // 6. Reader thread — listens for incoming messages forever
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
       JDialog profileDialog = new JDialog(frame, "My Profile", true);
       profileDialog.setSize(340, 420); // slightly taller to give space for the upload button
       profileDialog.setLocationRelativeTo(frame);
       profileDialog.setLayout(new BorderLayout());

       // Main Content layout panel
       JPanel contentPanel = new JPanel(new GridBagLayout());
       contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
       GridBagConstraints gbc = new GridBagConstraints();
       gbc.insets = new Insets(10, 10, 10, 10);
       gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

       // Custom avatar rendering component utilizing dynamic vector graphics clipping masks
       JLabel largeAvatar = new JLabel("", JLabel.CENTER) {
           @Override
           protected void paintComponent(Graphics g) {
               Graphics2D g2 = (Graphics2D) g.create();
               g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

               if (profileImage != null) {
                   // Precise graphics clipping path sequence
                   g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                   g2.drawImage(profileImage, 0, 0, getWidth(), getHeight(), null);
               } else {
                   // Fallback text avatar shape configuration
                   g2.setColor(Color.DARK_GRAY);
                   g2.fillOval(0, 0, getWidth(), getHeight());

                   // Manually draw the initial letter text directly onto the shape to keep it beneath structural rendering boundaries
                   g2.setColor(Color.WHITE);
                   g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                   FontMetrics fm = g2.getFontMetrics();
                   String text = avatarLabel.getText();
                   int textX = (getWidth() - fm.stringWidth(text)) / 2;
                   int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                   g2.drawString(text, textX, textY);
               }
               g2.dispose();
           }
       };
       largeAvatar.setPreferredSize(new Dimension(60, 60));

       gbc.gridy = 0; gbc.anchor = GridBagConstraints.CENTER;
       contentPanel.add(largeAvatar, gbc);

       // Upload Photo Action Button Intermediary Component
       JButton uploadButton = new JButton("Upload Photo");
       uploadButton.putClientProperty("JButton.buttonType", "toolBarButton");
       uploadButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
       gbc.gridy = 1;
       contentPanel.add(uploadButton, gbc);

       // Large structural text username display label
       JLabel largeUsername = new JLabel(userProfileName.getText(), JLabel.CENTER);
       largeUsername.setFont(new Font("Segoe UI", Font.BOLD, 18));
       gbc.gridy = 2;
       contentPanel.add(largeUsername, gbc);

       // Status metadata entry text field container
       JTextField statusField = new JTextField(currentStatus);
       statusField.putClientProperty("JTextField.placeholderText", "Status");
       gbc.gridy = 3; gbc.weightx = 1.0;
       contentPanel.add(statusField, gbc);

       profileDialog.add(contentPanel, BorderLayout.CENTER);

       // Profile submission block
       JButton saveButton = new JButton("Save");
       saveButton.putClientProperty("JButton.buttonType", "roundRect");
       saveButton.addActionListener(al -> {
           currentStatus = statusField.getText().trim();
           profileDialog.dispose();
       });

       JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
       bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 15));
       bottomPanel.add(saveButton);
       profileDialog.add(bottomPanel, BorderLayout.SOUTH);

       // Wire Up image file scanner targeting the Upload Button component frame
       uploadButton.addActionListener(e -> {
           JFileChooser fileChooser = new JFileChooser();
           // Restrict scan targets explicitly to discrete file image containers
           fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
               "Images (JPG, PNG, BMP)", "jpg", "jpeg", "png", "bmp"
           ));

           int result = fileChooser.showOpenDialog(profileDialog);
           if (result == JFileChooser.APPROVE_OPTION) {
               try {
                   File selectedFile = fileChooser.getSelectedFile();
                   // Read straight into memory space as a raw BufferedImage
                   java.awt.image.BufferedImage rawImg = javax.imageio.ImageIO.read(selectedFile);

                   if (rawImg != null) {
                       profileImage = rawImg;

                       // Force the component to invoke its custom paintComponent code logic stream
                       largeAvatar.repaint();

                       // Mirror the loaded photo container asset out onto the main screen background sidebar component view simultaneously
                       avatarLabel.repaint();
                   }
               } catch (IOException ex) {
                   JOptionPane.showMessageDialog(profileDialog, "Failed to load profile image: " + ex.getMessage(), "Image Error", JOptionPane.ERROR_MESSAGE);
               }
           }
       });

       profileDialog.setVisible(true);
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