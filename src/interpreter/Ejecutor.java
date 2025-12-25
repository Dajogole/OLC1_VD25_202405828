package interpreter;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ast.Programa;
import lexer.Lexer;
import parser.Parser;
import reports.ErrorTipo;
import reports.TablaErrores;
import semantic.SemanticError;
import semantic.TablaSimbolos;
import semantic.Tipo;
import semantic.VisitanteSemantico;
import semantic.Simbolo;
import ui.ConsolePanel;
import reports.ErrorInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import reports.AstDotGenerator;



public class Ejecutor {

    private static TablaErrores ultimaTablaErrores;
    private static TablaSimbolos ultimaTablaSimbolos;


    private static Path ultimoAstDotPath;
private static Path ultimoAstPngPath;
private static String ultimoAstError;

public static Path getUltimoAstDotPath() { return ultimoAstDotPath; }
public static Path getUltimoAstPngPath() { return ultimoAstPngPath; }
public static String getUltimoAstError() { return ultimoAstError; }


    public static TablaErrores getUltimaTablaErrores() {
        return ultimaTablaErrores;
    }

    public static TablaSimbolos getUltimaTablaSimbolos() {
        return ultimaTablaSimbolos;
    }

    public static void ejecutar(String codigo, ConsolePanel console) {

        if (console != null) {
            console.clear();
        }

        TablaErrores tablaErrores = new TablaErrores();
        ultimaTablaErrores = tablaErrores;
        ultimaTablaSimbolos = null;

        ultimoAstDotPath = null;
        ultimoAstPngPath = null;
        ultimoAstError = null;


        final String codigoOriginal = (codigo == null) ? "" : codigo;

        SourceIndex sourceIndex = SourceIndex.build(codigoOriginal);

        PreprocessResult prep = preprocesarParaParseo(codigoOriginal, tablaErrores);


if (tablaErrores.tieneErroresLexicos()) {
    if (console != null) {
        imprimirErroresEnConsola(tablaErrores, console);
    }
    return;
}


        try {
    Lexer lexer = new Lexer(new StringReader(prep.codigoProcesado));
    lexer.setTablaErrores(tablaErrores);

    Parser parser = new Parser(lexer);
    parser.setTablaErrores(tablaErrores);

    parser.parse();

    Programa programa = parser.getPrograma();
    if (programa == null) {
        tablaErrores.agregarError(
            ErrorTipo.SINTACTICO,
            "No se pudo construir el AST: el programa es nulo.",
            1,
            1
        );
        return;
    }

    VisitanteSemantico semantico = new VisitanteSemantico();
    programa.accept(semantico);

    TablaSimbolos tablaSemantica = semantico.getTablaSimbolos();

    actualizarLineasYColumnas(tablaSemantica, codigoOriginal);

    ultimaTablaSimbolos = tablaSemantica;

    List<SemanticError> erroresSemanticos = semantico.getErrores();
    agregarErroresSemanticosMapeados(tablaErrores, erroresSemanticos, sourceIndex);

    agregarErroresLengthManual(tablaErrores, sourceIndex);


generarReporteAST(programa, console);


    if (tablaErrores.tieneErrores()) {
        return;
    }

    ContextoEjecucion contexto = new ContextoEjecucion(console);
VisitanteEvaluacion eval = new VisitanteEvaluacion(contexto);
programa.accept(eval);


TablaSimbolos tablaEjecucion = contexto.getTablaSimbolos();
completarUbicacionesSimbolos(tablaEjecucion, codigoOriginal);
ultimaTablaSimbolos = tablaEjecucion;


} catch (Exception ex) {
    
    if (!tablaErrores.tieneErroresLexicos() && !tablaErrores.tieneErroresSintacticos()) {
        tablaErrores.agregarError(
            ErrorTipo.SINTACTICO,
            "Error fatal durante el analisis: " + ex.getMessage(),
            1,
            1
        );
    }
} finally {

    if (console != null && tablaErrores.tieneErrores()) {
        imprimirErroresEnConsola(tablaErrores, console);
    }
}

    }

