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
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class BasePredicateGenerator extends AbstractGenerator {

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        SQLPredicate predicate = (SQLPredicate) obj;
        boolean showParenthesis = predicate.isShowParenthesis();

        SQLObject leftObj = predicate.getSQLObject(SQLPredicate.LEFT);
        SQLObject rightObj = predicate.getSQLObject(SQLPredicate.RIGHT);

        String leftExpression = this.getDB().getGeneratorFactory().generate(leftObj, context);
        String rightExpression = this.getDB().getGeneratorFactory().generate(rightObj, context);
        String operator = predicate.getOperatorType();

        VelocityContext vContext = new VelocityContext();
        vContext.put("leftExpression", leftExpression);
        vContext.put("rightExpression", rightExpression);
        vContext.put("operator", operator);
        vContext.put("showParenthesis", new Boolean(showParenthesis));

        return TemplateBuilder.generateSql(this.getDB().getTemplateFileName("predicate"), vContext);
    }
}