package parser;

import java_cup.runtime.*;
import reports.TablaErrores;
import reports.ErrorTipo;

public class ErrorRecovery {
    
    private TablaErrores tablaErrores;
    
    public ErrorRecovery(TablaErrores tablaErrores) {
        this.tablaErrores = tablaErrores;
    }
    
    public void recoverFromError(Symbol token, Parser parser) {
        if (tablaErrores != null && token != null) {
            String desc = "Error de sintaxis en token: " + token;
            tablaErrores.agregarError(ErrorTipo.SINTACTICO, desc, 
                                     token.left, token.right);
        }
        
      
        skipToSyncPoint(parser);
    }
    
    private void skipToSyncPoint(Parser parser) {
     
    }
    
    public static void agregarError(TablaErrores tabla, String mensaje, int linea, int columna) {
        if (tabla != null) {
            tabla.agregarError(ErrorTipo.SINTACTICO, mensaje, linea, columna);
        }
    }
}