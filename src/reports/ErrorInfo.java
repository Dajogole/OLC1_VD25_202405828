package reports;

public class ErrorInfo {
    private int numero;
    private ErrorTipo tipo;
    private String descripcion;
    private int linea;
    private int columna;

    public ErrorInfo(int numero, ErrorTipo tipo, String descripcion, int linea, int columna) {
        this.numero = numero;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.linea = linea;
        this.columna = columna;
    }

    // Constructor sin número (para compatibilidad)
    public ErrorInfo(ErrorTipo tipo, String descripcion, int linea, int columna) {
        this(0, tipo, descripcion, linea, columna);
    }

    // Getters
    public int getNumero() { return numero; }
    public ErrorTipo getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }

    // Setters
    public void setNumero(int numero) { this.numero = numero; }

    @Override
    public String toString() {
        return String.format("#%d: %s - %s (Línea: %d, Columna: %d)",
            numero, tipo, descripcion, linea, columna);
    }
}