package interpreter;

import ast.*;
import ast.expresiones.*;
import ast.sentencias.*;
import semantic.Simbolo;
import semantic.Tipo;
import java.util.List;
import semantic.CategoriaSimbolo;



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

    @Override
    public Valor visit(Programa programa) {
        for (Sentencia stmt : programa.getSentencias()) {
            stmt.accept(this);
            if (contexto.debeBreakCiclo() || contexto.debeContinue() || contexto.debeBreakSwitch()) {
                contexto.agregarError("Break/Continue fuera de ciclo/switch");
                contexto.setDebeBreakCiclo(false);
                contexto.setDebeBreakSwitch(false);
                contexto.setDebeContinue(false);
                break;
            }
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
        contexto.agregarError("No se puede sumar tipos '" +
                izq.getTipo() + "' y '" + der.getTipo() + "'");
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
        contexto.agregarError("No se puede restar tipos '" +
                izq.getTipo() + "' y '" + der.getTipo() + "'");
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
        contexto.agregarError("No se puede multiplicar tipos '" +
                izq.getTipo() + "' y '" + der.getTipo() + "'");
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
        contexto.agregarError("No se puede dividir tipos '" +
                izq.getTipo() + "' y '" + der.getTipo() + "'");
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
        contexto.agregarError("No se puede aplicar potencia a tipos '" +
                izq.getTipo() + "' y '" + der.getTipo() + "'");
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
                case MENOR:
                    resultado = a < b;
                    break;
                case MAYOR:
                    resultado = a > b;
                    break;
                case MENOR_IGUAL:
                    resultado = a <= b;
                    break;
                case MAYOR_IGUAL:
                    resultado = a >= b;
                    break;
                case IGUAL:
                    resultado = Math.abs(a - b) < 1e-9;
                    break;
                case DIFERENTE:
                    resultado = Math.abs(a - b) >= 1e-9;
                    break;
            }
            return new Valor(resultado, "bool");
        }
        if (tIzq.equals("bool") && tDer.equals("bool")) {
            boolean a = (Boolean) izq.getValor();
            boolean b = (Boolean) der.getValor();
            switch (expr.getOperador()) {
                case IGUAL:
                    resultado = (a == b);
                    break;
                case DIFERENTE:
                    resultado = (a != b);
                    break;
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
                case IGUAL:
                    resultado = a.equals(b);
                    break;
                case DIFERENTE:
                    resultado = !a.equals(b);
                    break;
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
                    case AND:
                        return new Valor(a && b, "bool");
                    case OR:
                        return new Valor(a || b, "bool");
                    case XOR:
                        return new Valor(a ^ b, "bool");
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
        return new Valor((double) c, "double"); // <-- FIX: 'F' => 70.0
    }
                break;
            case CHAR:
                if (valor.getTipo().equals("int")) {
                    int i = (Integer) valor.getValor();
                    return new Valor((char) i, "char");
                }
                break;
            case STRING:
                return new Valor(valor.getValor().toString(), "string");
        }
        return new Valor(null, "error");
    }
    
    @Override
    public Valor visit(ExpresionAgrupada expr) {
        return expr.getExpresion().accept(this);
    }

    @Override
public Valor visit(LlamadaFuncion expr) {
   
    contexto.agregarError("Llamada a función no soportada en esta fase: " + expr.getNombre());
    return new Valor(null, "error");
}

    
    @Override
    public Valor visit(BloqueSentencias stmt) {
        contexto.pushBloque();
        try {
            for (Sentencia s : stmt.getSentencias()) {
                s.accept(this);
                if (contexto.debeBreakCiclo() || contexto.debeContinue() || contexto.debeBreakSwitch()) {
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
            Valor v = stmt.getExpresionInicial().accept(this);
            if (!v.getTipo().equals(tipo.toString())) {
                contexto.agregarError("Tipo incompatible en declaración de '" + id + "' en ejecución");
            } else {
                simbolo.setValor(v.getValor());
            }
        } else {
            switch (tipo) {
                case INT -> simbolo.setValor(0);
                case DOUBLE -> simbolo.setValor(0.0);
                case BOOL -> simbolo.setValor(true);
                case CHAR -> simbolo.setValor('\u0000');
                case STRING -> simbolo.setValor("");
            }
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
        Valor v = stmt.getExpresion().accept(this);
        if (!simbolo.getTipo().toString().equals(v.getTipo())) {
            contexto.agregarError("Tipo incompatible al asignar a '" + id + "' en ejecución");
            return new Valor(null, "void");
        }
        simbolo.setValor(v.getValor());
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(IfSentencia stmt) {
        Valor cond = stmt.getCondicion().accept(this);
        if (cond.getTipo().equals("bool") && (Boolean) cond.getValor()) {
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
                        if (contexto.debeContinue() || contexto.debeBreakCiclo()) {
                            return new Valor(null, "void");
                        }
                    }
                }
            }
            if (!ejecutar && stmt.getCasoDefault() != null) {
                stmt.getCasoDefault().accept(this);
                if (contexto.debeBreakSwitch()) contexto.setDebeBreakSwitch(false);
            } else if (ejecutar && stmt.getCasoDefault() != null) {
                stmt.getCasoDefault().accept(this);
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
        if (!a.getTipo().equals(b.getTipo())) return false;
        return a.getValor().equals(b.getValor());
    }

    @Override
    public Valor visit(Case stmt) {
        for (Sentencia s : stmt.getSentencias()) {
            s.accept(this);
            if (contexto.debeBreakSwitch() || contexto.debeBreakCiclo() || contexto.debeContinue()) {
                break;
            }
        }
        return new Valor(null, "void");
    }

    @Override
    public Valor visit(Default stmt) {
        for (Sentencia s : stmt.getSentencias()) {
            s.accept(this);
            if (contexto.debeBreakSwitch() || contexto.debeBreakCiclo() || contexto.debeContinue()) {
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
                if (!cond.getTipo().equals("bool") || !(Boolean) cond.getValor()) {
                    break;
                }
                stmt.getBloque().accept(this);
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
                    if (!cond.getTipo().equals("bool") || !(Boolean) cond.getValor()) {
                        break;
                    }
                }
                if (stmt.getBloque() != null) {
                    stmt.getBloque().accept(this);
                }
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
                if (contexto.debeBreakCiclo()) {
                    contexto.setDebeBreakCiclo(false);
                    break;
                }
                if (contexto.debeContinue()) {
                    contexto.setDebeContinue(false);
                }
                Valor cond = stmt.getCondicion().accept(this);
                if (!cond.getTipo().equals("bool") || !(Boolean) cond.getValor()) {
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
        contexto.imprimir(String.valueOf(val));
        return new Valor(null, "void");
    }

    private boolean esEntero(Valor v) {
        if (v == null) return false;
        String t = v.getTipo();
        return t.equals("int") || t.equals("char");
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