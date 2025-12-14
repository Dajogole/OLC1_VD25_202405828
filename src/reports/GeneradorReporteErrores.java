package reports;

import java.util.List;

public class GeneradorReporteErrores {
    
    public static String generarHTML(TablaErrores tablaErrores) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<title>Reporte de Errores - JavaUSAC</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("h1 { color: #333; }\n");
        html.append("table { border-collapse: collapse; width: 100%; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #f2f2f2; }\n");
        html.append("tr:nth-child(even) { background-color: #f9f9f9; }\n");
        html.append(".lexico { color: #d9534f; }\n");
        html.append(".sintactico { color: #f0ad4e; }\n");
        html.append(".semantico { color: #5bc0de; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<h1>Reporte de Errores - JavaUSAC</h1>\n");
        
        List<ErrorInfo> errores = tablaErrores.getErrores();
        if (errores.isEmpty()) {
            html.append("<p>No se encontraron errores.</p>\n");
        } else {
            html.append("<table>\n");
            html.append("<tr>\n");
            html.append("<th>Tipo</th>\n");
            html.append("<th>Descripción</th>\n");
            html.append("<th>Línea</th>\n");
            html.append("<th>Columna</th>\n");
            html.append("</tr>\n");
            
            for (ErrorInfo error : errores) {
                String clase = error.getTipo().toString().toLowerCase();
                html.append("<tr>\n");
                html.append("<td class=\"").append(clase).append("\">").append(error.getTipo()).append("</td>\n");
                html.append("<td>").append(error.getDescripcion()).append("</td>\n");
                html.append("<td>").append(error.getLinea()).append("</td>\n");
                html.append("<td>").append(error.getColumna()).append("</td>\n");
                html.append("</tr>\n");
            }
            
            html.append("</table>\n");
        }
        
        html.append("</body>\n");
        html.append("</html>");
        return html.toString();
    }
    
    public static String generarCSV(TablaErrores tablaErrores) {
        StringBuilder csv = new StringBuilder();
        csv.append("Tipo,Descripción,Línea,Columna\n");
        
        for (ErrorInfo error : tablaErrores.getErrores()) {
            csv.append(error.getTipo()).append(",");
            csv.append("\"").append(error.getDescripcion().replace("\"", "\"\"")).append("\",");
            csv.append(error.getLinea()).append(",");
            csv.append(error.getColumna()).append("\n");
        }
        
        return csv.toString();
    }
}