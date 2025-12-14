package interpreter;

public class Valor {
    private final Object valor;
    private final String tipo;

    public Valor(Object valor, String tipo) {
        this.valor = valor;
        this.tipo = tipo;
    }

    public Object getValor() {
        return valor;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return valor != null ? valor.toString() : "null";
    }
}