package ast.expresiones;

import ast.Visitor;

public class Identificador extends Expresion {
    private final String nombre;

    public Identificador(String nombre, int line, int column) {
        super(line, column);
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}