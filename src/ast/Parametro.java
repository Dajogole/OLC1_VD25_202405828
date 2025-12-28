package ast;

import semantic.Tipo;


public class Parametro {
    private final String nombre;
    private final Tipo tipo;
    private final int line;
    private final int column;

    public Parametro(String nombre, Tipo tipo, int line, int column) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.line = line;
        this.column = column;
    }

    public String getNombre() {
        return nombre;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
