package utils;

public class StringUtils {
    
    private StringUtils() {

    }
    
    public static String normalizeIdentifier(String identifier) {
        return identifier != null ? identifier.toLowerCase() : null;
    }
    
    public static String escapeString(String input) {
        if (input == null) return null;
        
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\\': sb.append("\\\\"); break;
                case '\"': sb.append("\\\""); break;
                case '\'': sb.append("\\'"); break;
                default: sb.append(c); break;
            }
        }
        return sb.toString();
    }
    
    public static String unescapeString(String input) {
        if (input == null) return null;
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < input.length()) {
                i++;
                char next = input.charAt(i);
                switch (next) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case '\\': sb.append('\\'); break;
                    case '\"': sb.append('\"'); break;
                    case '\'': sb.append('\''); break;
                    default: sb.append(next); break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}