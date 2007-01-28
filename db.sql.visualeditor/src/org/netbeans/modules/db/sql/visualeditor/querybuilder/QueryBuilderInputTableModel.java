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
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import org.openide.util.NbBundle;
import javax.swing.table.DefaultTableModel;

class QueryBuilderInputTableModel extends DefaultTableModel {

    // Variables

    final String[] columnNames = {
        // "Column",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "COLUMN"),       // NOI18N
        // "Alias",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "ALIAS"),         // NOI18N
        // "Table",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "TABLE"),        // NOI18N
        // "Output",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OUTPUT"),       // NOI18N
        // "Sort Type",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "SORT_TYPE"),        // NOI18N
        // "Sort Order",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "SORT_ORDER"),       // NOI18N
        // "Criteria",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "CRITERIA"),         // NOI18N
        // "Criteria Order"
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "CRITERIA_ORDER"),       // NOI18N
        // "Or...",
//        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OR"),       // NOI18N
        // "Or...",
//        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OR"),       // NOI18N
        // "Or..."
//        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OR"),       // NOI18N
    };

    Object[][] data = {
        { "", "", "", "", Boolean.FALSE, "", "", "" /*, "", "", "" */ }       // NOI18N
    };


    // Constructor

    public QueryBuilderInputTableModel ()
    {
        super(0, 10);
        setColumnIdentifiers ( columnNames );
    }


    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        if ( getRowCount() == 0 ) return (new String("").getClass());       // NOI18N
        if ( getValueAt(0,c) == null ) return (new String("").getClass());      // NOI18N
        return getValueAt(0, c).getClass();
    }


    /*
     * Don't need to implement this method unless your table's editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if ((col==QueryBuilderInputTable.Column_COLUMN) ||
            (col==QueryBuilderInputTable.Table_COLUMN)) {
            return false;
        }
        else if ( col==QueryBuilderInputTable.Criteria_COLUMN &&
                  getValueAt(row, col).equals (
                      QueryBuilderInputTable.Criteria_Uneditable_String) ) {
            return false;
        }
        else if ( col==QueryBuilderInputTable.CriteriaOrder_COLUMN &&
                  getValueAt(row, col).equals (
                    QueryBuilderInputTable.CriteriaOrder_Uneditable_String ) ) {
            return false;
        }
        else {
            return true;
        }
    }
}

