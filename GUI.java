import Core.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class GUI extends JFrame {
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private JPanel loginPanel;
    private JPanel mainPanel;
    private JTextField passwordField;
    private JTextField idField;
    private String currentUser;
    private JLabel welcomeLabel;
    private String currentUserName;

    public GUI() {
        setTitle("Library Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        // Create card layout and main container
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Create login panel
        createLoginPanel();

        // Create main application panel
        createMainPanel();

        // Add panels to card layout
        cardPanel.add(loginPanel, "login");
        cardPanel.add(mainPanel, "main");

        // Show login panel first
        cardLayout.show(cardPanel, "login");

        // Set the content pane
        setContentPane(cardPanel);
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel idLabel = new JLabel("User ID:");
        JLabel passwordLabel = new JLabel("Password:");
        idField = new JTextField(15);
        passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");

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

        loginButton.addActionListener(_ -> {
            String id = idField.getText();
            String password = passwordField.getText();

            DataBaseManager dbm = new DataBaseManager();
            JsonObject user = dbm.findUser(id, password);

            if (user != null) {
                try {
                    String encryptedPassword = user.get("Password").getAsString();
                    String decryptedPassword = Core.SecurityManager.decrypt(encryptedPassword, password);

                    if (decryptedPassword.equals(password)) {
                        String encryptedID = user.get("UserID").getAsString();
                        currentUser = Core.SecurityManager.decrypt(encryptedID, password);

                        String encryptedName = user.get("Name").getAsString();
                        currentUserName = Core.SecurityManager.decrypt(encryptedName, password);

                        switchToMainPanel();
                    } else {
                        JOptionPane.showMessageDialog(loginPanel, "Invalid Password!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(loginPanel, "Invalid Password!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(loginPanel, "Invalid User ID or Password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout());

        // Top panel with welcome message and logout button
        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("Welcome to the Library Management System");
        JButton logoutButton = new JButton("Logout");

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);

        // Main content with tabbed pane for different functions
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Browse Books", createBrowseBooksPanel());
        tabbedPane.addTab("My Books", createMyBooksPanel());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Add logout functionality
        logoutButton.addActionListener(_ -> {
            currentUser = null;
            currentUserName = null;
            welcomeLabel.setText("Welcome to the Library Management System");
            idField.setText("");
            passwordField.setText("");
            cardLayout.show(cardPanel, "login");
        });
    }

    private JPanel createBrowseBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        String[] columns = {"Shelf", "Title", "Author", "Publisher", "Available"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);

        JTable booksTable = new JTable(tableModel);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < booksTable.getColumnCount(); i++) {
            booksTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(booksTable);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadBooksToTable(tableModel, "");

        searchButton.addActionListener(_ -> {
            String searchTerm = searchField.getText();
            loadBooksToTable(tableModel, searchTerm);
        });

        return panel;
    }

    private void loadBooksToTable(DefaultTableModel model, String searchTerm) {
        model.setRowCount(0);

        DataBaseManager dbm = new DataBaseManager();
        JsonArray books = dbm.findBooks(searchTerm);

        for (int i = 0; i < books.size(); i++) {
            JsonObject book = books.get(i).getAsJsonObject();
            int shelfNumber = dbm.getShelfNumber(book.get("BookID").getAsString());

            model.addRow(new Object[]{
                    shelfNumber,
                    book.get("Title").getAsString(),
                    book.get("Author").getAsString(),
                    book.get("Publisher").getAsString(),
                    book.get("Available").getAsInt() > 0 ? "Yes" : "No"
            });
        }
    }

    private JPanel createMyBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Title", "Date Issued", "Status"};
        Object[][] data = {}; // Will be populated based on user
        JTable myBooksTable = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(myBooksTable);

        JButton returnButton = new JButton("Return Selected Book");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(returnButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createManageBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField bookIdField = new JTextField(15);
        JButton addButton = new JButton("Add Book");
        JButton removeButton = new JButton("Remove Book");

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Book ID:"));
        inputPanel.add(bookIdField);
        inputPanel.add(addButton);
        inputPanel.add(removeButton);

        panel.add(inputPanel, BorderLayout.NORTH);

        return panel;
    }

    private void switchToMainPanel() {
        welcomeLabel.setText("Welcome, " + currentUserName + " to the Library Management System");

        Component component = mainPanel.getComponent(1);
        if (component instanceof JTabbedPane tabbedPane) {

            // Remove admin tab if it exists (when switching users)
            if (tabbedPane.getTabCount() > 2) {
                tabbedPane.removeTabAt(2);
            }

            // Add admin tab if current user is admin
            if (currentUser.equals("admin")) {
                tabbedPane.addTab("Manage Books", createManageBooksPanel());
            }
        }

        cardLayout.show(cardPanel, "main");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}