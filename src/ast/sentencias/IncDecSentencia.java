package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;


public class IncDecSentencia extends Sentencia {

    private final Expresion objetivo;
    private final boolean incremento;

    public IncDecSentencia(Expresion objetivo, boolean incremento, int line, int column) {
        super(line, column);
        this.objetivo = objetivo;
        this.incremento = incremento;
    }

    public Expresion getObjetivo() {
        return objetivo;
    }

    public boolean esIncremento() {
        return incremento;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
