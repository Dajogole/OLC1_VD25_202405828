package semantic;

public enum Tipo {
    INT,
    DOUBLE,
    BOOL,
    CHAR,
    STRING,
    ERROR;

    public static Tipo fromString(String typeName) {
        switch (typeName.toLowerCase()) {
            case "int": return INT;
            case "double": return DOUBLE;
            case "bool": return BOOL;
            case "char": return CHAR;
            case "string": return STRING;
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
            default: return "error";
        }
    }
}