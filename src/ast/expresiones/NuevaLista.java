package ast.expresiones;

import ast.Visitor;
import semantic.Tipo;

/**
 * Expresión: new List()
 *
 * El tipo genérico (List<int>, List<double>, etc.) se determina por contexto en el semántico.
 */
public class NuevaLista extends Expresion {

    /**
     * Tipo genérico explícito cuando viene en el source:
     *   new List<int>()
     *
     * Puede ser null si el source fue:
     *   new List()
     *
     * Nota: esto se agregó para compatibilizar con las acciones del Parser
     * generado por CUP que instancia new NuevaLista(tipo, line, col).
     * Si tu análisis semántico ya deduce el tipo por contexto, puedes ignorarlo.
     */
    private final Tipo tipoExplicito;

    /** new List() */
    public NuevaLista(int line, int column) {
        this(null, line, column);
    }

    /** new List<T>() */
    public NuevaLista(Tipo tipoExplicito, int line, int column) {
        super(line, column);
        this.tipoExplicito = tipoExplicito;
    }

    public Tipo getTipoExplicito() {
        return tipoExplicito;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
