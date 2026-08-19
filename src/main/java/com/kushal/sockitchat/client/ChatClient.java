import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

public class ChatClient {
    
    // CardLayout to switch between login and chat screens
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Login screen components
    private JTextField usernameField;
    private JTextField serverIPField;
    private JButton connectButton;
    
    // Chat screen components
    private JPanel sidebarPanel;      // left — contacts list
    private JPanel chatPanel;         // right — everything else
    private JPanel topBarPanel;       // top of chat — contact name
    private JTextArea chatArea;       // message display
    private JTextField messageField;  // type message here
    private JButton sendButton;
    
    public ChatClient() {
        // 1. Setup JFrame
        JFrame frame = new JFrame("Chat Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 550);
        frame.setLocationRelativeTo(null); // Center on screen

        // 2. Create mainPanel with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 3. Build loginPanel — add to mainPanel as "login"
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Visual Improvement 2: Retitled to "SockItChat"
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
        // Visual Improvement 1: Rounded WhatsApp green accent styling
        connectButton.putClientProperty("JButton.buttonType", "roundRect");
        connectButton.setBackground(new Color(37, 211, 102)); // WhatsApp green
        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        loginPanel.add(connectButton, gbc);

        mainPanel.add(loginPanel, "login");

        // 4. Build chatPanel — add to mainPanel as "chat"
        // Setup Left Sidebar (Contacts List)
        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.DARK_GRAY));
        
        DefaultListModel<String> contactModel = new DefaultListModel<>();
        contactModel.addElement("Global Chatroom");
        contactModel.addElement("User 1");
        contactModel.addElement("User 2");
        JList<String> contactList = new JList<>(contactModel);
        
        // Header for Contacts Panel
        JPanel contactHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        JLabel contactsTitle = new JLabel("Contacts");
        contactsTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contactHeaderPanel.add(contactsTitle);
        sidebarPanel.add(contactHeaderPanel, BorderLayout.NORTH);
        sidebarPanel.add(new JScrollPane(contactList), BorderLayout.CENTER);

        // Visual Improvement 3: Bottom User/Avatar Profile Bar Placeholder
        JPanel userProfilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        userProfilePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
        
        // Circular profile initial label workaround
        JLabel avatarLabel = new JLabel("U", JLabel.CENTER);
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(Color.DARK_GRAY);
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setPreferredSize(new Dimension(32, 32));
        
        JLabel userProfileName = new JLabel("My Profile");
        userProfileName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        userProfilePanel.add(avatarLabel);
        userProfilePanel.add(userProfileName);
        sidebarPanel.add(userProfilePanel, BorderLayout.SOUTH);

        // Setup Right Screen Container (Everything else)
        chatPanel = new JPanel(new BorderLayout());

        // Top bar for the active contact name
        topBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        topBarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
        JLabel activeContactLabel = new JLabel("Global Chatroom");
        activeContactLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topBarPanel.add(activeContactLabel);
        chatPanel.add(topBarPanel, BorderLayout.NORTH);

        // Main message window display
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Bottom input layout bar
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messageField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // Assemble structural panels together into full UI container
        JPanel fullChatWindow = new JPanel(new BorderLayout());
        fullChatWindow.add(sidebarPanel, BorderLayout.WEST);
        fullChatWindow.add(chatPanel, BorderLayout.CENTER);
        
        mainPanel.add(fullChatWindow, "chat");

        // 5. Add mainPanel to frame
        frame.add(mainPanel);
        frame.setVisible(true);

        // 6. Connect button ActionListener → cardLayout.show(mainPanel, "chat")
        connectButton.addActionListener(e -> {
            String typedUsername = usernameField.getText().trim();
            if (!typedUsername.isEmpty()) {
                cardLayout.show(mainPanel, "chat");
                frame.setTitle("SockItChat — " + typedUsername);
                
                // Wire the placeholder layout text and initials dynamically
                userProfileName.setText(typedUsername);
                avatarLabel.setText(String.valueOf(typedUsername.charAt(0)).toUpperCase());
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a valid username.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
    
    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new ChatClient());
    }
}
