package ast;

import ast.sentencias.Sentencia;
import java.util.List;

public class Programa extends NodoAST {
    private final List<Sentencia> sentencias;

    public Programa(List<Sentencia> sentencias, int line, int column) {
        super(line, column);
        this.sentencias = sentencias;
    }

    public List<Sentencia> getSentencias() {
        return sentencias;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}