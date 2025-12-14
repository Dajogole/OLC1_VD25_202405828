package semantic;

public class SemanticError {

    private final String tipo;
    private final String mensaje;
    private final int linea;
    private final int columna;



    public SemanticError(String mensaje, int linea, int columna) {
        this("Semántico", mensaje, linea, columna);
    }


    public SemanticError(String tipo, String mensaje, int linea, int columna) {
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.linea = linea;
        this.columna = columna;
    }



    public String getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return "SemanticError{" +
                "tipo='" + tipo + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", linea=" + linea +
                ", columna=" + columna +
                '}';
    }
}
