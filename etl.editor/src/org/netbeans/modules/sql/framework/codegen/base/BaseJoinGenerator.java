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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.codegen.base;

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.AbstractGenerator;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLObject;
import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class BaseJoinGenerator extends AbstractGenerator {

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        SQLJoinOperator join = (SQLJoinOperator) obj;

        SQLObject left = join.getSQLObject(SQLJoinOperator.LEFT);
        SQLObject right = join.getSQLObject(SQLJoinOperator.RIGHT);
        SQLCondition joinCondition = join.getJoinCondition();
        SQLObject predicate = joinCondition.getRootPredicate();

        if (left == null || right == null) {
            throw new BaseException("Bad SQLJoin object. No left and right input.");
        }

        if (left.getObjectType() == SQLConstants.JOIN_TABLE) {
            left = ((SQLJoinTable) left).getSourceTable();
        }

        if (right.getObjectType() == SQLConstants.JOIN_TABLE) {
            right = ((SQLJoinTable) right).getSourceTable();
        }

        String leftObj = this.getGeneratorFactory().generate(left, context);
        String rightObj = this.getGeneratorFactory().generate(right, context);

        String condition = "";
        String joinType = "";

        if (predicate != null) {
            condition = this.getGeneratorFactory().generate(predicate, context);
            joinType = getJoinType(join);
        }

        VelocityContext vContext = new VelocityContext();
        vContext.put("leftObj", leftObj);
        vContext.put("rightObj", rightObj);
        vContext.put("condition", condition);
        vContext.put("joinType", joinType);

        return TemplateBuilder.generateSql(this.getDB().getTemplateFileName("join"), vContext);
    }

    private String getJoinType(SQLJoinOperator join) {
        String result = "";
        switch (join.getJoinType()) {
            case SQLConstants.LEFT_OUTER_JOIN:
                result = "LEFT OUTER JOIN";
                break;
            case SQLConstants.RIGHT_OUTER_JOIN:
                result = "RIGHT OUTER JOIN";
                break;
            case SQLConstants.FULL_OUTER_JOIN:
                result = "FULL OUTER JOIN";
                break;
            case SQLConstants.INNER_JOIN:
            default:
                result = "INNER JOIN";
                break;
        }
        return result;
    }
}