package ast.sentencias;

import ast.Visitor;
import java.util.List;

public class BloqueSentencias extends Sentencia {
    private final List<Sentencia> sentencias;

    public BloqueSentencias(List<Sentencia> sentencias, int line, int column) {
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