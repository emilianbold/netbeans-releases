/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import java.util.List;
import static com.sun.source.tree.Tree.*;

/**
 * This class demonstrates how to make trees. It gets the statement as a string,
 * string is get parsed and then parsed tree is reversed to code which can be
 * used in source code for tree/code creation.
 * 
 * @author Pavel Flaska
 */
public class TreeMakerDemo  {
    
    /* Prevent instantiation */
    private TreeMakerDemo() {
    }
    
    public static String reverse(Tree tree) {
        StringBuilder builder = new StringBuilder();
        reverse(tree, 0, builder);
        int length = builder.length();
        builder.replace(length-1, length, ";");
        return builder.toString();
    }
    
    private static void reverse(List<? extends Tree> list, int indent, StringBuilder builder, boolean comma) {
        int size = list.size();
        indent(indent, builder);
        switch (size) {
            case 0:
                builder.append("Collections.<typePar>emptyList(),");
                return; // finished, exit immediately, no ) is needed
            case 1:
                builder.append("Collections.<typePar>singletonList(");
                ++indent;
                reverse(list.iterator().next(), indent, builder);
                removeLastCharacter(builder);
                break;
            default:
                builder.append("new ArrayList<typePar>(");
                for (Tree t : list) {
                    indent(indent, builder);
                    reverse(t, ++indent, builder);
                    builder.append(',');
                }
                removeLastCharacter(builder);
                break;
        }
        indent(--indent, builder);
        builder.append(')');
        if (comma) builder.append(',');
    }

    private static void removeLastCharacter(StringBuilder builder) {
        builder.delete(builder.length()-1, builder.length());
    }
    
    private static void reverse(CharSequence identifier, int indent, StringBuilder builder) {
        indent(indent, builder);
        builder.append('"').append(identifier).append('"');
    }
    
    private static void reverse(Tree tree, int indent, StringBuilder builder) {
        reverse(tree, indent, builder, true);
    }
    
    private static void reverse(Tree tree, int indent, StringBuilder builder, boolean comma) {
        // null values are allowed
        if (tree == null) {
            indent(indent, builder);
            builder.append("null,");
            return; 
        }
        indent(indent, builder);
        switch (tree.getKind()) {
            case EXPRESSION_STATEMENT:
                builder.append("make.ExpressionStatement(");
                ExpressionStatementTree exprStatement = (ExpressionStatementTree) tree;
                reverse(exprStatement.getExpression(), ++indent, builder, false);
                break;
            case IDENTIFIER:
                builder.append("make.Identifier(\"");
                IdentifierTree identifier = (IdentifierTree) tree;
                builder.append(identifier.getName());
                builder.append("\")");
                if (comma) builder.append(',');
                return; // branch finished, return immediately
            case MEMBER_SELECT:
                builder.append("make.MemberSelect(");
                MemberSelectTree memberSelect = (MemberSelectTree) tree;
                ++indent;
                reverse(memberSelect.getExpression(), indent, builder);
                reverse(memberSelect.getIdentifier(), indent, builder);
                break;
            case METHOD_INVOCATION:
                builder.append("make.MethodInvocation(");
                MethodInvocationTree methodInvocation = (MethodInvocationTree) tree;
                ++indent;
                reverse(methodInvocation.getTypeArguments(), indent, builder, true);
                reverse(methodInvocation.getMethodSelect(), indent, builder);
                reverse(methodInvocation.getArguments(), indent, builder, false);
                break;
            case NEW_CLASS:
                builder.append("make.NewClass(");
                NewClassTree newClassTree = (NewClassTree) tree;
                ++indent;
                reverse(newClassTree.getEnclosingExpression(), indent, builder);
                reverse(newClassTree.getTypeArguments(), indent, builder, true);
                reverse(newClassTree.getIdentifier(), indent, builder);
                reverse(newClassTree.getArguments(), indent, builder, true);
                reverse(newClassTree.getClassBody(), indent, builder);
                break;
            case NULL_LITERAL:
            case INT_LITERAL:
            case LONG_LITERAL:
            case FLOAT_LITERAL:
            case DOUBLE_LITERAL:
            case BOOLEAN_LITERAL:
            case STRING_LITERAL:
            case CHAR_LITERAL:
                builder.append("make.Literal(");
                if (Kind.STRING_LITERAL == tree.getKind()) builder.append('"');
                if (Kind.CHAR_LITERAL == tree.getKind()) builder.append('\'');
                LiteralTree literalTree = (LiteralTree) tree;
                builder.append(literalTree.getValue());
                if (Kind.STRING_LITERAL == tree.getKind()) builder.append('"');
                if (Kind.CHAR_LITERAL == tree.getKind()) builder.append('\'');
                builder.append("),");
                return;
            default:
                builder.append("\"<");
                builder.append(tree.getKind());
                builder.append(" tree was not reversed!>\"");
                return;
        }
        indent(--indent, builder);
        builder.append(')');
        if (comma) builder.append(',');
    }
    
    static void indent(int i, StringBuilder builder) {
        builder.append('\n');
        for (int j = 0; j < i; j++)
            builder.append("    ");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
