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
import java.util.List;

public class SelectNode implements Select {

    // Fields

    // A vector of Column specifications
    // This will eventually include functions, but for now is simple columns

    // ToDo: consider replacing this with a HashMap
    private ArrayList _selectItemList;
    private String _quantifier;


    // Constructor

    public SelectNode() {
    }

    public SelectNode(ArrayList columnList, String quantifier) {
        _selectItemList = columnList;
        _quantifier = quantifier;
    }

    public SelectNode(ArrayList columnList) {
        this(columnList, "ALL");  // NOI18N
    }


    // Return the Select clause as a SQL string

    public String genText() {
        String res = "";  // NOI18N
        String res_select_quantifier = "";  // NOI18N

        if (_selectItemList.size() > 0) {

            res_select_quantifier  = "SELECT " + _quantifier + " " ;
            res = res_select_quantifier
                        + ((ColumnItem)_selectItemList.get(0)).genText(true);  // NOI18N

            for (int i=1; i<_selectItemList.size(); i++) {
                ColumnItem col = (ColumnItem)_selectItemList.get(i);
                if (col != null)
                {
                    res += ", " + "\n                    " + col.genText(true);  // NOI18N
                }
            }
        }
        return res;
    }


    // Accessors/Mutators

    public void setColumnList(ArrayList columnList) {
        _selectItemList = columnList;
    }

    public void getReferencedColumns(Collection columns) {
        for (int i = 0; i < _selectItemList.size(); i++)
            columns.add(((ColumnItem)_selectItemList.get(i)).getReferencedColumn());
    }

    public void getQueryItems(Collection items) {
        items.add(_selectItemList);
    }

    public void addColumn(Column col) {
        _selectItemList.add(col);
    }

    public void addColumn(String tableSpec, String columnName) {
        _selectItemList.add(new ColumnNode(tableSpec, columnName));
    }

    // Remove the specified column from the SELECT list
    // Iterate back-to-front for stability under deletion
    public void removeColumn(String tableSpec, String columnName) {
        for (int i=_selectItemList.size()-1; i>=0; i--) {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            ColumnNode c = (ColumnNode) item.getReferencedColumn();
            if ((c != null) && (c.getTableSpec().equals(tableSpec)) && (c.getColumnName().equals(columnName)))
            {
                _selectItemList.remove(i);
            }
        }
    }

    /**
     * set column name
     */
    public void setColumnName (String oldColumnName, String newColumnName) {
        for (int i=0; i<_selectItemList.size(); i++)  {
            ColumnNode c = (ColumnNode) _selectItemList.get(i);
            if ( c != null) {
                c.setColumnName(oldColumnName, newColumnName);
            }
        }
    }

    public boolean hasAsteriskQualifier() {
        for (int i=0; i<_selectItemList.size(); i++)  {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            if (item instanceof ColumnNode) {
                ColumnNode c = (ColumnNode) item;
                if (c.getColumnName().equals("*")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove any SELECT targets that reference this table
     */
    void removeTable (String tableSpec) {
        for (int i=_selectItemList.size()-1; i>=0; i--) {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            ColumnNode c = (ColumnNode) item.getReferencedColumn();
            if (c != null) {
                String tabSpec = c.getTableSpec();
                if (tabSpec != null && tabSpec.equals(tableSpec))
                    _selectItemList.remove(i);
            }
        }
    }

    /**
     * Rename a table
     */
    void renameTableSpec (String oldTableSpec, String corrName) {
        for (int i=0; i<_selectItemList.size(); i++)  {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            ColumnNode c = (ColumnNode) item.getReferencedColumn();
            if ( c != null)
            {
                c.renameTableSpec(oldTableSpec, corrName);
            }
        }
    }

}
