package ast;

import ast.expresiones.*;
import ast.sentencias.*;

public interface Visitor<T> {
    // Programa
    T visit(Programa programa);
    
    // Expresiones
    T visit(LiteralEntero expr);
    T visit(LiteralDouble expr);
    T visit(LiteralBooleano expr);
    T visit(LiteralChar expr);
    T visit(LiteralString expr);
    T visit(Identificador expr);
    T visit(ExpresionAritmetica expr);
    T visit(ExpresionRelacional expr);
    T visit(ExpresionLogica expr);
    T visit(ExpresionCasteo expr);
    T visit(ExpresionAgrupada expr);
    T visit(LlamadaFuncion expr);

    
    // Sentencias
    T visit(BloqueSentencias stmt);
    T visit(DeclaracionVariable stmt);
    T visit(AsignacionVariable stmt);
    T visit(IfSentencia stmt);
    T visit(SwitchSentencia stmt);
    T visit(Case stmt);
    T visit(Default stmt);
    T visit(WhileSentencia stmt);
    T visit(ForSentencia stmt);
    T visit(DoWhileSentencia stmt);
    T visit(BreakSentencia stmt);
    T visit(ContinueSentencia stmt);
    T visit(PrintlnSentencia stmt);
}