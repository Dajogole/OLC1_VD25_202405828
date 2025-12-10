package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;

public class WhileSentencia extends Sentencia {
    private final Expresion condicion;
    private final BloqueSentencias bloque;

    public WhileSentencia(Expresion condicion, BloqueSentencias bloque, int line, int column) {
        super(line, column);
        this.condicion = condicion;
        this.bloque = bloque;
    }

    public Expresion getCondicion() {
        return condicion;
    }

    public BloqueSentencias getBloque() {
        return bloque;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}