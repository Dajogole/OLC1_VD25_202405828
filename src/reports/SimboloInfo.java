package reports;

public class SimboloInfo {
    private final String identificador;
    private final String tipo;
    private final String categoria;
    private final String ambito;
    private final int linea;
    private final int columna;
    private final String valor;

    public SimboloInfo(String identificador, String tipo, String categoria, 
                      String ambito, int linea, int columna, String valor) {
        this.identificador = identificador;
        this.tipo = tipo;
        this.categoria = categoria;
        this.ambito = ambito;
        this.linea = linea;
        this.columna = columna;
        this.valor = valor;
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getAmbito() {
        return ambito;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public String getValor() {
        return valor;
    }
    
    public String[] toTableRow() {
        return new String[] {
            identificador,
            tipo,
            categoria,
            ambito,
            String.valueOf(linea),
            String.valueOf(columna),
            valor != null ? valor : "null"
        };
    }
}