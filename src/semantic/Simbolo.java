package semantic;

public class Simbolo {
    private final String identificador;
    private final Tipo tipo;
    private final CategoriaSimbolo categoria;
    private Object valor;
    private int linea;
    private int columna;


    private String ambito;

    private boolean valorPorDefecto;

    public Simbolo(String identificador,
                   Tipo tipo,
                   CategoriaSimbolo categoria,
                   int linea,
                   int columna) {
        this.identificador = identificador;
        this.tipo = tipo;
        this.categoria = categoria;
        this.linea = linea;
        this.columna = columna;
        this.valorPorDefecto = false;


        this.ambito = "Global";
    }

    public String getIdentificador() { return identificador; }
    public Tipo getTipo() { return tipo; }
    public CategoriaSimbolo getCategoria() { return categoria; }

    public Object getValor() { return valor; }
    public void setValor(Object valor) { this.valor = valor; }

    public int getLinea() { return linea; }
    public int getColumna() { return columna; }

    public void setLinea(int linea) { this.linea = linea; }
    public void setColumna(int columna) { this.columna = columna; }

    public boolean isValorPorDefecto() { return valorPorDefecto; }
    public void setValorPorDefecto(boolean valorPorDefecto) { this.valorPorDefecto = valorPorDefecto; }


    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        if (ambito == null || ambito.trim().isEmpty()) {
            this.ambito = "Global";
        } else {
            this.ambito = ambito;
        }
    }

    @Override
    public String toString() {
        return identificador + " (" + tipo + ") = "
                + (valor != null ? valor.toString() : "null");
    }
}
