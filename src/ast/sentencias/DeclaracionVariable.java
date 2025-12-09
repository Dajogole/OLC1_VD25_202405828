package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;
import semantic.Tipo;

public class DeclaracionVariable extends Sentencia {
    private final String identificador;
    private final Tipo tipo;
    private final Expresion expresionInicial;

    public DeclaracionVariable(String identificador, Tipo tipo, Expresion expresionInicial, int line, int column) {
        super(line, column);
        this.identificador = identificador;
        this.tipo = tipo;
        this.expresionInicial = expresionInicial;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Expresion getExpresionInicial() {
        return expresionInicial;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}