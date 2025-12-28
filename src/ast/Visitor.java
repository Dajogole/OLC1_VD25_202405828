package ast;

import ast.expresiones.*;
import ast.sentencias.*;

public interface Visitor<T> {

    T visit(Programa programa);


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


    default T visit(LiteralVector expr) { return null; }
    default T visit(LiteralMatriz expr) { return null; }
    default T visit(AccesoIndexado expr) { return null; }
    default T visit(LlamadaMiembro expr) { return null; }
    default T visit(NuevaLista expr) { return null; }


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

   
    default T visit(DeclaracionFuncion stmt) { return null; }
    default T visit(ReturnSentencia stmt) { return null; }
    default T visit(StartSentencia stmt) { return null; }
    default T visit(SentenciaExpresion stmt) { return null; }
    default T visit(IncDecSentencia stmt) { return null; }
    default T visit(AsignacionIndexada stmt) { return null; }
}
