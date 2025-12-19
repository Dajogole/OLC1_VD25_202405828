package ast.expresiones;

import ast.Visitor;
import java.util.List;

/**
 * Literal de matriz: [[...],[...],...]
 *
 * Se representa como lista de filas, donde cada fila es un LiteralVector.
 */
public class LiteralMatriz extends Expresion {

    private final List<LiteralVector> filas;

    public LiteralMatriz(List<LiteralVector> filas, int line, int column) {
        super(line, column);
        this.filas = filas;
    }

    public List<LiteralVector> getFilas() {
        return filas;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
