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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a SQL And term in a WHERE clause
 * Example Form: ((a.x = b.y) AND (c.w = d.v))
 */
public class NotNode implements Expression {

    // Fields

    // A condition

    Expression _cond;


    // Constructor

    public NotNode(Expression cond) {

        _cond = cond;
    }


    // Methods

    public Expression findExpression(String table1, String column1, String table2, String column2) {
        return _cond.findExpression(table1, column1, table2, column2);
    }

    // get the column specified in the condition if any
    public void getReferencedColumns(Collection comlumns) {
        _cond.getReferencedColumns(comlumns);
    }

    public void getQueryItems(Collection items) {
        items.add(_cond);
    }

    // Return the Where clause as a SQL string
    public String genText() {
        return " ( NOT " + _cond.genText() + ") ";  // NOI18N
    }

    public String toString() {
        return "";    // NOI18N
    }

    public boolean isParameterized() {
        return _cond.isParameterized();
    }

    public void renameTableSpec(String oldTableSpec, String corrName) {
        _cond.renameTableSpec(oldTableSpec, corrName);
    }
}
