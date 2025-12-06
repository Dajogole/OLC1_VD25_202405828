package ast.expresiones;

import ast.Visitor;
import semantic.Tipo;

public class ExpresionCasteo extends Expresion {
    private final Tipo tipoDestino;
    private final Expresion expresion;

    public ExpresionCasteo(Tipo tipoDestino, Expresion expresion, int line, int column) {
        super(line, column);
        this.tipoDestino = tipoDestino;
        this.expresion = expresion;
    }

    public Tipo getTipoDestino() {
        return tipoDestino;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}