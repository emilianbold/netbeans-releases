/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.util.List;

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.AbstractGeneratorFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.codegen.base.BaseCaseGenerator;
import org.netbeans.modules.sql.framework.model.SQLCaseOperator;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SQLWhen;

import com.sun.etl.exception.BaseException;

/**
 * Velocity-based Generator that generates SQL Case expressions with Oracle 8i-specific
 * syntax quirks.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class Oracle8CaseGenerator extends BaseCaseGenerator {

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        if (obj == null || getObjectType(obj) != SQLConstants.CASE) {
            throw new BaseException("Bad SQLObject type - case expected.");
        }

        SQLCaseOperator operator = (SQLCaseOperator) obj;
        List whenObjects = operator.getWhenList();
        SQLObject defaultAction = operator.getSQLObject(SQLCaseOperator.DEFAULT);

        StringBuilder whenClause = new StringBuilder(100);
        final int count = operator.getWhenCount();
        for (int i = 0; i < count; i++) {
            SQLWhen when = (SQLWhen) whenObjects.get(i);
            SQLObject wcondition = when.getSQLObject(SQLWhen.CONDITION);
            if (wcondition == null) {
                throw new BaseException("Missing condition in when clause of case object.");
            }

            whenClause.append(genWhenExpression(when, (i == count - 1), defaultAction, context));
        }

        VelocityContext vContext = new VelocityContext();
        vContext.put("whenClause", whenClause.toString());

        String nestedIndent = (String) context.getClientProperty("nestedIndent");
        vContext.put("nestedIndent", ((nestedIndent != null) ? nestedIndent : "    "));
        return TemplateBuilder.generateSql(this.getDB().getTemplateFileName("caseWhen"), vContext);
    }

    private String genWhenExpression(SQLWhen when, boolean isLast, SQLObject def, StatementContext context) throws BaseException {
        String op = ((SQLPredicate) when.getSQLObject(SQLWhen.CONDITION)).getOperatorType().trim();

        SQLObject left = ((SQLPredicate) when.getSQLObject(SQLWhen.CONDITION)).getSQLObject(SQLPredicate.LEFT);

        SQLObject right = ((SQLPredicate) when.getSQLObject(SQLWhen.CONDITION)).getSQLObject(SQLPredicate.RIGHT);

        SQLObject then = when.getSQLObject(SQLWhen.RETURN);

        final AbstractGeneratorFactory genFactory = getDB().getGeneratorFactory();

        String result = "DECODE(SIGN(" + genFactory.generate(left, context);
        result += " - ";
        result += genFactory.generate(right, context) + "), ";

        if (op.equals("=")) {
            result += " 0, " + genFactory.generate(then, context) + ", ";
            if (isLast) {
                result += genFactory.generate(def, context) + ")";
            }
            return (result);
        }

        if (op.equals(">")) {
            result += " 1, " + genFactory.generate(then, context) + ", ";
            if (isLast) {
                result += genFactory.generate(def, context) + ")";
            }
            return (result);
        }

        if (op.equals("<")) {
            result += " -1, " + genFactory.generate(then, context) + ", ";
            if (isLast) {
                result += genFactory.generate(def, context) + ")";
            }
            return (result);
        }

        if (op.equals(">=")) {
            result += " 0, " + genFactory.generate(then, context) + ", ";
            result += " 1, " + genFactory.generate(then, context) + ", ";
            if (isLast) {
                result += genFactory.generate(def, context) + ")";
            }
            return (result);
        }

        if (op.equals("=<")) {
            result += " 0, " + genFactory.generate(then, context) + ", ";
            result += " -1, " + genFactory.generate(then, context) + ", ";
            if (isLast) {
                result += genFactory.generate(def, context) + ")";
            }
            return (result);
        }

        if (op.equals("<>") || op.equals("!=")) {
            result += " -1, " + genFactory.generate(then, context) + ", ";
            result += " 1, " + genFactory.generate(then, context) + ", ";
            if (isLast) {
                result += genFactory.generate(def, context) + ")";
            }
            return (result);
        }

        throw new BaseException("Unknown operator in when predicate.");
    }
}
