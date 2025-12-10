package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;

public class IfSentencia extends Sentencia {
    private final Expresion condicion;
    private final BloqueSentencias bloqueIf;
    private final BloqueSentencias bloqueElse;
    private final IfSentencia elseIf;

    public IfSentencia(Expresion condicion, BloqueSentencias bloqueIf, BloqueSentencias bloqueElse, IfSentencia elseIf, int line, int column) {
        super(line, column);
        this.condicion = condicion;
        this.bloqueIf = bloqueIf;
        this.bloqueElse = bloqueElse;
        this.elseIf = elseIf;
    }

    public Expresion getCondicion() {
        return condicion;
    }

    public BloqueSentencias getBloqueIf() {
        return bloqueIf;
    }

    public BloqueSentencias getBloqueElse() {
        return bloqueElse;
    }

    public IfSentencia getElseIf() {
        return elseIf;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}