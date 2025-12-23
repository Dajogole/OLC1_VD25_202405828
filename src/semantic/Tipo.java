package semantic;

import java.util.Locale;

public enum Tipo {
    // Primitivos
    INT,
    DOUBLE,
    BOOL,
    CHAR,
    STRING,

    // Especiales
    VOID,
    ERROR,

    // Vectores
    VECTOR_INT,
    VECTOR_DOUBLE,
    VECTOR_BOOL,
    VECTOR_CHAR,
    VECTOR_STRING,

    // Matrices
    MATRIX_INT,
    MATRIX_DOUBLE,
    MATRIX_BOOL,
    MATRIX_CHAR,
    MATRIX_STRING,

    // Listas
    LIST_INT,
    LIST_DOUBLE,
    LIST_BOOL,
    LIST_CHAR,
    LIST_STRING;

    public static Tipo fromString(String typeName) {
    if (typeName == null) return ERROR;

    String s = typeName.trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", "");

    if (s.isEmpty()) return ERROR;

    // List<T>
    if (s.startsWith("list<") && s.endsWith(">")) {
        String inner = s.substring(5, s.length() - 1);
        Tipo base = fromString(inner);
        if (base == ERROR || base == VOID) return ERROR;
        return listOf(base);
    }

    // Matriz: T[][]
    if (s.endsWith("[][]")) {
        String inner = s.substring(0, s.length() - 4);
        Tipo base = fromString(inner);
        if (base == ERROR || base == VOID) return ERROR;
        return matrixOf(base);
    }

    // Vector: T[]
    if (s.endsWith("[]")) {
        String inner = s.substring(0, s.length() - 2);
        Tipo base = fromString(inner);
        if (base == ERROR || base == VOID) return ERROR;
        return vectorOf(base);
    }

    switch (s) {
        case "int": return INT;
        case "double": return DOUBLE;
        case "bool": return BOOL;
        case "char": return CHAR;
        case "string": return STRING;
        case "void": return VOID;
        default: return ERROR;
    }
}


    public boolean isPrimitive() {
        return this == INT || this == DOUBLE || this == BOOL || this == CHAR || this == STRING;
    }

    public boolean isNumeric() {
        return this == INT || this == DOUBLE || this == CHAR;
    }

    public boolean isVector() {
        return this == VECTOR_INT || this == VECTOR_DOUBLE || this == VECTOR_BOOL || this == VECTOR_CHAR || this == VECTOR_STRING;
    }

    public boolean isMatrix() {
        return this == MATRIX_INT || this == MATRIX_DOUBLE || this == MATRIX_BOOL || this == MATRIX_CHAR || this == MATRIX_STRING;
    }

    public boolean isList() {
        return this == LIST_INT || this == LIST_DOUBLE || this == LIST_BOOL || this == LIST_CHAR || this == LIST_STRING;
    }

    public Tipo baseType() {
        switch (this) {
            case VECTOR_INT:
            case MATRIX_INT:
            case LIST_INT:
                return INT;
            case VECTOR_DOUBLE:
            case MATRIX_DOUBLE:
            case LIST_DOUBLE:
                return DOUBLE;
            case VECTOR_BOOL:
            case MATRIX_BOOL:
            case LIST_BOOL:
                return BOOL;
            case VECTOR_CHAR:
            case MATRIX_CHAR:
            case LIST_CHAR:
                return CHAR;
            case VECTOR_STRING:
            case MATRIX_STRING:
            case LIST_STRING:
                return STRING;
            default:
                return this;
        }
    }

    public static Tipo vectorOf(Tipo base) {
        switch (base) {
            case INT: return VECTOR_INT;
            case DOUBLE: return VECTOR_DOUBLE;
            case BOOL: return VECTOR_BOOL;
            case CHAR: return VECTOR_CHAR;
            case STRING: return VECTOR_STRING;
            default: return ERROR;
        }
    }

    public static Tipo matrixOf(Tipo base) {
        switch (base) {
            case INT: return MATRIX_INT;
            case DOUBLE: return MATRIX_DOUBLE;
            case BOOL: return MATRIX_BOOL;
            case CHAR: return MATRIX_CHAR;
            case STRING: return MATRIX_STRING;
            default: return ERROR;
        }
    }

    public static Tipo listOf(Tipo base) {
        switch (base) {
            case INT: return LIST_INT;
            case DOUBLE: return LIST_DOUBLE;
            case BOOL: return LIST_BOOL;
            case CHAR: return LIST_CHAR;
            case STRING: return LIST_STRING;
            default: return ERROR;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case INT: return "int";
            case DOUBLE: return "double";
            case BOOL: return "bool";
            case CHAR: return "char";
            case STRING: return "string";
            case VOID: return "void";

            case VECTOR_INT: return "int[]";
            case VECTOR_DOUBLE: return "double[]";
            case VECTOR_BOOL: return "bool[]";
            case VECTOR_CHAR: return "char[]";
            case VECTOR_STRING: return "string[]";

            case MATRIX_INT: return "int[][]";
            case MATRIX_DOUBLE: return "double[][]";
            case MATRIX_BOOL: return "bool[][]";
            case MATRIX_CHAR: return "char[][]";
            case MATRIX_STRING: return "string[][]";

            case LIST_INT: return "List<int>";
            case LIST_DOUBLE: return "List<double>";
            case LIST_BOOL: return "List<bool>";
            case LIST_CHAR: return "List<char>";
            case LIST_STRING: return "List<string>";

            default: return "error";
        }
    }
}