    private static class PreprocessResult {
        final String codigoProcesado;
        PreprocessResult(String codigoProcesado) {
            this.codigoProcesado = codigoProcesado;
        }
    }
private static PreprocessResult preprocesarParaParseo(String codigoOriginal, TablaErrores tablaErrores) {

    String[] lines = codigoOriginal.split("\\r\\n|\\r|\\n", -1);
    List<String> out = new ArrayList<>();

    for (int i = 0; i < lines.length; i++) {
        String s = lines[i];

        // Reporta caracteres inválidos, pero NO altera estructura del programa (Fase 2 requiere funciones/start)
        s = reemplazarInvalidosYReportar(s, i + 1, tablaErrores);

        // Arreglos menores de formato
       s = repararMasPuntoYComa(s, i + 1, tablaErrores);
s = repararPrintlnParentesis(s, i + 1, tablaErrores);


        out.add(s);
    }

    String codigoProcesado = String.join("\n", out);
    return new PreprocessResult(codigoProcesado);
}


    private static void imprimirErroresEnConsola(TablaErrores tablaErrores, ConsolePanel console) {
    console.appendLine("----------- Errores -----------");
    for (ErrorInfo e : tablaErrores.getErrores()) {
        String tipo = tipoBonito(e.getTipo());
        console.appendLine("[" + tipo + "] Línea " + e.getLinea() + ", Col " + e.getColumna() + ": " + e.getDescripcion());
    }
}

private static String tipoBonito(ErrorTipo tipo) {
    if (tipo == null) return "Desconocido";
    switch (tipo) {
        case LEXICO: return "Léxico";
        case SINTACTICO: return "Sintáctico";
        case SEMANTICO: return "Semántico";
        default: return "Desconocido";
    }
}


    private static String reemplazarInvalidosYReportar(String line, int lineNo, TablaErrores tablaErrores) {

        StringBuilder sb = new StringBuilder(line.length());

        boolean inString = false;
        boolean inChar = false;
        boolean escape = false;

        for (int idx = 0; idx < line.length(); idx++) {
            char ch = line.charAt(idx);
            int col = idx + 1;

            if (escape) {
                sb.append(ch);
                escape = false;
                continue;
            }

            if (ch == '\\') {
                if (inString || inChar) {
                    sb.append(ch);
                    escape = true;
                    continue;
                }
                sb.append(ch);
                continue;
            }

            if (ch == '"' && !inChar) {
                inString = !inString;
                sb.append(ch);
                continue;
            }

            if (ch == '\'' && !inString) {
                inChar = !inChar;
                sb.append(ch);
                continue;
            }

            if (inString || inChar) {
                sb.append(ch);
                continue;
            }

            if (esCharPermitido(ch)) {
                sb.append(ch);
            } else {
                tablaErrores.agregarError(
                    ErrorTipo.LEXICO,
                    "Símbolo no reconocido: '" + ch + "'",
                    lineNo,
                    col
                );
                sb.append(' ');
            }
        }

        return sb.toString();
    }

    private static boolean esCharPermitido(char ch) {
        if (Character.isWhitespace(ch)) return true;
        if (Character.isLetterOrDigit(ch)) return true;
        if (ch == '_') return true;

        switch (ch) {
            case '+': case '-': case '*': case '/': case '%': case '^':
            case '=': case '<': case '>': case '!':
            case '&': case '|':
            case ';': case ':': case ',':
            case '(': case ')':
            case '{': case '}':
            case '[': case ']':
            case '.':
                return true;
            default:
                return false;
        }
    }

    private static String repararMasPuntoYComa(String line, int lineNo, TablaErrores tablaErrores) {
    int from = 0;
    String s = line;

    while (true) {
        int plus = s.indexOf('+', from);
        if (plus < 0) break;

        if ((plus + 1 < s.length() && s.charAt(plus + 1) == '+') ||
            (plus - 1 >= 0 && s.charAt(plus - 1) == '+')) {
            from = plus + 1;
            continue;
        }

        int j = plus + 1;
        while (j < s.length() && Character.isWhitespace(s.charAt(j))) j++;

        if (j < s.length() && s.charAt(j) == ';') {
            int colSemicolon = j + 1;
            tablaErrores.agregarError(
                ErrorTipo.SINTACTICO,
                "Se esperaba una expresión después de '+', encontró ';'.",
                lineNo,
                colSemicolon
            );

            s = s.substring(0, j) + "0" + s.substring(j);
            from = j + 1;
        } else {
            from = plus + 1;
        }
    }

    return s;
}


