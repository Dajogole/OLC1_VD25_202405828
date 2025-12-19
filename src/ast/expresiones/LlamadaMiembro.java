package ast.expresiones;

import ast.Visitor;
import java.util.List;

/**
 * Llamada de miembro:
 *   obj.metodo(arg1, arg2, ...)
 *
 * Usado para List.append / remove / Find, y también puede ser extendido a futuro.
 */
public class LlamadaMiembro extends Expresion {

    private final Expresion objetivo;
    private final String nombre;
    private final List<Expresion> argumentos;

    public LlamadaMiembro(Expresion objetivo, String nombre, List<Expresion> argumentos, int line, int column) {
        super(line, column);
        this.objetivo = objetivo;
        this.nombre = nombre;
        this.argumentos = argumentos;
    }

    public Expresion getObjetivo() {
        return objetivo;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Expresion> getArgumentos() {
        return argumentos;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
