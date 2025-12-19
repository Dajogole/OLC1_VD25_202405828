package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;


public class ReturnSentencia extends Sentencia {

    private final Expresion valor;

    public ReturnSentencia(Expresion valor, int line, int column) {
        super(line, column);
        this.valor = valor;
    }

    public Expresion getValor() {
        return valor;
    }

    public boolean esReturnVoid() {
        return valor == null;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
