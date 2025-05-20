package main.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Utility class for creating and configuring tables in the library management system UI.
 * Provides methods to create standard and centered-aligned tables with non-editable cells.
 */
public class TableUtils {
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