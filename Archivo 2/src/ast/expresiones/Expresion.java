package ast.expresiones;

import ast.NodoAST;

public abstract class Expresion extends NodoAST {
    protected Expresion(int line, int column) {
        super(line, column);
    }
}