    private static String repararPrintlnParentesis(String line, int lineNo, TablaErrores tablaErrores) {

        String lower = line.toLowerCase();
        if (!lower.contains("println")) return line;

        int semicolon = line.lastIndexOf(';');
        if (semicolon < 0) return line;

        int opens = countCharOutsideStrings(line, '(');
        int closes = countCharOutsideStrings(line, ')');

        if (opens > closes) {
            int missing = opens - closes;
            int colSemicolon = semicolon + 1;

            tablaErrores.agregarError(
                ErrorTipo.SINTACTICO,
                "Paréntesis desbalanceados en println(...): se esperaba ')' antes de ';'.",
                lineNo,
                colSemicolon
            );

            StringBuilder sb = new StringBuilder();
            sb.append(line, 0, semicolon);
            for (int k = 0; k < missing; k++) sb.append(')');
            sb.append(line.substring(semicolon));
            return sb.toString();
        }

        return line;
    }

    private static String reemplazarLengthPorCero(String line) {
        Pattern p = Pattern.compile("(?i)\\blength\\s*\\([^\\)]*\\)");
        Matcher m = p.matcher(line);
        return m.replaceAll("0");
    }

    private static int countCharOutsideStrings(String line, char target) {

        boolean inString = false;
        boolean inChar = false;
        boolean escape = false;
        int count = 0;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (escape) {
                escape = false;
                continue;
            }

            if (ch == '\\') {
                if (inString || inChar) escape = true;
                continue;
            }

            if (ch == '"' && !inChar) {
                inString = !inString;
                continue;
            }

            if (ch == '\'' && !inString) {
                inChar = !inChar;
                continue;
            }

            if (inString || inChar) continue;

            if (ch == target) count++;
        }

        return count;
    }

    private static void agregarErroresSemanticosMapeados(
        TablaErrores tablaErrores,
        List<SemanticError> erroresSemanticos,
        SourceIndex idx
    ) {
        for (SemanticError e : erroresSemanticos) {

            MappedSemantic ms = idx.mapearSemanticError(e);

            tablaErrores.agregarError(
                ErrorTipo.SEMANTICO,
                ms.mensaje,
                ms.linea,
                ms.columna
            );
        }
    }

    private static void agregarErroresLengthManual(TablaErrores tablaErrores, SourceIndex idx) {
        for (LengthCall lc : idx.lengthCalls) {

            String arg = lc.argumento == null ? "" : lc.argumento.trim();

            boolean esStringLiteral = arg.startsWith("\"") && arg.endsWith("\"");
            if (esStringLiteral) continue;

            boolean pareceNumero = arg.matches("[+-]?\\d+(\\.\\d+)?");
            boolean pareceBool = arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false");

            if (pareceNumero || pareceBool) {
                String recibido = pareceBool ? "bool" : "int";
                String mostrado = arg;

                tablaErrores.agregarError(
                    ErrorTipo.SEMANTICO,
                    "length(...) espera string, pero recibió " + recibido + " ('" + mostrado + "')",
                    lc.linea,
                    lc.colArg
                );
            }
        }
    }

    private static void actualizarLineasYColumnas(TablaSimbolos tablaSimbolos, String codigoOriginal) {

        Pattern patronDeclaracion = Pattern.compile(
            "(?i)\\bvar\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*:\\s*([a-zA-Z_][a-zA-Z0-9_]*)"
        );

        Matcher matcher = patronDeclaracion.matcher(codigoOriginal);

        while (matcher.find()) {
            String id = matcher.group(1);
            int indexMatch = matcher.start(1);

            int linea = 1;
            int columna = 1;

            for (int i = 0; i < indexMatch; i++) {
                if (codigoOriginal.charAt(i) == '\n') {
                    linea++;
                    columna = 1;
                } else {
                    columna++;
                }
            }

            for (Simbolo s : tablaSimbolos.getSimbolos()) {
                if (s.getIdentificador().equalsIgnoreCase(id)) {
                    s.setLinea(linea);
                    s.setColumna(columna);
                    break;
                }
            }
        }
    }


