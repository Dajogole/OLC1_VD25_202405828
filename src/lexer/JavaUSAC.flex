package lexer;

import java_cup.runtime.*;
import parser.sym;

%%

%public
%class Lexer
%cup
%unicode
%line
%column
%ignorecase

%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline+1, yycolumn+1);
    }
    
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline+1, yycolumn+1, value);
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
WhiteSpace = {LineTerminator} | [ \t\f]

/* Comments */
LineComment = "//" [^\r\n]*
BlockComment = "/*" ~"*/"
Comment = {LineComment} | {BlockComment}

/* Identifiers */
Identifier = [a-zA-Z_][a-zA-Z0-9_]*

/* Literals */
IntegerLiteral = 0 | [1-9][0-9]*
DoubleLiteral = {IntegerLiteral} \. [0-9]+
BooleanLiteral = "true" | "false"

/* Escape sequences */
EscapeSequence = \\[nrt\\\"\']

/* Character and String literals */
CharLiteral = \' ([^\'\\] | {EscapeSequence}) \'
StringLiteral = \" ([^\"\\] | {EscapeSequence})* \"

%%

<YYINITIAL> {
    /* Keywords */
    "var"                       { return symbol(sym.VAR); }
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
                                        // Error: char with more than one character
                                        System.err.println("Error léxico: char literal con más de un carácter en línea " + (yyline+1) + ", columna " + (yycolumn+1));
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
                                    System.err.println("Error léxico: carácter no reconocido '" + yytext() + "' en línea " + (yyline+1) + ", columna " + (yycolumn+1));
                                    return symbol(sym.error);
                                }
}

<<EOF>>                         { return symbol(sym.EOF); }

