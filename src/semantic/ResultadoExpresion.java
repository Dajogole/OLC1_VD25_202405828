package semantic;

public class ResultadoExpresion {
    private final Tipo tipo;
    private final Object valor;
    private final SemanticError error;

    public ResultadoExpresion(Tipo tipo, Object valor) {
        this.tipo = tipo;
        this.valor = valor;
        this.error = null;
    }

    public ResultadoExpresion(SemanticError error) {
        this.tipo = Tipo.ERROR;
        this.valor = null;
        this.error = error;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Object getValor() {
        return valor;
    }

    public SemanticError getError() {
        return error;
    }

    public boolean tieneError() {

    return this.tipo == Tipo.ERROR || error != null;
}

}