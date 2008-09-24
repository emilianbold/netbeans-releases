/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a for statement
 * <pre>e.g.<pre>
 * for (expr1; expr2; expr3)
 * 	 statement;
 * 
 * for (expr1; expr2; expr3):
 * 	 statement
 * 	 ...
 * endfor;
 */
public class ForStatement extends Statement {

    private final ArrayList<Expression> initializers = new ArrayList<Expression>();
    private final ArrayList<Expression> conditions = new ArrayList<Expression>();
    private final ArrayList<Expression> updaters = new ArrayList<Expression>();
    private Statement body;

    private ForStatement(int start, int end, Expression[] initializations, Expression[] conditions, Expression[] increasements, Statement action) {
        super(start, end);

        if (initializations == null || conditions == null || increasements == null || action == null) {
            throw new IllegalArgumentException();
        }
        for (Expression init : initializations) {
            this.initializers.add(init);
        }
        for (Expression cond : conditions) {
            this.conditions.add(cond);
        }
        for (Expression inc : increasements) {
            this.updaters.add(inc);
        }
        this.body = action;
    }

    public ForStatement(int start, int end, List<Expression> initializations, List<Expression> conditions, List<Expression> increasements, Statement action) {
        this(start, end,
                initializations == null ? null : (Expression[]) initializations.toArray(new Expression[initializations.size()]),
                conditions == null ? null : (Expression[]) conditions.toArray(new Expression[conditions.size()]),
                increasements == null ? null : (Expression[]) increasements.toArray(new Expression[increasements.size()]),
                action);
    }

    /**
     * Returns the live ordered list of initializer expressions in this for
     * statement.
     * <p>
     * The list should consist of either a list of so called statement 
     * expressions (JLS2, 14.8), or a single <code>VariableDeclarationExpression</code>. 
     * Otherwise, the for statement would have no Java source equivalent.
     * </p>
     * 
     * @return the live list of initializer expressions 
     *    (element type: <code>Expression</code>)
     */
    public List<Expression> getInitializers() {
        return this.initializers;
    }

    /**
     * Returns the condition expression of this for statement, or 
     * <code>null</code> if there is none.
     * 
     * @return the condition expression node, or <code>null</code> if 
     *     there is none
     */
    public List<Expression> getConditions() {
        return this.conditions;
    }

    /**
     * Returns the live ordered list of update expressions in this for
     * statement.
     * <p>
     * The list should consist of so called statement expressions. Otherwise,
     * the for statement would have no Java source equivalent.
     * </p>
     * 
     * @return the live list of update expressions 
     *    (element type: <code>Expression</code>)
     */
    public List<Expression> getUpdaters() {
        return this.updaters;
    }

    /**
     * Returns the body of this for statement.
     * 
     * @return the body statement node
     */
    public Statement getBody() {
        return this.body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
