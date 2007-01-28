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

// The most basic Expression interface.
// I.e., it represents both a boolean expression, a simple predicate and a literal
// This is the most common type to talk to expressions
// The methods provided here are mostly helpers for the editor and are used internally so they may disappear from here at some point
// Usually a type test (instanceof) with a more derived interface (And, ExpressionList, etc.) defines the kind of expression
public interface Expression extends QueryItem {
    public Expression findExpression(String table1, String column1, String table2, String column2);
    public boolean isParameterized();
    public void renameTableSpec(String oldTableSpec, String corrName);
}
