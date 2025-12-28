package ui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class AstReportDialog extends JDialog {

    public AstReportDialog(JFrame owner, Path pngPath, Path dotPath, String generationError) {
        super(owner, "Reporte AST", true);

        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        JLabel info = new JLabel();
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        top.add(info, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);

        JComponent center = buildCenterComponent(pngPath, dotPath, generationError, info);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardarPng = new JButton("Guardar PNG...");
        JButton btnGuardarDot = new JButton("Guardar DOT...");
        JButton btnCerrar = new JButton("Cerrar");

        btnGuardarPng.setEnabled(pngPath != null && Files.exists(pngPath));
        btnGuardarDot.setEnabled(dotPath != null && Files.exists(dotPath));

        btnGuardarPng.addActionListener(e -> guardarArchivo(owner, pngPath, "png"));
        btnGuardarDot.addActionListener(e -> guardarArchivo(owner, dotPath, "dot"));
        btnCerrar.addActionListener(e -> dispose());

        bottom.add(btnGuardarDot);
        bottom.add(btnGuardarPng);
        bottom.add(btnCerrar);

        add(bottom, BorderLayout.SOUTH);

        setSize(1000, 700);
        setLocationRelativeTo(owner);
    }

    private JComponent buildCenterComponent(Path pngPath, Path dotPath, String generationError, JLabel info) {
        boolean pngOk = pngPath != null && Files.exists(pngPath);

        if (pngOk) {
            info.setText("AST generado correctamente: " + pngPath.toAbsolutePath());
            ImageIcon icon = new ImageIcon(pngPath.toAbsolutePath().toString());

            JLabel label = new JLabel(icon);
            label.setHorizontalAlignment(SwingConstants.CENTER);

            JScrollPane scroll = new JScrollPane(label);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.getHorizontalScrollBar().setUnitIncrement(16);

            scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            return scroll;
        }

        // Si no existe el PNG, mostramos un panel con el error y el DOT como fallback.
        StringBuilder msg = new StringBuilder();
        msg.append("<html><b>No se pudo generar ast.png</b><br/>");
        if (generationError != null && !generationError.isEmpty()) {
            msg.append(escapeHtml(generationError)).append("<br/>");
        }
        msg.append("Asegúrate de tener Graphviz instalado y el comando <code>dot</code> disponible en PATH.");
        msg.append("</html>");

        info.setText(msg.toString());

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        if (dotPath != null && Files.exists(dotPath)) {
            try {
                area.setText(new String(Files.readAllBytes(dotPath), java.nio.charset.StandardCharsets.UTF_8));
            } catch (IOException ignored) {
                area.setText("No se pudo leer el archivo DOT.");
            }
        } else {
            area.setText("No se encontró el archivo DOT.");
        }

        panel.add(new JLabel("DOT generado:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);

        return panel;
    }

    private void guardarArchivo(Component parent, Path source, String ext) {
        if (source == null || !Files.exists(source)) {
            JOptionPane.showMessageDialog(
                    parent,
                    "No existe el archivo a guardar.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar " + ext.toUpperCase());
        chooser.setFileFilter(new FileNameExtensionFilter(ext.toUpperCase() + " (*." + ext + ")", ext));

        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return;

        Path target = chooser.getSelectedFile().toPath();
        if (!target.getFileName().toString().toLowerCase().endsWith("." + ext)) {
            target = target.resolveSibling(target.getFileName().toString() + "." + ext);
        }

        try {
            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(
                    parent,
                    "Guardado en:\n" + target.toAbsolutePath(),
                    "Listo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    parent,
                    "No se pudo guardar el archivo:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
