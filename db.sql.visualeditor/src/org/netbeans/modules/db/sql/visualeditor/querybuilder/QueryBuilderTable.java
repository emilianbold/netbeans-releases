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

/**
 *
 * @author  Sanjay Dhamankar
 */

package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import javax.swing.JTable;

import javax.swing.table.TableColumn;

import java.awt.*;

import org.openide.ErrorManager;

import org.netbeans.modules.db.sql.visualeditor.Log;

// Represents the information presented inside a table node, which includes
// selected status, key status, and column name

public class QueryBuilderTable extends JTable {

    private boolean DEBUG = false;


    // Constructor

    public QueryBuilderTable( QueryBuilderTableModel model) {

        super();
        super.setModel( model );

        Log.err.log(ErrorManager.INFORMATIONAL, "Entering QueryBuilderTable ctor, model: " + model); // NOI18N

        this.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);

        // This may not be required afterall. We need to keep the size of the cell fixed.
        this.initColumnSizes(this, model);
        this.setShowHorizontalLines(false);
        this.setShowVerticalLines(false);
        this.setBackground(Color.white);
        this.setRowHeight(this.getRowHeight() + 2);
        this.setRowSelectionAllowed (false);
        this.setTableHeader (null);
    }


    // Methods

    private void initColumnSizes(JTable table, QueryBuilderTableModel model) {

        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;

        for (int i = 0; i < getColumnCount(); i++) {

            column = table.getColumnModel().getColumn(i);

            comp = table.getDefaultRenderer(column.getClass()).
                getTableCellRendererComponent(
                    table, column.getHeaderValue(),
                    false, false, -1, 0);
            headerWidth = comp.getPreferredSize().width;

            try {
                comp = column.getHeaderRenderer().
                    getTableCellRendererComponent(
                        null, column.getHeaderValue(),
                        false, false, 0, 0);
                headerWidth = comp.getPreferredSize().width;
            } catch (NullPointerException e) {
            }

            if ( i  != 0 )
            {
                for (int j=0; j< table.getRowCount(); j++)
                {
                    comp = table.getDefaultRenderer(model.getColumnClass(i)).
                        getTableCellRendererComponent(
                            table, getValueAt(j, 2),
                            false, false, 0, i);
                    int tmpCellWidth = comp.getPreferredSize().width;

                    if ( tmpCellWidth > cellWidth )
                        cellWidth = tmpCellWidth;
                }
            }

            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            column.setPreferredWidth(Math.max(headerWidth+15, cellWidth+15));
        }

        table.addNotify();
    }
}
