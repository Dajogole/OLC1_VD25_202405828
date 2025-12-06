package ast.expresiones;

import ast.Visitor;

public class LiteralChar extends Expresion {
    private final char valor;

    public LiteralChar(char valor, int line, int column) {
        super(line, column);
        this.valor = valor;
    }

    public char getValor() {
        return valor;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}