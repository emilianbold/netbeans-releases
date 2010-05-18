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
package org.netbeans.modules.sql.framework.model.visitors;

import org.netbeans.modules.sql.framework.model.SQLFilter;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.impl.VisibleSQLPredicateImpl;

import com.sun.etl.exception.BaseException;

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
