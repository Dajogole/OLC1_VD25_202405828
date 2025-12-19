package ast.sentencias;

import ast.Visitor;
import ast.expresiones.AccesoIndexado;
import ast.expresiones.Expresion;


public class AsignacionIndexada extends Sentencia {

    private final AccesoIndexado acceso;
    private final Expresion valor;

    public AsignacionIndexada(AccesoIndexado acceso, Expresion valor, int line, int column) {
        super(line, column);
        this.acceso = acceso;
        this.valor = valor;
    }

    
    public AsignacionIndexada(Expresion objetivo,
                              Expresion indice1,
                              Expresion indice2,
                              Expresion valor,
                              int line,
                              int column) {
        this(new AccesoIndexado(objetivo, indice1, indice2, line, column), valor, line, column);
    }

    public AccesoIndexado getAcceso() {
        return acceso;
    }

    public Expresion getValor() {
        return valor;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
