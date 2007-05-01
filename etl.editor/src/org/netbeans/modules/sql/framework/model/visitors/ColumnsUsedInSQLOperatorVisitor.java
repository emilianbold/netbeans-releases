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
package org.netbeans.modules.sql.framework.model.visitors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperator;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Girish Patil
 * @version Revision
 */
public class ColumnsUsedInSQLOperatorVisitor {

    /**
     * Visits each of Expression Object Nodes.
     * 
     * @param operator SQLOperator
     * @return Set of columns used in the expression.
     * @throws BaseException
     */
    public static Set visit(SQLOperator operator) throws BaseException {
        if (operator == null) {
            return null;
        }

        Set columnsNames = new HashSet();
        Map inputMap = operator.getInputObjectMap();

        Set keys = null;
        Iterator keyItr = null;
        Object keyObject = null;
        Object operandObject = null;

        if (inputMap != null) {
            keys = inputMap.keySet();

            if (keys != null) {
                keyItr = keys.iterator();
                Set columnNamesFromChildOperators = null;

                while (keyItr.hasNext()) {
                    keyObject = keyItr.next();
                    if (keyObject != null) {
                        operandObject = inputMap.get(keyObject);

                        if (operandObject instanceof SQLInputObject) {
                            SQLInputObject inputObj = (SQLInputObject) operandObject;
                            SQLObject sqlObject = inputObj.getSQLObject();

                            if (sqlObject instanceof SQLOperator) {
                                columnNamesFromChildOperators = visit((SQLOperator) sqlObject);
                                columnsNames.addAll(columnNamesFromChildOperators);
                            } else {
                                if (sqlObject instanceof ColumnRef) {
                                    ColumnRef conditionColumn = (ColumnRef) sqlObject;
                                    SQLDBColumn column = (SQLDBColumn) conditionColumn.getColumn();
                                    String colName = column.getName();
                                    columnsNames.add(colName);
                                }
                            }
                        }
                    }
                }
            }
        }
        return columnsNames;
    }

}
