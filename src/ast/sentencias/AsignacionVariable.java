package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;

public class AsignacionVariable extends Sentencia {
    private final String identificador;
    private final Expresion expresion;

    public AsignacionVariable(String identificador, Expresion expresion, int line, int column) {
        super(line, column);
        this.identificador = identificador;
        this.expresion = expresion;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}