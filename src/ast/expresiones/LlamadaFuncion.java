package ast.expresiones;

import ast.Visitor;
import java.util.List;

public class LlamadaFuncion extends Expresion {

    private final String nombre;
    private final List<Expresion> argumentos;

    public LlamadaFuncion(String nombre, List<Expresion> argumentos, int line, int column) {
        super(line, column);
        this.nombre = nombre;
        this.argumentos = argumentos;
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
