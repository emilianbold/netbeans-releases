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

import org.netbeans.modules.sql.framework.codegen.AbstractGenerator;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLObject;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class BaseColumnGenerator extends AbstractGenerator {

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        SQLDBColumn column = (SQLDBColumn) obj;

        StringBuilder result = new StringBuilder(50);
        String nestedIndent = (String) context.getClientProperty("nestedIndent");
        if (nestedIndent != null) {
            result.append(nestedIndent);
        }

        // This is to check for runtime input output, we need to refactor this (runtime
        // input holds source column, runtime output holds target columns so it comes
        // here) but for now do this check here
        SQLObject tableObj = (SQLObject) column.getParentObject();
        if (tableObj.getObjectType() == SQLConstants.RUNTIME_INPUT || tableObj.getObjectType() == SQLConstants.RUNTIME_OUTPUT) {
            result.append("$");
            result.append(column.getName());
        } else {
            String legalName = this.getDB().getEscapedName(column.getName());
            if ((column.getObjectType() == SQLConstants.TARGET_COLUMN && !context.isSuppressingTablePrefixForTargetColumn()) || (column.getObjectType() == SQLConstants.SOURCE_COLUMN && !context.isSuppressingTablePrefixForSourceColumn())) {
                String prefix = getPrefix(column, context).trim();
                if (!StringUtil.isNullString(prefix)) {
                    result.append(prefix);
                    result.append(".");
                }
            }
            result.append(legalName);
        }
        return result.toString();
    }

    /**
     * Generates appropriate column prefix for the given column type and SQLObject.
     *
     * @param type column type
     * @param object SQLObject (column) to be prefixed
     * @return appropriate column prefix
     * @throws BaseException if prefix cannot be generated
     */
    private String getPrefix(SQLDBColumn column, StatementContext context) throws BaseException {
        SQLDBTable table = (SQLDBTable) column.getParentObject();
        String alias = table.getAliasName();

        if ((column.getObjectType() == SQLConstants.SOURCE_COLUMN && context.isUseSourceTableAliasName()) || (column.getObjectType() == SQLConstants.TARGET_COLUMN && context.isUseTargetTableAliasName())) {

            if (!StringUtil.isNullString(alias)) {
                return alias;
            }
        }
        return this.getGeneratorFactory().generate(table, context);
    }
}