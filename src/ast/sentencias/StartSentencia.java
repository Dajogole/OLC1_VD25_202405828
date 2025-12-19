package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;
import java.util.List;


public class StartSentencia extends Sentencia {

    private final String nombre;
    private final List<Expresion> argumentos;

    public StartSentencia(String nombre, List<Expresion> argumentos, int line, int column) {
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
