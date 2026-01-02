package reports;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TablaErrores {

    private final List<ErrorInfo> errores = new ArrayList<>();

    public void agregarError(ErrorTipo tipo, String descripcion, int linea, int columna) {
        int numeroInterno = errores.size() + 1;
        errores.add(new ErrorInfo(numeroInterno, tipo, descripcion, linea, columna));
    }

    public boolean tieneErrores() {
        return !errores.isEmpty();
    }

    public boolean tieneErroresDeTipo(ErrorTipo tipo) {
    if (tipo == null) return false;
    for (ErrorInfo e : errores) {
        if (tipo.equals(e.getTipo())) return true;
    }
    return false;
}

public boolean tieneErroresLexicos() {
    return tieneErroresDeTipo(ErrorTipo.LEXICO);
}

public boolean tieneErroresSintacticos() {
    return tieneErroresDeTipo(ErrorTipo.SINTACTICO);
}

    public void limpiar() {
        errores.clear();
    }

   
    public List<ErrorInfo> getErrores() {
        List<ErrorInfo> copia = new ArrayList<>(errores);

        copia.sort(
    Comparator
        .comparingInt((ErrorInfo e) -> ordenTipo(e.getTipo()))
        .thenComparingInt(ErrorInfo::getLinea)
        .thenComparingInt(ErrorInfo::getColumna)
        .thenComparingInt(ErrorInfo::getNumero)
);

        return copia;
    }

    public List<String[]> toTableData() {
        List<ErrorInfo> lista = getErrores();
        List<String[]> rows = new ArrayList<>();

        for (int i = 0; i < lista.size(); i++) {
            ErrorInfo e = lista.get(i);


            rows.add(new String[] {
                String.valueOf(i + 1),
                formatTipo(e.getTipo()),
                e.getDescripcion(),
                String.valueOf(e.getLinea()),
                String.valueOf(e.getColumna())
            });
        }

        return rows;
    }

    private int ordenTipo(ErrorTipo tipo) {
    if (tipo == null) return 99;
    switch (tipo) {
        case LEXICO: return 0;
        case SEMANTICO: return 1;
        case SINTACTICO: return 2;
        default: return 99;
    }
}


    private String formatTipo(ErrorTipo tipo) {
        if (tipo == null) return "Desconocido";
        switch (tipo) {
            case LEXICO: return "Léxico";
            case SINTACTICO: return "Sintáctico";
            case SEMANTICO: return "Semántico";
            default: return "Desconocido";
        }
    }
}
