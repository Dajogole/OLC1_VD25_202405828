package reports;

import ast.NodoAST;
import ast.Programa;
import ast.expresiones.*;
import ast.sentencias.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Genera un grafo DOT a partir del AST.
 * Implementación híbrida:
 *  - Etiquetas "bonitas" para nodos comunes.
 *  - Recorrido por reflexión para no depender de todos los nodos (útil para Fase 2).
 */
public class AstDotGenerator {

    private final IdentityHashMap<Object, String> ids = new IdentityHashMap<>();
    private final StringBuilder sb = new StringBuilder();
    private int counter = 0;

    public String generate(NodoAST root) {
        ids.clear();
        sb.setLength(0);
        counter = 0;

        sb.append("digraph AST {\n");
        sb.append("  rankdir=TB;\n");
        sb.append("  node [shape=box, fontname=\"Helvetica\", fontsize=10];\n");
        sb.append("  edge [fontname=\"Helvetica\", fontsize=9];\n");

        if (root != null) {
            walk(root);
        }

        sb.append("}\n");
        return sb.toString();
    }

    private void walk(Object obj) {
        if (obj == null) return;

        if (obj instanceof NodoAST) {
            NodoAST node = (NodoAST) obj;
            String parentId = idFor(node);
            defineNode(node, parentId);

            // Recorremos TODOS los campos (incluyendo superclases) y conectamos hijos que sean NodoAST o List<NodoAST>
            for (Field f : getAllFields(node.getClass())) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);

                Object value;
                try {
                    value = f.get(node);
                } catch (IllegalAccessException e) {
                    continue;
                }

                if (value instanceof NodoAST) {
                    NodoAST child = (NodoAST) value;
                    link(parentId, child, f.getName());
                    walk(child);
                } else if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    for (int i = 0; i < list.size(); i++) {
                        Object item = list.get(i);
                        if (item instanceof NodoAST) {
                            NodoAST child = (NodoAST) item;
                            link(parentId, child, f.getName() + "[" + i + "]");
                            walk(child);
                        }
                    }
                }
            }
        }
    }

    private void defineNode(NodoAST node, String id) {
        // Evitar definir 2 veces el mismo nodo
        if (sb.indexOf(id + " [") >= 0) return;

        String label = buildLabel(node);
        sb.append("  ").append(id)
                .append(" [label=\"").append(escape(label)).append("\"];\n");
    }

    private void link(String parentId, NodoAST child, String edgeLabel) {
        String childId = idFor(child);
        defineNode(child, childId);

        sb.append("  ").append(parentId).append(" -> ").append(childId);
        if (edgeLabel != null && !edgeLabel.isEmpty()) {
            sb.append(" [label=\"").append(escape(edgeLabel)).append("\"]");
        }
        sb.append(";\n");
    }

    private String idFor(Object o) {
        String id = ids.get(o);
        if (id == null) {
            id = "n" + counter++;
            ids.put(o, id);
        }
        return id;
    }

    private static Field[] getAllFields(Class<?> clazz) {
        java.util.ArrayList<Field> fields = new java.util.ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                fields.add(f);
            }
            current = current.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    private String buildLabel(NodoAST node) {
        String base = node.getClass().getSimpleName();

        // Etiquetas específicas para hacer el AST legible.
        // (Para nuevos nodos de Fase 2, el recorrido por reflexión igual funcionará,
        // aunque la etiqueta quedará como el nombre de clase.)
        if (node instanceof Identificador) {
            return base + "\\n" + ((Identificador) node).getNombre();
        }
        if (node instanceof LiteralEntero) {
            return base + "\\n" + ((LiteralEntero) node).getValor();
        }
        if (node instanceof LiteralDouble) {
            return base + "\\n" + ((LiteralDouble) node).getValor();
        }
        if (node instanceof LiteralBooleano) {
            return base + "\\n" + ((LiteralBooleano) node).getValor();
        }
        if (node instanceof LiteralChar) {
            return base + "\\n'" + ((LiteralChar) node).getValor() + "'";
        }
        if (node instanceof LiteralString) {
            return base + "\\n\"" + ((LiteralString) node).getValor() + "\"";
        }
        if (node instanceof ExpresionAritmetica) {
            return base + "\\n" + ((ExpresionAritmetica) node).getOperador();
        }
        if (node instanceof ExpresionRelacional) {
            return base + "\\n" + ((ExpresionRelacional) node).getOperador();
        }
        if (node instanceof ExpresionLogica) {
            return base + "\\n" + ((ExpresionLogica) node).getOperador();
        }
        if (node instanceof ExpresionCasteo) {
            return base + "\\n(" + ((ExpresionCasteo) node).getTipoDestino() + ")";
        }
        if (node instanceof DeclaracionVariable) {
            DeclaracionVariable d = (DeclaracionVariable) node;
            return base + "\\n" + d.getIdentificador() + " : " + d.getTipo();
        }
        if (node instanceof AsignacionVariable) {
            AsignacionVariable a = (AsignacionVariable) node;
            return base + "\\n" + a.getIdentificador();
        }
        if (node instanceof PrintlnSentencia) {
            return base + "\\nprintln";
        }
        if (node instanceof LlamadaFuncion) {
            return base + "\\n" + ((LlamadaFuncion) node).getNombre();
        }
        if (node instanceof Programa) {
            return base;
        }

        return base;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
    }
}
