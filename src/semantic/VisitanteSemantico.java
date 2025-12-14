package semantic;

import ast.*;
import ast.expresiones.*;
import ast.sentencias.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;
import ast.expresiones.LlamadaFuncion;

public class VisitanteSemantico implements Visitor<ResultadoExpresion> {
    
    private Entorno entornoActual;
    private final List<SemanticError> errores;
    private final TablaSimbolos tablaSimbolos;
    private final Deque<String> ambitoStack;
    
    public VisitanteSemantico() {
        this.entornoActual = new Entorno(null);
        this.errores = new ArrayList<>();
        this.tablaSimbolos = new TablaSimbolos();
        this.ambitoStack = new ArrayDeque<>();
        this.ambitoStack.push("Global");
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

    @Override
    public ResultadoExpresion visit(Programa programa) {
        for (Sentencia stmt : programa.getSentencias()) {
            stmt.accept(this);
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
        if (expr.getIzquierda() == null &&
            expr.getOperador() == OperadorAritmetico.NEGACION_UNARIA) {
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
        if (expr.getOperador() == OperadorAritmetico.SUMA &&
            (tIzq == Tipo.STRING || tDer == Tipo.STRING)) {
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
        if (izq.tieneError()) return izq;
        if (der.tieneError()) return der;
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
    
    @Override
    public ResultadoExpresion visit(DeclaracionVariable stmt) {
        String id = stmt.getIdentificador();
        Tipo tipo = stmt.getTipo();
        if (entornoActual.buscarLocal(id) != null) {
            agregarError("Variable '" + id + "' ya declarada en este ámbito",
                    stmt.getLine(), stmt.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }
        Simbolo simbolo = new Simbolo(
                id,
                tipo,
                CategoriaSimbolo.VARIABLE,
                stmt.getLine(),
                stmt.getColumn()
        );
        simbolo.setAmbito(ambitoActual());
        tablaSimbolos.agregarSimbolo(simbolo);
        if (stmt.getExpresionInicial() != null) {
            ResultadoExpresion res = stmt.getExpresionInicial().accept(this);
            if (res.tieneError()) {
                return res;
            }
            if (res.getTipo() != tipo) {
                agregarError(
                        "Tipo incompatible en declaración de '" + id + "'. Esperado: "
                                + tipo + ", Encontrado: " + res.getTipo(),
                        stmt.getLine(), stmt.getColumn()
                );
            } else {
                simbolo.setValor(res.getValor());
                simbolo.setValorPorDefecto(false);
            }
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
        entornoActual.insertar(simbolo);
        return new ResultadoExpresion(Tipo.ERROR, null);
    }
    
    @Override
    public ResultadoExpresion visit(AsignacionVariable stmt) {
        String id = stmt.getIdentificador();
        Simbolo simbolo = entornoActual.buscar(id);
        if (simbolo == null) {
            agregarError("Variable '" + id + "' no declarada",
                    stmt.getLine(), stmt.getColumn());
            return new ResultadoExpresion(Tipo.ERROR, null);
        }
        ResultadoExpresion res = stmt.getExpresion().accept(this);
        if (res.tieneError()) {
            return res;
        }
        if (simbolo.getTipo() != res.getTipo()) {
            agregarError(
                    "Tipo incompatible en asignación a '" + id + "'. Esperado: "
                            + simbolo.getTipo() + ", Encontrado: " + res.getTipo(),
                    stmt.getLine(), stmt.getColumn()
            );
        } else {
            simbolo.setValor(res.getValor());
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
    
    @Override
    public ResultadoExpresion visit(LlamadaFuncion expr) {
        String nombre = expr.getNombre();
        List<Expresion> args = expr.getArgumentos();
        if (args == null) args = Collections.emptyList();
        if (nombre != null && nombre.equalsIgnoreCase("length")) {
            if (args.size() != 1) {
                agregarError("length(...) espera exactamente 1 parámetro",
                        expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            ResultadoExpresion arg0 = args.get(0).accept(this);
            if (arg0.getTipo() != Tipo.STRING) {
                agregarError("length(...) espera string",
                        expr.getLine(), expr.getColumn());
                return new ResultadoExpresion(Tipo.ERROR, null);
            }
            return new ResultadoExpresion(Tipo.INT, null);
        }
        agregarError("Función no soportada: '" + nombre + "'",
                expr.getLine(), expr.getColumn());
        return new ResultadoExpresion(Tipo.ERROR, null);
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
}