package ast;

public abstract class NodoAST {
    private final int line;
    private final int column;

    protected NodoAST(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public abstract <T> T accept(Visitor<T> visitor);
}