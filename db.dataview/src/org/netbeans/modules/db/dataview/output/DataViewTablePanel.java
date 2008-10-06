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
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 * Renders rows and columns of a given ResultSet via JTable.
 *
 * @author Ahimanikya Satapathy
 */
class DataViewTablePanel extends JPanel {

    private final DataViewDBTable tblMeta;
    private final UpdatedRowContext tblContext;
    private final DataViewUI dataViewUI;
    private final DataViewTableUI tableUI;
    private final SQLStatementGenerator stmtGenerator;
    private boolean isEditable = true;
    private boolean isDirty = false;
    private int MAX_COLUMN_WIDTH = 25;
    private TableModel model;
    private final List<Integer> columnWidthList;
    private static Logger mLogger = Logger.getLogger(DataViewTablePanel.class.getName());

    public DataViewTablePanel(DataView dataView, DataViewUI dataViewUI, DataViewActionHandler actionHandler) {
        this.tblMeta = dataView.getDataViewDBTable();
        this.dataViewUI = dataViewUI;

        this.setLayout(new BorderLayout());
        tableUI = new DataViewTableUI(this, actionHandler, dataView);
        tableUI.setColumnToolTips(tblMeta.getColumnToolTips());
        JScrollPane sp = new JScrollPane(tableUI);
        this.add(sp, BorderLayout.CENTER);

        stmtGenerator = dataView.getSQLStatementGenerator();
        tblContext = new UpdatedRowContext(stmtGenerator);
        columnWidthList = getColumnWidthList();
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
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
        if (!isDirty) {
            tblContext.resetUpdateState();
        }
    }

    DataViewTableUI getDataViewTableUI() {
        return tableUI;
    }

    UpdatedRowContext getUpdatedRowContext() {
        return tblContext;
    }

    DataViewDBTable getDataViewDBTable() {
        return tblMeta;
    }

    boolean isCommitEnabled() {
        return dataViewUI.isCommitEnabled();
    }

    public void createTableModel(List<Object[]> rows) {
        assert rows != null;

        setDirty(false);
        model = createModelFrom(rows);
        final TableModel tempModel = model;
        Runnable run = new Runnable() {

            public void run() {
                tableUI.setModel(tempModel);
                if (!columnWidthList.isEmpty()) {
                    setHeader(tableUI, columnWidthList);
                }
            }
        };
        SwingUtilities.invokeLater(run);
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
            mLogger.log(Level.INFO, "Failed to set the size of the table headers" + e);
        }
    }

    private List<Integer> getColumnWidthList() {
        List<Integer> colWidthList = new ArrayList<Integer>();
        try {
            for (int i = 0; i < tblMeta.getColumnCount(); i++) {
                DBColumn col = tblMeta.getColumn(i);
                int fieldWidth = col.getDisplaySize();
                int labelWidth = col.getDisplayName().length();
                int colWidth = Math.max(fieldWidth, labelWidth) * tableUI.getMultiplier();
                if (colWidth > MAX_COLUMN_WIDTH * tableUI.getMultiplier()) {
                    colWidth = MAX_COLUMN_WIDTH * tableUI.getMultiplier();
                }
                colWidthList.add(colWidth);
            }
        } catch (Exception e) {
            mLogger.log(Level.INFO, "Failed to set the size of the table headers" + e);
        }
        return colWidthList;
    }

    private TableModel createModelFrom(List<Object[]> rows) {
        DataViewTableModel dtm = new DataViewTableModel(rows);
        DataViewTableSorter sorter = new DataViewTableSorter(dtm);
        sorter.setTableHeader(tableUI.getTableHeader());
        // Obtain display name
        for (int i = 0, I = tblMeta.getColumnCount(); i < I; i++) {
            DBColumn col = tblMeta.getColumn(i);
            dtm.addColumn(col.getDisplayName());
        }

        for (Object[] row : rows) {
            dtm.addRow(row);
        }
        return sorter;
    }

    List<Object[]> getPageDataFromTable() {
        DataViewTableSorter sorter = (DataViewTableSorter) tableUI.getModel();
        DefaultTableModel dtm = (DefaultTableModel) sorter.getTableModel();
        List<Object[]> rows = new ArrayList<Object[]>();
        int colCnt = dtm.getColumnCount();
        for (Object row : dtm.getDataVector()) {
            Object[] rowObj = new Object[colCnt];
            int i = 0;
            for (Object colVal : (Vector) row) {
                rowObj[i++] = colVal;
            }
            rows.add(rowObj);
        }
        return rows;
    }

    class DataViewTableModel extends DefaultTableModel {

        Class[] collumnClasses;

        DataViewTableModel(List<Object[]> rows) {
            super();
            // TODO there should be a better way to do this
            collumnClasses = new Class[tblMeta.getColumnCount()];
            if (rows.size() > 0) {
                Object[] row = rows.get(0);
                for (int i = 0, I = row.length; i < I; i++) {
                    if (row[i] != null) {
                        collumnClasses[i] = row[i].getClass();
                    }
                }
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            // column specific
            DBColumn col = tblMeta.getColumn(column);
            if (DataViewUtils.isString(col.getJdbcType())) {
                return true;
            } else if (col.isGenerated()) {
                return false;
            } else if (!isEditable) {
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
            if (oldVal != null && oldVal.toString().equals(value == null ? "" : value.toString()) || (oldVal == null && value == null)) {
                return;
            }

            try {
                Object newVal = DBReadWriteHelper.validate(value, tblMeta.getColumn(col));
                tblContext.createUpdateStatement(row, col, newVal, model);
                isDirty = true;
                super.setValueAt(newVal, row, col);
                dataViewUI.setCommitEnabled(true);
                dataViewUI.setCancelEnabled(true);
                fireTableDataChanged();
            } catch (DBException dbe) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(dbe.getMessage());
                DialogDisplayer.getDefault().notify(nd);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            tableUI.revalidate();
            tableUI.repaint();
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            if (collumnClasses[columnIndex] == null) {
                return super.getColumnClass(columnIndex);
            } else {
                return collumnClasses[columnIndex];
            }
        }
    }

    public void enableDeleteBtn(boolean value) {
        dataViewUI.enableDeleteBtn(value);
    }
}
