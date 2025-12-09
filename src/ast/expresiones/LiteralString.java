package ast.expresiones;

import ast.Visitor;

public class LiteralString extends Expresion {
    private final String valor;

    public LiteralString(String valor, int line, int column) {
        super(line, column);
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}