private static void completarUbicacionesSimbolos(TablaSimbolos tablaSimbolos, String codigoOriginal) {
    if (tablaSimbolos == null || codigoOriginal == null) return;

    List<Simbolo> simbolos = tablaSimbolos.getSimbolos();
    if (simbolos.isEmpty()) return;


    Map<String, List<Simbolo>> idx = new HashMap<>();
    for (Simbolo s : simbolos) {
        String k = keySim(s.getIdentificador(), s.getAmbito(), s.getCategoria());
        idx.computeIfAbsent(k, __ -> new ArrayList<>()).add(s);
    }

    String[] lines = codigoOriginal.split("\\r\\n|\\r|\\n", -1);


    Pattern pFunc = Pattern.compile(
            "^\\s*([a-zA-Z_][a-zA-Z0-9_<>\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(([^)]*)\\)",
            Pattern.CASE_INSENSITIVE
    );
    Pattern pVar = Pattern.compile("\\bvar\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*:", Pattern.CASE_INSENSITIVE);
    Pattern pParam = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_<>\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\b", Pattern.CASE_INSENSITIVE);
    Pattern pTypedVar = Pattern.compile(
        "^\\s*([a-zA-Z_][a-zA-Z0-9_<>\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=|;)",
        Pattern.CASE_INSENSITIVE
);


    int braceDepth = 0;
    String currentFunc = "Global";

    for (int i = 0; i < lines.length; i++) {
        String line = lines[i];
        int lineNo = i + 1;


        if (braceDepth == 0) {
            Matcher mf = pFunc.matcher(line);
            if (mf.find()) {
                String nombre = mf.group(2);
                currentFunc = nombre;

                int colInicio = firstNonWsCol1(line);


                update(idx, nombre, "Global", semantic.CategoriaSimbolo.METODO, lineNo, colInicio);
                update(idx, nombre, "Global", semantic.CategoriaSimbolo.FUNCION, lineNo, colInicio);


                int parenOpen = line.indexOf('(');
                if (parenOpen >= 0) {
                    String params = mf.group(3);
                    Matcher mp = pParam.matcher(params);
                    while (mp.find()) {
                        String paramName = mp.group(2);
                        int colParam = parenOpen + 1 + mp.start(2) + 1; 
                        update(idx, paramName, nombre, semantic.CategoriaSimbolo.PARAMETRO, lineNo, colParam);
                    }
                }
            }
        }


        Matcher mv = pVar.matcher(line);
        while (mv.find()) {
            String id = mv.group(1);
            int colVar = mv.start() + 1; // inicio del "var"
            String amb = (braceDepth == 0) ? "Global" : currentFunc;
            update(idx, id, amb, semantic.CategoriaSimbolo.VARIABLE, lineNo, colVar);
        }


Matcher mtv = pTypedVar.matcher(line);
if (mtv.find()) {
    String id = mtv.group(2);
    int colId = mtv.start(2) + 1; 
    String amb = (braceDepth == 0) ? "Global" : currentFunc;

    update(idx, id, amb, semantic.CategoriaSimbolo.VARIABLE, lineNo, colId);
}



        braceDepth += countChar(line, '{') - countChar(line, '}');
        if (braceDepth == 0) {
            currentFunc = "Global";
        }
    }
}

private static String keySim(String id, String ambito, semantic.CategoriaSimbolo cat) {
    String a = (ambito == null) ? "" : ambito.toLowerCase();
    String i = (id == null) ? "" : id.toLowerCase();
    return i + "|" + a + "|" + (cat == null ? "" : cat.name());
}

private static void update(Map<String, List<Simbolo>> idx,
                           String id,
                           String ambito,
                           semantic.CategoriaSimbolo cat,
                           int line,
                           int col) {
    String k = keySim(id, ambito, cat);
    List<Simbolo> lst = idx.get(k);
    if (lst == null || lst.isEmpty()) return;


    Simbolo s = lst.get(0);
    if (line > 0) s.setLinea(line);
    if (col > 0) s.setColumna(col);
}

private static int firstNonWsCol1(String line) {
    if (line == null) return 1;
    for (int i = 0; i < line.length(); i++) {
        if (!Character.isWhitespace(line.charAt(i))) return i + 1;
    }
    return 1;
}

