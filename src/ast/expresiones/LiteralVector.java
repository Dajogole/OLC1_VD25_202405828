package ast.expresiones;

import ast.Visitor;
import java.util.List;

/**
 * Literal de vector: [e1, e2, e3]
 */
public class LiteralVector extends Expresion {

    private final List<Expresion> elementos;

    public LiteralVector(List<Expresion> elementos, int line, int column) {
        super(line, column);
        this.elementos = elementos;
    }

    public List<Expresion> getElementos() {
        return elementos;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
