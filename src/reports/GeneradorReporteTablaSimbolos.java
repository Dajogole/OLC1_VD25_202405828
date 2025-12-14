package reports;

import semantic.Simbolo;
import semantic.TablaSimbolos;
import java.util.List;

public class GeneradorReporteTablaSimbolos {
    
    public static String generarHTML(TablaSimbolos tablaSimbolos) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<title>Tabla de Símbolos - JavaUSAC</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("h1 { color: #333; }\n");
        html.append("table { border-collapse: collapse; width: 100%; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #4CAF50; color: white; }\n");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<h1>Tabla de Símbolos - JavaUSAC</h1>\n");
        
        List<Simbolo> simbolos = tablaSimbolos.getSimbolos();
        if (simbolos.isEmpty()) {
            html.append("<p>No se encontraron símbolos.</p>\n");
        } else {
            html.append("<table>\n");
            html.append("<tr>\n");
            html.append("<th>Identificador</th>\n");
            html.append("<th>Tipo</th>\n");
            html.append("<th>Categoría</th>\n");
            html.append("<th>Línea</th>\n");
            html.append("<th>Columna</th>\n");
            html.append("<th>Valor</th>\n");
            html.append("</tr>\n");
            
            for (Simbolo simbolo : simbolos) {
                html.append("<tr>\n");
                html.append("<td>").append(simbolo.getIdentificador()).append("</td>\n");
                html.append("<td>").append(simbolo.getTipo()).append("</td>\n");
                html.append("<td>").append(simbolo.getCategoria()).append("</td>\n");
                html.append("<td>").append(simbolo.getLinea()).append("</td>\n");
                html.append("<td>").append(simbolo.getColumna()).append("</td>\n");
                html.append("<td>").append(simbolo.getValor() != null ? simbolo.getValor().toString() : "null").append("</td>\n");
                html.append("</tr>\n");
            }
            
            html.append("</table>\n");
        }
        
        html.append("</body>\n");
        html.append("</html>");
        return html.toString();
    }
    
    public static String generarCSV(TablaSimbolos tablaSimbolos) {
        StringBuilder csv = new StringBuilder();
        csv.append("Identificador,Tipo,Categoría,Línea,Columna,Valor\n");
        
        for (Simbolo simbolo : tablaSimbolos.getSimbolos()) {
            csv.append(simbolo.getIdentificador()).append(",");
            csv.append(simbolo.getTipo()).append(",");
            csv.append(simbolo.getCategoria()).append(",");
            csv.append(simbolo.getLinea()).append(",");
            csv.append(simbolo.getColumna()).append(",");
            csv.append("\"").append(simbolo.getValor() != null ? simbolo.getValor().toString().replace("\"", "\"\"") : "").append("\"\n");
        }
        
        return csv.toString();
    }
}