package ast.expresiones;

import ast.Visitor;

public class LiteralBooleano extends Expresion {
    private final boolean valor;

    public LiteralBooleano(boolean valor, int line, int column) {
        super(line, column);
        this.valor = valor;
    }

    public boolean getValor() {
        return valor;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}