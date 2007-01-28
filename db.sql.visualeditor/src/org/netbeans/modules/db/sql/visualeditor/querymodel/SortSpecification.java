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
 * Represents a column in an ORDER BY clause
 */
public final class SortSpecification implements QueryItem {

    // Fields

    private ColumnItem _column;

    // direction is one of standard SQL 'ASC' or DESC'
    private String _direction;


    // Constructors

    public SortSpecification(ColumnItem col, String direction) {
        _column = col;
        _direction = direction;
    }

    public SortSpecification(ColumnItem col) {
        this(col, "ASC");  // NOI18N
    }


    // Methods

    public String genText() {
        return _column.genText() + " " +  // NOI18N
              _direction;
    }


    // Accessors/Mutators

    public String getDirection() {
        return _direction;
    }

    public Column getColumn() {
        if (_column instanceof ColumnNode) return (Column)_column;  return null;
    }

    public void  getReferencedColumns(Collection columns) {
        columns.add(_column.getReferencedColumn());
    }

    void renameTableSpec(String oldTableSpec, String corrName) {
        _column.renameTableSpec(oldTableSpec, corrName);
    }

    public void getQueryItems(Collection items) {
        items.add(_column);
    }

//     public String getCorrName() {
//      return _corrName;
//     }

//     public String getTableName() {
//      return _tableName;
//     }

//     public String getColumnName() {
//      return _columnName;
//     }

//     public String getTableSpec() {
//      return (_corrName != null ?
//              _corrName :
//              _tableName);
//     }
}


