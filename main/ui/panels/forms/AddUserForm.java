package main.ui.panels.forms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import main.core.DataBaseManager;
import main.core.ResourceManager;
import main.core.SecurityManager;
import main.core.UserTypeMapper;
import main.ui.GUI;
import main.ui.panels.ManagementPanel;
import main.ui.panels.PanelSwitcher;
import main.ui.utils.DialogUtils;
import main.ui.utils.StatusUtils;
import main.ui.utils.TableUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static main.ui.utils.StatusUtils.getStatusMessage;


public class AddUserForm {
    private static final Logger logger = Logger.getLogger(AddUserForm.class.getName());

    /**
     * Creates a panel for adding a new user with input fields for user properties.
     * The form includes fields for user ID, username, password, and user type.
     * Handles validation and database operations when saving the new user.
     *
     * @param gui The GUI instance to which the form will be added
     * @return JPanel containing the add user form with input fields and buttons
     */
    public static JPanel createAddUserFormPanel(GUI gui) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Input fields for user properties
        ManagementPanel.userIdField = new JTextField("",15);
        ManagementPanel.userNameField = new JTextField("",15);
        ManagementPanel.userPasswordField = new JPasswordField("",15);
        ManagementPanel.userTypeComboBox = new JComboBox<>(new String[]{
                ResourceManager.getString("user.type.student"),
                ResourceManager.getString("user.type.public"),
        });

