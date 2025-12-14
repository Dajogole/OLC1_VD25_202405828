package interpreter;

import semantic.Simbolo;
import semantic.TablaSimbolos;
import semantic.Tipo;
import semantic.CategoriaSimbolo;
import ui.ConsolePanel;

import java.util.*;

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

    public Simbolo declararVariable(String id, Tipo tipo, int line, int column) {
        String k = normId(id);
        Map<String, Simbolo> actual = pilaEntornos.peek();

        if (actual.containsKey(k)) {
            agregarError("Variable '" + id + "' ya declarada en este bloque (ejecución)");
            return null;
        }

        String claveReporte = k + "|" + rutaAmbito();
        Simbolo simbolo = indiceReporte.get(claveReporte);

        if (simbolo == null) {
            simbolo = new Simbolo(id, tipo, CategoriaSimbolo.VARIABLE, line, column);
            simbolo.setAmbito(ambitoActual());
            tablaSimbolos.agregarSimbolo(simbolo);
            indiceReporte.put(claveReporte, simbolo);
        } else {
            simbolo.setAmbito(ambitoActual());
        }

        actual.put(k, simbolo);
        return simbolo;
    }

    public Simbolo buscarSimbolo(String id) {
        String k = normId(id);
        for (Map<String, Simbolo> env : pilaEntornos) {
            Simbolo s = env.get(k);
            if (s != null) return s;
        }
        return null;
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
}
