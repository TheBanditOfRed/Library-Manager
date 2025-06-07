package main.ui.utils;

import main.core.ResourceManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Utility class for creating and configuring tables in the library management system UI.
 * Provides methods to create standard and centered-aligned tables with non-editable cells.
 */
public class TableUtils {
    /**
     * Checks if a row is selected in the given JTable.
     * If no row is selected, displays an error message dialog.
     *
     * @param table The JTable to check for selection
     * @param parentComponent The parent component for the dialog
     * @param errorMsg The error message to display if no row is selected
     * @return true if a row is selected, false otherwise
     */
    public static boolean isRowSelected(JTable table, Component parentComponent, String errorMsg) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentComponent,
                    errorMsg,
                    ResourceManager.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Record class that bundles together the components of a table setup.
     * Includes the JTable, its containing JScrollPane, and the table's model for data manipulation.
     *
     * @param table The JTable component
     * @param scrollPane The JScrollPane containing the table
     * @param model The DefaultTableModel used by the table
     */
    public record TableComponents(JTable table, JScrollPane scrollPane, DefaultTableModel model) {}

    /**
     * Creates a standard table with the specified columns.
     * The table has the following properties:
     * - Non-editable cells
     * - Fills the entire viewport height
     * - Uses the default cell renderer
     * - Comes with a scroll pane container
     *
     * @param columns Array of column names for the table
     * @return TableComponents record containing the table, scroll pane, and table model
     */
    public  static TableComponents createTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());

        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        return new TableComponents(table, scrollPane, model);
    }

    /**
     * Creates a table with centered cell content alignment.
     * This method builds on createTable() and additionally configures all columns
     * to have center-aligned content using a custom cell renderer.
     *
     * @param columns Array of column names for the table
     * @return TableComponents record containing the table, scroll pane, and table model
     */
    public static TableComponents createCenteredTable(String[] columns) {
        TableComponents tableComponents = createTable(columns);
        JTable table = tableComponents.table();

        // Center align all cell content
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        return tableComponents;
    }

}