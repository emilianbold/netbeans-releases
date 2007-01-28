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
package org.netbeans.modules.db.sql.visualeditor.querymodel;

/**
 * Represents a SQL WHERE clause
 * Example Form: WHERE ((a.x = b.y) AND (c.w = d.v))
 */
// ToDo: Decide whether a null WHERE clause is better represented as a null
// ptr, or a Where with null condition.

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public class WhereNode implements Where {

    // Fields

    // This could be an AND or (in future) OR
    private Expression _cond;

    // Constructors

    public WhereNode() {
        _cond = null;
    }

    public WhereNode(Expression cond) {
        _cond = cond;
//         if (cond instanceof Predicate)
//             _cond = new And((Predicat)_cond);
//         else
//             _cond = cond;
    }


    // Accessors/mutators

    public void resetExpression() {
        _cond = null;
    }

    public void replaceExpression(Expression expression) {
        _cond = expression;
    }

    public Expression getExpression () {
        return _cond;
    }

    // Methods

    public Expression findExpression(String table1, String column1, String table2, String column2) {
        return _cond.findExpression(table1, column1, table2, column2);
    }

    // Remove any WHERE clauses that mention the table in question,
    // since the table itself is being removed from the model
    void removeTable(String tableSpec) {
        if (_cond instanceof ExpressionList) {
            ExpressionList list = (ExpressionList)_cond;
            list.removeTable(tableSpec);
            if (list.size() == 0)
                _cond = null;
        }
        else {
            ArrayList column = new ArrayList();
            _cond.getReferencedColumns(column);
            for (int i = 0; i < column.size(); i++) {
                Column col = (Column)column.get(i);
                if (col.matches(tableSpec)) {
                    _cond = null;
                }
            }
        }
    }

    // adds any column in the condition to the ArrayList of columns
    public void getReferencedColumns (Collection columns) {
        if (_cond != null)
            _cond.getReferencedColumns (columns);
    }

    public void getQueryItems(Collection items) {
        if (_cond != null)
            items.add(_cond);
    }

    // Return the Where clause as a SQL string
    public String genText() {
        if (_cond!=null)
            return "\nWHERE " + _cond.genText() ;  // NOI18N
        else
            return "";  // NOI18N
    }

    // See if we have a parameter marker (string literal "?")
    public boolean isParameterized() {
        if (_cond!=null)
            return _cond.isParameterized();
        else
            return false;
    }


    void renameTableSpec(String oldTableSpec, String corrName) {
        _cond.renameTableSpec(oldTableSpec, corrName);
    }
}
