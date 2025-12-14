package semantic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TablaSimbolos {
    private final List<Simbolo> simbolos;

    public TablaSimbolos() {
        this.simbolos = new ArrayList<>();
    }

    public void agregarSimbolo(Simbolo simbolo) {
        simbolos.add(simbolo);
    }

    public void agregarDesdeEntorno(Entorno entorno) {
        for (Map.Entry<String, Simbolo> entry : entorno.getTabla().entrySet()) {
            simbolos.add(entry.getValue());
        }
    }

        public Simbolo buscar(String identificador) {
        if (identificador == null) return null;
        String idLower = identificador.toLowerCase();
        for (Simbolo s : simbolos) {
            if (s.getIdentificador() != null &&
                s.getIdentificador().toLowerCase().equals(idLower)) {
                return s;
            }
        }
        return null;
    }

    public List<Simbolo> getSimbolos() {
        return new ArrayList<>(simbolos);
    }
    
    public void limpiar() {
        simbolos.clear();
    }
}