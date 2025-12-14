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

public class Ejecutor {

    private static TablaErrores ultimaTablaErrores;
    private static TablaSimbolos ultimaTablaSimbolos;

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

        final String codigoOriginal = (codigo == null) ? "" : codigo;

        SourceIndex sourceIndex = SourceIndex.build(codigoOriginal);

        PreprocessResult prep = preprocesarParaParseo(codigoOriginal, tablaErrores);

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

            if (tablaErrores.tieneErrores()) {
                return;
            }

            ContextoEjecucion contexto = new ContextoEjecucion(console);
            VisitanteEvaluacion eval = new VisitanteEvaluacion(contexto);
            programa.accept(eval);


        } catch (Exception ex) {
            tablaErrores.agregarError(
                ErrorTipo.SINTACTICO,
                "Error fatal durante el análisis: " + ex.getMessage(),
                1,
                1
            );
            if (console != null) {
    console.appendLine("ERROR: " + ex.getMessage());
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
        List<String> out = new ArrayList<>(lines.length);

        Pattern pWrapper = Pattern.compile("^\\s*void\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(\\s*\\)\\s*\\{\\s*$", Pattern.CASE_INSENSITIVE);
        Pattern pStart = Pattern.compile("^\\s*start\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(\\s*\\)\\s*;\\s*$", Pattern.CASE_INSENSITIVE);

        boolean wrapperActivo = false;
        int braceDepth = 0;

        for (int i = 0; i < lines.length; i++) {
            int lineNo = i + 1;
            String line = lines[i];

            if (!wrapperActivo && pWrapper.matcher(line).matches()) {
                wrapperActivo = true;
                braceDepth = 1;
                out.add("");
                continue;
            }

            if (wrapperActivo) {
                int delta = countCharOutsideStrings(line, '{') - countCharOutsideStrings(line, '}');
                braceDepth += delta;
                if (braceDepth <= 0) {
                    wrapperActivo = false;
                    out.add("");
                    continue;
                }
            }

            if (pStart.matcher(line).matches()) {
                out.add("");
                continue;
            }

            String s = reemplazarInvalidosYReportar(line, lineNo, tablaErrores);

            s = repararMasPuntoYComa(s, lineNo, tablaErrores);

            s = repararPrintlnParentesis(s, lineNo, tablaErrores);

            s = reemplazarLengthPorCero(s);

            out.add(s);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < out.size(); i++) {
            sb.append(out.get(i));
            if (i < out.size() - 1) sb.append("\n");
        }
        return new PreprocessResult(sb.toString());
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
}
