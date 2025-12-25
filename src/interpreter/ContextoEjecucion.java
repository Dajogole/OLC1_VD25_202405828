package interpreter;

import semantic.Simbolo;
import semantic.TablaSimbolos;
import semantic.Tipo;
import semantic.CategoriaSimbolo;
import ui.ConsolePanel;

import java.util.*;
import ast.sentencias.DeclaracionFuncion; 

public class ContextoEjecucion {
    private final ConsolePanel consola;

    private final TablaSimbolos tablaSimbolos;

    private final Deque<Map<String, Simbolo>> pilaEntornos;
    private final Deque<String> pilaAmbitos;

    private final Map<String, Simbolo> indiceReporte;
    private final List<String> errores;

    private int cicloDepth;
    private int switchDepth;

    private boolean debeBreakCiclo;
    private boolean debeBreakSwitch;
    private boolean debeContinue;


    private final Map<String, DeclaracionFuncion> funciones = new HashMap<>();

    private boolean debeReturn;
    private Valor valorReturn;

    private boolean returnActivo = false;
    private Valor valorRetorno = new Valor(null, "void");

    public static class ControlSnapshot {
        final int cicloDepth;
        final int switchDepth;
        final boolean debeBreakCiclo;
        final boolean debeBreakSwitch;
        final boolean debeContinue;
        final boolean debeReturn;
        final Valor valorReturn;

        ControlSnapshot(int cicloDepth, int switchDepth,
                        boolean debeBreakCiclo, boolean debeBreakSwitch, boolean debeContinue,
                        boolean debeReturn, Valor valorReturn) {
            this.cicloDepth = cicloDepth;
            this.switchDepth = switchDepth;
            this.debeBreakCiclo = debeBreakCiclo;
            this.debeBreakSwitch = debeBreakSwitch;
            this.debeContinue = debeContinue;
            this.debeReturn = debeReturn;
            this.valorReturn = valorReturn;
        }
    }

    public ContextoEjecucion(ConsolePanel consola) {
        this.consola = consola;
        this.tablaSimbolos = new TablaSimbolos();
        this.errores = new ArrayList<>();

        this.pilaEntornos = new ArrayDeque<>();
        this.pilaAmbitos = new ArrayDeque<>();
        this.indiceReporte = new HashMap<>();

        // Global
        this.pilaEntornos.push(new HashMap<>());
        this.pilaAmbitos.push("Global");

        this.cicloDepth = 0;
        this.switchDepth = 0;
        this.debeBreakCiclo = false;
        this.debeBreakSwitch = false;
        this.debeContinue = false;

        // Fase 2
        this.debeReturn = false;
        this.valorReturn = null;
    }

    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }

    public List<String> getErrores() {
        return errores;
    }

    public void agregarError(String error) {
        errores.add(error);
    }

    public void pushBloque() {
        pilaEntornos.push(new HashMap<>());
    }

    public void popBloque() {
        if (pilaEntornos.size() > 1) {
            pilaEntornos.pop();
        }
    }

    public void pushAmbito(String ambito) {
        if (ambito == null || ambito.isBlank()) return;
        pilaAmbitos.push(ambito);
    }

    public void popAmbito() {
        if (pilaAmbitos.size() > 1) pilaAmbitos.pop();
    }

    public String ambitoActual() {
        return pilaAmbitos.peek();
    }

    private String rutaAmbito() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = pilaAmbitos.descendingIterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) sb.append("> ");
        }
        return sb.toString();
    }

    private static String normId(String id) {
        return id == null ? "" : id.toLowerCase();
    }

private String claveReporte(String id, String ambito, CategoriaSimbolo categoria) {
    return normId(id) + "|" + normId(ambito) + "|" + categoria;
}

private Simbolo declararSimbolo(String id, Tipo tipo, CategoriaSimbolo categoria, int line, int column) {
    String k = normId(id);
    Map<String, Simbolo> actual = pilaEntornos.peek();

    if (actual.containsKey(k)) {
        agregarError("Identificador '" + id + "' ya declarado en este bloque (ejecución)");
        return null;
    }

   
    Simbolo runtime = new Simbolo(id, tipo, categoria, line, column);
    runtime.setAmbito(ambitoActual());
    actual.put(k, runtime);

  
    String cr = claveReporte(id, ambitoActual(), categoria);
    Simbolo rep = indiceReporte.get(cr);

    if (rep == null) {
        rep = new Simbolo(id, tipo, categoria, line, column);
        rep.setAmbito(ambitoActual());
        indiceReporte.put(cr, rep);
        tablaSimbolos.agregarSimbolo(rep);
    }

    return runtime;
}


public Simbolo declararVariable(String id, Tipo tipo, int line, int column) {
    return declararSimbolo(id, tipo, CategoriaSimbolo.VARIABLE, line, column);
}

