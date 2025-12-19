package ast.sentencias;

import ast.Parametro;
import ast.Visitor;
import java.util.List;
import semantic.Tipo;


public class DeclaracionFuncion extends Sentencia {

    private final Tipo tipoRetorno;
    private final String nombre;
    private final List<Parametro> parametros;
    private final BloqueSentencias cuerpo;

    public DeclaracionFuncion(Tipo tipoRetorno, String nombre, List<Parametro> parametros, BloqueSentencias cuerpo, int line, int column) {
        super(line, column);
        this.tipoRetorno = tipoRetorno;
        this.nombre = nombre;
        this.parametros = parametros;
        this.cuerpo = cuerpo;
    }

    public Tipo getTipoRetorno() {
        return tipoRetorno;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Parametro> getParametros() {
        return parametros;
    }

    public BloqueSentencias getCuerpo() {
        return cuerpo;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
