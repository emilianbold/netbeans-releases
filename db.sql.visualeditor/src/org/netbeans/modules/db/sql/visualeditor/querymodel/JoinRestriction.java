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
 * Represents the restriction in a JOIN clause
 * Must be of form a.x = b.y
 */
public class JoinRestriction {

    // Fields

    private ColumnNode  _col1;

    private String      _op;

    private ColumnNode  _col2;


    // Constructors

    public JoinRestriction (ColumnNode col1, String op, ColumnNode col2) {
        _col1 = col1;
        _op = op;
        _col2 = col2;
    }


    // Methods

    // Return the SQL string that corresponds to this From clause
    // For now, assume no joins
    public String genText() {
        return " " + _col1.genText() + " " + _op + " " + _col2.genText();    // NOI18N
    }

    // Methods

    // Accessors/Mutators

    public Column getCol1 () {
        return _col1;
    }

    public Column getCol2 () {
        return _col2;
    }
}


