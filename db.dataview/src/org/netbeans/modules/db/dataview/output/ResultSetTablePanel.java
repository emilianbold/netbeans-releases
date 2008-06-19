/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.db.dataview.output;

import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import java.awt.BorderLayout;
import java.sql.ResultSet;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Renders rows and columns of an arbitrary ResultSet via JTable.
 *
 * @author Ahimanikya Satapathy
 */
class ResultSetTablePanel extends JPanel {

    private boolean isEditable = true;
    private boolean isDirty = false;
    private DBTableWrapper tblMeta;
    private ResultSetUpdatedRowContext tblContext;
    private DataViewOutputPanelUI parent;
    private int MAX_COLUMN_WIDTH = 50;
    private TableModel model;
    private ResulSetTable table;
    private SQLStatementGenerator stmtBlrd;
    private Logger mLogger = Logger.getLogger(ResultSetTablePanel.class.getName());

    public ResultSetTablePanel(DBTableWrapper tblMeta, DataViewOutputPanelUI parent) {
        this.tblMeta = tblMeta;
        this.parent = parent;

        this.setLayout(new BorderLayout());
        table = new ResulSetTable(this);
        table.setColumnToolTips(tblMeta.getColumnToolTips());
        JScrollPane sp = new JScrollPane(table);
        this.add(sp, BorderLayout.CENTER);

        stmtBlrd = new SQLStatementGenerator(tblMeta, table);
        tblContext = new ResultSetUpdatedRowContext(stmtBlrd);
    }

    public void clearView() {
        DataTableModel dtm = new DataTableModel();
        final DataViewTableModel sorter = new DataViewTableModel(dtm);
        sorter.setTableHeader(table.getTableHeader());

        Runnable run = new Runnable() {

            public void run() {
                table.setModel(sorter);
            }
        };
        SwingUtilities.invokeLater(run);
    }

    public void fireTableModelChange() {
    }

    public void setEditable(boolean edit) {
        this.isEditable = edit;
    }

    protected boolean isEditable() {
        return isEditable;
    }

    public boolean isDirty() {
        if (!isDirty) {
            tblContext.resetUpdateState();
        }
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
        if (!isDirty) {
            tblContext.resetUpdateState();
        }
    }

    protected ResulSetTable getResulSetTable() {
        return table;
    }

    protected ResultSetUpdatedRowContext getResultSetRowContext() {
        return tblContext;
    }

    protected DBTableWrapper getDBTableWrapper() {
        return tblMeta;
    }

    /**
     * Updates this view's data model to display the contents of the given ResultSet.
     *
     * @param rsMap new ResultSet to be displayed.
     */
    public void setResultSet(ResultSet rs, int maxRowsToShow, int startFrom) throws DBException {
        if (rs == null) {
            throw new DBException("Must supply non-null ResultSet reference for rs");
        }
        List<Integer> columnWidthList = getColumnWidthList();

        tblContext.resetUpdateState();
        model = createModelFrom(rs, maxRowsToShow, startFrom);
        final TableModel tempModel = model;
        final List<Integer> columnWidthList1 = columnWidthList;
        Runnable run = new Runnable() {

            public void run() {
                table.setModel(tempModel);
                if (!columnWidthList1.isEmpty()) {
                    setHeader(table, columnWidthList1);
                }
            }
        };
        SwingUtilities.invokeLater(run);
    }

    /**
     * create a table model
     *
     * @param rs resultset
     * @return TableModel
     */
    private TableModel createModelFrom(ResultSet rs, int pageSize, int startFrom) throws DBException {
        DataTableModel dtm = new DataTableModel();
        DataViewTableModel sorter = new DataViewTableModel(dtm);
        sorter.setTableHeader(table.getTableHeader());

        try {
            rs.setFetchSize(pageSize);
            int colCnt = tblMeta.getColumnCount();
            Object[] row = new Object[colCnt];

            // Obtain display name
            for (int i = 0; i < colCnt; i++) {
                DBColumn col = tblMeta.getColumn(i);
                dtm.addColumn(col.getDisplayName());
            }

            // Skip till current position
            boolean lastRowPicked = rs.next();
            while (lastRowPicked && rs.getRow() < (startFrom + 1)) {
                lastRowPicked = rs.next();
            }

            // Get next page
            int rowCnt = 0;
            while (((pageSize == -1) || (pageSize > rowCnt)) && (lastRowPicked || rs.next())) {
                for (int i = 0; i < colCnt; i++) {
                    int type = tblMeta.getColumn(i).getJdbcType();
                    row[i] = DBReadWriteHelper.readResultSet(rs, type, i + 1);
                }
                dtm.addRow(row);
                rowCnt++;
                if (lastRowPicked) {
                    lastRowPicked = false;
                }
            }
        } catch (Exception e) {
            mLogger.info(" Failed to set up table model " + e.getMessage());
            throw new DBException(e);
        }

        return sorter;
    }

    private void setHeader(JTable table, List<Integer> columnWidthList) {
        try {
            TableColumnModel cModel = table.getColumnModel();
            for (int i = 0; i < columnWidthList.size(); i++) {
                TableColumn column = cModel.getColumn(i);
                column.setPreferredWidth(columnWidthList.get(i));
            }
            table.getTableHeader().setColumnModel(cModel);
        } catch (Exception e) {
            mLogger.info(" Failed to set the size of the table headers" + e);
        }
    }

    private List<Integer> getColumnWidthList() {
        List<Integer> columnWidthList = new ArrayList<Integer>();
        try {
            for (int i = 0; i < tblMeta.getColumnCount(); i++) {
                DBColumn col = tblMeta.getColumn(i);
                int fieldWidth = col.getDisplaySize();
                int labelWidth = col.getDisplayName().length();
                int colWidth = Math.max(fieldWidth, labelWidth) * table.getMultiplier();
                if (colWidth > MAX_COLUMN_WIDTH * table.getMultiplier()) {
                    colWidth = MAX_COLUMN_WIDTH * table.getMultiplier();
                }
                columnWidthList.add(colWidth);
            }
        } catch (Exception e) {
            mLogger.info("Failed to set the size of the table headers" + e);
        }
        return columnWidthList;
    }

    private class DataTableModel extends DefaultTableModel {

        /**
         * Returns true regardless of parameter values.
         *
         * @param row the row whose value is to be queried
         * @param column the column whose value is to be queried
         * @return true
         * @see #setValueAt
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            DBColumn col = tblMeta.getColumn(column);
            if (col.isGenerated()) {
                return false;
            }
            return col.isEditable();
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (tblMeta == null) {
                return;
            }

            Object oldVal = getValueAt(row, col);
            if (oldVal != null && oldVal.toString().equals(value) || (oldVal == null && value == null)) {
                return;
            }

            try {
                DBReadWriteHelper.validate(value, tblMeta.getColumn(col));
                tblContext.createUpdateStatement(row, col, value);
                isDirty = true;
                super.setValueAt(value, row, col);
                parent.setCommitEnabled(true);
                fireTableDataChanged();
            } catch (DBException dbe) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(dbe.getMessage());
                DialogDisplayer.getDefault().notify(nd);
            } catch (Exception ex) {
                //ignore
                mLogger.warning(new DBException(ex).getMessage());
            }
            table.revalidate();
            table.repaint();
        }
    }
}
