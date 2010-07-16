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
package org.netbeans.modules.sql.framework.codegen.db2v7;

import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.base.BaseCaseGenerator;
import org.netbeans.modules.sql.framework.model.SQLCaseOperator;
import org.netbeans.modules.sql.framework.model.SQLObject;
import com.sun.etl.exception.BaseException;

/**
 * Velocity-based generator that generates SQL Case expressions with DB2-specific syntax
 * quirks.
 *
 * @author Jonathan Giron
 * @version $Revision$
 */
public class DB2V7CaseGenerator extends BaseCaseGenerator {

    /**
     * Overrides base implementation to properly handle situation where the default action
     * is the null oeprator.
     *
     * @param operator SQLCaseOperator which will be interrogated to obtain default clause
     * @param context StatementContext to be used in evaluating default action
     * @return DB2-specific default clause
     */
    @Override
    protected String buildDefaultClause(SQLCaseOperator operator, StatementContext context) throws BaseException {
        String defaultClause = "";

        SQLObject defaultAction = operator.getSQLObject(SQLCaseOperator.DEFAULT);
        if (defaultAction != null) {
            if ("null".equalsIgnoreCase(defaultAction.getDisplayName())) {
                defaultClause = "NULL";
            } else {
                defaultClause = "(" + this.getDB().getGeneratorFactory().generate(defaultAction, context) + ")";
            }
        }
        return defaultClause;
    }
}