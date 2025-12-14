package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


public class ErrorTableDialog extends JDialog {

    public ErrorTableDialog(JFrame owner, List<String[]> rows) {
        super(owner, "Reporte de Errores", true);

        String[] columnNames = {"#", "Tipo", "Descripción", "Línea", "Columna"};

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (rows != null) {
            for (String[] row : rows) {
                model.addRow(row);
            }
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnCerrar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(900, 450);
        setLocationRelativeTo(owner);
    }
}
