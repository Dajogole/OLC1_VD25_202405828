package ui;

import interpreter.Ejecutor;
import reports.TablaErrores;
import reports.ErrorTipo;
import reports.ErrorInfo;
import semantic.Simbolo;
import semantic.TablaSimbolos;
import semantic.VisitanteSemantico;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UiController {

    private final MainWindow mainWindow;

    public UiController(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void nuevoArchivo() {
        mainWindow.getEditorTabPanel().newUntitledFile();
        mainWindow.getConsolePanel().appendLine("Nuevo archivo creado.");
    }

    public void abrirArchivo() {
        JFileChooser chooser = createFileChooser();
        int result = chooser.showOpenDialog(mainWindow);

        if (result == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            try {
                byte[] bytes = Files.readAllBytes(path);
                String content = new String(bytes, StandardCharsets.UTF_8);
                mainWindow.getEditorTabPanel().openFile(path, content);
                mainWindow.getConsolePanel().appendLine("Archivo abierto: " + path);
            } catch (IOException e) {
                mainWindow.showErrorDialog("No se pudo abrir el archivo:\n" + e.getMessage());
            }
        }
    }

    public void guardarArchivo() {
        EditorTabPanel editor = mainWindow.getEditorTabPanel();
        if (!editor.hasOpenTabs()) {
            mainWindow.showInfoDialog("No hay pestañas abiertas para guardar.");
            return;
        }

        Path path = editor.getCurrentFilePath();
        if (path == null) {
            guardarArchivoComo();
        } else {
            writeFile(path);
        }
    }

    public void guardarArchivoComo() {
    EditorTabPanel editor = mainWindow.getEditorTabPanel();
    if (!editor.hasOpenTabs()) {
        mainWindow.showInfoDialog("No hay pestañas abiertas para guardar.");
        return;
    }

    JFileChooser chooser = createFileChooser();
    int result = chooser.showSaveDialog(mainWindow);

    if (result == JFileChooser.APPROVE_OPTION) {
        Path path = chooser.getSelectedFile().toPath();

        String lower = path.toString().toLowerCase();
        if (!lower.endsWith(".ju") && !lower.endsWith(".jc")) {
            path = Paths.get(path.toString() + ".ju");
        }

        editor.setCurrentFilePath(path);
        writeFile(path);
    }
}


    private void writeFile(Path path) {
        EditorTabPanel editor = mainWindow.getEditorTabPanel();
        String content = editor.getCurrentFileContent();

        if (content == null) {
            mainWindow.showErrorDialog("No hay contenido para guardar.");
            return;
        }

        try {
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            mainWindow.getConsolePanel().appendLine("Archivo guardado: " + path);
        } catch (IOException e) {
            mainWindow.showErrorDialog("No se pudo guardar el archivo:\n" + e.getMessage());
        }
    }

    public void cerrarPestañaActual() {
        EditorTabPanel editor = mainWindow.getEditorTabPanel();
        if (!editor.hasOpenTabs()) {
            mainWindow.showInfoDialog("No hay pestañas abiertas.");
            return;
        }

        String title = editor.getCurrentTabTitle();
        editor.closeCurrentTab();
        mainWindow.getConsolePanel().appendLine("Pestaña cerrada: " + title);
    }

    public void salirAplicacion() {
        int option = JOptionPane.showConfirmDialog(
                mainWindow,
                "¿Desea salir de la aplicación?",
                "Salir",
                JOptionPane.YES_NO_OPTION
        );
        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private JFileChooser createFileChooser() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(
        new FileNameExtensionFilter("Archivos JavaUSAC (*.ju, *.jc)", "ju", "jc")
    );
    return chooser;
}


    public void ejecutarArchivoActual() {
        EditorTabPanel editor = mainWindow.getEditorTabPanel();
        ConsolePanel console = mainWindow.getConsolePanel();

        if (!editor.hasOpenTabs()) {
            mainWindow.showInfoDialog("No hay archivo abierto para ejecutar.");
            return;
        }

        String codigo = editor.getCurrentFileContent();
        if (codigo == null || codigo.trim().isEmpty()) {
            mainWindow.showInfoDialog("El archivo actual está vacío.");
            return;
        }

        Ejecutor.ejecutar(codigo, console);
    }

    public void mostrarReporteErrores() {
    TablaErrores tablaErrores = Ejecutor.getUltimaTablaErrores();

    if (tablaErrores == null) {
        mainWindow.showInfoDialog(
                "Primero ejecuta un archivo para poder generar el reporte de errores."
        );
        return;
    }

    if (!tablaErrores.tieneErrores()) {
        mainWindow.showInfoDialog(
                "No se encontraron errores en la última ejecución."
        );
        return;
    }

    ErrorTableDialog dialog = new ErrorTableDialog(mainWindow, tablaErrores.toTableData());
    dialog.setVisible(true);
}


    public void mostrarReporteTablaSimbolos() {
    TablaSimbolos tabla = Ejecutor.getUltimaTablaSimbolos();

    if (tabla == null || tabla.getSimbolos().isEmpty()) {
        mainWindow.showInfoDialog(
                "Primero ejecuta un archivo para poder generar el reporte de tabla de símbolos."
        );
        return;
    }

    List<Simbolo> simbolos = tabla.getSimbolos();
    simbolos.sort((s1, s2) -> Integer.compare(s1.getLinea(), s2.getLinea()));

    List<String[]> rows = new ArrayList<>();
    int index = 1;

    for (Simbolo simbolo : simbolos) {
        String numero = String.valueOf(index++);
        String identificador = simbolo.getIdentificador();

        String categoria = simbolo.getCategoria().toString().toLowerCase();
        if (!categoria.isEmpty()) {
            categoria = Character.toUpperCase(categoria.charAt(0)) + categoria.substring(1);
        }

        String tipoDato = simbolo.getTipo().toString().toLowerCase();

       String entorno = simbolo.getAmbito() != null ? simbolo.getAmbito() : "Global";

        Object valor = simbolo.getValor();
        String valorStr = "";
        if (valor != null) {
            valorStr = valor.toString();
        }

        if (simbolo.isValorPorDefecto()) {
            if (!valorStr.isEmpty()) {
                valorStr = valorStr + " (valor por defecto)";
            } else {
                valorStr = "(valor por defecto)";
            }
        }

        String lineaStr = simbolo.getLinea() > 0
                ? String.valueOf(simbolo.getLinea())
                : "";
        String columnaStr = simbolo.getColumna() > 0
                ? String.valueOf(simbolo.getColumna())
                : "";

        rows.add(new String[]{
                numero,
                identificador,
                categoria,
                tipoDato,
                entorno,
                valorStr,
                lineaStr,
                columnaStr
        });
    }

    SymbolTableDialog dialog = new SymbolTableDialog(mainWindow, rows);
    dialog.setVisible(true);
}


}
