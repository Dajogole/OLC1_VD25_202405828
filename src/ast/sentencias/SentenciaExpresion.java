package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;


public class SentenciaExpresion extends Sentencia {

    private final Expresion expresion;

    public SentenciaExpresion(Expresion expresion, int line, int column) {
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
