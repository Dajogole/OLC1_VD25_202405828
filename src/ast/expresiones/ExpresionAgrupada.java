package ast.expresiones;

import ast.Visitor;

public class ExpresionAgrupada extends Expresion {
    private final Expresion expresion;

    public ExpresionAgrupada(Expresion expresion, int line, int column) {
        super(line, column);
        this.expresion = expresion;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}