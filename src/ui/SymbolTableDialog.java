package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


public class SymbolTableDialog extends JDialog {

    public SymbolTableDialog(JFrame owner, List<String[]> rows) {
        super(owner, "Reporte Tabla de Símbolos", true);

    String[] columnNames = {
                "#",
                "Id",
                "Tipo",
                "Tipo",
                "Entorno",
                "Valor",
                "Línea",
                "Columna"
        };



        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        if (rows != null) {
            for (String[] row : rows) {
                model.addRow(row);
            }
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnCerrar);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(700, 400);
        setLocationRelativeTo(owner);
    }
}