private static int countChar(String s, char c) {
    if (s == null) return 0;
    int n = 0;
    for (int i = 0; i < s.length(); i++) {
        if (s.charAt(i) == c) n++;
    }
    return n;
}


    private static class PosDecl {
        final String idOriginal;
        final String idLower;
        final String tipo;
        final int linea;
        final int colId;
        final int colTipo;
        final int colRhs;
        final String rhsSnippet;

        PosDecl(String idOriginal, String tipo, int linea, int colId, int colTipo, int colRhs, String rhsSnippet) {
            this.idOriginal = idOriginal;
            this.idLower = idOriginal.toLowerCase();
            this.tipo = tipo;
            this.linea = linea;
            this.colId = colId;
            this.colTipo = colTipo;
            this.colRhs = colRhs;
            this.rhsSnippet = rhsSnippet;
        }
    }

    private static class OpOcc {
        final int linea;
        final int colOp;
        final String snippet;
        OpOcc(int linea, int colOp, String snippet) {
            this.linea = linea;
            this.colOp = colOp;
            this.snippet = snippet;
        }
    }

    private static class CastOcc {
        final int linea;
        final int colError;
        final String snippet;
        CastOcc(int linea, int colError, String snippet) {
            this.linea = linea;
            this.colError = colError;
            this.snippet = snippet;
        }
    }

    private static class LengthCall {
        final int linea;
        final int colArg;
        final String argumento;
        LengthCall(int linea, int colArg, String argumento) {
            this.linea = linea;
            this.colArg = colArg;
            this.argumento = argumento;
        }
    }

    private static class MappedSemantic {
        final String mensaje;
        final int linea;
        final int columna;

        MappedSemantic(String mensaje, int linea, int columna) {
            this.mensaje = mensaje;
            this.linea = linea;
            this.columna = columna;
        }
    }

    private static class SourceIndex {

        final Map<String, List<PosDecl>> decls = new HashMap<>();
        final Map<String, List<int[]>> assigns = new HashMap<>();
        final List<OpOcc> boolPlusOps = new ArrayList<>();
        final List<OpOcc> stringMinusOps = new ArrayList<>();
        final List<CastOcc> casts = new ArrayList<>();
        final List<LengthCall> lengthCalls = new ArrayList<>();

        private int idxBoolPlus = 0;
        private int idxStrMinus = 0;
        private int idxCasts = 0;

        static SourceIndex build(String codigoOriginal) {
            SourceIndex idx = new SourceIndex();

            String[] lines = codigoOriginal.split("\\r\\n|\\r|\\n", -1);

            Pattern pDecl = Pattern.compile("(?i)^\\s*var\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*:\\s*([a-zA-Z_][a-zA-Z0-9_]*)");
            Pattern pAssign = Pattern.compile("^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*=");

            Pattern pBoolPlus = Pattern.compile("(?i)\\b(true|false)\\b\\s*\\+\\s*([0-9]+(\\.[0-9]+)?)");
            Pattern pStrMinus = Pattern.compile("\"[^\"]*\"\\s*-\\s*\"[^\"]*\"");
            Pattern pLength = Pattern.compile("(?i)\\blength\\s*\\(\\s*([^\\)]*?)\\s*\\)");

            Pattern pCastBoolNum = Pattern.compile("(?i)\\(\\s*bool\\s*\\)\\s*([0-9]+(\\.[0-9]+)?)");

            for (int i = 0; i < lines.length; i++) {
                int lineNo = i + 1;
                String line = lines[i];

                Matcher md = pDecl.matcher(line);
                if (md.find()) {
                    String id = md.group(1);
                    String tipo = md.group(2);

                    int colId = md.start(1) + 1;
                    int colTipo = md.start(2) + 1;

                    int colRhs = 0;
                    String rhsSnippet = "";
                    int eq = line.indexOf('=');
                    if (eq >= 0) {
                        int k = eq + 1;
                        while (k < line.length() && Character.isWhitespace(line.charAt(k))) k++;
                        colRhs = (k < line.length()) ? (k + 1) : 0;

                        int semi = line.lastIndexOf(';');
                        if (semi < 0) semi = line.length();
                        rhsSnippet = line.substring(k, semi).trim();
                    }

                    PosDecl occ = new PosDecl(id, tipo, lineNo, colId, colTipo, colRhs, rhsSnippet);

                    idx.decls.computeIfAbsent(occ.idLower, _k -> new ArrayList<>()).add(occ);
                }

                Matcher ma = pAssign.matcher(line);
                if (ma.find() && !line.trim().toLowerCase().startsWith("var ")) {
                    String id = ma.group(1);
                    int col = ma.start(1) + 1;
                    idx.assigns.computeIfAbsent(id.toLowerCase(), _k -> new ArrayList<>()).add(new int[] { lineNo, col });
                }

                Matcher mb = pBoolPlus.matcher(line);
                if (mb.find()) {
                    int colPlus = line.indexOf('+', mb.start());
                    if (colPlus >= 0) {
                        idx.boolPlusOps.add(new OpOcc(lineNo, colPlus + 1, mb.group(0).trim()));
                    }
                }

                Matcher ms = pStrMinus.matcher(line);
                if (ms.find()) {
                    int colMinus = line.indexOf('-', ms.start());
                    if (colMinus >= 0) {
                        idx.stringMinusOps.add(new OpOcc(lineNo, colMinus + 1, ms.group(0).trim()));
                    }
                }

                Matcher ml = pLength.matcher(line);
                if (ml.find()) {
                    int idxParen = line.toLowerCase().indexOf("(", ml.start());
                    if (idxParen >= 0) {
                        int k = idxParen + 1;
                        while (k < line.length() && Character.isWhitespace(line.charAt(k))) k++;
                        int colArg = k + 1;
                        idx.lengthCalls.add(new LengthCall(lineNo, colArg, ml.group(1)));
                    }
                }

                Matcher mc = pCastBoolNum.matcher(line);
                if (mc.find()) {
                    int colError = 0;
                    Matcher md2 = pDecl.matcher(line);
                    if (md2.find()) {
                        colError = md2.start(2) + 1;
                    } else {
                        int colCastBool = line.toLowerCase().indexOf("bool", mc.start());
                        colError = (colCastBool >= 0) ? (colCastBool + 1) : 1;
                    }
                    idx.casts.add(new CastOcc(lineNo, colError, mc.group(0).trim()));
                }
            }

            return idx;
        }

        MappedSemantic mapearSemanticError(SemanticError e) {

            String msg = (e.getMensaje() == null) ? "" : e.getMensaje();
            String lower = msg.toLowerCase();

            if (lower.contains("ya declarada")) {
                String id = extraerPrimeraComillaSimple(msg);
                if (id != null) {
                    List<PosDecl> occs = decls.get(id.toLowerCase());
                    if (occs != null && occs.size() >= 2) {
                        PosDecl prev = occs.get(0);
                        PosDecl now = occs.get(1);
                        return new MappedSemantic(
                            "Redeclaración: '" + now.idOriginal + "' colisiona con '" + prev.idOriginal + "' (lenguaje insensitive)",
                            now.linea,
                            now.colId
                        );
                    }
                }
            }

            if (lower.contains("no declarada")) {
                String id = extraerPrimeraComillaSimple(msg);
                if (id != null) {
                    List<int[]> poss = assigns.get(id.toLowerCase());
                    if (poss != null && !poss.isEmpty()) {
                        int[] p = poss.get(0);
                        return new MappedSemantic(
                            "Variable no declarada: '" + id + "'",
                            p[0],
                            p[1]
                        );
                    }
                }
            }

            if (lower.contains("tipos incompatibles") && lower.contains("expresión aritmética")) {
                if (lower.contains("bool") && (lower.contains("suma") || lower.contains("+"))) {
                    OpOcc op = (idxBoolPlus < boolPlusOps.size()) ? boolPlusOps.get(idxBoolPlus++) : null;
                    if (op != null) {
                        return new MappedSemantic(
                            "Tipos incompatibles: 'bool + int' (` " + op.snippet + " `)",
                            op.linea,
                            op.colOp
                        );
                    }
                }

                if (lower.contains("string") && (lower.contains("resta") || lower.contains("-"))) {
                    OpOcc op = (idxStrMinus < stringMinusOps.size()) ? stringMinusOps.get(idxStrMinus++) : null;
                    if (op != null) {
                        return new MappedSemantic(
                            "Operación inválida: 'string - string' (` " + op.snippet + " `)",
                            op.linea,
                            op.colOp
                        );
                    }
                }
            }

            if (lower.contains("casteo no permitido") || lower.contains("casteo")) {
                CastOcc c = (idxCasts < casts.size()) ? casts.get(idxCasts++) : null;
                if (c != null) {
                    return new MappedSemantic(
                        "Casteo inválido: " + c.snippet + " (no permitido int -> bool)",
                        c.linea,
                        c.colError
                    );
                }
            }

            if (lower.contains("tipo incompatible en declaración")) {
                String id = extraerPrimeraComillaSimple(msg);
                if (id != null) {
                    List<PosDecl> occs = decls.get(id.toLowerCase());
                    if (occs != null && !occs.isEmpty()) {
                        PosDecl d = occs.get(0);
                        int col = (d.colRhs > 0) ? d.colRhs : d.colId;
                        String rhs = (d.rhsSnippet == null || d.rhsSnippet.isEmpty()) ? "..." : d.rhsSnippet;
                        return new MappedSemantic(
                            "Asignación incompatible: 'int = string' (` " + id + " = " + rhs + " `)",
                            d.linea,
                            col
                        );
                    }
                }
            }

            int ln = e.getLinea() > 0 ? e.getLinea() : 1;
            int col = e.getColumna() > 0 ? e.getColumna() : 1;

            return new MappedSemantic(msg, ln, col);
        }

        private String extraerPrimeraComillaSimple(String msg) {
            if (msg == null) return null;
            int a = msg.indexOf('\'');
            if (a < 0) return null;
            int b = msg.indexOf('\'', a + 1);
            if (b < 0) return null;
            return msg.substring(a + 1, b);
        }
    }

    private static void generarReporteAST(Programa programa, ConsolePanel console) {
    try {
        Path reportDir = Paths.get(System.getProperty("user.dir"), "reportes");
        Files.createDirectories(reportDir);

        Path dotPath = reportDir.resolve("ast.dot");
        Path pngPath = reportDir.resolve("ast.png");

        AstDotGenerator gen = new AstDotGenerator();
        String dot = gen.generate(programa);

        Files.write(dotPath, dot.getBytes(StandardCharsets.UTF_8));

        ultimoAstDotPath = dotPath;
        ultimoAstPngPath = null;
        ultimoAstError = null;

        // Ejecutar Graphviz (dot -Tpng ast.dot -o ast.png)
        ProcessBuilder pb = new ProcessBuilder(
                "dot",
                "-Tpng",
                dotPath.toAbsolutePath().toString(),
                "-o",
                pngPath.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);

        Process p = pb.start();
        String output = readAll(p.getInputStream());
        int exitCode = p.waitFor();

        if (exitCode == 0 && Files.exists(pngPath)) {
            ultimoAstPngPath = pngPath;

            if (console != null) {
                console.appendLine("[AST] Generado: " + pngPath.toAbsolutePath());
            }
        } else {
            ultimoAstError = (output == null || output.isEmpty())
                    ? ("Graphviz terminó con código " + exitCode)
                    : output;

            if (console != null) {
                console.appendLine("[AST] No se pudo generar PNG. " + ultimoAstError);
            }
        }

    } catch (IOException ex) {
        ultimoAstError = "No se encontró el comando 'dot' (Graphviz). Instálalo y asegúrate de que esté en PATH.\n" + ex.getMessage();
        if (console != null) {
            console.appendLine("[AST] " + ultimoAstError);
        }
    } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        ultimoAstError = "La generación del AST fue interrumpida.";
        if (console != null) {
            console.appendLine("[AST] " + ultimoAstError);
        }
    } catch (Exception ex) {
        ultimoAstError = "Error inesperado generando AST: " + ex.getMessage();
        if (console != null) {
            console.appendLine("[AST] " + ultimoAstError);
        }
    }
}

private static String readAll(InputStream in) throws IOException {
    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int n;
    while ((n = in.read(buffer)) != -1) {
        baos.write(buffer, 0, n);
    }
    return new String(baos.toByteArray(), StandardCharsets.UTF_8);
}

}
