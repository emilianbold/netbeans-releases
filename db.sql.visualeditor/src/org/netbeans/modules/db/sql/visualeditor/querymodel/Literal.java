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

import java.util.Collection;

/**
 * Represents a SQL literal valus
 */
public class Literal implements Value {

    // Fields

    private Object _value;


    // Constructors

    public Literal() {
    }

    public Literal(Object value) {
        _value = value;
    }


    // Methods

    public String genText() {
        return _value.toString();
    }

    public String toString() {
        return _value.toString();
    }


    // Accessors/Mutators

    public Object getValue() {
        return _value;
    }

    //REVIEW: this should probably go away, and change Literal to not be a QueryItem?
    public void getReferencedColumns(Collection columns) {}
    public void getQueryItems(Collection items) {}
    public Expression findExpression(String table1, String column1, String table2, String column2) {
        return null;
    }

    public boolean isParameterized() {
        // Original version
        // return _value.equals("?");

        // Expand prev version, to try to catch literals like "id = ?" or "id IN (?,?,?).
        // return (_value.toString().indexOf("?") != -1 );

        // Prev version was also catching "id = '?'", which is a string, not a parameter.  Exclude it.
	// return (_value.toString().matches(".*[^']?[^']"));

        // Prev regexp had problems because ? was not escaped
        // Return true if (a) string contains ? (b) string does not contain '?'
        // This still gets other cases wrong, like ? in the middle of a string.
        return (((_value.toString().indexOf("?")) != -1) &&
                ((_value.toString().indexOf("'?'")) == -1));
    }

    public void renameTableSpec(String oldTableSpec, String corrName) {}

}


