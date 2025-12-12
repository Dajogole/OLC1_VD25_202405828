package ast.sentencias;

import ast.Visitor;
import java.util.List;

public class Default extends Sentencia {
    private final List<Sentencia> sentencias;

    public Default(List<Sentencia> sentencias, int line, int column) {
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