public Simbolo declararParametro(String id, Tipo tipo, int line, int column) {
    return declararSimbolo(id, tipo, CategoriaSimbolo.PARAMETRO, line, column);
}


    public Simbolo buscarSimbolo(String id) {
        String k = normId(id);
        for (Map<String, Simbolo> env : pilaEntornos) {
            Simbolo s = env.get(k);
            if (s != null) return s;
        }
        return null;
    }

    public void setValor(Simbolo runtime, Object valor) {
    if (runtime == null) return;


    runtime.setValor(valor);


    String cr = claveReporte(runtime.getIdentificador(), runtime.getAmbito(), runtime.getCategoria());
    Simbolo rep = indiceReporte.get(cr);

    if (rep != null) {

        if (runtime.getCategoria() == CategoriaSimbolo.PARAMETRO) {
            if (rep.getValor() == null) {
                rep.setValor(valor);
            }
        } else {
            rep.setValor(valor);
        }
    }
}


    public void entrarCiclo() { cicloDepth++; }
    public void salirCiclo() { if (cicloDepth > 0) cicloDepth--; }
    public boolean isEnCiclo() { return cicloDepth > 0; }

    public void entrarSwitch() { switchDepth++; }
    public void salirSwitch() { if (switchDepth > 0) switchDepth--; }
    public boolean isEnSwitch() { return switchDepth > 0; }

    public boolean debeBreakCiclo() { return debeBreakCiclo; }
    public void setDebeBreakCiclo(boolean v) { this.debeBreakCiclo = v; }

    public boolean debeBreakSwitch() { return debeBreakSwitch; }
    public void setDebeBreakSwitch(boolean v) { this.debeBreakSwitch = v; }

    public boolean debeContinue() { return debeContinue; }
    public void setDebeContinue(boolean v) { this.debeContinue = v; }

    public void imprimir(String texto) {
        if (consola != null) consola.appendLine(texto);
        else System.out.println(texto);
    }



    public void resetControlFlujo() {
        this.cicloDepth = 0;
        this.switchDepth = 0;
        this.debeBreakCiclo = false;
        this.debeBreakSwitch = false;
        this.debeContinue = false;
        this.debeReturn = false;
        this.valorReturn = null;
    }

    public void activarReturn(Valor v) {
    this.returnActivo = true;
    this.valorRetorno = (v == null) ? new Valor(null, "void") : v;
}

    public boolean debeReturn() {
        return debeReturn;
    }

    public Valor consumirReturn() {
        Valor v = valorReturn;
        debeReturn = false;
        valorReturn = null;
        return v;
    }

    public void limpiarReturn() {
        debeReturn = false;
        valorReturn = null;
    }

    public ControlSnapshot snapshotControl() {
        return new ControlSnapshot(cicloDepth, switchDepth,
                debeBreakCiclo, debeBreakSwitch, debeContinue,
                debeReturn, valorReturn);
    }

    public void restoreControl(ControlSnapshot s) {
        this.cicloDepth = s.cicloDepth;
        this.switchDepth = s.switchDepth;
        this.debeBreakCiclo = s.debeBreakCiclo;
        this.debeBreakSwitch = s.debeBreakSwitch;
        this.debeContinue = s.debeContinue;
        this.debeReturn = s.debeReturn;
        this.valorReturn = s.valorReturn;
    }


private static String normCallable(String id) {
    return id == null ? "" : id.toLowerCase();
}

public void registrarFuncion(DeclaracionFuncion f) {
    if (f == null) return;
    String k = normCallable(f.getNombre());
    if (k.isEmpty()) return;
    funciones.put(k, f);


    CategoriaSimbolo cat = (f.getTipoRetorno() == Tipo.VOID)
            ? CategoriaSimbolo.METODO
            : CategoriaSimbolo.FUNCION;

    String claveReporte = normId(f.getNombre()) + "|" + normId("Global") + "|" + cat;
    if (!indiceReporte.containsKey(claveReporte)) {
        Simbolo s = new Simbolo(f.getNombre(), f.getTipoRetorno(), cat, f.getLine(), f.getColumn());
        s.setAmbito("Global");
        s.setValor("-");
        tablaSimbolos.agregarSimbolo(s);
        indiceReporte.put(claveReporte, s);
    }
}


public DeclaracionFuncion obtenerFuncion(String nombre) {
    if (nombre == null) return null;
    return funciones.get(normCallable(nombre));
}


public void pushCallFrame(String ambito) {
    pushAmbito(ambito);
    pushBloque();
}

public void popCallFrame() {
    popBloque();
    popAmbito();
}




public boolean hayReturn() {
    return returnActivo;
}

public Valor tomarReturnYLimpiar() {
    Valor v = valorRetorno;
    returnActivo = false;
    valorRetorno = new Valor(null, "void");
    return v;
}


    
}
