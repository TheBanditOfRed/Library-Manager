package main.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TableUtils {
    public record TableComponents(JTable table, JScrollPane scrollPane, DefaultTableModel model) {
    }

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