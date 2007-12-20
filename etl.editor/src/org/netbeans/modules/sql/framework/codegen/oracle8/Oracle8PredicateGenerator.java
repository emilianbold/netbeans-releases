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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.sql.framework.codegen.oracle8;

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.AbstractGenerator;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.impl.VisibleSQLPredicateImpl;

import com.sun.sql.framework.exception.BaseException;

/**
 * Overrides parent implementation to handle construction of Oracle 8i-specific join
 * condition syntax.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class Oracle8PredicateGenerator extends AbstractGenerator {

    private static final String JOIN_SYMBOL = "(+)";

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        SQLPredicate predicate = (SQLPredicate) obj;
        boolean showParenthesis = predicate.isShowParenthesis();

        SQLObject leftObj = predicate.getSQLObject(SQLPredicate.LEFT);
        SQLObject rightObj = predicate.getSQLObject(SQLPredicate.RIGHT);
        String leftExpression = "";
        String rightExpression = "";
        String operator = predicate.getOperatorType();
        
        if (!(predicate instanceof VisibleSQLPredicateImpl.RightUnary)) {
        	leftExpression = this.getDB().getGeneratorFactory().generate(leftObj, context);
        }

        if (predicate instanceof VisibleSQLPredicateImpl.LeftUnary) {
            if ((operator != null) && (operator.equalsIgnoreCase("is") || operator.equalsIgnoreCase("is not"))) {
                rightExpression = "NULL";
            }
        } else {
            rightExpression = this.getDB().getGeneratorFactory().generate(rightObj, context);
        }

        boolean leftObjIsColumn = (leftObj instanceof ColumnRef);
        boolean rightObjIsColumn = (rightObj instanceof ColumnRef);

        SQLJoinOperator join = (SQLJoinOperator) context.getClientProperty(StatementContext.JOIN_OPERATOR);
        if (join != null) {
            switch (join.getJoinType()) {
                case SQLConstants.LEFT_OUTER_JOIN:
                    if (leftObjIsColumn) {
                        rightExpression += JOIN_SYMBOL;
                    }
                    break;

                case SQLConstants.RIGHT_OUTER_JOIN:
                    if (rightObjIsColumn) {
                        leftExpression += JOIN_SYMBOL;
                    }
                    break;

                case SQLConstants.FULL_OUTER_JOIN:
                    throw new BaseException("Not yet implemented.");

                case SQLConstants.INNER_JOIN:
                default:
                    // Do nothing.
                    break;
            }
        }

        VelocityContext vContext = new VelocityContext();
        vContext.put("leftExpression", leftExpression);
        vContext.put("rightExpression", rightExpression);
        vContext.put("operator", operator);
        vContext.put("showParenthesis", new Boolean(showParenthesis));

        return TemplateBuilder.generateSql(this.getDB().getTemplateFileName("predicate"), vContext);
    }
}
