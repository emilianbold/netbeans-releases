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
 * Represents a HAVING clause in a SQL Table Expression
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public class HavingNode implements Having {

    // Fields

    private Expression   _condition; // simplified WHERE clause

    // Constructors

    public HavingNode () {
    }

    public HavingNode(Expression condition) {
        _condition = condition;
    }


    // Methods

    // Return the SQL string that corresponds to this From clause
    // For now, assume no joins
    public String genText() {
        String res="";    // NOI18N
        if (_condition != null) {
            res = "\nHAVING " + _condition.genText();  // NOI18N
        }

        return res;
    }


    // Methods

    // Accessors/Mutators

    public Expression getExpression() {
        return _condition;
    }

    void renameTableSpec(String oldTableSpec, String corrName) {

        if (_condition instanceof Predicate)
            ((Predicate) _condition).renameTableSpec(oldTableSpec, corrName);
    }

    // adds any column in the condition to the ArrayList of columns
    public void  getReferencedColumns (Collection columns) {
        if (_condition != null)
            _condition.getReferencedColumns(columns);
    }

    public void getQueryItems(Collection items) {
        items.add(_condition);
    }
}


