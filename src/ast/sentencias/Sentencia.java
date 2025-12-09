package ast.sentencias;

import ast.NodoAST;

public abstract class Sentencia extends NodoAST {
    protected Sentencia(int line, int column) {
        super(line, column);
    }
}