package ast.sentencias;

import ast.Visitor;

public class BreakSentencia extends Sentencia {
    public BreakSentencia(int line, int column) {
        super(line, column);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}