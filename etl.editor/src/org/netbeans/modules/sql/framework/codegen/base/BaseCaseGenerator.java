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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.AbstractGenerator;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.model.SQLCaseOperator;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLWhen;
import com.sun.etl.exception.BaseException;

/**
 * Velocity-based generator that generates SQL Case expressions.
 *
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class BaseCaseGenerator extends AbstractGenerator {

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        if (obj == null || getObjectType(obj) != SQLConstants.CASE) {
            throw new BaseException("Bad SQLObject type - case expected.");
        }

        SQLCaseOperator operator = (SQLCaseOperator) obj;
        VelocityContext vContext = new VelocityContext();

        vContext.put("switch", buildSwitchClause(operator, context));
        vContext.put("whenClauses", buildWhenClauses(operator, context));
        vContext.put("default", buildDefaultClause(operator, context));

        String nestedIndent = (String) context.getClientProperty("nestedIndent");
        vContext.put("nestedIndent", (nestedIndent != null) ? nestedIndent : "    ");
        return TemplateBuilder.generateSql(this.getDB().getTemplateFileName("caseWhen"), vContext);
    }

    /**
     * Builds appropriate switch clause, if applicable, from the given SQLCaseOperator
     * instance. DB-specific extensions should override this method to reflect
     * vendor-specific syntax quirks.
     *
     * @param operator SQLCaseOperator to be interrogated
     * @param context StatementContext to use in evaluating switch clause
     * @return String representing switch clause, possibly the empty string (&quot;&quot;)
     * @throws BaseException if error occurs during evaluation
     */
    protected String buildSwitchClause(SQLCaseOperator operator, StatementContext context) throws BaseException {
        String switchClause = "";

        SQLObject condition = operator.getSQLObject(SQLCaseOperator.SWITCH);
        boolean containsPredicate = condition != null && getObjectType(condition) == SQLConstants.PREDICATE;
        if (containsPredicate) {
            switchClause = this.getDB().getGeneratorFactory().generate(condition, context);
        }

        return switchClause;
    }

    /**
     * Builds List of mapped when-then clauses to be inserted into the Velocity template
     * context. DB-specific extensions should override this method to reflect
     * vendor-specific syntax quirks.
     *
     * @param operator SQLCaseOperator to be interrogated
     * @param context StatementContext to use in evaluating individual when clauses
     * @return List of Maps containing "when" and "then" clauses
     * @throws BaseException if error occurs during evaluation
     */
    protected List buildWhenClauses(SQLCaseOperator operator, StatementContext context) throws BaseException {
        SQLObject condition = operator.getSQLObject(SQLCaseOperator.SWITCH);
        boolean containsPredicate = condition != null && getObjectType(condition) == SQLConstants.PREDICATE;

        List<SQLWhen> whenObjects = operator.getWhenList();
        List<Map<String, String>> whenClauses = new ArrayList<Map<String, String>>(whenObjects.size());

        for (int i = 0; i < operator.getWhenCount(); i++) {
            SQLWhen when = whenObjects.get(i);
            SQLCondition wcondition = when.getCondition();
            if (wcondition == null || containsPredicate) {
                throw new BaseException("Badly formed case object.");
            }

            Map<String, String> whenThen = new HashMap<String, String>(1);
            whenThen.put("when", this.getDB().getGeneratorFactory().generate(wcondition.getRootPredicate(), context));
            whenThen.put("then", this.getDB().getGeneratorFactory().generate(when.getSQLObject(SQLWhen.RETURN), context));

            whenClauses.add(whenThen);
        }

        return whenClauses;
    }

    /**
     * Builds appropriate default clause, if applicable, from the given SQLCaseOperator
     * instance. DB-specific extensions should override this method to reflect
     * vendor-specific syntax quirks.
     *
     * @param operator SQLCaseOperator to be interrogated to obtain appropriate default
     *        clause
     * @param context StatementContext to use in evaluating default clause
     */
    protected String buildDefaultClause(SQLCaseOperator operator, StatementContext context) throws BaseException {
        String defaultClause = "";

        SQLObject defaultAction = operator.getSQLObject(SQLCaseOperator.DEFAULT);
        if (defaultAction != null) {
            defaultClause = this.getDB().getGeneratorFactory().generate(defaultAction, context);
        }

        return defaultClause;
    }

    /**
     * Resolve Object type if required.
     *
     * @param object SQLObject
     * @return int value of SQLObject type
     */
    protected final int getObjectType(SQLObject object) {
        int type;
        switch (object.getObjectType()) {
            case SQLConstants.VISIBLE_PREDICATE:
                type = SQLConstants.PREDICATE;
                break;
            case SQLConstants.VISIBLE_LITERAL:
                type = SQLConstants.LITERAL;
                break;
            default:
                type = object.getObjectType();
        }

        return type;
    }
}