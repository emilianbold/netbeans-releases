/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.scheduler.configeditor;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;

/**
 * View/Controller for Trigger Table.
 * 
 * @author sunsoabi_edwong
 */
public class TriggerTableViewController implements SchedulerConstants {
    
    private JScrollPane scrollPane;
    private JTable table;
    private TableModelListener tableModelListener;
    private PropertyChangeListener tablePropertyChangeListener;
    
    public TriggerTableViewController(JScrollPane scrollPane, JTable table) {
        super();
        
        this.scrollPane = scrollPane;
        this.table = table;
    }
    
    public void adjustTriggersTable() {
        // Pack columns of the table
        int margin = 5;

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        packColumns(margin);
    }
    
    public void packColumns(int margin) {
        int ncols = table.getColumnCount() - 1;
        int sumWidth = 0;
        for (int c = 0; c < ncols; c++) {
            sumWidth += packColumn(c, margin);
        }
        DefaultTableColumnModel colModel =
                (DefaultTableColumnModel) table.getColumnModel();
        TableColumn col = colModel.getColumn(ncols);
        col.setPreferredWidth(scrollPane.getViewport().getWidth() - sumWidth);
        col.setMinWidth(optimalColumnWidth(col, ncols, margin));
    }

    /**
     * Sets the preferred width of the visible column specified by vColIndex.
     * The column will be just wide enough to show the column head and the
     * widest cell in the column. margin pixels are added to the left and
     * right (resulting in an additional width of 2*margin pixels).
     */
    private int packColumn(int colIndex, int margin) {
        DefaultTableColumnModel colModel =
                (DefaultTableColumnModel) table.getColumnModel();
        TableColumn col = colModel.getColumn(colIndex);
        int packWidth = optimalColumnWidth(col, colIndex, margin);

        // Set the width
        col.setPreferredWidth(packWidth);

        return packWidth;
    }

    private int optimalColumnWidth(TableColumn col, int colIndex, int margin) {
        int width = 0;

        // Get width of column header
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(
                table, col.getHeaderValue(), false, false, 0, 0);
        width = comp.getPreferredSize().width;

        // Get maximum width of column data
        for (int r = 0; r < table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, colIndex);
            comp = renderer.getTableCellRendererComponent(
                    table, table.getValueAt(r, colIndex),
                    false, false, r, colIndex);
            width = Math.max(width, comp.getPreferredSize().width);
        }

        // Add margin
        width += (2 * margin);

        return width;
    }
    
    public TableModelListener getTableModelListener() {
        if (null == tableModelListener) {
            tableModelListener = new TriggerTableModelListener();
        }
        return tableModelListener;
    }
    
    public PropertyChangeListener getTablePropertyChangeListener() {
        if (null == tablePropertyChangeListener) {
            tablePropertyChangeListener =
                    new TriggerTablePropertyChangeListener();
        }
        return tablePropertyChangeListener;
    }
    
    private class TriggerTableModelListener implements
            TableModelListener {
        
        public TriggerTableModelListener() {
            super();
        }

        public void tableChanged(final TableModelEvent evt) {
            if (((evt.getType() == TableModelEvent.INSERT)
                    || (evt.getType() == TableModelEvent.UPDATE)
                    || (evt.getType() == TableModelEvent.DELETE))
                    && (evt.getColumn() == TableModelEvent.ALL_COLUMNS)
                    && (evt.getFirstRow() != TableModelEvent.HEADER_ROW)) {
                Utils.callFromEDT(true, new Runnable() {
                    public void run() {
                        int selectRow = evt.getFirstRow();
                        if ((selectRow < 0)
                                || (selectRow >= table.getRowCount())) {
                            selectRow = (table.getRowCount() > 0)
                                    ? table.getRowCount() - 1 : -1;
                        }
                        if (selectRow > -1) {
                            table.getSelectionModel().setSelectionInterval(
                                    selectRow, selectRow);
                        }
                        adjustTriggersTable();
                    }
                });
            }
        }
    }
    
    private class TriggerTablePropertyChangeListener implements
            PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            String propKey = evt.getPropertyName();
            AbstractTableModel atm = (AbstractTableModel) table.getModel();
            int firstRow = (evt.getNewValue() instanceof Integer)
                ? ((Integer) evt.getNewValue()).intValue() : -1;
            if (TRIGGER_ADDED.equals(propKey)) {
                atm.fireTableRowsInserted(firstRow, firstRow);
            } else if (TRIGGER_EDITED.equals(propKey)) {
                atm.fireTableRowsUpdated(firstRow, firstRow);
            } else if (TRIGGER_REMOVED.equals(propKey)) {
                atm.fireTableRowsDeleted(firstRow, firstRow);
            }
        }
    }
}
