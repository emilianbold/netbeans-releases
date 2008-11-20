/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    public Expression(String expr, String language) {
        this.strExpression = expr;
        this.language = language;
    }

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
            replace_return = "return" + new Random().nextLong(); // NOI18N
        }
        String replace_class = REPLACE_class;
        while (expr.indexOf(replace_class) >= 0) {
            replace_class = "class" + new Random().nextLong(); // NOI18N
        }
        String replacedExpr = replaceSpecialVar(expr, "return", replace_return); // NOI18N
        replacedExpr = replaceSpecialVar(replacedExpr, "class", replace_class); // NOI18N
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

    private static String replaceSpecialVar(String expr, String var, String replace_var) {
        int i = expr.indexOf(var);
        while (i >= 0) {
            boolean replace;
            if (i > 0) {
                char ch = expr.charAt(i - 1);
                if (Character.isJavaIdentifierStart(ch) ||
                    Character.isJavaIdentifierPart(ch) ||
                    ch == '.') {
                    replace = false;
                } else {
                    replace = true;
                }
            } else {
                replace = true;
            }
            if (replace && i < (expr.length() - var.length())) {
                char ch = expr.charAt(i + var.length());
                if (Character.isJavaIdentifierPart(ch)) {
                    replace = false;
                }
            }
            if (replace) {
                expr = expr.substring(0, i) + replace_var + expr.substring(i + var.length());
                i += replace_var.length();
            } else {
                i += var.length();
            }
            i = expr.indexOf(var, i);
        }
        return expr;
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
