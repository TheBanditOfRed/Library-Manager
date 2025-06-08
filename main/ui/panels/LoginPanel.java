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

/**
 * LoginPanel is responsible for handling user login functionality.
 * It includes fields for user ID and password, and manages the login process.
 * The panel updates dynamically based on the current user's session.
 */
public class LoginPanel extends JPanel {
    private static final Logger logger = Logger.getLogger(LoginPanel.class.getName());

    /** Password input field for user authentication */
    private final JPasswordField passwordField;

    /** User ID input field for authentication */
    private final JTextField idField;

    /** Reference to the parent GUI container */
    private final GUI parentGUI;

    /** Session manager instance for handling user sessions */
    private final SessionManager sessionManager;

    /**
     * Constructs a new LoginPanel with the specified GUI reference.
     * Initializes the layout and components for user login.
     *
     * @param gui The GUI instance to which this panel belongs
     */
    public LoginPanel(GUI gui) {
        this.parentGUI = gui;
        this.sessionManager = SessionManager.getInstance();
        setLayout(new GridBagLayout());
        
        // Initialize components
        this.passwordField = new JPasswordField(15);
        this.idField = new JTextField(15);
        
        initializeComponents();
    }

    /**
     * Initializes the components of the login panel.
     * Sets up the layout, labels, and action listeners for user interaction.
     */
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

    /**
     * Sets up action listeners for the login panel components.
     * Handles focus transitions and login button actions.
     *
     * @param loginButton The button that triggers the login process
     */
    private void setupActionListeners(JButton loginButton) {
        idField.addActionListener(_ -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(_ -> loginButton.doClick());
        loginButton.addActionListener(_ -> handleLogin());
    }

    /**
     * Handles the login process when the user clicks the login button.
     * Validates input fields and processes the login attempt.
     */
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

    /**
     * Processes the login attempt by checking the user credentials against the database.
     * Handles successful and failed login attempts with appropriate actions.
     *
     * @param id The user ID entered by the user
     * @param password The password entered by the user
     */
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

    /**
     * Handles a successful login by updating the session and switching to the main panel.
     * Decrypts user information and logs the successful login event.
     *
     * @param user The JsonObject containing user data retrieved from the database
     * @param password The password used for decryption and session management
     */
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

    /**
     * Handles a failed login attempt by displaying an error message and logging the event.
     * Determines the reason for failure and shows the appropriate error dialog.
     *
     * @param reason The reason for the failed login attempt
     * @param id The user ID that was attempted
     */
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

    /**
     * Clears the input fields in the login panel.
     * Resets both the user ID and password fields to empty strings.
     */
    public void clearFields() {
        idField.setText("");
        passwordField.setText("");
    }

    /**
     * Updates the login panel in the GUI.
     * Removes the old login panel and adds a new one, ensuring the card layout is refreshed.
     *
     * @param gui The GUI instance to update the login panel for
     */
    public static void updateLoginPanel(GUI gui) {
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