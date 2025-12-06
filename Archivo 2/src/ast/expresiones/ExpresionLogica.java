package ast.expresiones;

import ast.Visitor;
import ast.OperadorLogico;

public class ExpresionLogica extends Expresion {
    private final Expresion izquierda;
    private final Expresion derecha;
    private final OperadorLogico operador;

    public ExpresionLogica(Expresion izquierda, Expresion derecha, OperadorLogico operador, int line, int column) {
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

    public OperadorLogico getOperador() {
        return operador;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}