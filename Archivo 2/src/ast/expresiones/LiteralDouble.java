package ast.expresiones;

import ast.Visitor;

public class LiteralDouble extends Expresion {
    private final double valor;

    public LiteralDouble(double valor, int line, int column) {
        super(line, column);
        this.valor = valor;
    }

    public double getValor() {
        return valor;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}