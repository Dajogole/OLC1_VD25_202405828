package lexer;

import java_cup.runtime.*;
import parser.sym;
import reports.TablaErrores;
import reports.ErrorTipo;

%%

%public
%class Lexer
%cup
%unicode
%line
%column
%ignorecase

%{
    // ---- Soporte de errores léxicos persistente (NO se pierde al regenerar) ----
    private TablaErrores tablaErrores;

    public void setTablaErrores(TablaErrores tablaErrores) {
        this.tablaErrores = tablaErrores;
    }

    private void agregarErrorLexico(String caracter, int linea, int columna) {
        String desc = String.format("Símbolo no reconocido: '%s'", caracter);
        if (tablaErrores != null) {
            tablaErrores.agregarError(ErrorTipo.LEXICO, desc, linea, columna);
        } else {
            System.err.println("Error léxico: " + desc + " en línea " + linea + ", columna " + columna);
        }
    }

    private void agregarErrorLexicoDesc(String desc, int linea, int columna) {
        if (tablaErrores != null) {
            tablaErrores.agregarError(ErrorTipo.LEXICO, desc, linea, columna);
        } else {
            System.err.println("Error léxico: " + desc + " en línea " + linea + ", columna " + columna);
        }
    }

    private Symbol symbol(int type, int line, int column) {
        return new Symbol(type, line, column);
    }

    private Symbol symbol(int type, Object value, int line, int column) {
        return new Symbol(type, line, column, value);
    }

    private Symbol symbol(int type) {
        return symbol(type, yyline + 1, yycolumn + 1);
    }

    private Symbol symbol(int type, Object value) {
        return symbol(type, value, yyline + 1, yycolumn + 1);
    }

    // Helper method to unescape strings
    private String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
                if (i < s.length()) {
                    char next = s.charAt(i);
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
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]

/* Comments */
LineComment  = "//" [^\r\n]*
BlockComment = "/*" ~"*/"
Comment      = {LineComment} | {BlockComment}

/* Identifiers */
Identifier = [a-zA-Z_][a-zA-Z0-9_]*

/* Literals */
IntegerLiteral  = 0 | [1-9][0-9]*
DoubleLiteral   = {IntegerLiteral} \. [0-9]+
BooleanLiteral  = "true" | "false"

/* Escape sequences */
EscapeSequence = \\[nrt\\\"\']

/* Character and String literals */
CharLiteral   = \' ([^\'\\] | {EscapeSequence}) \'
StringLiteral = \" ([^\"\\] | {EscapeSequence})* \"

%%

<YYINITIAL> {
    /* Keywords */
    "var"                       { return symbol(sym.VAR); }
    "void"                      { return symbol(sym.VOID); }
    "return"                    { return symbol(sym.RETURN); }
    "start"                     { return symbol(sym.START); }

    "list"                      { return symbol(sym.LIST); }
    "new"                       { return symbol(sym.NEW); }
    "append"                    { return symbol(sym.APPEND); }
    "remove"                    { return symbol(sym.REMOVE); }

    /* Native functions */
    "round"                     { return symbol(sym.ROUND); }
    "length"                    { return symbol(sym.LENGTH); }
    "tostring"                  { return symbol(sym.TOSTRING); }
    "find"                      { return symbol(sym.FIND); }
    "start_with"                { return symbol(sym.START_WITH); }

    "if"                        { return symbol(sym.IF); }
    "else"                      { return symbol(sym.ELSE); }
    "switch"                    { return symbol(sym.SWITCH); }
    "case"                      { return symbol(sym.CASE); }
    "default"                   { return symbol(sym.DEFAULT); }
    "while"                     { return symbol(sym.WHILE); }
    "for"                       { return symbol(sym.FOR); }
    "do"                        { return symbol(sym.DO); }
    "break"                     { return symbol(sym.BREAK); }
    "continue"                  { return symbol(sym.CONTINUE); }
    "println"                   { return symbol(sym.PRINTLN); }
    "print"                     { return symbol(sym.PRINTLN); }

    /* Types */
    "int"                       { return symbol(sym.INT); }
    "double"                    { return symbol(sym.DOUBLE); }
    "bool"                      { return symbol(sym.BOOL); }
    "char"                      { return symbol(sym.CHAR); }
    "string"                    { return symbol(sym.STRING); }

    /* Boolean literals */
    {BooleanLiteral}            { return symbol(sym.BOOLEAN_LITERAL, Boolean.valueOf(yytext().equalsIgnoreCase("true"))); }

    /* Identifiers */
    {Identifier}                { return symbol(sym.IDENTIFIER, yytext()); }

    /* Numeric literals */
    {IntegerLiteral}            { return symbol(sym.INTEGER_LITERAL, Integer.parseInt(yytext())); }
    {DoubleLiteral}             { return symbol(sym.DOUBLE_LITERAL, Double.parseDouble(yytext())); }

    /* Character and String literals */
    {CharLiteral}               {
                                    String text = yytext();
                                    text = text.substring(1, text.length()-1);
                                    text = unescape(text);
                                    if (text.length() == 1) {
                                        return symbol(sym.CHAR_LITERAL, text.charAt(0));
                                    } else {
                                        // Esto en práctica no debería ocurrir con esta regex, pero lo dejamos por robustez
                                        agregarErrorLexicoDesc("Char literal con más de un carácter", yyline+1, yycolumn+1);
                                        return symbol(sym.error);
                                    }
                                }

    {StringLiteral}             {
                                    String text = yytext();
                                    text = text.substring(1, text.length()-1);
                                    text = unescape(text);
                                    return symbol(sym.STRING_LITERAL, text);
                                }

    /* Operators */
    "++"                        { return symbol(sym.INCREMENT); }
    "--"                        { return symbol(sym.DECREMENT); }

    "["                         { return symbol(sym.LBRACKET); }
    "]"                         { return symbol(sym.RBRACKET); }

    "."                         { return symbol(sym.DOT); }

    "**"                        { return symbol(sym.POW); }
    "+"                         { return symbol(sym.PLUS); }
    "-"                         { return symbol(sym.MINUS); }
    "*"                         { return symbol(sym.MULTIPLY); }
    "/"                         { return symbol(sym.DIVIDE); }
    "%"                         { return symbol(sym.MODULO); }

    /* Assignment and comparison */
    "="                         { return symbol(sym.ASSIGN); }
    "=="                        { return symbol(sym.EQ); }
    "!="                        { return symbol(sym.NEQ); }
    "<"                         { return symbol(sym.LT); }
    "<="                        { return symbol(sym.LE); }
    ">"                         { return symbol(sym.GT); }
    ">="                        { return symbol(sym.GE); }

    /* Logical operators */
    "&&"                        { return symbol(sym.AND); }
    "||"                        { return symbol(sym.OR); }
    "^"                         { return symbol(sym.XOR); }
    "!"                         { return symbol(sym.NOT); }

    /* Grouping and punctuation */
    "("                         { return symbol(sym.LPAREN); }
    ")"                         { return symbol(sym.RPAREN); }
    "{"                         { return symbol(sym.LBRACE); }
    "}"                         { return symbol(sym.RBRACE); }
    ";"                         { return symbol(sym.SEMICOLON); }
    ":"                         { return symbol(sym.COLON); }
    ","                         { return symbol(sym.COMMA); }

    /* Comments and whitespace */
    {Comment}                   { /* ignore */ }
    {WhiteSpace}                { /* ignore */ }

    /* Error for any other character */
    [^]                         {
                                    agregarErrorLexico(yytext(), yyline+1, yycolumn+1);
                                    return symbol(sym.error);
                                }
}

<<EOF>>                         { return symbol(sym.EOF); }
