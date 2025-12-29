package ui;

import javax.swing.*;
import java.awt.*;


public class MainWindow extends JFrame {

    private final EditorTabPanel editorTabPanel;
    private final ConsolePanel consolePanel;
    private final UiController controller;

    public MainWindow() {
        super("JavaUSAC - Fase 1");

        this.editorTabPanel = new EditorTabPanel();
        this.consolePanel = new ConsolePanel();
        this.controller = new UiController(this);

        configureWindow();
        createMenuBar();
        layoutComponents();
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        add(editorTabPanel, BorderLayout.CENTER);
        add(consolePanel, BorderLayout.SOUTH);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();


        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemNuevo = new JMenuItem("Nuevo");
        JMenuItem itemAbrir = new JMenuItem("Abrir...");
        JMenuItem itemGuardar = new JMenuItem("Guardar");
        JMenuItem itemGuardarComo = new JMenuItem("Guardar como...");
        JMenuItem itemCerrarPestaña = new JMenuItem("Cerrar pestaña");
        JMenuItem itemSalir = new JMenuItem("Salir");

        itemNuevo.addActionListener(e -> controller.nuevoArchivo());
        itemAbrir.addActionListener(e -> controller.abrirArchivo());
        itemGuardar.addActionListener(e -> controller.guardarArchivo());
        itemGuardarComo.addActionListener(e -> controller.guardarArchivoComo());
        itemCerrarPestaña.addActionListener(e -> controller.cerrarPestañaActual());
        itemSalir.addActionListener(e -> controller.salirAplicacion());

        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.addSeparator();
        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemGuardarComo);
        menuArchivo.addSeparator();
        menuArchivo.add(itemCerrarPestaña);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);


        JMenu menuEjecutar = new JMenu("Ejecutar");
        JMenuItem itemEjecutar = new JMenuItem("Ejecutar archivo actual");
        itemEjecutar.addActionListener(e -> controller.ejecutarArchivoActual());
        menuEjecutar.add(itemEjecutar);


        JMenu menuReportes = new JMenu("Reportes");
        JMenuItem itemReporteErrores = new JMenuItem("Reporte de Errores");
        JMenuItem itemReporteSimbolos = new JMenuItem("Reporte Tabla de Símbolos");
        JMenuItem itemReporteAst = new JMenuItem("Reporte AST");

itemReporteAst.addActionListener(e -> controller.mostrarReporteAST());

menuReportes.add(itemReporteAst);


        itemReporteErrores.addActionListener(e -> controller.mostrarReporteErrores());
        itemReporteSimbolos.addActionListener(e -> controller.mostrarReporteTablaSimbolos());

        menuReportes.add(itemReporteErrores);
        menuReportes.add(itemReporteSimbolos);

        menuBar.add(menuArchivo);
        menuBar.add(menuEjecutar);
        menuBar.add(menuReportes);

        setJMenuBar(menuBar);
    }



    public EditorTabPanel getEditorTabPanel() {
        return editorTabPanel;
    }

    public ConsolePanel getConsolePanel() {
        return consolePanel;
    }

    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    
    public void showInfoDialog(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
