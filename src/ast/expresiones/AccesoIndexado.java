package ast.expresiones;

import ast.Visitor;

/**
 * Acceso indexado:
 *  - id[i]
 *  - id[i][j]
 *  - también puede usarse sobre listas
 */
public class AccesoIndexado extends Expresion {

    private final Expresion objetivo;
    private final Expresion indice1;
    private final Expresion indice2; // null si es 1D

    public AccesoIndexado(Expresion objetivo, Expresion indice1, Expresion indice2, int line, int column) {
        super(line, column);
        this.objetivo = objetivo;
        this.indice1 = indice1;
        this.indice2 = indice2;
    }

    public Expresion getObjetivo() {
        return objetivo;
    }

    public Expresion getIndice1() {
        return indice1;
    }

    public Expresion getIndice2() {
        return indice2;
    }

    public boolean es2D() {
        return indice2 != null;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
