package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;

public class DoWhileSentencia extends Sentencia {
    private final BloqueSentencias bloque;
    private final Expresion condicion;

    public DoWhileSentencia(BloqueSentencias bloque, Expresion condicion, int line, int column) {
        super(line, column);
        this.bloque = bloque;
        this.condicion = condicion;
    }

    public BloqueSentencias getBloque() {
        return bloque;
    }

    public Expresion getCondicion() {
        return condicion;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}