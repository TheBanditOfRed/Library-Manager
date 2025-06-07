package main.ui.panels;

import com.google.gson.JsonObject;
import main.core.DataBaseManager;
import main.core.ResourceManager;
import main.core.SecurityManager;
import main.ui.GUI;
import main.ui.utils.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LoginPanel is responsible for creating and managing the login interface of the application.
 * It allows users to enter their user ID and password, authenticates them against the database,
 * and handles errors such as empty credentials or incorrect passwords.
 * The panel uses GridBagLayout for precise component positioning.
 */
public class LoginPanel extends JPanel {
    private static final Logger logger = Logger.getLogger(LoginPanel.class.getName());

    /** Panel containing the login interface */
    public static JPanel loginPanel;

    /** Password input field for user authentication */
    public static JPasswordField passwordField;

    /** User ID input field for authentication */
    public static JTextField idField;

    /** ID of the currently logged-in user */
    public static String currentUser;

    /** Display name of the current user */
    public static String currentUserName;

    /** Encryption key derived from user's password */
    public static String key;

    /**
     * Creates the login panel with user ID and password fields.
     * Uses GridBagLayout for precise component positioning.
     * Sets up the login button action to authenticate users and handle errors.
     * @param gui The GUI instance to which the login panel will be added
     */
    public static void createLoginPanel(GUI gui) {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel idLabel = new JLabel(ResourceManager.getString("login.userid"));
        JLabel passwordLabel = new JLabel(ResourceManager.getString("login.password"));
        idField = new JTextField(15);
        passwordField = new JPasswordField(15);
        JButton loginButton = new JButton(ResourceManager.getString("login.button"));

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(idLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        loginPanel.add(loginButton, gbc);

        idField.addActionListener(_ -> passwordField.requestFocusInWindow());

        passwordField.addActionListener(_ -> loginButton.doClick());

        loginButton.addActionListener(_ -> {
            String id = idField.getText();

            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);
            // Security practice: overwrite password in memory after use
            Arrays.fill(passwordChars, '0');

            DataBaseManager dbm = new DataBaseManager();
            JsonObject user = dbm.findUser(id, password);

            if (id.isEmpty() || password.isEmpty()) {
                logger.log(Level.INFO, "Login attempt with empty credentials");

                DialogUtils.showErrorDialog(loginPanel,
                        ResourceManager.getString("login.error.empty"),
                        ResourceManager.getString("error")
                );
                return;
            }

            if (user != null) {
                try {
                    String encryptedPassword = user.get("Password").getAsString();
                    // Verify password by decrypting stored password and comparing
                    String decryptedPassword = main.core.SecurityManager.decrypt(encryptedPassword, password);

                    if (decryptedPassword.equals(password)) {
                        logger.log(Level.INFO, "Successful login for user: " + id);
                        // Password acts as encryption key for other user data
                        key = password;

                        String encryptedID = user.get("UserID").getAsString();
                        currentUser = main.core.SecurityManager.decrypt(encryptedID, password);

                        String encryptedName = user.get("Name").getAsString();
                        currentUserName = SecurityManager.decrypt(encryptedName, password);

                        PanelManager.switchToMainPanel(gui);
                    } else {
                        logger.log(Level.WARNING, "Failed login attempt - incorrect password for user: " + id);
                        DialogUtils.showErrorDialog(loginPanel,
                                ResourceManager.getString("login.error.invalid.password"),
                                ResourceManager.getString("error")
                        );
                    }
                } catch (RuntimeException ex) {
                    logger.log(Level.WARNING, "Failed login attempt - decryption failed for user: " + id);
                    // Decryption failure treated as authentication failure
                    DialogUtils.showErrorDialog(loginPanel,
                            ResourceManager.getString("login.error.invalid.password"),
                            ResourceManager.getString("error")
                    );
                }
            } else {
                logger.log(Level.WARNING, "Failed login attempt - user not found: " + id);
                DialogUtils.showErrorDialog(loginPanel,
                        ResourceManager.getString("login.error.invalid"),
                        ResourceManager.getString("error")
                );
            }
        });
    }

    /**
     * Updates the login panel with new language text.
     * @param gui The GUI instance to update the login panel for
     */
    public static void updateLoginPanel(GUI gui) {
        try {
            Container contentPane = gui.getContentPane();
            contentPane.remove(loginPanel);
            createLoginPanel(gui);
            gui.cardPanel.add(loginPanel, "login");
            gui.cardLayout.show(gui.cardPanel, "login");
        } catch (Exception e) {
            throw new RuntimeException("Failed to update login panel: " + e.getMessage(), e);
        }
    }
}
