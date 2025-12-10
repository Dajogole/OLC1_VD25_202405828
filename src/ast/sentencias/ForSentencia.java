package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;

public class ForSentencia extends Sentencia {
    private final Sentencia inicializacion;
    private final Expresion condicion;
    private final Sentencia incremento;
    private final BloqueSentencias bloque;

    public ForSentencia(Sentencia inicializacion, Expresion condicion, Sentencia incremento, BloqueSentencias bloque, int line, int column) {
        super(line, column);
        this.inicializacion = inicializacion;
        this.condicion = condicion;
        this.incremento = incremento;
        this.bloque = bloque;
    }

    public Sentencia getInicializacion() {
        return inicializacion;
    }

    public Expresion getCondicion() {
        return condicion;
    }

    public Sentencia getIncremento() {
        return incremento;
    }

    public BloqueSentencias getBloque() {
        return bloque;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}