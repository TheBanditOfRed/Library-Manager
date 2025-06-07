package main.ui.panels;

import com.google.gson.JsonObject;
import main.core.DataBaseManager;
import main.core.ResourceManager;
import main.core.SecurityManager;
import main.core.SessionManager;
import main.ui.GUI;
import main.ui.utils.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginPanel extends JPanel {
    private static final Logger logger = Logger.getLogger(LoginPanel.class.getName());

    private final JPasswordField passwordField;
    private final JTextField idField;
    private final GUI parentGUI;
    private final SessionManager sessionManager;

    public LoginPanel(GUI gui) {
        this.parentGUI = gui;
        this.sessionManager = SessionManager.getInstance();
        setLayout(new GridBagLayout());
        
        // Initialize components
        this.passwordField = new JPasswordField(15);
        this.idField = new JTextField(15);
        
        initializeComponents();
    }

    private void initializeComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create labels and button
        JLabel idLabel = new JLabel(ResourceManager.getString("login.userid"));
        JLabel passwordLabel = new JLabel(ResourceManager.getString("login.password"));
        JButton loginButton = new JButton(ResourceManager.getString("login.button"));

        // Layout components
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(idLabel, gbc);

        gbc.gridx = 1;
        add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);

        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        add(loginButton, gbc);

        // Add listeners
        setupActionListeners(loginButton);
    }

    private void setupActionListeners(JButton loginButton) {
        idField.addActionListener(_ -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(_ -> loginButton.doClick());
        loginButton.addActionListener(_ -> handleLogin());
    }

    private void handleLogin() {
        String id = idField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        Arrays.fill(passwordChars, '0');

        if (id.isEmpty() || password.isEmpty()) {
            logger.log(Level.INFO, "Login attempt with empty credentials");
            DialogUtils.showErrorDialog(this,
                    ResourceManager.getString("login.error.empty"),
                    ResourceManager.getString("error"));
            return;
        }

        processLoginAttempt(id, password);
    }

    private void processLoginAttempt(String id, String password) {
        DataBaseManager dbm = new DataBaseManager();
        JsonObject user = dbm.findUser(id, password);

        if (user != null) {
            try {
                String encryptedPassword = user.get("Password").getAsString();
                String decryptedPassword = SecurityManager.decrypt(encryptedPassword, password);

                if (decryptedPassword.equals(password)) {
                    handleSuccessfulLogin(user, password);
                } else {
                    handleFailedLogin("incorrect_password", id);
                }
            } catch (RuntimeException ex) {
                handleFailedLogin("decryption_failed", id);
            }
        } else {
            handleFailedLogin("user_not_found", id);
        }
    }

    private void handleSuccessfulLogin(JsonObject user, String password) {
        try {
            String encryptedID = user.get("UserID").getAsString();
            String currentUser = SecurityManager.decrypt(encryptedID, password);

            String encryptedName = user.get("Name").getAsString();
            String currentUserName = SecurityManager.decrypt(encryptedName, password);

            // Update session
            sessionManager.login(currentUser, currentUserName, password);
            
            logger.log(Level.INFO, "Successful login for user: " + currentUser);
            
            // Switch to main panel
            PanelSwitcher.switchToMainPanel(parentGUI);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during successful login processing", e);
            DialogUtils.showErrorDialog(this,
                    ResourceManager.getString("error.login.failed"),
                    ResourceManager.getString("error"));
        }
    }

    private void handleFailedLogin(String reason, String id) {
        logger.log(Level.WARNING, "Failed login attempt - " + reason + " for user: " + id);
        String errorKey = switch (reason) {
            case "incorrect_password", "decryption_failed" -> "login.error.invalid.password";
            case "user_not_found" -> "login.error.invalid";
            default -> "login.error.failed";
        };
        
        DialogUtils.showErrorDialog(this,
                ResourceManager.getString(errorKey),
                ResourceManager.getString("error"));
    }

    public void clearFields() {
        idField.setText("");
        passwordField.setText("");
    }

    public static void updateLoginPanel(GUI gui) {
        Container contentPane = gui.getContentPane();
        Component[] components = gui.cardPanel.getComponents();
        
        // Find and remove old login panel
        for (Component comp : components) {
            if (comp instanceof LoginPanel) {
                gui.cardPanel.remove(comp);
                break;
            }
        }
        
        // Create and add new login panel
        LoginPanel newLoginPanel = new LoginPanel(gui);
        gui.cardPanel.add(newLoginPanel, "login");
        gui.cardLayout.show(gui.cardPanel, "login");
        
        // Update display
        gui.revalidate();
        gui.repaint();
    }
}