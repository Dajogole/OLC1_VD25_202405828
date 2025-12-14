package ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class JavaUSACApp {

    private JavaUSACApp() {

    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            MainWindow mainWindow = new MainWindow();
            mainWindow.setLocationRelativeTo(null); 
            mainWindow.setVisible(true);
        });
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException |
                 InstantiationException |
                 IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            System.err.println("No se pudo aplicar el Look & Feel del sistema: " + e.getMessage());
        }
    }
}
