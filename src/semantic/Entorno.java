package semantic;

import java.util.HashMap;
import java.util.Map;

public class Entorno {
    private final Map<String, Simbolo> tabla;
    private final Entorno padre;

    public Entorno(Entorno padre) {
        this.tabla = new HashMap<>();
        this.padre = padre;
    }

    public boolean insertar(Simbolo simbolo) {
        String id = simbolo.getIdentificador().toLowerCase();
        if (tabla.containsKey(id)) {
            return false;
        }
        tabla.put(id, simbolo);
        return true;
    }

    public Simbolo buscar(String identificador) {
        String id = identificador.toLowerCase();
        Simbolo s = tabla.get(id);
        if (s != null) {
            return s;
        }
        if (padre != null) {
            return padre.buscar(identificador);
        }
        return null;
    }

    public Simbolo buscarLocal(String identificador) {
        return tabla.get(identificador.toLowerCase());
    }
    
    public Map<String, Simbolo> getTabla() {
        return tabla;
    }
}