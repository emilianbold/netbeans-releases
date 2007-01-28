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

import java.util.List;

// Identify a Column.
// It derives from Value which derives from Expression.
// REVIEW: not clear we need all these methods, need a clean up
public interface Column extends Value {

    //
    // this first 3 methods are used for equality. Equality is based on string equality and not object identity
    //
    public boolean matches(String table, String column);
    public boolean matches(String table);
    public boolean equals(Column column);

    // getters
    public String getColumnName();
    public String getTableSpec();
    public String getFullTableName();
    public String getDerivedColName();

    // modifiers
    public void setDerivedColName(String derivedColName);
    public void setTableSpec(String oldTableSpec, String newTableSpec);
    public void setColumnName(String oldColumnName, String newColumnName);
    public void setColumnTableName(String tableName);
    public void setColumnCorrName(String corrName);
}

