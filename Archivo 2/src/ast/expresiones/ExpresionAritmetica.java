package ast.expresiones;

import ast.Visitor;
import ast.OperadorAritmetico;

public class ExpresionAritmetica extends Expresion {
    private final Expresion izquierda;
    private final Expresion derecha;
    private final OperadorAritmetico operador;

    public ExpresionAritmetica(Expresion izquierda, Expresion derecha, OperadorAritmetico operador, int line, int column) {
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

    public OperadorAritmetico getOperador() {
        return operador;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}