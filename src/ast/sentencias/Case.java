package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;
import java.util.List;

public class Case extends Sentencia {
    private final Expresion valor;
    private final List<Sentencia> sentencias;

    public Case(Expresion valor, List<Sentencia> sentencias, int line, int column) {
        super(line, column);
        this.valor = valor;
        this.sentencias = sentencias;
    }

    public Expresion getValor() {
        return valor;
    }

    public List<Sentencia> getSentencias() {
        return sentencias;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}