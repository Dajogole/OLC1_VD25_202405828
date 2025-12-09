package ast.expresiones;

import ast.Visitor;

public class LiteralEntero extends Expresion {
    private final int valor;

    public LiteralEntero(int valor, int line, int column) {
        super(line, column);
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}