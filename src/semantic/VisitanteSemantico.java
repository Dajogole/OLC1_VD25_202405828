package semantic;

import ast.*;
import ast.expresiones.*;
import ast.sentencias.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.HashMap;

public class VisitanteSemantico implements Visitor<ResultadoExpresion> {

    private Entorno entornoActual;
    private final List<SemanticError> errores;
    private final TablaSimbolos tablaSimbolos;
    private final Deque<String> ambitoStack;

    // ---- Fase 2: funciones/métodos, start y estructuras ----
    private final Map<String, DeclaracionFuncion> funciones;
    private int startCount;
    private int funcionesDeclaradas;
    private Tipo retornoEsperado;
    private boolean enFuncion;
    private boolean returnEncontrado;

    public VisitanteSemantico() {
        this.entornoActual = new Entorno(null);
        this.errores = new ArrayList<>();
        this.tablaSimbolos = new TablaSimbolos();
        this.ambitoStack = new ArrayDeque<>();
        this.ambitoStack.push("Global");

        // 3.3 init fase 2
        this.funciones = new HashMap<>();
        this.startCount = 0;
        this.funcionesDeclaradas = 0;
        this.retornoEsperado = null;
        this.enFuncion = false;
        this.returnEncontrado = false;
    }

    public List<SemanticError> getErrores() {
        return errores;
    }

    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }

    private void agregarError(String mensaje, int linea, int columna) {
        errores.add(new SemanticError(mensaje, linea, columna));
    }

    private boolean esNumerico(Tipo tipo) {
        return tipo == Tipo.INT || tipo == Tipo.DOUBLE || tipo == Tipo.CHAR;
    }

    private String ambitoActual() {
        return ambitoStack.isEmpty() ? "Global" : ambitoStack.peek();
    }

    private void pushAmbito(String ambito) {
        ambitoStack.push(ambito == null || ambito.trim().isEmpty() ? "Global" : ambito);
    }

    private void popAmbito() {
        if (!ambitoStack.isEmpty()) ambitoStack.pop();
        if (ambitoStack.isEmpty()) ambitoStack.push("Global");
    }

    // ---- Helpers Fase 2 ----
    private boolean esAsignable(Tipo destino, Tipo fuente) {
        if (destino == null || fuente == null) return false;
        if (destino == fuente) return true;
        // Conversión permitida por enunciado: bool -> int
        if (destino == Tipo.INT && fuente == Tipo.BOOL) return true;
        return false;
    }

    private String firma(DeclaracionFuncion df) {
        StringBuilder sb = new StringBuilder();
        sb.append(df.getNombre()).append("(");
        List<Parametro> ps = df.getParametros();
        if (ps != null) {
            for (int i = 0; i < ps.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(ps.get(i).getTipo());
            }
        }
        sb.append(") : ").append(df.getTipoRetorno());
        return sb.toString();
    }

    private String exprToString(Expresion e) {
        if (e == null) return "";
        if (e instanceof Identificador) return ((Identificador) e).getNombre();
        if (e instanceof LiteralEntero) return String.valueOf(((LiteralEntero) e).getValor());
        if (e instanceof LiteralDouble) return String.valueOf(((LiteralDouble) e).getValor());
        if (e instanceof LiteralBooleano) return String.valueOf(((LiteralBooleano) e).getValor());
        if (e instanceof LiteralChar) return "'" + ((LiteralChar) e).getValor() + "'";
        if (e instanceof LiteralString) return ((LiteralString) e).getValor();
        if (e instanceof ExpresionAgrupada) {
            return "(" + exprToString(((ExpresionAgrupada) e).getExpresion()) + ")";
        }
        if (e instanceof ExpresionAritmetica) {
            ExpresionAritmetica a = (ExpresionAritmetica) e;
            String op = switch (a.getOperador()) {
                case SUMA -> "+";
                case RESTA -> "-";
                case MULTIPLICACION -> "*";
                case DIVISION -> "/";
                case MODULO -> "%";
                case POTENCIA -> "**";
                case NEGACION_UNARIA -> "-";
            };
            if (a.getIzquierda() == null) return op + exprToString(a.getDerecha());
            return exprToString(a.getIzquierda()) + " " + op + " " + exprToString(a.getDerecha());
        }
        if (e instanceof ExpresionRelacional) {
            ExpresionRelacional r = (ExpresionRelacional) e;
            String op = switch (r.getOperador()) {
                case IGUAL -> "==";
                case DIFERENTE -> "!=";
                case MENOR -> "<";
                case MAYOR -> ">";
                case MENOR_IGUAL -> "<=";
                case MAYOR_IGUAL -> ">=";
            };
            return exprToString(r.getIzquierda()) + " " + op + " " + exprToString(r.getDerecha());
        }
        return e.getClass().getSimpleName();
    }

    // 3.4 REEMPLAZO visit(Programa)
    @Override
    public ResultadoExpresion visit(Programa programa) {
        // Primera pasada: registrar firmas de funciones/métodos (sin analizar cuerpos aún)
        funciones.clear();
        funcionesDeclaradas = 0;
        startCount = 0;

        for (Sentencia stmt : programa.getSentencias()) {
            if (stmt instanceof DeclaracionFuncion) {
                DeclaracionFuncion df = (DeclaracionFuncion) stmt;
                String key = df.getNombre().toLowerCase();
                if (funciones.containsKey(key)) {
                    agregarError("No se permite sobrecarga: ya existe una función/método llamado '" + df.getNombre() + "'",
                            df.getLine(), df.getColumn());
                    continue;
                }
                funciones.put(key, df);
                funcionesDeclaradas++;

                // Reporte en tabla de símbolos (global)
                CategoriaSimbolo cat = (df.getTipoRetorno() == Tipo.VOID) ? CategoriaSimbolo.METODO : CategoriaSimbolo.FUNCION;
                Simbolo simbolo = new Simbolo(df.getNombre(), df.getTipoRetorno(), cat, df.getLine(), df.getColumn());
                simbolo.setAmbito("Global");
                simbolo.setValor(firma(df));
                simbolo.setValorPorDefecto(false);
                tablaSimbolos.agregarSimbolo(simbolo);
            }
        }

        // Segunda pasada: análisis semántico completo
        for (Sentencia stmt : programa.getSentencias()) {
            stmt.accept(this);
        }

        // START es obligatorio para programas que declaran funciones/métodos o usan start
        if (funcionesDeclaradas > 0 || startCount > 0) {
            if (startCount == 0) {
                agregarError("Falta la sentencia obligatoria 'start ...();' (punto de entrada)",
                        0, 0);
            } else if (startCount > 1) {
                agregarError("Solo se permite una sentencia 'start ...();' en el programa",
                        0, 0);
            }
        }

        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    @Override
    public ResultadoExpresion visit(LiteralEntero expr) {
        return new ResultadoExpresion(Tipo.INT, expr.getValor());
    }

    @Override
    public ResultadoExpresion visit(LiteralDouble expr) {
        return new ResultadoExpresion(Tipo.DOUBLE, expr.getValor());
    }

    @Override
    public ResultadoExpresion visit(LiteralBooleano expr) {
        return new ResultadoExpresion(Tipo.BOOL, expr.getValor());
    }

    @Override
    public ResultadoExpresion visit(LiteralChar expr) {
        return new ResultadoExpresion(Tipo.CHAR, expr.getValor());
    }

    @Override
    public ResultadoExpresion visit(LiteralString expr) {
        return new ResultadoExpresion(Tipo.STRING, expr.getValor());
    }

    @Override
    public ResultadoExpresion visit(Identificador expr) {
        Simbolo simbolo = entornoActual.buscar(expr.getNombre());
        if (simbolo == null) {
            agregarError("Variable '" + expr.getNombre() + "' no declarada", expr.getLine(), expr.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }
        return new ResultadoExpresion(simbolo.getTipo(), simbolo.getValor());
    }

    @Override
    public ResultadoExpresion visit(ExpresionAritmetica expr) {
        ResultadoExpresion izq = expr.getIzquierda() != null
                ? expr.getIzquierda().accept(this)
                : null;
        ResultadoExpresion der = expr.getDerecha().accept(this);

        if (izq != null && izq.tieneError()) return izq;
        if (der.tieneError()) return der;

        if (expr.getIzquierda() == null && expr.getOperador() == OperadorAritmetico.NEGACION_UNARIA) {
            Tipo tDer = der.getTipo();
            if (tDer == Tipo.INT || tDer == Tipo.DOUBLE) {
                return new ResultadoExpresion(tDer, null);
            } else if (tDer == Tipo.CHAR) {
                return new ResultadoExpresion(Tipo.INT, null);
            } else {
                agregarError("La negación unaria solo se puede aplicar a tipos numéricos",
                        expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
        }

        Tipo tIzq = izq.getTipo();
        Tipo tDer = der.getTipo();

        if (expr.getOperador() == OperadorAritmetico.SUMA && (tIzq == Tipo.STRING || tDer == Tipo.STRING)) {
            return new ResultadoExpresion(Tipo.STRING, null);
        }

        boolean izqNum = esNumerico(tIzq);
        boolean derNum = esNumerico(tDer);

        if (!izqNum || !derNum) {
            agregarError("Tipos incompatibles en expresión aritmética: " +
                            tIzq + " " + expr.getOperador() + " " + tDer,
                    expr.getLine(), expr.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        switch (expr.getOperador()) {
            case SUMA:
            case RESTA:
            case MULTIPLICACION:
                if (tIzq == Tipo.DOUBLE || tDer == Tipo.DOUBLE) {
                    return new ResultadoExpresion(Tipo.DOUBLE, null);
                } else {
                    return new ResultadoExpresion(Tipo.INT, null);
                }
            case DIVISION:
                return new ResultadoExpresion(Tipo.DOUBLE, null);
            case MODULO:
                if ((tIzq == Tipo.INT || tIzq == Tipo.CHAR) &&
                        (tDer == Tipo.INT || tDer == Tipo.CHAR)) {
                    return new ResultadoExpresion(Tipo.INT, null);
                } else {
                    agregarError("El operador % solo se permite entre enteros o chars",
                            expr.getLine(), expr.getColumn());
                    return new ResultadoExpresion(Tipo.ERROR, null);
                }
            case POTENCIA:
                return new ResultadoExpresion(Tipo.DOUBLE, null);
            default:
                agregarError("Operador aritmético no soportado",
                        expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
        }
    }

    @Override
public ResultadoExpresion visit(ExpresionRelacional expr) {
    ResultadoExpresion izq = expr.getIzquierda().accept(this);
    ResultadoExpresion der = expr.getDerecha().accept(this);

    Tipo tIzq = (izq == null) ? Tipo.ERROR : izq.getTipo();
    Tipo tDer = (der == null) ? Tipo.ERROR : der.getTipo();

    // Mapeo de operador a símbolo textual
    String op;
    switch (expr.getOperador()) {
        case IGUAL: op = "=="; break;
        case DIFERENTE: op = "!="; break;
        case MENOR: op = "<"; break;
        case MAYOR: op = ">"; break;
        case MENOR_IGUAL: op = "<="; break;
        case MAYOR_IGUAL: op = ">="; break;
        default: op = "?"; break;
    }

    // Ubicación: preferimos la del lado izquierdo si existe (p.ej. var1)
    int linea = expr.getLine();
    int columna = expr.getColumn();
    if (expr.getIzquierda() != null) {
        linea = expr.getIzquierda().getLine();
        columna = expr.getIzquierda().getColumn();
    }

    // Si alguno ya es ERROR (p.ej. variable no declarada),
    // igualmente reportamos la incompatibilidad del operador.
    if (tIzq == Tipo.ERROR || tDer == Tipo.ERROR) {
        agregarError(
            "No se puede aplicar '" + op + "' a tipos incompatibles (" + tIzq + " y " + tDer + ")",
            linea,
            columna
        );
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    boolean compatible = false;
    switch (expr.getOperador()) {
        case IGUAL:
        case DIFERENTE:
            // Igualdad/desigualdad: permitir mismos tipos o ambos numéricos
            compatible = (tIzq == tDer) || (tIzq.isNumeric() && tDer.isNumeric());
            break;
        case MENOR:
        case MAYOR:
        case MENOR_IGUAL:
        case MAYOR_IGUAL:
            // Orden: solo numéricos
            compatible = tIzq.isNumeric() && tDer.isNumeric();
            break;
    }

    if (!compatible) {
        agregarError(
            "No se puede aplicar '" + op + "' a tipos incompatibles (" + tIzq + " y " + tDer + ")",
            linea,
            columna
        );
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    return new ResultadoExpresion(Tipo.BOOL, null);
}


    @Override
    public ResultadoExpresion visit(ExpresionLogica expr) {
        ResultadoExpresion izq = expr.getIzquierda() != null ? expr.getIzquierda().accept(this) : null;
        ResultadoExpresion der = expr.getDerecha().accept(this);
        if (izq != null && izq.tieneError()) return izq;
        if (der.tieneError()) return der;
        return new ResultadoExpresion(Tipo.BOOL, null);
    }

    @Override
    public ResultadoExpresion visit(ExpresionCasteo expr) {
        ResultadoExpresion exp = expr.getExpresion().accept(this);
        if (exp.tieneError()) return exp;

        Tipo tipoOrigen = exp.getTipo();
        Tipo tipoDestino = expr.getTipoDestino();

        boolean casteoValido = false;
        if (tipoOrigen == Tipo.INT && tipoDestino == Tipo.DOUBLE) casteoValido = true;
        else if (tipoOrigen == Tipo.DOUBLE && tipoDestino == Tipo.INT) casteoValido = true;
        else if (tipoOrigen == Tipo.INT && tipoDestino == Tipo.STRING) casteoValido = true;
        else if (tipoOrigen == Tipo.INT && tipoDestino == Tipo.CHAR) casteoValido = true;
        else if (tipoOrigen == Tipo.DOUBLE && tipoDestino == Tipo.STRING) casteoValido = true;
        else if (tipoOrigen == Tipo.CHAR && tipoDestino == Tipo.INT) casteoValido = true;
        else if (tipoOrigen == Tipo.CHAR && tipoDestino == Tipo.DOUBLE) casteoValido = true;

        if (!casteoValido) {
            agregarError("Casteo no permitido de " + tipoOrigen + " a " + tipoDestino,
                    expr.getLine(), expr.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }
        return new ResultadoExpresion(tipoDestino, null);
    }

    @Override
    public ResultadoExpresion visit(ExpresionAgrupada expr) {
        return expr.getExpresion().accept(this);
    }

    @Override
    public ResultadoExpresion visit(BloqueSentencias stmt) {
        Entorno anterior = entornoActual;
        entornoActual = new Entorno(anterior);
        for (Sentencia s : stmt.getSentencias()) {
            s.accept(this);
        }
        entornoActual = anterior;
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    // 3.5 REEMPLAZO visit(DeclaracionVariable)
    @Override
    public ResultadoExpresion visit(DeclaracionVariable stmt) {
        String id = stmt.getIdentificador();
        Tipo tipo = stmt.getTipo();

        if (entornoActual.buscarLocal(id) != null) {
            agregarError("Variable '" + id + "' ya declarada en este ámbito",
                    stmt.getLine(), stmt.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        CategoriaSimbolo cat = (tipo.isVector() || tipo.isMatrix() || tipo.isList())
                ? CategoriaSimbolo.ESTRUCTURA
                : CategoriaSimbolo.VARIABLE;

        Simbolo simbolo = new Simbolo(
                id,
                tipo,
                cat,
                stmt.getLine(),
                stmt.getColumn()
        );
        simbolo.setAmbito(ambitoActual());
        tablaSimbolos.agregarSimbolo(simbolo);

        if (stmt.getExpresionInicial() != null) {
            Expresion ini = stmt.getExpresionInicial();

            // new List() solo es válido si el tipo declarado es List<T>
            if (ini instanceof NuevaLista) {
                if (!tipo.isList()) {
                    agregarError("Solo se puede usar 'new List()' al declarar variables de tipo List<T>",
                            stmt.getLine(), stmt.getColumn());
                } else {
                    simbolo.setValor(new ArrayList<>());
                    simbolo.setValorPorDefecto(false);
                }
            } else {
                ResultadoExpresion res = ini.accept(this);
                if (res.tieneError()) return res;

                if (!esAsignable(tipo, res.getTipo())) {
                    agregarError(
                            "Tipo incompatible en declaración de '" + id + "'. Esperado: "
                                    + tipo + ", Encontrado: " + res.getTipo(),
                            stmt.getLine(), stmt.getColumn()
                    );
                } else {
                    Object v = res.getValor();
                    if (tipo == Tipo.INT && res.getTipo() == Tipo.BOOL && v instanceof Boolean) {
                        v = ((Boolean) v) ? 1 : 0;
                    }
                    simbolo.setValor(v);
                    simbolo.setValorPorDefecto(false);
                }
            }
        } else {
            // Valor por defecto
            if (tipo.isVector() || tipo.isMatrix() || tipo.isList()) {
                simbolo.setValor(new ArrayList<>());
                simbolo.setValorPorDefecto(true);
            } else {
                switch (tipo) {
                    case INT:
                        simbolo.setValor(0);
                        break;
                    case DOUBLE:
                        simbolo.setValor(0.0);
                        break;
                    case BOOL:
                        simbolo.setValor(true);
                        break;
                    case CHAR:
                        simbolo.setValor('\u0000');
                        break;
                    case STRING:
                        simbolo.setValor("");
                        break;
                    default:
                        break;
                }
                simbolo.setValorPorDefecto(true);
            }
        }

        entornoActual.insertar(simbolo);
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    // 3.6 REEMPLAZO visit(AsignacionVariable)
    @Override
    public ResultadoExpresion visit(AsignacionVariable stmt) {
        String id = stmt.getIdentificador();
        Simbolo simbolo = entornoActual.buscar(id);
        if (simbolo == null) {
            agregarError("Variable '" + id + "' no declarada",
                    stmt.getLine(), stmt.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        Expresion expr = stmt.getExpresion();

        if (expr instanceof NuevaLista) {
            if (!simbolo.getTipo().isList()) {
                agregarError("Solo se puede asignar 'new List()' a variables de tipo List<T>",
                        stmt.getLine(), stmt.getColumn());
            } else {
                simbolo.setValor(new ArrayList<>());
                simbolo.setValorPorDefecto(false);
            }
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        ResultadoExpresion res = expr.accept(this);
        if (res.tieneError()) return res;

        if (!esAsignable(simbolo.getTipo(), res.getTipo())) {
            agregarError("Tipo incompatible en asignación a '" + id + "'. Esperado: "
                            + simbolo.getTipo() + ", Encontrado: " + res.getTipo(),
                    stmt.getLine(), stmt.getColumn());
        } else {
            Object v = res.getValor();
            if (simbolo.getTipo() == Tipo.INT && res.getTipo() == Tipo.BOOL && v instanceof Boolean) {
                v = ((Boolean) v) ? 1 : 0;
            }
            simbolo.setValor(v);
            simbolo.setValorPorDefecto(false);
        }
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    @Override
    public ResultadoExpresion visit(IfSentencia stmt) {
        ResultadoExpresion cond = stmt.getCondicion().accept(this);
        if (cond.tieneError()) return cond;
        if (cond.getTipo() != Tipo.BOOL) {
            agregarError("La condición del IF debe ser booleana",
                    stmt.getLine(), stmt.getColumn());
        }
        stmt.getBloqueIf().accept(this);
        if (stmt.getBloqueElse() != null) {
            stmt.getBloqueElse().accept(this);
        }
        if (stmt.getElseIf() != null) {
            stmt.getElseIf().accept(this);
        }
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    @Override
    public ResultadoExpresion visit(SwitchSentencia stmt) {
        ResultadoExpresion exp = stmt.getExpresion().accept(this);
        if (exp.tieneError()) return exp;

        String etiqueta = "switch(" + exprToString(stmt.getExpresion()) + ")";
        pushAmbito(etiqueta);

        Entorno anterior = entornoActual;
        entornoActual = new Entorno(anterior);
        try {
            if (stmt.getCasos() != null) {
                for (Case caso : stmt.getCasos()) {
                    caso.accept(this);
                }
            }
            if (stmt.getCasoDefault() != null) {
                stmt.getCasoDefault().accept(this);
            }
        } finally {
            entornoActual = anterior;
            popAmbito();
        }
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    @Override
    public ResultadoExpresion visit(Case stmt) {
        ResultadoExpresion val = stmt.getValor().accept(this);
        if (val.tieneError()) return val;
        for (Sentencia s : stmt.getSentencias()) {
            s.accept(this);
        }
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    @Override
    public ResultadoExpresion visit(Default stmt) {
        for (Sentencia s : stmt.getSentencias()) {
            s.accept(this);
        }
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    @Override
    public ResultadoExpresion visit(WhileSentencia stmt) {
        ResultadoExpresion cond = stmt.getCondicion().accept(this);
        if (cond.tieneError()) return cond;
        if (cond.getTipo() != Tipo.BOOL) {
            agregarError("La condición del WHILE debe ser booleana",
                    stmt.getLine(), stmt.getColumn());
        }
        pushAmbito("while(" + exprToString(stmt.getCondicion()) + ")");
        stmt.getBloque().accept(this);
        popAmbito();
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    @Override
    public ResultadoExpresion visit(ForSentencia stmt) {
        Entorno anterior = entornoActual;
        entornoActual = new Entorno(anterior);

        if (stmt.getInicializacion() != null) {
            stmt.getInicializacion().accept(this);
        }
        if (stmt.getCondicion() != null) {
            ResultadoExpresion cond = stmt.getCondicion().accept(this);
            if (cond.tieneError()) {
                entornoActual = anterior;
                return cond;
            }
            if (cond.getTipo() != Tipo.BOOL) {
                agregarError("La condición del FOR debe ser booleana",
                        stmt.getLine(), stmt.getColumn());
            }
        }
        if (stmt.getBloque() != null) {
         stmt.getBloque().accept(this);
        }


        if (stmt.getIncremento() != null) {
            stmt.getIncremento().accept(this);
        }

        entornoActual = anterior;
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    @Override
    public ResultadoExpresion visit(DoWhileSentencia stmt) {
        stmt.getBloque().accept(this);
        ResultadoExpresion cond = stmt.getCondicion().accept(this);
        if (cond.tieneError()) return cond;
        if (cond.getTipo() != Tipo.BOOL) {
            agregarError("La condición del DO-WHILE debe ser booleana",
                    stmt.getLine(), stmt.getColumn());
        }
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    // 3.7 REEMPLAZO visit(LlamadaFuncion) (nativas + funciones del usuario)
    @Override
    public ResultadoExpresion visit(LlamadaFuncion expr) {
        String nombre = (expr.getNombre() == null) ? "" : expr.getNombre();
        String key = nombre.toLowerCase();

        List<Expresion> args = expr.getArgumentos();
        if (args == null) args = Collections.emptyList();

        // ---- Nativas ----
        if (key.equals("round")) {
            if (args.size() != 1) {
                agregarError("La función nativa round(exp) requiere 1 argumento", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            ResultadoExpresion a = args.get(0).accept(this);
            if (a.tieneError()) return a;
            if (!(a.getTipo() == Tipo.INT || a.getTipo() == Tipo.DOUBLE)) {
                agregarError("round(exp) solo acepta int o double", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            return new ResultadoExpresion(Tipo.INT, null);
        }

        if (key.equals("length")) {
            if (args.size() != 1) {
                agregarError("La función nativa length(exp) requiere 1 argumento", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            ResultadoExpresion a = args.get(0).accept(this);
            if (a.tieneError()) return a;
            if (!(a.getTipo() == Tipo.STRING || a.getTipo().isVector() || a.getTipo().isList() || a.getTipo().isMatrix())) {
                agregarError("length(exp) solo aplica a string, vector, matriz o lista", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            return new ResultadoExpresion(Tipo.INT, null);
        }

        if (key.equals("tostring")) {
            if (args.size() != 1) {
                agregarError("La función nativa toString(exp) requiere 1 argumento", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            ResultadoExpresion a = args.get(0).accept(this);
            if (a.tieneError()) return a;
            return new ResultadoExpresion(Tipo.STRING, null);
        }

        if (key.equals("find")) {
            if (args.size() != 2) {
                agregarError("La función nativa find(a,b) requiere 2 argumentos", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            ResultadoExpresion a0 = args.get(0).accept(this);
            ResultadoExpresion a1 = args.get(1).accept(this);
            if (a0.tieneError()) return a0;
            if (a1.tieneError()) return a1;

            if (a0.getTipo() == Tipo.STRING) {
                if (a1.getTipo() != Tipo.STRING) {
                    agregarError("find(string, string) requiere ambos argumentos string", expr.getLine(), expr.getColumn());
                    return new ResultadoExpresion(Tipo.ERROR, null);
                }
                return new ResultadoExpresion(Tipo.INT, null);
            }

            if (a0.getTipo().isList() || a0.getTipo().isVector()) {
                Tipo base = a0.getTipo().baseType();
                if (!esAsignable(base, a1.getTipo()) && !esAsignable(a1.getTipo(), base)) {
                    agregarError("find(estructura, valor) requiere un valor compatible con el tipo interno de la estructura", expr.getLine(), expr.getColumn());
                    return new ResultadoExpresion(Tipo.ERROR, null);
                }
                return new ResultadoExpresion(Tipo.INT, null);
            }

            agregarError("find(...) solo aplica a string, vector o lista", expr.getLine(), expr.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        if (key.equals("start_with") || key.equals("startswith") || key.equals("startwith")) {
            if (args.size() != 2) {
                agregarError("La función nativa START_WITH(a,b) requiere 2 argumentos", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            ResultadoExpresion a0 = args.get(0).accept(this);
            ResultadoExpresion a1 = args.get(1).accept(this);
            if (a0.tieneError()) return a0;
            if (a1.tieneError()) return a1;
            if (a0.getTipo() != Tipo.STRING || a1.getTipo() != Tipo.STRING) {
                agregarError("START_WITH(a,b) requiere argumentos string", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            return new ResultadoExpresion(Tipo.BOOL, null);
        }

        // ---- Funciones/Métodos del usuario ----
        DeclaracionFuncion df = funciones.get(key);
        if (df == null) {
            agregarError("Función/método '" + nombre + "' no declarado", expr.getLine(), expr.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        List<Parametro> params = df.getParametros();
        int nParams = (params == null) ? 0 : params.size();
        if (args.size() != nParams) {
            agregarError("Aridad incorrecta al llamar '" + nombre + "'. Esperado: " + nParams + ", Encontrado: " + args.size(),
                    expr.getLine(), expr.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        for (int i = 0; i < args.size(); i++) {
            ResultadoExpresion a = args.get(i).accept(this);
            if (a.tieneError()) return a;
            Tipo esperado = params.get(i).getTipo();
            if (!esAsignable(esperado, a.getTipo())) {
                agregarError("Tipo incompatible en parámetro " + (i + 1) + " de '" + nombre + "'. Esperado: " + esperado + ", Encontrado: " + a.getTipo(),
                        expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
        }

        return new ResultadoExpresion(df.getTipoRetorno(), null);
    }

    @Override
    public ResultadoExpresion visit(BreakSentencia stmt) {
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    @Override
    public ResultadoExpresion visit(ContinueSentencia stmt) {
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    @Override
    public ResultadoExpresion visit(PrintlnSentencia stmt) {
        ResultadoExpresion res = stmt.getExpresion().accept(this);
        if (res.tieneError()) return res;
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    /* =========================================================
       FASE 2: visits adicionales (sin @Override para evitar choque)
       ========================================================= */

    public ResultadoExpresion visit(DeclaracionFuncion stmt) {
        // Ya se registró en la 1ra pasada (visit Programa). Aquí solo analizamos el cuerpo.
        if (stmt == null) return new ResultadoExpresion(Tipo.ERROR, null);

        boolean prevEnFuncion = this.enFuncion;
        Tipo prevRetorno = this.retornoEsperado;
        boolean prevReturnEncontrado = this.returnEncontrado;

        this.enFuncion = true;
        this.retornoEsperado = stmt.getTipoRetorno();
        this.returnEncontrado = false;

        pushAmbito("func(" + stmt.getNombre() + ")");

        Entorno anterior = entornoActual;
        entornoActual = new Entorno(anterior);

        // Parámetros como variables en el entorno local
        List<Parametro> ps = stmt.getParametros();
        if (ps != null) {
            for (Parametro p : ps) {
                String pid = p.getNombre();
                if (entornoActual.buscarLocal(pid) != null) {
                    agregarError("Parámetro duplicado '" + pid + "' en función '" + stmt.getNombre() + "'",
                            stmt.getLine(), stmt.getColumn());
                    continue;
                }
                Simbolo sp = new Simbolo(pid, p.getTipo(), CategoriaSimbolo.VARIABLE, p.getLine(), p.getColumn());
                sp.setAmbito(ambitoActual());
                tablaSimbolos.agregarSimbolo(sp);
                entornoActual.insertar(sp);
            }
        }

        if (stmt.getCuerpo() != null) {
      stmt.getCuerpo().accept(this);
        }


        // Return obligatorio si NO es void
        if (this.retornoEsperado != Tipo.VOID && !this.returnEncontrado) {
            agregarError("La función '" + stmt.getNombre() + "' debe tener al menos un return",
                    stmt.getLine(), stmt.getColumn());
        }

        entornoActual = anterior;
        popAmbito();

        this.enFuncion = prevEnFuncion;
        this.retornoEsperado = prevRetorno;
        this.returnEncontrado = prevReturnEncontrado;

        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    public ResultadoExpresion visit(StartSentencia stmt) {
        startCount++;

        // Validación mínima: que exista la función/método
        String nombre = (stmt.getNombre() == null) ? "" : stmt.getNombre();
        DeclaracionFuncion df = funciones.get(nombre.toLowerCase());
        if (df == null) {
            agregarError("Función/método '" + nombre + "' no declarado (usado en start)", stmt.getLine(), stmt.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        // Validar args
        List<Expresion> args = stmt.getArgumentos();
        if (args == null) args = Collections.emptyList();

        List<Parametro> params = df.getParametros();
        int nParams = (params == null) ? 0 : params.size();
        if (args.size() != nParams) {
            agregarError("Aridad incorrecta en start '" + nombre + "'. Esperado: " + nParams + ", Encontrado: " + args.size(),
                    stmt.getLine(), stmt.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        for (int i = 0; i < args.size(); i++) {
            ResultadoExpresion a = args.get(i).accept(this);
            if (a.tieneError()) return a;
            Tipo esperado = params.get(i).getTipo();
            if (!esAsignable(esperado, a.getTipo())) {
                agregarError("Tipo incompatible en parámetro " + (i + 1) + " de start '" + nombre + "'. Esperado: " + esperado + ", Encontrado: " + a.getTipo(),
                        stmt.getLine(), stmt.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
        }

        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    public ResultadoExpresion visit(ReturnSentencia stmt) {
        if (!enFuncion) {
            agregarError("La sentencia return solo es válida dentro de una función/método",
                    stmt.getLine(), stmt.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        Expresion e = stmt.getValor();


        if (retornoEsperado == Tipo.VOID) {
            if (e != null) {
                agregarError("Un método void no puede retornar un valor (return exp;)",
                        stmt.getLine(), stmt.getColumn());
            }
            returnEncontrado = true;
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        if (e == null) {
            agregarError("La función debe retornar un valor (return exp;)",
                    stmt.getLine(), stmt.getColumn());
            returnEncontrado = true;
            return new ResultadoExpresion(Tipo.ERROR, null);
        }

        ResultadoExpresion r = e.accept(this);
        if (r.tieneError()) return r;

        if (!esAsignable(retornoEsperado, r.getTipo())) {
            agregarError("Tipo incompatible en return. Esperado: " + retornoEsperado + ", Encontrado: " + r.getTipo(),
                    stmt.getLine(), stmt.getColumn());
        }

        returnEncontrado = true;
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    public ResultadoExpresion visit(SentenciaExpresion stmt) {
        if (stmt.getExpresion() != null) {
            return stmt.getExpresion().accept(this);
        }
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    public ResultadoExpresion visit(NuevaLista expr) {
        // new List<T>() se valida principalmente en declaración/asignación, aquí solo tipamos

        return new ResultadoExpresion(Tipo.ERROR, null);
    }


    public ResultadoExpresion visit(AccesoIndexado expr) {
    ResultadoExpresion obj = expr.getObjetivo().accept(this);
    if (obj.tieneError()) return obj;

    ResultadoExpresion i1 = expr.getIndice1().accept(this);
    if (i1.tieneError()) return i1;
    if (i1.getTipo() != Tipo.INT) {
        agregarError("El índice debe ser int", expr.getLine(), expr.getColumn());
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    Tipo to = obj.getTipo();

    // 2D: matriz[i][j] => tipo base
    if (expr.getIndice2() != null) {
        ResultadoExpresion i2 = expr.getIndice2().accept(this);
        if (i2.tieneError()) return i2;
        if (i2.getTipo() != Tipo.INT) {
            agregarError("El segundo índice debe ser int", expr.getLine(), expr.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }
        if (!to.isMatrix()) {
            agregarError("Acceso [i][j] solo es válido en matrices", expr.getLine(), expr.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }
        return new ResultadoExpresion(to.baseType(), null);
    }

    // 1D: vector/lista/matriz
    if (!(to.isVector() || to.isList() || to.isMatrix())) {
        agregarError("Acceso [i] solo es válido en vector, lista o matriz", expr.getLine(), expr.getColumn());
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    // ✅ FIX CLAVE:
    // matriz[i] debe devolver la FILA => vector del tipo base (ej: char[][] -> char[])
    if (to.isMatrix()) {
        return new ResultadoExpresion(Tipo.vectorOf(to.baseType()), null);
    }

    // vector[i] o lista[i] => tipo base
    return new ResultadoExpresion(to.baseType(), null);
}


    public ResultadoExpresion visit(LiteralVector expr) {
        // Tipado mínimo: vector de ERROR si no se puede inferir
        List<Expresion> vals = expr.getElementos();

        if (vals == null || vals.isEmpty()) {
            return new ResultadoExpresion(Tipo.vectorOf(Tipo.ERROR), null);
        }
        Tipo t = null;
        for (Expresion e : vals) {
            ResultadoExpresion r = e.accept(this);
            if (r.tieneError()) return r;
            if (t == null) t = r.getTipo();
            else if (!esAsignable(t, r.getTipo()) && !esAsignable(r.getTipo(), t)) {
                agregarError("Vector literal con tipos incompatibles", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.vectorOf(Tipo.ERROR), null);
            } else {
                // unificación simple int+double => double
                if ((t == Tipo.INT && r.getTipo() == Tipo.DOUBLE) || (t == Tipo.DOUBLE && r.getTipo() == Tipo.INT)) {
                    t = Tipo.DOUBLE;
                }
            }
        }
        if (t == null) t = Tipo.ERROR;
        return new ResultadoExpresion(Tipo.vectorOf(t), null);
    }

    public ResultadoExpresion visit(LiteralMatriz expr) {
        List<LiteralVector> filas = expr.getFilas();
        if (filas == null || filas.isEmpty()) {
            return new ResultadoExpresion(Tipo.matrixOf(Tipo.ERROR), null);
        }
        Tipo base = null;
        for (LiteralVector fila : filas) {
            ResultadoExpresion rf = fila.accept(this);
            if (rf.tieneError()) return rf;
            Tipo tf = rf.getTipo();
            if (tf == null || !tf.isVector()) {
                agregarError("Cada fila de la matriz debe ser un vector", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.matrixOf(Tipo.ERROR), null);
            }
            Tipo b = tf.baseType();
            if (base == null) base = b;
            else if (!esAsignable(base, b) && !esAsignable(b, base)) {
                agregarError("Matriz literal con filas de tipo incompatible", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.matrixOf(Tipo.ERROR), null);
            } else {
                if ((base == Tipo.INT && b == Tipo.DOUBLE) || (base == Tipo.DOUBLE && b == Tipo.INT)) {
                    base = Tipo.DOUBLE;
                }
            }
        }
        if (base == null) base = Tipo.ERROR;
        return new ResultadoExpresion(Tipo.matrixOf(base), null);
    }

    public ResultadoExpresion visit(LlamadaMiembro expr) {
        // Validación mínima para no romper: tipa nativas de miembro comunes.
        ResultadoExpresion obj = expr.getObjetivo().accept(this);
        if (obj.tieneError()) return obj;

        String m = (expr.getNombre() == null) ? "" : expr.getNombre().toLowerCase();
        List<Expresion> args = expr.getArgumentos();
        if (args == null) args = Collections.emptyList();

        Tipo to = obj.getTipo();

        if (m.equals("append")) {
            if (!(to.isList() || to.isVector())) {
                agregarError("append(...) solo es válido sobre listas o vectores", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            if (args.size() != 1) {
                agregarError("append(valor) requiere 1 argumento", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            ResultadoExpresion a = args.get(0).accept(this);
            if (a.tieneError()) return a;
            Tipo base = to.baseType();
            if (!esAsignable(base, a.getTipo())) {
                agregarError("append(valor) requiere un valor compatible con " + base, expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            return new ResultadoExpresion(to.baseType(), null);
        }

        if (m.equals("remove")) {
            if (!(to.isList() || to.isVector())) {
                agregarError("remove(...) solo es válido sobre listas o vectores", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            if (args.size() != 1) {
                agregarError("remove(indice) requiere 1 argumento", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            ResultadoExpresion a = args.get(0).accept(this);
            if (a.tieneError()) return a;
            if (a.getTipo() != Tipo.INT) {
                agregarError("remove(indice) requiere indice int", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
             return new ResultadoExpresion(to.baseType(), null);
        }

                if (m.equals("find")) {
            if (!(to.isList() || to.isVector() || to == Tipo.STRING)) {
                agregarError("find(valor) solo es válido sobre string, listas o vectores", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            if (args.size() != 1) {
                agregarError("obj.find(valor) requiere 1 argumento", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            ResultadoExpresion a = args.get(0).accept(this);
            if (a.tieneError()) return a;

            return new ResultadoExpresion(Tipo.BOOL, null);
        }


        if (m.equals("start_with") || m.equals("startwith") || m.equals("startswith")) {
            if (to != Tipo.STRING) {
                agregarError("START_WITH(...) solo es válido sobre string", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            if (args.size() != 1) {
                agregarError("obj.START_WITH(prefijo) requiere 1 argumento", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            ResultadoExpresion a = args.get(0).accept(this);
            if (a.tieneError()) return a;
            if (a.getTipo() != Tipo.STRING) {
                agregarError("START_WITH(prefijo) requiere string", expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            return new ResultadoExpresion(Tipo.BOOL, null);
        }

        agregarError("Método no soportado: '" + expr.getNombre() + "'", expr.getLine(), expr.getColumn());

        return new ResultadoExpresion(Tipo.ERROR, null);
    }

   public ResultadoExpresion visit(AsignacionIndexada stmt) {
    // En tu AST: stmt.getAcceso() devuelve AccesoIndexado
    AccesoIndexado acc = stmt.getAcceso();

    ResultadoExpresion obj = acc.getObjetivo().accept(this);
    if (obj.tieneError()) return obj;

    ResultadoExpresion i1 = acc.getIndice1().accept(this);
    if (i1.tieneError()) return i1;
    if (i1.getTipo() != Tipo.INT) {
        agregarError("El índice debe ser int", stmt.getLine(), stmt.getColumn());
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    if (acc.getIndice2() != null) {
        ResultadoExpresion i2 = acc.getIndice2().accept(this);
        if (i2.tieneError()) return i2;
        if (i2.getTipo() != Tipo.INT) {
            agregarError("El segundo índice debe ser int", stmt.getLine(), stmt.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }
    }

    ResultadoExpresion val = stmt.getValor().accept(this);
    if (val.tieneError()) return val;

    Tipo to = obj.getTipo();
    if (!(to.isVector() || to.isList() || to.isMatrix())) {
        agregarError("Asignación indexada solo es válida sobre vector, lista o matriz",
                stmt.getLine(), stmt.getColumn());
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    if (acc.es2D() && !to.isMatrix()) {
        agregarError("Asignación [i][j] solo es válida sobre matrices",
                stmt.getLine(), stmt.getColumn());
        return new ResultadoExpresion(Tipo.ERROR, null);
    }

    Tipo base = to.baseType();
    if (!esAsignable(base, val.getTipo())) {
        agregarError("Tipo incompatible en asignación indexada. Esperado: " + base + ", Encontrado: " + val.getTipo(),
                stmt.getLine(), stmt.getColumn());
    }

    return new ResultadoExpresion(Tipo.ERROR, null);
}


    public ResultadoExpresion visit(IncDecSentencia stmt) {
        ResultadoExpresion lv = stmt.getObjetivo().accept(this);

        if (lv.tieneError()) return lv;

        if (!(lv.getTipo() == Tipo.INT || lv.getTipo() == Tipo.DOUBLE || lv.getTipo() == Tipo.CHAR)) {
            agregarError("++/-- solo se permite sobre tipos numéricos (int/double/char)",
                    stmt.getLine(), stmt.getColumn());
        }

        return new ResultadoExpresion(Tipo.ERROR, null);
    }
}
