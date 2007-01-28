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
import java.util.List;
import java.util.Collection;

public class GroupByNode implements GroupBy {

    // Fields

    // A vector of Column specifications
    // This will eventually include functions, but for now is simple columns

    // ToDo: consider replacing this with a HashMap
    private List _columnList;


    // Constructor

    public GroupByNode() {
    }

    public GroupByNode(ArrayList columnList) {
        _columnList = columnList;
    }


    // Return the Select clause as a SQL string

    public String genText() {
        String res = "\nGROUP BY ";  // NOI18N

        if (_columnList.size() > 0) {
            res += ((ColumnNode)_columnList.get(0)).genText();
            for (int i=1; i<_columnList.size(); i++) {
                res += ", " + ((ColumnNode)_columnList.get(i)).genText();    // NOI18N
            }
        }

        return res;
    }


    // Accessors/Mutators

    public void setColumnList(List columnList) {
        _columnList = columnList;
    }

    // adds any column in the condition to the ArrayList of columns
    public void getReferencedColumns(Collection columns) {
        if (_columnList != null)
            columns.addAll(_columnList);
    }

    public void getQueryItems(Collection items) {
        if (_columnList != null)
            items.addAll(_columnList);
    }

    public void addColumn(Column col) {
        _columnList.add(col);
    }

    public void addColumn(String tableSpec, String columnName) {
        _columnList.add(new ColumnNode(tableSpec, columnName));
    }

    // Remove the specified column from the SELECT list
    // Iterate back-to-front for stability under deletion
    public void removeColumn(String tableSpec, String columnName) {
        for (int i=_columnList.size()-1; i>=0; i--) {
            ColumnNode c = (ColumnNode) _columnList.get(i);
            if ((c.getTableSpec().equals(tableSpec)) &&
                (c.getColumnName().equals(columnName))) {
                _columnList.remove(i);
            }
        }
    }

    /**
     * Remove any GroupBy targets that reference this table
     */
    void removeTable (String tableSpec) {
        for (int i=_columnList.size()-1; i>=0; i--) {
            ColumnNode c = (ColumnNode) _columnList.get(i);
            if (c.getTableSpec().equals(tableSpec))
                _columnList.remove(i);
        }
    }

    void renameTableSpec(String oldTableSpec, String corrName) {

        for (int i=0; i<_columnList.size(); i++)
            ((ColumnNode)_columnList.get(i)).renameTableSpec(oldTableSpec, corrName);
    }
}
