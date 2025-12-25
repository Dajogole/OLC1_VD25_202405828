package interpreter;

import ast.*;
import ast.expresiones.*;
import ast.sentencias.*;
import semantic.Simbolo;
import semantic.Tipo;
import semantic.CategoriaSimbolo;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class VisitanteEvaluacion implements Visitor<Valor> {

    private ContextoEjecucion contexto;

    public VisitanteEvaluacion(ContextoEjecucion contexto) {
        this.contexto = contexto;
    }

    private boolean esNumerico(Valor v) {
        if (v == null) return false;
        String t = v.getTipo();
        return t.equals("int") || t.equals("double") || t.equals("char");
    }

    private double toDouble(Valor v) {
        if (v == null || v.getValor() == null) return 0.0;
        switch (v.getTipo()) {
            case "int":
                return ((Integer) v.getValor()).doubleValue();
            case "double":
                return (Double) v.getValor();
            case "char":
                return (double) ((Character) v.getValor());
            default:
                return 0.0;
        }
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private String tipoDe(Tipo t) {
        return (t == null) ? "error" : t.toString();
    }

    private boolean esAsignableRuntime(Tipo destino, Valor origen) {
        if (destino == null || origen == null) return false;
        String tDest = destino.toString();
        String tOri = origen.getTipo();

        if (tDest.equals(tOri)) return true;
        if (tDest.equals("int") && tOri.equals("bool")) return true;
        if (tDest.equals("double") && (tOri.equals("int") || tOri.equals("char"))) return true;
        return false;
    }

    private Valor convertirSiAplica(Tipo destino, Valor v) {
        if (destino == null || v == null) return v;
        if (destino.toString().equals("int") && v.getTipo().equals("bool")) {
            boolean b = (Boolean) v.getValor();
            return new Valor(b ? 1 : 0, "int");
        }
        if (destino.toString().equals("double") && (v.getTipo().equals("int") || v.getTipo().equals("char"))) {
            return new Valor(toDouble(v), "double");
        }
        return v;
    }

    private String stringify(Object o) {
        if (o == null) return "null";
        if (o instanceof List) {
            List<?> l = (List<?>) o;
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < l.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(stringify(l.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return String.valueOf(o);
    }

    private Object call0(Object target, String method) {
        try {
            return target.getClass().getMethod(method).invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Object call0Any(Object target, String... methods) {
        if (target == null) return null;
        for (String m : methods) {
            Object r = call0(target, m);
            if (r != null) return r;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Expresion> getExprListAny(Object target, String... methods) {
        Object r = call0Any(target, methods);
        if (r instanceof List) return (List<Expresion>) r;
        return null;
    }

    private Expresion getExprAny(Object target, String... methods) {
        Object r = call0Any(target, methods);
        return (r instanceof Expresion) ? (Expresion) r : null;
    }

    private String getStringAny(Object target, String... methods) {
        Object r = call0Any(target, methods);
        return (r instanceof String) ? (String) r : null;
    }

    private Boolean getBoolAny(Object target, String... methods) {
        Object r = call0Any(target, methods);
        return (r instanceof Boolean) ? (Boolean) r : null;
    }

    @Override
    public Valor visit(Programa programa) {
        for (Sentencia stmt : programa.getSentencias()) {
            if (stmt instanceof DeclaracionFuncion) {
                contexto.registrarFuncion((DeclaracionFuncion) stmt);
            }
        }
        StartSentencia startStmt = null;
        for (Sentencia stmt : programa.getSentencias()) {
            if (stmt instanceof DeclaracionFuncion) continue;
            if (stmt instanceof StartSentencia) {
                if (startStmt == null) startStmt = (StartSentencia) stmt;
                continue;
            }
            if (stmt instanceof DeclaracionVariable) {
                stmt.accept(this);
            } else {
                if (startStmt == null) {
                    stmt.accept(this);
                }
            }
            if (contexto.hayReturn()) {
                contexto.tomarReturnYLimpiar();
            }
            if (contexto.debeBreakCiclo() || contexto.debeContinue() || contexto.debeBreakSwitch()) {
                contexto.agregarError("Break/Continue fuera de ciclo/switch");
                contexto.setDebeBreakCiclo(false);
                contexto.setDebeBreakSwitch(false);
                contexto.setDebeContinue(false);
                break;
            }
        }
        if (startStmt != null) {
            startStmt.accept(this);
            if (contexto.hayReturn()) {
                contexto.tomarReturnYLimpiar();
            }
        } else {
            contexto.agregarError("No se encontró sentencia START");
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(LiteralEntero expr) {
        return new Valor(expr.getValor(), "int");
    }

    @Override
    public Valor visit(LiteralDouble expr) {
        return new Valor(expr.getValor(), "double");
    }

    @Override
    public Valor visit(LiteralBooleano expr) {
        return new Valor(expr.getValor(), "bool");
    }

    @Override
    public Valor visit(LiteralChar expr) {
        return new Valor(expr.getValor(), "char");
    }

    @Override
    public Valor visit(LiteralString expr) {
        return new Valor(expr.getValor(), "string");
    }

    @Override
    public Valor visit(Identificador expr) {
        String id = expr.getNombre();
        Simbolo simbolo = contexto.buscarSimbolo(id);
        if (simbolo == null) {
            contexto.agregarError("Variable '" + id + "' no declarada (ejecución)");
            return new Valor(null, "error");
        }
        return new Valor(simbolo.getValor(), simbolo.getTipo().toString());
    }

    @Override
    public Valor visit(ExpresionAritmetica expr) {
        Valor izq = expr.getIzquierda() != null ? expr.getIzquierda().accept(this) : null;
        Valor der = expr.getDerecha().accept(this);
        if (izq == null) {
            if (expr.getOperador() == OperadorAritmetico.NEGACION_UNARIA) {
                if (der.getTipo().equals("int")) {
                    int valor = (Integer) der.getValor();
                    return new Valor(-valor, "int");
                } else if (der.getTipo().equals("double")) {
                    double valor = (Double) der.getValor();
                    return new Valor(-valor, "double");
                } else if (der.getTipo().equals("char")) {
                    char c = (Character) der.getValor();
                    return new Valor((int) -c, "int");
                }
            }
        } else {
            switch (expr.getOperador()) {
                case SUMA:
                    return sumar(izq, der);
                case RESTA:
                    return restar(izq, der);
                case MULTIPLICACION:
                    return multiplicar(izq, der);
                case DIVISION:
                    return dividir(izq, der);
                case MODULO:
                    return modulo(izq, der);
                case POTENCIA:
                    return potencia(izq, der);
            }
        }
        return new Valor(null, "error");
    }

    private Valor sumar(Valor izq, Valor der) {
        if (izq.getTipo().equals("string") || der.getTipo().equals("string")) {
            String s1 = izq.getValor() != null ? izq.getValor().toString() : "null";
            String s2 = der.getValor() != null ? der.getValor().toString() : "null";
            return new Valor(s1 + s2, "string");
        }
        if (esNumerico(izq) && esNumerico(der)) {
            if (izq.getTipo().equals("double") || der.getTipo().equals("double")) {
                double a = toDouble(izq);
                double b = toDouble(der);
                return new Valor(a + b, "double");
            } else {
                int a = toInt(izq);
                int b = toInt(der);
                return new Valor(a + b, "int");
            }
        }
        contexto.agregarError("No se puede sumar tipos '" + izq.getTipo() + "' y '" + der.getTipo() + "'");
        return new Valor(null, "error");
    }

    private Valor restar(Valor izq, Valor der) {
        if (esNumerico(izq) && esNumerico(der)) {
            if (izq.getTipo().equals("double") || der.getTipo().equals("double")) {
                double a = toDouble(izq);
                double b = toDouble(der);
                return new Valor(a - b, "double");
            } else {
                int a = toInt(izq);
                int b = toInt(der);
                return new Valor(a - b, "int");
            }
        }
        contexto.agregarError("No se puede restar tipos '" + izq.getTipo() + "' y '" + der.getTipo() + "'");
        return new Valor(null, "error");
    }

    private Valor multiplicar(Valor izq, Valor der) {
        if (esNumerico(izq) && esNumerico(der)) {
            if (izq.getTipo().equals("double") || der.getTipo().equals("double")) {
                double a = toDouble(izq);
                double b = toDouble(der);
                return new Valor(a * b, "double");
            } else {
                int a = toInt(izq);
                int b = toInt(der);
                return new Valor(a * b, "int");
            }
        }
        contexto.agregarError("No se puede multiplicar tipos '" + izq.getTipo() + "' y '" + der.getTipo() + "'");
        return new Valor(null, "error");
    }

    private Valor dividir(Valor izq, Valor der) {
        if (esNumerico(izq) && esNumerico(der)) {
            double b = toDouble(der);
            if (Math.abs(b) < 1e-9) {
                contexto.agregarError("División entre cero");
                return new Valor(null, "error");
            }
            double a = toDouble(izq);
            return new Valor(a / b, "double");
        }
        contexto.agregarError("No se puede dividir tipos '" + izq.getTipo() + "' y '" + der.getTipo() + "'");
        return new Valor(null, "error");
    }

    private Valor modulo(Valor izq, Valor der) {
        if (esEntero(izq) && esEntero(der)) {
            int b = toInt(der);
            if (b == 0) {
                contexto.agregarError("Módulo por cero");
                return new Valor(null, "error");
            }
            int a = toInt(izq);
            return new Valor(a % b, "int");
        }
        contexto.agregarError("El operador % solo se permite entre enteros (int/char)");
        return new Valor(null, "error");
    }

    private Valor potencia(Valor izq, Valor der) {
        if (esNumerico(izq) && esNumerico(der)) {
            if (izq.getTipo().equals("int") && der.getTipo().equals("int")) {
                int a = toInt(izq);
                int b = toInt(der);
                return new Valor((int) Math.pow(a, b), "int");
            } else {
                double a = toDouble(izq);
                double b = toDouble(der);
                return new Valor(Math.pow(a, b), "double");
            }
        }
        contexto.agregarError("No se puede aplicar potencia a tipos '" + izq.getTipo() + "' y '" + der.getTipo() + "'");
        return new Valor(null, "error");
    }

    @Override
    public Valor visit(ExpresionRelacional expr) {
        Valor izq = expr.getIzquierda().accept(this);
        Valor der = expr.getDerecha().accept(this);
        String tIzq = izq.getTipo();
        String tDer = der.getTipo();
        boolean izqNum = tIzq.equals("int") || tIzq.equals("double") || tIzq.equals("char");
        boolean derNum = tDer.equals("int") || tDer.equals("double") || tDer.equals("char");
        boolean resultado = false;
        if (izqNum && derNum) {
            double a = toDouble(izq);
            double b = toDouble(der);
            switch (expr.getOperador()) {
                case MENOR: resultado = a < b; break;
                case MAYOR: resultado = a > b; break;
                case MENOR_IGUAL: resultado = a <= b; break;
                case MAYOR_IGUAL: resultado = a >= b; break;
                case IGUAL: resultado = Math.abs(a - b) < 1e-9; break;
                case DIFERENTE: resultado = Math.abs(a - b) >= 1e-9; break;
            }
            return new Valor(resultado, "bool");
        }
        if (tIzq.equals("bool") && tDer.equals("bool")) {
            boolean a = (Boolean) izq.getValor();
            boolean b = (Boolean) der.getValor();
            switch (expr.getOperador()) {
                case IGUAL: resultado = (a == b); break;
                case DIFERENTE: resultado = (a != b); break;
                default:
                    contexto.agregarError("Operador relacional no válido para booleanos");
                    break;
            }
            return new Valor(resultado, "bool");
        }
        if (tIzq.equals("string") && tDer.equals("string")) {
            String a = (String) izq.getValor();
            String b = (String) der.getValor();
            switch (expr.getOperador()) {
                case IGUAL: resultado = a.equals(b); break;
                case DIFERENTE: resultado = !a.equals(b); break;
                default:
                    contexto.agregarError("Operador relacional no válido para strings");
                    break;
            }
            return new Valor(resultado, "bool");
        }
        contexto.agregarError("Tipos incompatibles en expresión relacional");
        return new Valor(false, "bool");
    }

    @Override
    public Valor visit(ExpresionLogica expr) {
        Valor izq = expr.getIzquierda() != null ? expr.getIzquierda().accept(this) : null;
        Valor der = expr.getDerecha().accept(this);
        if (izq == null) {
            if (der.getTipo().equals("bool")) {
                boolean valor = (Boolean) der.getValor();
                return new Valor(!valor, "bool");
            }
        } else {
            if (izq.getTipo().equals("bool") && der.getTipo().equals("bool")) {
                boolean a = (Boolean) izq.getValor();
                boolean b = (Boolean) der.getValor();
                switch (expr.getOperador()) {
                    case AND: return new Valor(a && b, "bool");
                    case OR:  return new Valor(a || b, "bool");
                    case XOR: return new Valor(a ^ b, "bool");
                }
            }
        }
        return new Valor(false, "bool");
    }

    @Override
    public Valor visit(ExpresionCasteo expr) {
        Valor valor = expr.getExpresion().accept(this);
        switch (expr.getTipoDestino()) {
            case INT:
                if (valor.getTipo().equals("double")) {
                    double d = (Double) valor.getValor();
                    return new Valor((int) d, "int");
                } else if (valor.getTipo().equals("char")) {
                    char c = (Character) valor.getValor();
                    return new Valor((int) c, "int");
                }
                break;
            case DOUBLE:
                if (valor.getTipo().equals("int")) {
                    int i = (Integer) valor.getValor();
                    return new Valor((double) i, "double");
                } else if (valor.getTipo().equals("char")) {
                    char c = (Character) valor.getValor();
                    return new Valor((double) c, "double");
                }
                break;
            case CHAR:
                if (valor.getTipo().equals("int")) {
                    int i = (Integer) valor.getValor();
                    return new Valor((char) i, "char");
                }
                break;
            case STRING:
                return new Valor(valor.getValor() == null ? "null" : valor.getValor().toString(), "string");
        }
        return new Valor(null, "error");
    }

    @Override
    public Valor visit(ExpresionAgrupada expr) {
        return expr.getExpresion().accept(this);
    }

    @Override
    public Valor visit(BloqueSentencias stmt) {
        contexto.pushBloque();
        try {
            for (Sentencia s : stmt.getSentencias()) {
                s.accept(this);
                if (contexto.debeBreakCiclo() || contexto.debeContinue() || contexto.debeBreakSwitch() || contexto.hayReturn()) {
                    break;
                }
            }
        } finally {
            contexto.popBloque();
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(DeclaracionVariable stmt) {
        String id = stmt.getIdentificador();
        Tipo tipo = stmt.getTipo();
        Simbolo simbolo = contexto.declararVariable(id, tipo, stmt.getLine(), stmt.getColumn());
        if (simbolo == null) return new Valor(null, "void");
        if (stmt.getExpresionInicial() != null) {
            Expresion ini = stmt.getExpresionInicial();
            if (ini instanceof NuevaLista) {
                if (!tipo.isList()) {
                    contexto.agregarError("new List() solo es válido para variables List<T> (ejecución)");
                } else {
                    contexto.setValor(simbolo, new ArrayList<>());
                }
                return new Valor(null, "void");
            }
            Valor v = ini.accept(this);
            v = convertirSiAplica(tipo, v);
            if (!esAsignableRuntime(tipo, v)) {
                contexto.agregarError("Tipo incompatible en declaración de '" + id + "' en ejecución. Esperado: "
                        + tipo + ", recibido: " + v.getTipo());
            } else {
                contexto.setValor(simbolo, v.getValor());
            }
            return new Valor(null, "void");
        }
        if (tipo != null && (tipo.isList() || tipo.isVector())) {
            contexto.setValor(simbolo, new ArrayList<>());
        } else if (tipo != null && tipo.isMatrix()) {
            contexto.setValor(simbolo, new ArrayList<List<Object>>());
        } else if (tipo != null) {
            switch (tipo) {
                case INT -> contexto.setValor(simbolo, 0);
                case DOUBLE -> contexto.setValor(simbolo, 0.0);
                case BOOL -> contexto.setValor(simbolo, false);
                case CHAR -> contexto.setValor(simbolo, '\u0000');
                case STRING -> contexto.setValor(simbolo, "");
                default -> contexto.setValor(simbolo, null);
            }
        } else {
            contexto.setValor(simbolo, null);
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(AsignacionVariable stmt) {
        String id = stmt.getIdentificador();
        Simbolo simbolo = contexto.buscarSimbolo(id);
        if (simbolo == null) {
            contexto.agregarError("Variable '" + id + "' no declarada (ejecución)");
            return new Valor(null, "void");
        }
        if (stmt.getExpresion() instanceof NuevaLista) {
            if (!simbolo.getTipo().isList()) {
                contexto.agregarError("new List() solo se puede asignar a List<T> (ejecución)");
            } else {
                contexto.setValor(simbolo, new ArrayList<>());
            }
            return new Valor(null, "void");
        }
        Valor v = stmt.getExpresion().accept(this);
        v = convertirSiAplica(simbolo.getTipo(), v);
        if (!esAsignableRuntime(simbolo.getTipo(), v)) {
            contexto.agregarError("Tipo incompatible al asignar a '" + id + "' en ejecución. Esperado: "
                    + simbolo.getTipo() + ", recibido: " + v.getTipo());
            return new Valor(null, "void");
        }
        contexto.setValor(simbolo, v.getValor());
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(IfSentencia stmt) {
        Valor cond = stmt.getCondicion().accept(this);
        if (cond.getTipo().equals("bool") && cond.getValor() instanceof Boolean && (Boolean) cond.getValor()) {
            stmt.getBloqueIf().accept(this);
        } else {
            if (stmt.getBloqueElse() != null) {
                stmt.getBloqueElse().accept(this);
            } else if (stmt.getElseIf() != null) {
                stmt.getElseIf().accept(this);
            }
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(SwitchSentencia stmt) {
        contexto.entrarSwitch();
        contexto.pushAmbito("switch(" + stmt.getExpresion().toString() + ")");
        contexto.pushBloque();
        try {
            Valor exp = stmt.getExpresion().accept(this);
            boolean ejecutar = false;
            if (stmt.getCasos() != null) {
                for (Case caso : stmt.getCasos()) {
                    Valor casoValor = caso.getValor().accept(this);
                    if (!ejecutar && sonIguales(exp, casoValor)) {
                        ejecutar = true;
                    }
                    if (ejecutar) {
                        caso.accept(this);
                        if (contexto.debeBreakSwitch()) {
                            contexto.setDebeBreakSwitch(false);
                            return new Valor(null, "void");
                        }
                        if (contexto.debeContinue() || contexto.debeBreakCiclo() || contexto.hayReturn()) {
                            return new Valor(null, "void");
                        }
                    }
                }
            }
            if (stmt.getCasoDefault() != null) {
                if (!ejecutar) {
                    stmt.getCasoDefault().accept(this);
                } else {
                    stmt.getCasoDefault().accept(this);
                }
                if (contexto.debeBreakSwitch()) contexto.setDebeBreakSwitch(false);
            }
            return new Valor(null, "void");
        } finally {
            contexto.popBloque();
            contexto.popAmbito();
            contexto.salirSwitch();
        }
    }

    private boolean sonIguales(Valor a, Valor b) {
        if (a == null || b == null) return false;
        if (!a.getTipo().equals(b.getTipo())) return false;
        if (a.getValor() == null) return b.getValor() == null;
        return a.getValor().equals(b.getValor());
    }

    @Override
    public Valor visit(Case stmt) {
        for (Sentencia s : stmt.getSentencias()) {
            s.accept(this);
            if (contexto.debeBreakSwitch() || contexto.debeBreakCiclo() || contexto.debeContinue() || contexto.hayReturn()) {
                break;
            }
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(Default stmt) {
        for (Sentencia s : stmt.getSentencias()) {
            s.accept(this);
            if (contexto.debeBreakSwitch() || contexto.debeBreakCiclo() || contexto.debeContinue() || contexto.hayReturn()) {
                break;
            }
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(WhileSentencia stmt) {
        contexto.entrarCiclo();
        try {
            while (true) {
                Valor cond = stmt.getCondicion().accept(this);
                if (!cond.getTipo().equals("bool") || !(cond.getValor() instanceof Boolean) || !(Boolean) cond.getValor()) {
                    break;
                }
                stmt.getBloque().accept(this);
                if (contexto.hayReturn()) break;
                if (contexto.debeBreakCiclo()) {
                    contexto.setDebeBreakCiclo(false);
                    break;
                }
                if (contexto.debeContinue()) {
                    contexto.setDebeContinue(false);
                    continue;
                }
            }
        } finally {
            contexto.salirCiclo();
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(ForSentencia stmt) {
        contexto.entrarCiclo();
        try {
            if (stmt.getInicializacion() != null) {
                stmt.getInicializacion().accept(this);
            }
            while (true) {
                if (stmt.getCondicion() != null) {
                    Valor cond = stmt.getCondicion().accept(this);
                    if (!cond.getTipo().equals("bool") || !(cond.getValor() instanceof Boolean) || !(Boolean) cond.getValor()) {
                        break;
                    }
                }
                if (stmt.getBloque() != null) {
                    stmt.getBloque().accept(this);
                }
                if (contexto.hayReturn()) break;
                if (contexto.debeBreakCiclo()) {
                    contexto.setDebeBreakCiclo(false);
                    break;
                }
                if (contexto.debeContinue()) {
                    contexto.setDebeContinue(false);
                    if (stmt.getIncremento() != null) {
                        stmt.getIncremento().accept(this);
                    }
                    continue;
                }
                if (stmt.getIncremento() != null) {
                    stmt.getIncremento().accept(this);
                }
            }
        } finally {
            contexto.salirCiclo();
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(DoWhileSentencia stmt) {
        contexto.entrarCiclo();
        try {
            do {
                stmt.getBloque().accept(this);
                if (contexto.hayReturn()) break;
                if (contexto.debeBreakCiclo()) {
                    contexto.setDebeBreakCiclo(false);
                    break;
                }
                if (contexto.debeContinue()) {
                    contexto.setDebeContinue(false);
                }
                Valor cond = stmt.getCondicion().accept(this);
                if (!cond.getTipo().equals("bool") || !(cond.getValor() instanceof Boolean) || !(Boolean) cond.getValor()) {
                    break;
                }
            } while (true);
        } finally {
            contexto.salirCiclo();
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(BreakSentencia stmt) {
        if (contexto.isEnSwitch()) {
            contexto.setDebeBreakSwitch(true);
        } else if (contexto.isEnCiclo()) {
            contexto.setDebeBreakCiclo(true);
        } else {
            contexto.agregarError("Break fuera de ciclo/switch");
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(ContinueSentencia stmt) {
        if (contexto.isEnCiclo()) {
            contexto.setDebeContinue(true);
        } else {
            contexto.agregarError("Continue fuera de ciclo");
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(PrintlnSentencia stmt) {
        Valor v = stmt.getExpresion().accept(this);
        Object val = (v != null) ? v.getValor() : null;
        contexto.imprimir(stringify(val));
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(DeclaracionFuncion stmt) {
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(StartSentencia stmt) {
        String nombre = stmt.getNombre();
        DeclaracionFuncion f = contexto.obtenerFuncion(nombre);
        if (f == null) {
            contexto.agregarError("No existe la función/método para start: '" + nombre + "'");
            return new Valor(null, "void");
        }
        List<Expresion> args = stmt.getArgumentos();
        return ejecutarCallable(f, args, true);
    }

    @Override
    public Valor visit(ReturnSentencia stmt) {
        Valor v;
        Expresion exp = getExprAny(stmt, "getValor", "getExpresion");
        if (exp == null) {
            v = new Valor(null, "void");
        } else {
            v = exp.accept(this);
        }
        contexto.activarReturn(v);
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(SentenciaExpresion stmt) {
        if (stmt.getExpresion() != null) stmt.getExpresion().accept(this);
        return new Valor(null, "void");
    }

    private Valor ejecutarCallable(DeclaracionFuncion f, List<Expresion> args, boolean esStart) {
        List<Valor> evalArgs = new ArrayList<>();
        if (args != null) {
            for (Expresion e : args) {
                evalArgs.add(e.accept(this));
            }
        }
        contexto.pushCallFrame(f.getNombre());
        try {
            List<Parametro> params = f.getParametros();
            int nParams = (params == null) ? 0 : params.size();
            int nArgs = evalArgs.size();
            if (nParams != nArgs) {
                contexto.agregarError("Aridad incorrecta al llamar '" + f.getNombre() + "': se esperaban "
                        + nParams + " argumentos y se recibieron " + nArgs);
                return new Valor(null, "error");
            }
            for (int i = 0; i < nParams; i++) {
                Parametro p = params.get(i);
                Valor v = evalArgs.get(i);
                if (!esAsignableRuntime(p.getTipo(), v)) {
                    contexto.agregarError("Parámetro incompatible en llamada a '" + f.getNombre()
                            + "': se esperaba '" + p.getTipo().toString() + "' y se recibió '" + v.getTipo() + "'");
                    return new Valor(null, "error");
                }
                v = convertirSiAplica(p.getTipo(), v);
                Simbolo s = contexto.declararParametro(p.getNombre(), p.getTipo(), p.getLine(), p.getColumn());
                if (s != null) {
                    contexto.setValor(s, v.getValor());
                }
            }
            f.getCuerpo().accept(this);
            if (contexto.hayReturn()) {
                return contexto.tomarReturnYLimpiar();
            }
            return new Valor(null, "void");
        } finally {
            contexto.popCallFrame();
        }
    }

    @Override
    public Valor visit(LiteralVector expr) {
        List<Expresion> elems = getExprListAny(expr, "getElementos", "getValores");
        List<Object> out = new ArrayList<>();
        String tipoBase = null;
        if (elems != null) {
            for (Expresion e : elems) {
                Valor v = e.accept(this);
                out.add(v.getValor());
                if (tipoBase == null) tipoBase = v.getTipo();
                else if (!tipoBase.equals(v.getTipo())) {
                    if ((tipoBase.equals("int") && v.getTipo().equals("double")) ||
                            (tipoBase.equals("double") && v.getTipo().equals("int"))) {
                        tipoBase = "double";
                    }
                }
            }
        }
        if (tipoBase == null) tipoBase = "int";
        return new Valor(out, tipoBase + "[]");
    }

    @Override
    public Valor visit(LiteralMatriz expr) {
        List<LiteralVector> filas = expr.getFilas();
        List<List<Object>> out = new ArrayList<>();
        String tipoBase = null;
        if (filas != null) {
            for (LiteralVector fila : filas) {
                Valor row = fila.accept(this);
                @SuppressWarnings("unchecked")
                List<Object> rowList = (List<Object>) row.getValor();
                out.add(rowList);
                String t = row.getTipo();
                String base = t.endsWith("[]") ? t.substring(0, t.length() - 2) : t;
                if (tipoBase == null) tipoBase = base;
                else if (!tipoBase.equals(base)) {
                    if ((tipoBase.equals("int") && base.equals("double")) ||
                            (tipoBase.equals("double") && base.equals("int"))) {
                        tipoBase = "double";
                    }
                }
            }
        }
        if (tipoBase == null) tipoBase = "int";
        return new Valor(out, tipoBase + "[][]");
    }

    @Override
    public Valor visit(AccesoIndexado expr) {
        Expresion objetivo = getExprAny(expr, "getObjetivo", "getObjeto");
        Valor objV = (objetivo != null) ? objetivo.accept(this) : new Valor(null, "error");
        Object obj = objV.getValor();
        Expresion i1e = getExprAny(expr, "getIndice1");
        Valor i1v = (i1e != null) ? i1e.accept(this) : new Valor(0, "int");
        int i1 = toInt(i1v);
        Expresion i2e = getExprAny(expr, "getIndice2");
        boolean es2D = (i2e != null);
        if (!es2D) {
            if (!(obj instanceof List)) {
                contexto.agregarError("Acceso indexado 1D solo aplica a vector/lista (ejecución)");
                return new Valor(null, "error");
            }
            List<?> l = (List<?>) obj;
            if (i1 < 0 || i1 >= l.size()) {
                contexto.agregarError("Índice fuera de rango: " + i1);
                return new Valor(null, "error");
            }
            Object val = l.get(i1);
            if (val instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> row = (List<Object>) val;
                String base = "int";
                if (!row.isEmpty()) {
                    Object e0 = row.get(0);
                    if (e0 instanceof Integer) base = "int";
                    else if (e0 instanceof Double) base = "double";
                    else if (e0 instanceof Boolean) base = "bool";
                    else if (e0 instanceof Character) base = "char";
                    else if (e0 instanceof String) base = "string";
                }
                return new Valor(row, base + "[]");
            }
            if (val instanceof Integer) return new Valor(val, "int");
            if (val instanceof Double) return new Valor(val, "double");
            if (val instanceof Boolean) return new Valor(val, "bool");
            if (val instanceof Character) return new Valor(val, "char");
            if (val instanceof String) return new Valor(val, "string");
            return new Valor(val, "unknown");
        }
        Valor i2v = i2e.accept(this);
        int i2 = toInt(i2v);
        if (!(obj instanceof List)) {
            contexto.agregarError("Acceso indexado 2D solo aplica a matriz (ejecución)");
            return new Valor(null, "error");
        }
        List<?> rows = (List<?>) obj;
        if (i1 < 0 || i1 >= rows.size()) {
            contexto.agregarError("Fila fuera de rango: " + i1);
            return new Valor(null, "error");
        }
        Object rowObj = rows.get(i1);
        if (!(rowObj instanceof List)) {
            contexto.agregarError("Estructura de matriz inválida (fila no es lista)");
            return new Valor(null, "error");
        }
        List<?> row = (List<?>) rowObj;
        if (i2 < 0 || i2 >= row.size()) {
            contexto.agregarError("Columna fuera de rango: " + i2);
            return new Valor(null, "error");
        }
        Object val = row.get(i2);
        if (val instanceof Integer) return new Valor(val, "int");
        if (val instanceof Double) return new Valor(val, "double");
        if (val instanceof Boolean) return new Valor(val, "bool");
        if (val instanceof Character) return new Valor(val, "char");
        if (val instanceof String) return new Valor(val, "string");
        return new Valor(val, "unknown");
    }

    @Override
    public Valor visit(AsignacionIndexada stmt) {
        AccesoIndexado acc = (AccesoIndexado) call0Any(stmt, "getAcceso");
        Expresion objetivo;
        Expresion idx1;
        Expresion idx2;
        if (acc != null) {
            objetivo = getExprAny(acc, "getObjetivo", "getObjeto");
            idx1 = getExprAny(acc, "getIndice1");
            idx2 = getExprAny(acc, "getIndice2");
        } else {
            objetivo = getExprAny(stmt, "getObjetivo", "getObjeto");
            idx1 = getExprAny(stmt, "getIndice1");
            idx2 = getExprAny(stmt, "getIndice2");
        }
        Valor objV = (objetivo != null) ? objetivo.accept(this) : new Valor(null, "error");
        Object obj = objV.getValor();
        int i1 = (idx1 != null) ? toInt(idx1.accept(this)) : 0;
        Expresion valExpr = getExprAny(stmt, "getValor");
        Valor val = (valExpr != null) ? valExpr.accept(this) : new Valor(null, "error");
        boolean es2D = (idx2 != null);
        if (!es2D) {
            if (!(obj instanceof List)) {
                contexto.agregarError("Asignación indexada 1D solo aplica a vector/lista (ejecución)");
                return new Valor(null, "void");
            }
            @SuppressWarnings("unchecked")
            List<Object> l = (List<Object>) obj;
            if (i1 < 0 || i1 >= l.size()) {
                contexto.agregarError("Índice fuera de rango en asignación: " + i1);
                return new Valor(null, "void");
            }
            l.set(i1, val.getValor());
            return new Valor(null, "void");
        }
        int i2 = toInt(idx2.accept(this));
        if (!(obj instanceof List)) {
            contexto.agregarError("Asignación indexada 2D solo aplica a matriz (ejecución)");
            return new Valor(null, "void");
        }
        @SuppressWarnings("unchecked")
        List<Object> rows = (List<Object>) obj;
        if (i1 < 0 || i1 >= rows.size()) {
            contexto.agregarError("Fila fuera de rango en asignación: " + i1);
            return new Valor(null, "void");
        }
        Object rowObj = rows.get(i1);
        if (!(rowObj instanceof List)) {
            contexto.agregarError("Estructura de matriz inválida en asignación");
            return new Valor(null, "void");
        }
        @SuppressWarnings("unchecked")
        List<Object> row = (List<Object>) rowObj;
        if (i2 < 0 || i2 >= row.size()) {
            contexto.agregarError("Columna fuera de rango en asignación: " + i2);
            return new Valor(null, "void");
        }
        row.set(i2, val.getValor());
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(IncDecSentencia stmt) {
        Expresion obj = getExprAny(stmt, "getObjetivo", "getLvalue", "getLValue");
        boolean inc = true;
        Boolean bInc = getBoolAny(stmt, "esIncremento", "isIncremento", "getIncremento", "getEsIncremento");
        if (bInc != null) inc = bInc;
        if (obj instanceof Identificador) {
            String id = ((Identificador) obj).getNombre();
            Simbolo s = contexto.buscarSimbolo(id);
            if (s == null) {
                contexto.agregarError("Variable '" + id + "' no declarada para ++/--");
                return new Valor(null, "void");
            }
            Object cur = s.getValor();
            if (cur instanceof Integer) contexto.setValor(s, ((Integer) cur) + (inc ? 1 : -1));
            else if (cur instanceof Double) contexto.setValor(s, ((Double) cur) + (inc ? 1.0 : -1.0));
            else if (cur instanceof Character) contexto.setValor(s, (char) (((Character) cur) + (inc ? 1 : -1)));
            else contexto.agregarError("++/-- solo aplica a int/double/char (ejecución)");
            return new Valor(null, "void");
        }
        if (obj instanceof AccesoIndexado) {
            AccesoIndexado acc = (AccesoIndexado) obj;
            Expresion contExpr = getExprAny(acc, "getObjetivo", "getObjeto");
            Valor contV = (contExpr != null) ? contExpr.accept(this) : new Valor(null, "error");
            Object cont = contV.getValor();
            Expresion i1e = getExprAny(acc, "getIndice1");
            int i1 = (i1e != null) ? toInt(i1e.accept(this)) : 0;
            Expresion i2e = getExprAny(acc, "getIndice2");
            boolean es2D = (i2e != null);
            if (!es2D) {
                if (!(cont instanceof List)) {
                    contexto.agregarError("++/-- indexado 1D solo aplica a vector/lista");
                    return new Valor(null, "void");
                }
                @SuppressWarnings("unchecked")
                List<Object> l = (List<Object>) cont;
                if (i1 < 0 || i1 >= l.size()) {
                    contexto.agregarError("Índice fuera de rango en ++/--");
                    return new Valor(null, "void");
                }
                Object cur = l.get(i1);
                if (cur instanceof Integer) l.set(i1, ((Integer) cur) + (inc ? 1 : -1));
                else if (cur instanceof Double) l.set(i1, ((Double) cur) + (inc ? 1.0 : -1.0));
                else if (cur instanceof Character) l.set(i1, (char) (((Character) cur) + (inc ? 1 : -1)));
                else contexto.agregarError("++/-- solo aplica a int/double/char (ejecución)");
                return new Valor(null, "void");
            }
            int i2 = toInt(i2e.accept(this));
            if (!(cont instanceof List)) {
                contexto.agregarError("++/-- indexado 2D solo aplica a matriz");
                return new Valor(null, "void");
            }
            @SuppressWarnings("unchecked")
            List<Object> rows = (List<Object>) cont;
            if (i1 < 0 || i1 >= rows.size()) {
                contexto.agregarError("Fila fuera de rango en ++/--");
                return new Valor(null, "void");
            }
            Object rowObj = rows.get(i1);
            if (!(rowObj instanceof List)) {
                contexto.agregarError("Estructura de matriz inválida en ++/--");
                return new Valor(null, "void");
            }
            @SuppressWarnings("unchecked")
            List<Object> row = (List<Object>) rowObj;
            if (i2 < 0 || i2 >= row.size()) {
                contexto.agregarError("Columna fuera de rango en ++/--");
                return new Valor(null, "void");
            }
            Object cur = row.get(i2);
            if (cur instanceof Integer) row.set(i2, ((Integer) cur) + (inc ? 1 : -1));
            else if (cur instanceof Double) row.set(i2, ((Double) cur) + (inc ? 1.0 : -1.0));
            else if (cur instanceof Character) row.set(i2, (char) (((Character) cur) + (inc ? 1 : -1)));
            else contexto.agregarError("++/-- solo aplica a int/double/char (ejecución)");
            return new Valor(null, "void");
        }
        contexto.agregarError("++/-- requiere identificador o acceso indexado (ejecución)");
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(LlamadaFuncion expr) {
        String nombre = norm(expr.getNombre());
        List<Expresion> args = expr.getArgumentos();
        if (args == null) args = new ArrayList<>();
        if (nombre.equals("round")) {
            if (args.size() != 1) {
                contexto.agregarError("round(exp) requiere 1 argumento");
                return new Valor(null, "error");
            }
            Valor a = args.get(0).accept(this);
            if (!(a.getTipo().equals("int") || a.getTipo().equals("double") || a.getTipo().equals("char"))) {
                contexto.agregarError("round(exp) solo acepta int/double/char");
                return new Valor(null, "error");
            }
            long r = Math.round(toDouble(a));
            return new Valor((int) r, "int");
        }
        if (nombre.equals("length")) {
            if (args.size() != 1) {
                contexto.agregarError("length(exp) requiere 1 argumento");
                return new Valor(null, "error");
            }
            Valor a = args.get(0).accept(this);
            Object v = a.getValor();
            if (v instanceof String) return new Valor(((String) v).length(), "int");
            if (v instanceof List) return new Valor(((List<?>) v).size(), "int");
            contexto.agregarError("length(exp) solo aplica a string, vector/lista/matriz");
            return new Valor(null, "error");
        }
        if (nombre.equals("tostring")) {
            if (args.size() != 1) {
                contexto.agregarError("toString(exp) requiere 1 argumento");
                return new Valor(null, "error");
            }
            Valor a = args.get(0).accept(this);
            return new Valor(stringify(a.getValor()), "string");
        }
        if (nombre.equals("find")) {
            if (args.size() != 2) {
                contexto.agregarError("find(a,b) requiere 2 argumentos");
                return new Valor(null, "error");
            }
            Valor a0 = args.get(0).accept(this);
            Valor a1 = args.get(1).accept(this);
            Object cont = a0.getValor();
            Object needle = a1.getValor();
            if (cont instanceof String && needle instanceof String) {
                return new Valor(((String) cont).indexOf((String) needle), "int");
            }
            if (cont instanceof List) {
                List<?> l = (List<?>) cont;
                int idx = -1;
                for (int i = 0; i < l.size(); i++) {
                    if (Objects.equals(l.get(i), needle)) {
                        idx = i;
                        break;
                    }
                }
                return new Valor(idx, "int");
            }
            contexto.agregarError("find(...) solo aplica a string o lista/vector");
            return new Valor(null, "error");
        }
        if (nombre.equals("start_with") || nombre.equals("startwith") || nombre.equals("startswith")) {
            if (args.size() != 2) {
                contexto.agregarError("START_WITH(a,b) requiere 2 argumentos");
                return new Valor(null, "error");
            }
            Valor a0 = args.get(0).accept(this);
            Valor a1 = args.get(1).accept(this);
            if (!(a0.getValor() instanceof String) || !(a1.getValor() instanceof String)) {
                contexto.agregarError("START_WITH(a,b) requiere string,string");
                return new Valor(null, "error");
            }
            boolean ok = ((String) a0.getValor()).startsWith((String) a1.getValor());
            return new Valor(ok, "bool");
        }
        DeclaracionFuncion f = contexto.obtenerFuncion(expr.getNombre());
        if (f == null) {
            contexto.agregarError("Función/método no definido: '" + expr.getNombre() + "'");
            return new Valor(null, "error");
        }
        return ejecutarCallable(f, args, false);
    }

    @Override
    public Valor visit(LlamadaMiembro expr) {
        Expresion objetivo = getExprAny(expr, "getObjetivo", "getObjeto");
        Valor objV = (objetivo != null) ? objetivo.accept(this) : new Valor(null, "error");
        Object obj = objV.getValor();
        String nombre = norm(getStringAny(expr, "getNombre", "getMetodo"));
        List<Expresion> args = getExprListAny(expr, "getArgumentos");
        if (args == null) args = new ArrayList<>();
        if (!(obj instanceof List)) {
            contexto.agregarError("Llamada de miembro solo soportada sobre List en esta fase (ejecución)");
            return new Valor(null, "error");
        }
        @SuppressWarnings("unchecked")
        List<Object> lista = (List<Object>) obj;
        if (nombre.equals("append")) {
            if (args.size() != 1) {
                contexto.agregarError("append(x) requiere 1 argumento");
                return new Valor(null, "error");
            }
            Valor v = args.get(0).accept(this);
            lista.add(v.getValor());
            return new Valor(null, "void");
        }
        if (nombre.equals("remove")) {
            if (args.size() != 1) {
                contexto.agregarError("remove(indice) requiere 1 argumento");
                return new Valor(null, "error");
            }
            Valor v = args.get(0).accept(this);
            if (!"int".equals(v.getTipo())) {
                contexto.agregarError("remove(indice) requiere indice int");
                return new Valor(null, "error");
            }
            int idx = (Integer) v.getValor();
            if (idx < 0 || idx >= lista.size()) {
                contexto.agregarError("remove(indice) fuera de rango: " + idx);
                return new Valor(null, "error");
            }
            Object eliminado = lista.remove(idx);
            return new Valor(eliminado, tipoDesdeObjeto(eliminado));
        }
        if (nombre.equals("find")) {
            if (args.size() != 1) {
                contexto.agregarError("find(valor) requiere 1 argumento");
                return new Valor(null, "error");
            }
            Valor v = args.get(0).accept(this);
            Object needle = v.getValor();
            boolean existe = false;
            for (int i = 0; i < lista.size(); i++) {
                if (Objects.equals(lista.get(i), needle)) {
                    existe = true;
                    break;
                }
            }
            return new Valor(existe, "bool");
        }
        contexto.agregarError("Método no soportado en List: " + nombre);
        return new Valor(null, "error");
    }

    private boolean esEntero(Valor v) {
        if (v == null) return false;
        String t = v.getTipo();
        return t.equals("int") || t.equals("char");
    }

    private String tipoDesdeObjeto(Object o) {
        if (o instanceof Integer) return "int";
        if (o instanceof Double) return "double";
        if (o instanceof Boolean) return "bool";
        if (o instanceof Character) return "char";
        if (o instanceof String) return "string";
        if (o instanceof List) return "list";
        return "any";
    }

    private int toInt(Valor v) {
        if (v == null || v.getValor() == null) return 0;
        switch (v.getTipo()) {
            case "int":
                return (Integer) v.getValor();
            case "char":
                return (Character) v.getValor();
            case "double":
                return ((Double) v.getValor()).intValue();
            default:
                return 0;
        }
    }
}
