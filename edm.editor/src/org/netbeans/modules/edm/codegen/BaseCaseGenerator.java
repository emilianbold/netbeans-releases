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
package org.netbeans.modules.edm.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.netbeans.modules.edm.model.SQLCaseOperator;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLWhen;
import org.netbeans.modules.edm.model.EDMException;
import org.openide.util.NbBundle;

/**
 * Velocity-based generator that generates SQL Case expressions.
 *
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 */
public class BaseCaseGenerator extends AbstractGenerator {

    public String generate(SQLObject obj, StatementContext context) throws EDMException {
        if (obj == null || getObjectType(obj) != SQLConstants.CASE) {
            throw new EDMException(NbBundle.getMessage(BaseCaseGenerator.class, "MSG_Bad_SQLObject_type"));
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

    protected String buildSwitchClause(SQLCaseOperator operator, StatementContext context) throws EDMException {
        String switchClause = "";

        SQLObject condition = operator.getSQLObject(SQLCaseOperator.SWITCH);
        boolean containsPredicate = condition != null && getObjectType(condition) == SQLConstants.PREDICATE;
        if (containsPredicate) {
            switchClause = this.getDB().getGeneratorFactory().generate(condition, context);
        }

        return switchClause;
    }

    protected List buildWhenClauses(SQLCaseOperator operator, StatementContext context) throws EDMException {
        SQLObject condition = operator.getSQLObject(SQLCaseOperator.SWITCH);
        boolean containsPredicate = condition != null && getObjectType(condition) == SQLConstants.PREDICATE;

        List<SQLWhen> whenObjects = operator.getWhenList();
        List<Map<String, String>> whenClauses = new ArrayList<Map<String, String>>(whenObjects.size());

        for (int i = 0; i < operator.getWhenCount(); i++) {
            SQLWhen when = whenObjects.get(i);
            SQLCondition wcondition = when.getCondition();
            if (wcondition == null || containsPredicate) {
                throw new EDMException(NbBundle.getMessage(BaseCaseGenerator.class, "MSG_Badly_formed_case_object"));
            }

            Map<String, String> whenThen = new HashMap<String, String>(1);
            whenThen.put("when", this.getDB().getGeneratorFactory().generate(wcondition.getRootPredicate(), context));
            whenThen.put("then", this.getDB().getGeneratorFactory().generate(when.getSQLObject(SQLWhen.RETURN), context));

            whenClauses.add(whenThen);
        }

        return whenClauses;
    }

    protected String buildDefaultClause(SQLCaseOperator operator, StatementContext context) throws EDMException {
        String defaultClause = "";

        SQLObject defaultAction = operator.getSQLObject(SQLCaseOperator.DEFAULT);
        if (defaultAction != null) {
            defaultClause = this.getDB().getGeneratorFactory().generate(defaultAction, context);
        }

        return defaultClause;
    }

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