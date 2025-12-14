package ui;

import javax.swing.*;
import java.awt.*;


public class ConsolePanel extends JPanel {

    private final JTextArea consoleArea;

    public ConsolePanel() {
        setLayout(new BorderLayout());

        consoleArea = new JTextArea(6, 80);
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        consoleArea.setLineWrap(true);
        consoleArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(consoleArea);
        add(scrollPane, BorderLayout.CENTER);

        setBorder(BorderFactory.createTitledBorder("Consola"));

        setPreferredSize(new Dimension(1000, 260));
    }

    public void clear() {
        consoleArea.setText("");
    }

    public void appendLine(String text) {
        if (consoleArea.getText().length() == 0) {
            consoleArea.append(text);
        } else {
            consoleArea.append("\n" + text);
        }
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }
}