        // Field labels
        JLabel userIdLabel = new JLabel(ResourceManager.getString("form.user.id") + ":");
        JLabel userNameLabel = new JLabel(ResourceManager.getString("form.user.username") + ":");
        JLabel userPasswordLabel = new JLabel(ResourceManager.getString("form.user.password") + ":");
        JLabel userTypeLabel = new JLabel(ResourceManager.getString("form.user.type") + ":");

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton(ResourceManager.getString("button.adduser"));
        JButton cancelButton = new JButton(ResourceManager.getString("form.button.cancel"));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add title panel
        JPanel titlePanel = new JPanel();
        JLabel formTitle = new JLabel(ResourceManager.getString("form.header.add.user"));
        formTitle.setFont(formTitle.getFont().deriveFont(Font.BOLD, 16));
        titlePanel.add(formTitle);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(userIdLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.userIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(userNameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.userNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(userPasswordLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.userPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(userTypeLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.userTypeComboBox, gbc);

        setupUserFormActionListeners(gui, saveButton, cancelButton, panel);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a panel for editing an existing user with pre-populated fields and book management.
     * Similar to createAddUserFormPanel but in edit mode with existing user data and a books table.
     *
     * @param gui The GUI instance to which the form will be added
     * @param userId The ID of the user being edited
     * @param userName The current name of the user
     * @param userPassword The current password of the user
     * @param userType The current type of the user
     * @return JPanel containing the edit user form with populated fields and book management
     */
    public static JPanel createEditUserFormPanel(GUI gui, String userId, String userName, String userPassword, String userType) {
        JPanel panel = new JPanel(new BorderLayout());

        // User details form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Input fields for user properties - populate with existing data
        ManagementPanel.userIdField = new JTextField(userId, 15);
        ManagementPanel.userIdField.setEditable(false); // User ID should not be editable
        ManagementPanel.userNameField = new JTextField(userName, 15);
        ManagementPanel.userPasswordField = new JPasswordField(userPassword, 15);

        ManagementPanel.userTypeComboBox = new JComboBox<>(new String[]{
                ResourceManager.getString("user.type.student"),
                ResourceManager.getString("user.type.public"),
                ResourceManager.getString("user.type.admin")
        });

        String studentType = ResourceManager.getString("user.type.student");
        String publicType = ResourceManager.getString("user.type.public");
        String adminType = ResourceManager.getString("user.type.admin");
        if (userType.equals("Students") || userType.equals(studentType)) {
            ManagementPanel.userTypeComboBox.setSelectedIndex(0);
        } else if (userType.equals("General Public") || userType.equals(publicType)) {
            ManagementPanel.userTypeComboBox.setSelectedIndex(1);
        } else if (userType.equals("Admins") || userType.equals(adminType)) {
            ManagementPanel.userTypeComboBox.setSelectedIndex(2);
        } else {
            ManagementPanel.userTypeComboBox.setSelectedIndex(0);
        }

        // Field labels
        JLabel userIdLabel = new JLabel(ResourceManager.getString("form.user.id") + ":");
        JLabel userNameLabel = new JLabel(ResourceManager.getString("form.user.username") + ":");
        JLabel userPasswordLabel = new JLabel(ResourceManager.getString("form.user.password") + ":");
        JLabel userTypeLabel = new JLabel(ResourceManager.getString("form.user.type") + ":");

        // Layout user form components
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(userIdLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.userIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(userNameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.userNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(userPasswordLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.userPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(userTypeLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.userTypeComboBox, gbc);

        // Books management section
        JPanel booksPanel = createUserBooksPanel(userId, userPassword);

        // Create a split pane to hold both user form and books table
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(formPanel);
        splitPane.setRightComponent(booksPanel);
        splitPane.setDividerLocation(400); // Adjust as needed
        splitPane.setResizeWeight(0.4); // Give 40% to form, 60% to books table

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton(ResourceManager.getString("form.button.update"));
        JButton cancelButton = new JButton(ResourceManager.getString("form.button.cancel"));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add title panel
        JPanel titlePanel = new JPanel();
        JLabel formTitle = new JLabel(ResourceManager.getString("form.header.edit.user"));
        formTitle.setFont(formTitle.getFont().deriveFont(Font.BOLD, 16));
        titlePanel.add(formTitle);

        setupEditUserFormActionListeners(gui, saveButton, cancelButton, panel, userId);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Sets up action listeners for the user form fields and buttons.
     * Handles focus transitions between fields and button actions for saving or canceling.
     *
     * @param gui         The GUI instance to which the form belongs
     * @param saveButton  The button to save the user data
     * @param cancelButton The button to cancel the operation
     * @param panel       The panel containing the form
     */
    private static void setupUserFormActionListeners(GUI gui, JButton saveButton, JButton cancelButton, JPanel panel) {
        ManagementPanel.userIdField.addActionListener(_ -> ManagementPanel.userNameField.requestFocusInWindow());
        ManagementPanel.userNameField.addActionListener(_ -> ManagementPanel.userPasswordField.requestFocusInWindow());
        ManagementPanel.userPasswordField.addActionListener(_ -> ManagementPanel.userTypeComboBox.requestFocusInWindow());

        cancelButton.addActionListener(_ -> PanelSwitcher.switchToManageBooksPanel(gui));

        saveButton.addActionListener(_ -> handleUserSave(gui, panel));
    }

    /**
     * Shows a dialog to select and edit a user.
     * Requests user ID and password for authentication before opening edit form.
     *
     * @param parentPanel The parent panel for the dialog
     * @param gui The GUI instance for navigation
     */
    public static void showEditUserDialog(JPanel parentPanel, GUI gui) {
        JPanel dialogPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField editUserIdField = new JTextField(15);
        JPasswordField editPasswordField = new JPasswordField(15);

        gbc.gridx = 0; gbc.gridy = 0;
        dialogPanel.add(new JLabel(ResourceManager.getString("form.userid") + ":"), gbc);
        gbc.gridx = 1;
        dialogPanel.add(editUserIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialogPanel.add(new JLabel(ResourceManager.getString("form.password") + ":"), gbc);
        gbc.gridx = 1;
        dialogPanel.add(editPasswordField, gbc);

        int result = JOptionPane.showConfirmDialog(
                parentPanel,
                dialogPanel,
                ResourceManager.getString("button.edituser"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String userId = editUserIdField.getText().trim();
            String password = new String(editPasswordField.getPassword()).trim();

            if (userId.isEmpty() || password.isEmpty()) {
                DialogUtils.showErrorDialog(parentPanel,
                        ResourceManager.getString("validation.fields.required"),
                        ResourceManager.getString("validation.error")
                );
                return;
            }

            try {
                // Verify user exists and password is correct
                DataBaseManager dbm = new DataBaseManager();
                JsonObject user = dbm.findUser(userId, password);

                if (user == null) {
                    DialogUtils.showErrorDialog(parentPanel,
                            ResourceManager.getString("error.user.not.found"),
                            ResourceManager.getString("error")
                    );
                    return;
                }

                // Decrypt user data for display in the form
                String decryptedUserId = SecurityManager.decrypt(user.get("UserID").getAsString(), password);
                String decryptedUserName = SecurityManager.decrypt(user.get("Name").getAsString(), password);
                String decryptedPassword = SecurityManager.decrypt(user.get("Password").getAsString(), password);
                String userType = dbm.getUserType(userId, password);
                
                JPanel editUserFormPanel = createEditUserFormPanel(gui, decryptedUserId, decryptedUserName, decryptedPassword, userType);
                gui.cardPanel.add(editUserFormPanel, "editUser");
                gui.cardLayout.show(gui.cardPanel, "editUser");
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to decrypt user data: " + e.getMessage(), e);
                DialogUtils.showErrorDialog(parentPanel,
                        ResourceManager.getString("error.user.decrypt.failed"),
                        ResourceManager.getString("error")
                );
            }
        }
    }

    /**
     * Sets up action listeners for the edit user form.
     *
     * @param gui The GUI instance
     * @param saveButton The save button
     * @param cancelButton The cancel button  
     * @param panel The form panel
     * @param originalUserId The original user ID being edited
     */
    private static void setupEditUserFormActionListeners(GUI gui, JButton saveButton, JButton cancelButton, JPanel panel, String originalUserId) {
        ManagementPanel.userNameField.addActionListener(_ -> ManagementPanel.userPasswordField.requestFocusInWindow());
        ManagementPanel.userPasswordField.addActionListener(_ -> ManagementPanel.userTypeComboBox.requestFocusInWindow());

        cancelButton.addActionListener(_ -> PanelSwitcher.switchToManageBooksPanel(gui));

        saveButton.addActionListener(_ -> handleEditUserSave(gui, panel, originalUserId));
    }

    /**
     * Handles the save action for adding a new user.
     * Validates input fields, performs database operations, and provides user feedback.
     *
     * @param gui   The GUI instance to which the form belongs
     * @param panel The panel containing the form
     */
    private static void handleUserSave(GUI gui, JPanel panel){
        try {
            // Validate input fields
            String userId = ManagementPanel.userIdField.getText().trim();
            String userName = ManagementPanel.userNameField.getText().trim();
            String userPassword = new String(ManagementPanel.userPasswordField.getPassword()).trim();

            String userType = (String) ManagementPanel.userTypeComboBox.getSelectedItem();
            String canonicalUserType = UserTypeMapper.mapToCanonical(userType);


            if (StatusUtils.validateNewUserFields(userId, userName, userPassword, userType)) {
                DialogUtils.showErrorDialog(panel,
                        ResourceManager.getString("validation.fields.required"),
                        ResourceManager.getString("validation.error")
                );
                return;
            }

            DataBaseManager dbm = new DataBaseManager();

            boolean success = dbm.addUser(userId, userName, userPassword, canonicalUserType);

            if (success) {
                JOptionPane.showMessageDialog(panel,
                        ResourceManager.getString("user.add.success"),
                        ResourceManager.getString("success"),
                        JOptionPane.INFORMATION_MESSAGE);

                clearUserFields();

                PanelSwitcher.switchToManageBooksPanel(gui);
            } else {
                DialogUtils.showErrorDialog(panel,
                        ResourceManager.getString("error.user.add.failed") + "\n" + ResourceManager.getString("error.logs.check"),
                        "Database Error"
                );

                PanelSwitcher.switchToManageBooksPanel(gui);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to add user: " + e.getMessage(), e);
            DialogUtils.showErrorDialog(panel,
                    ResourceManager.getString("error.user.add") + "\n" + ResourceManager.getString("error.logs.check"),
                    "Error"
            );
        }
    }

    /**
     * Handles saving changes to an edited user.
     *
     * @param gui The GUI instance
     * @param panel The form panel
     * @param originalUserId The original user ID (decrypted)
     */
    private static void handleEditUserSave(GUI gui, JPanel panel, String originalUserId) {
        try {
            String userName = ManagementPanel.userNameField.getText().trim();
            String userPassword = new String(ManagementPanel.userPasswordField.getPassword()).trim();
            String userType = (String) ManagementPanel.userTypeComboBox.getSelectedItem();

            if (StatusUtils.validateNewUserFields(originalUserId, userName, userPassword, userType)) {
                DialogUtils.showErrorDialog(panel,
                        ResourceManager.getString("validation.fields.required"),
                        ResourceManager.getString("validation.error")
                );
                return;
            }

            DataBaseManager dbm = new DataBaseManager();
            boolean success = dbm.updateUser(originalUserId, userName, userPassword, userType);

            if (success) {
                JOptionPane.showMessageDialog(panel,
                        ResourceManager.getString("user.update.success"),
                        ResourceManager.getString("success"),
                        JOptionPane.INFORMATION_MESSAGE);

                clearUserFields();
                PanelSwitcher.switchToManageBooksPanel(gui);
            } else {
                DialogUtils.showErrorDialog(panel,
                        ResourceManager.getString("error.user.update.failed"),
                        ResourceManager.getString("error")
                );
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update user: " + e.getMessage(), e);
            DialogUtils.showErrorDialog(panel,
                    ResourceManager.getString("error.user.update") + "\n" + ResourceManager.getString("error.logs.check"),
                    ResourceManager.getString("error")
            );
        }
    }

    /**
     * Shows a dialog to remove a user from the database.
     * Requests user ID and password for verification before removal.
     *
     * @param parentPanel The parent panel for the dialog
     */
    public static void showRemoveUserDialog(JPanel parentPanel) {
        JPanel dialogPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField removeUserIdField = new JTextField(15);
        JPasswordField removePasswordField = new JPasswordField(15);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialogPanel.add(new JLabel(ResourceManager.getString("form.userid") + ":"), gbc);
        gbc.gridx = 1;
        dialogPanel.add(removeUserIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialogPanel.add(new JLabel(ResourceManager.getString("form.password") + ":"), gbc);
        gbc.gridx = 1;
        dialogPanel.add(removePasswordField, gbc);

        int result = JOptionPane.showConfirmDialog(
                parentPanel,
                dialogPanel,
                ResourceManager.getString("dialog.removeuser.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String userId = removeUserIdField.getText().trim();
            String password = new String(removePasswordField.getPassword()).trim();

            if (userId.isEmpty() || password.isEmpty()) {
                DialogUtils.showErrorDialog(parentPanel,
                        ResourceManager.getString("validation.fields.required"),
                        ResourceManager.getString("validation.error")
                );
                return;
            }

            // Verify user exists and password is correct
            DataBaseManager dbm = new DataBaseManager();
            JsonObject user = dbm.findUser(userId, password);

            if (user == null) {
                DialogUtils.showErrorDialog(parentPanel,
                        ResourceManager.getString("error.user.not.found"),
                        ResourceManager.getString("error")
                );
                return;
            }

            // Check if user has borrowed books
            JsonArray borrowedBooks = dbm.findBorrowedBooks(userId, password);
            if (borrowedBooks != null && !borrowedBooks.isEmpty()) {
                DialogUtils.showErrorDialog(parentPanel,
                        ResourceManager.getString("error.user.has.books"),
                        ResourceManager.getString("error")
                );
                return;
            }

            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(parentPanel,
                    ResourceManager.getString("confirm.delete.user", userId),
                    ResourceManager.getString("confirm"),
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {

                boolean success = dbm.removeUser(userId, password);

                if (success) {
                    JOptionPane.showMessageDialog(parentPanel,
                            ResourceManager.getString("user.remove.success"),
                            ResourceManager.getString("success"),
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    DialogUtils.showErrorDialog(parentPanel,
                            ResourceManager.getString("error.user.remove.failed"),
                            ResourceManager.getString("error")
                    );
                }
            }
        }
    }

    /** Clears all input fields in the user form. */
    private static void clearUserFields() {
        ManagementPanel.userIdField.setText("");
        ManagementPanel.userNameField.setText("");
        ManagementPanel.userPasswordField.setText("");
        ManagementPanel.userTypeComboBox.setSelectedIndex(0);
    }

    /**
     * Creates a panel containing the user's borrowed books with management options.
     * Shows a table of books with options to manually return books or modify loan status.
     *
     * @param userId The user ID for which to display books
     * @param userPassword The user's password for authentication
     * @return JPanel containing the books management interface
     */
    private static JPanel createUserBooksPanel(String userId, String userPassword) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Title for books section
        JPanel titlePanel = new JPanel();
        JLabel booksTitle = new JLabel(ResourceManager.getString("user.books.title"));
        booksTitle.setFont(booksTitle.getFont().deriveFont(Font.BOLD, 14));
        titlePanel.add(booksTitle);
        
        // Create table for user's books
        String[] columns = {
                ResourceManager.getString("column.bookid"),
                ResourceManager.getString("column.dateissued"),
                ResourceManager.getString("column.status")
        };
        
        TableUtils.TableComponents tableComponents = TableUtils.createCenteredTable(columns);
        DefaultTableModel booksTableModel = tableComponents.model();
        
        // Load user's books
        loadUserBooksToTable(userId, userPassword, booksTableModel, panel);
        
        // Buttons for book management
        JPanel bookButtonPanel = new JPanel();
        JButton returnBookButton = new JButton(ResourceManager.getString("button.return.book"));
        JButton refreshBooksButton = new JButton(ResourceManager.getString("button.refresh"));
        bookButtonPanel.add(returnBookButton);
        bookButtonPanel.add(refreshBooksButton);
        
        // Set up action listeners for book management
        setupUserBooksActionListeners(userId, userPassword, booksTableModel, tableComponents,returnBookButton, refreshBooksButton, panel);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(tableComponents.scrollPane(), BorderLayout.CENTER);
        panel.add(bookButtonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Sets up action listeners for user book management.
     *
     * @param userId The user ID
     * @param userPassword The user's password
     * @param booksTableModel The books table model
     * @param tableComponents The table components
     * @param returnBookButton The return book button
     * @param refreshBooksButton The refresh books button
     * @param panel The parent panel
     */
    private static void setupUserBooksActionListeners(String userId, String userPassword, DefaultTableModel booksTableModel, TableUtils.TableComponents tableComponents, JButton returnBookButton, JButton refreshBooksButton, JPanel panel) {
    
    returnBookButton.addActionListener(_ -> {
        int selectedRow = tableComponents.table().getSelectedRow();
        if (selectedRow == -1) {
            DialogUtils.showErrorDialog(panel,
                    ResourceManager.getString("button.return.noselection"),
                    ResourceManager.getString("warning")
            );
            return;
        }
        
        String bookId = booksTableModel.getValueAt(selectedRow, 0).toString();
        
        if (bookId.equals(ResourceManager.getString("books.none")) || 
            bookId.equals(ResourceManager.getString("error.load.books"))) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(panel,
                ResourceManager.getString("confirm.return.admin", bookId),
                ResourceManager.getString("confirm"),
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DataBaseManager dbm = new DataBaseManager();
                boolean success = dbm.returnBook(userId, userPassword, bookId);
                
                if (success) {
                    JOptionPane.showMessageDialog(panel,
                            ResourceManager.getString("book.return.success"),
                            ResourceManager.getString("success"),
                            JOptionPane.INFORMATION_MESSAGE);
                    
                    // Refresh the books table
                    loadUserBooksToTable(userId, userPassword, booksTableModel, panel);
                } else {
                    DialogUtils.showErrorDialog(panel,
                            ResourceManager.getString("error.return.failed"),
                            ResourceManager.getString("error")
                    );
                }
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to return book: " + e.getMessage(), e);
                DialogUtils.showErrorDialog(panel,
                        ResourceManager.getString("error.return.exception"),
                        ResourceManager.getString("error")
                );
            }
        }
    });
    
    refreshBooksButton.addActionListener(_ -> loadUserBooksToTable(userId, userPassword, booksTableModel, panel));
}

    /**
     * Loads the user's borrowed books into the table with decrypted data.
     *
     * @param userId The user ID
     * @param userPassword The user's password for decryption
     * @param model The table model to populate
     */
    private static void loadUserBooksToTable(String userId, String userPassword, DefaultTableModel model, JPanel panel) {
        model.setRowCount(0);
        
        try {
            DataBaseManager dbm = new DataBaseManager();
            JsonArray books = dbm.findBorrowedBooks(userId, userPassword);
            
            if (books == null || books.isEmpty()) {
                model.addRow(new Object[]{
                        ResourceManager.getString("books.none"),
                        "",
                        ""
                });
                return;
            }
            
            for (int i = 0; i < books.size(); i++) {
                try {
                    JsonObject book = books.get(i).getAsJsonObject();
                    
                    String bookId = book.get("BookID").getAsString();
                    String dateIssued = SecurityManager.decrypt(book.get("DateIssued").getAsString(), userPassword);
                    int statusCode = book.get("Status").getAsInt();
                    String status = getStatusMessage(statusCode);
                    
                    model.addRow(new Object[]{
                            bookId,
                            dateIssued,
                            status
                    });
                    
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error processing user book data at index " + i, e);
                }
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load user books: " + e.getMessage(), e);
            DialogUtils.showErrorDialog(panel,
                    ResourceManager.getString("error.load.books") + "\n" + ResourceManager.getString("error.logs.check"),
                    ResourceManager.getString("error")
            );
        }
    }
}