/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.expr;

import java.io.StringReader;

/**
 * Represents an pre-parsed Java expression to be later evaluated in a specific JVM context.
 * The expression can have a 1.4 or 1.5 syntax.
 *
 * @author Maros Sandor
 */
public class Expression {

    public static final String LANGUAGE_JAVA_1_4 = JavaParser.LANGUAGE_JAVA_1_4;
    public static final String LANGUAGE_JAVA_1_5 = JavaParser.LANGUAGE_JAVA_1_5;

    private String       strExpression;
    private String       language;
    private SimpleNode   root;

    /**
     * Creates a new expression by pre-parsing the given String representation of the expression.
     *
     * @param expr textual representation of an expression
     * @param language one of the LANGUAGE_XXX constants
     * @return pre-parsed Java expression
     * @throws ParseException if the expression has wrong syntax
     */
    public static Expression parse(String expr, String language) throws ParseException {
        StringReader reader = new StringReader(expr);
        try {
            JavaParser parser = new JavaParser(reader);
            parser.setTargetJDK(language);
            SimpleNode root = parser.Expression();
            return new Expression(expr, language, root);
        } catch (Error e) {
            throw new ParseException(e.getMessage());
        } finally {
            reader.close();
        }
    }

    private Expression(String expression, String language, SimpleNode root) {
        strExpression = expression;
        this.language = language;
        this.root = root;
    }

    /**
     * Creates an evaluator engine that can be used to evaluate this expression in a given
     * runtime JVM context.
     *
     * @param context a runtime JVM context
     * @return the evaluator engine
     */
    public Evaluator evaluator(EvaluationContext context) {
        return new Evaluator(this, context);
    }

    SimpleNode getRoot() {
        return root;
    }

    public String getLanguage() {
        return language;
    }

    public String getExpression() {
        return strExpression;
    }
}
