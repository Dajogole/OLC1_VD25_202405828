package ast.sentencias;

import ast.Visitor;
import ast.expresiones.Expresion;
import java.util.List;

public class SwitchSentencia extends Sentencia {
    private final Expresion expresion;
    private final List<Case> casos;
    private final Default casoDefault;

    public SwitchSentencia(Expresion expresion, List<Case> casos, Default casoDefault, int line, int column) {
        super(line, column);
        this.expresion = expresion;
        this.casos = casos;
        this.casoDefault = casoDefault;
    }

    public Expresion getExpresion() {
        return expresion;
    }

    public List<Case> getCasos() {
        return casos;
    }

    public Default getCasoDefault() {
        return casoDefault;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}