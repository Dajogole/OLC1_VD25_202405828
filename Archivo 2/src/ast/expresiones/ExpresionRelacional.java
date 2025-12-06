package ast.expresiones;

import ast.Visitor;
import ast.OperadorRelacional;

public class ExpresionRelacional extends Expresion {
    private final Expresion izquierda;
    private final Expresion derecha;
    private final OperadorRelacional operador;

    public ExpresionRelacional(Expresion izquierda, Expresion derecha, OperadorRelacional operador, int line, int column) {
        super(line, column);
        this.izquierda = izquierda;
        this.derecha = derecha;
        this.operador = operador;
    }

    public Expresion getIzquierda() {
        return izquierda;
    }

    public Expresion getDerecha() {
        return derecha;
    }

    public OperadorRelacional getOperador() {
        return operador;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}