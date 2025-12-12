package ast.sentencias;

import ast.Visitor;

public class ContinueSentencia extends Sentencia {
    public ContinueSentencia(int line, int column) {
        super(line, column);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}