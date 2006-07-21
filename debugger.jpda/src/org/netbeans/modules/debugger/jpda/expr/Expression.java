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

package org.netbeans.modules.debugger.jpda.expr;

import java.io.StringReader;
import java.util.Random;

/**
 * Represents an pre-parsed Java expression to be later evaluated in a specific JVM context.
 * The expression can have a 1.4 or 1.5 syntax.
 *
 * @author Maros Sandor
 */
public class Expression {

    public static final String LANGUAGE_JAVA_1_4 = JavaParser.LANGUAGE_JAVA_1_4;
    public static final String LANGUAGE_JAVA_1_5 = JavaParser.LANGUAGE_JAVA_1_5;
    
    private static final String REPLACE_return = "return01234";
    private static final String REPLACE_class = "class01234";

    private String       strExpression;
    private String       language;
    private SimpleNode   root;
    private String       replace_return;
    private String       replace_class;

    /**
     * Creates a new expression by pre-parsing the given String representation of the expression.
     *
     * @param expr textual representation of an expression
     * @param language one of the LANGUAGE_XXX constants
     * @return pre-parsed Java expression
     * @throws ParseException if the expression has wrong syntax
     */
    public static Expression parse (String expr, String language) 
    throws ParseException {
        String replace_return = REPLACE_return;
        while (expr.indexOf(replace_return) >= 0) {
            replace_return = "return" + new Random().nextLong();
        }
        String replace_class = REPLACE_class;
        while (expr.indexOf(replace_class) >= 0) {
            replace_class = "class" + new Random().nextLong();
        }
        String replacedExpr = expr.replace("return", replace_return);
        replacedExpr = replacedExpr.replaceAll("^class|[^\\.]class", replace_class);
        StringReader reader = new StringReader(replacedExpr);
        try {
            JavaParser parser = new JavaParser(reader);
            parser.setTargetJDK(language);
            SimpleNode root = parser.Expression();
            return new Expression(expr, language, root, replace_return, replace_class);
        } catch (Error e) {
            throw new ParseException(e.getMessage());
        } finally {
            reader.close();
        }
    }
    
    private Expression(String expression, String language, SimpleNode root,
                       String replace_return, String replace_class) {
        strExpression = expression;
        this.language = language;
        this.root = root;
        this.replace_return = replace_return;
        this.replace_class = replace_class;
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
    
    String returnReplaced() {
        return replace_return;
    }
    
    String classReplaced() {
        return replace_class;
    }
}
