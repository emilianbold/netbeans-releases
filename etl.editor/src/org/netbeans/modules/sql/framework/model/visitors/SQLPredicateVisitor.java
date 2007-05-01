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

import org.netbeans.modules.sql.framework.model.SQLFilter;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.impl.VisibleSQLPredicateImpl;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLPredicateVisitor {
    public static SQLFilter visit(SQLPredicate predicate) throws BaseException {
        if (predicate == null) {
            return null;
        }

        SQLObject leftObj = predicate.getSQLObject(SQLPredicate.LEFT);
        SQLObject rightObj = predicate.getSQLObject(SQLPredicate.RIGHT);
        String operator = predicate.getOperatorType();
        SQLFilter rootFilter = null;
        SQLFilter nextFilter = null;

        if (!(leftObj instanceof SQLPredicate) && !(rightObj instanceof SQLPredicate)) {
            if (isLeftUnaryOperator(predicate)) {
                // This is a unary predicate, like IS NULL or IS NOT NULL, and does not
                // require a right input.
                rootFilter = SQLModelObjectFactory.getInstance().createLeftUnarySQLFilter();
                rootFilter.addInput(SQLFilter.LEFT, leftObj);
            } else {
                if (isRightUnaryOperator(predicate)) {
                    rootFilter = SQLModelObjectFactory.getInstance().createRightUnarySQLFilter();
                    rootFilter.addInput(SQLFilter.RIGHT, rightObj);
                } else {
                    rootFilter = SQLModelObjectFactory.getInstance().createSQLFilter();
                    rootFilter.addInput(SQLFilter.LEFT, leftObj);
                    rootFilter.addInput(SQLFilter.RIGHT, rightObj);
                }
            }

            rootFilter.setOperator(operator);
            return rootFilter;
        }

        if (leftObj instanceof SQLPredicate) {
            rootFilter = visit((SQLPredicate) leftObj);
        }

        if (rightObj instanceof SQLPredicate) {
            nextFilter = visit((SQLPredicate) rightObj);
            nextFilter.setPrefix(operator);
            if (rootFilter != null) {
                rootFilter.setNextFilter(nextFilter);
            }
        }

        return rootFilter;
    }

    /**
     * Indicates whether the given predicate is LeftUnary, that is, it accepts a single
     * input left side of the operator.
     * 
     * @param predicate predicate instance to be tested
     * @return true if unary, false otherwise
     */
    private static boolean isLeftUnaryOperator(SQLPredicate predicate) {
        return (predicate instanceof VisibleSQLPredicateImpl.LeftUnary);
    }

    /**
     * Indicates whether the given predicate is RightUnary, that is, it accepts a single
     * input right side of the operator.
     * 
     * @param predicate predicate instance to be tested
     * @return true if unary, false otherwise
     */
    private static boolean isRightUnaryOperator(SQLPredicate predicate) {
        return (predicate instanceof VisibleSQLPredicateImpl.RightUnary);
    }

}
