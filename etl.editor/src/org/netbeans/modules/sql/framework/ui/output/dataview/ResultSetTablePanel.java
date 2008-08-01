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
package org.netbeans.modules.sql.framework.ui.output.dataview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;
import java.util.LinkedHashMap;
import java.sql.Connection;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.sql.framework.ui.utils.BinaryToStringConverter;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.utils.StringUtil;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.model.PrimaryKey;
import org.netbeans.modules.sql.framework.ui.utils.TableSorter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExClipboard;

/**
 * Renders rows and columns of an arbitrary ResultSet via JTable.
 *
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class ResultSetTablePanel extends JPanel {

    private boolean isEditable = true;
    private boolean isDirty = false;
    private DataOutputPanel dataPanel;
    private Map<String, String> changes = new LinkedHashMap<String, String>();
    private Map<String, List> valuesList = new LinkedHashMap<String, List>();
    private Map<String, List> typesList = new LinkedHashMap<String, List>();
    private static transient final Logger mLogger = Logger.getLogger(ResultSetTablePanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

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
            return isEditable;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (dataPanel.meta != null) {
                List values = new ArrayList();
                List<Integer> types = new ArrayList<Integer>();
                DBTableMetadata meta = dataPanel.meta;

                String updateStmt = "UPDATE " + meta.getQualifiedTableName() + " SET ";
                updateStmt += meta.getColumnName(col) + " = ? " + " WHERE ";
                values.add(value);
                types.add(meta.getColumnType(col));
                updateStmt += generateWhereCondition(types, values, row);
                String changeData = (row + 1) + ";" + (col + 1);

                try {
                    Object oldVal = getValueAt(row, col);
                    if (oldVal == null && StringUtil.isNullString(value.toString())) {
                        // do nothing
                    } else if (!oldVal.equals(value)) {
                        changes.put(changeData, updateStmt);
                        valuesList.put(changeData, values);
                        typesList.put(changeData, types);
                        isDirty = true;
                        super.setValueAt(value, row, col);
                        dataPanel.commit.setEnabled(true);
                        fireTableDataChanged();
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
            table.revalidate();
            table.repaint();
        }
    }

    public Set<String> getUpdateKeys() {
        return changes.keySet();
    }

    public String getUpdateStmt(String key) {
        return changes.get(key);
    }

    public List getTypeList(String key) {
        return typesList.get(key);
    }

    public List getValueList(String key) {
        return valuesList.get(key);
    }

    public void fireTableModelChange() {
    }

    public int executeDeleteRow(DBTableMetadata meta, int rowNum) {
        List values = new ArrayList();
        List<Integer> types = new ArrayList<Integer>();

        String deleteStmt = "DELETE FROM " + meta.getQualifiedTableName() + " WHERE ";
        deleteStmt += generateWhereCondition(types, values, rowNum);

        PreparedStatement pstmt = null;
        Connection conn = null;
        boolean error = false;
        String errorMsg = null;

        try {
            conn = meta.createConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(deleteStmt);
            int pos = 1;
            for (Object val : values) {
                SQLUtils.setAttributeValue(pstmt, pos, types.get(pos - 1), val);
                pos++;
            }
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                error = true;
                errorMsg = errorMsg + "No rows deleted.";
            } else if (rows > 1) {
                error = true;
                errorMsg = errorMsg + " Statement cannot delete unique row.";
            }

            return rows;
        } catch (Exception ex) {
            error = true;
            errorMsg = errorMsg + ex.getMessage();
        } finally {
            if (!error) {
                try {
                    String msg = "Permanently delete record(s) from the database?";
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Confirm delete", NotifyDescriptor.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
                        conn.commit();
                    } else {
                        msg = "Discarded the delete operation.";
                        DialogDisplayer.getDefault().notify(new Message(msg, NotifyDescriptor.INFORMATION_MESSAGE));
                        conn.rollback();
                    }
                } catch (SQLException ex) {
                    errorMsg = "Commit Failed.";
                    DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.INFORMATION_MESSAGE));
                }
            } else {
                errorMsg = "Delete command failed for " + errorMsg;
                DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.INFORMATION_MESSAGE));
            }
            closeResources(pstmt, conn);
        }
        return 0;
    }

    public void setEditable(boolean edit) {
        this.isEditable = edit;
        this.table.setRowSelectionAllowed(edit);
    }

    public boolean isDirty() {
        if (!isDirty) {
            changes.clear();
        }
        return isDirty;
    }

    public void setDirtyStatus(boolean dirty) {
        isDirty = dirty;
        if (!isDirty) {
            changes.clear();
        }
    }

    private static class NullObjectCellRenderer extends DefaultTableCellRenderer {

        static String nbBundle1 = mLoc.t("BUND356: <NULL>");
        static final String NULL_LABEL = nbBundle1.substring(15);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setValue(NULL_LABEL);
            c.setForeground(Color.GRAY);
            return c;
        }
    }

    private static class ResultSetCellRenderer extends DefaultTableCellRenderer {

        static final TableCellRenderer NULL_RENDERER = new NullObjectCellRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return (null == value) ? NULL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) : super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    private static final String data = "WE WILL EITHER FIND A WAY, OR MAKE ONE.";
    /* Log4J category string */
    private static final String LOG_CATEGORY = ResultSetTablePanel.class.getName();
    int MAX_COLUMN_WIDTH = 50;
    /* TableModel containing contents of result set */
    protected TableModel model;
    private final int multiplier;
    /* JTable displaying contents of TableModel */
    protected JTable table;
    private JPopupMenu tablePopupMenu;

    /**
     * Constructs empty instance of SQLResultSetTableView. Call setResultSet(ResultSet) to
     * display the contents of a given ResultSet.
     *
     * @see #setResultSet
     */
    public ResultSetTablePanel() {
        this.setLayout(new BorderLayout());
        table = new JTable();

        // content popup menu on table with results
        tablePopupMenu = new JPopupMenu();
        JMenuItem miCopyValue = new JMenuItem("Copy Cell Value"); //NOI18N
        miCopyValue.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    Object o = table.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
                    String output = (o != null) ? o.toString() : ""; //NOI18N
                    ExClipboard clipboard = (ExClipboard) Lookup.getDefault().lookup(ExClipboard.class);
                    StringSelection strSel = new StringSelection(output);
                    clipboard.setContents(strSel, strSel);
                } catch (ArrayIndexOutOfBoundsException exc) {
                }
            }
        });
        tablePopupMenu.add(miCopyValue);

        JMenuItem miCopyRowValues = new JMenuItem("Copy Row Values"); //NOI18N
        miCopyRowValues.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                copyRowValues(false);
            }
        });
        tablePopupMenu.add(miCopyRowValues);

        JMenuItem miCopyRowValuesH = new JMenuItem("Copy Row Values With Header"); //NOI18N
        miCopyRowValuesH.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                copyRowValues(true);
            }
        });
        tablePopupMenu.add(miCopyRowValuesH);

        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new ResultSetCellRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane sp = new JScrollPane(table);
        this.add(sp, BorderLayout.CENTER);

        multiplier = table.getFontMetrics(table.getFont()).stringWidth(data) / data.length();

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int row = table.rowAtPoint(e.getPoint());
                    int column = table.columnAtPoint(e.getPoint());
                    boolean inSelection = false;
                    int[] rows = table.getSelectedRows();
                    for (int a = 0; a < rows.length; a++) {
                        if (rows[a] == row) {
                            inSelection = true;
                            break;
                        }
                    }
                    if (!table.getRowSelectionAllowed()) {
                        inSelection = false;
                        int[] columns = table.getSelectedColumns();
                        for (int a = 0; a < columns.length; a++) {
                            if (columns[a] == column) {
                                inSelection = true;
                                break;
                            }
                        }
                    }
                    if (!inSelection) {
                        table.changeSelection(row, column, false, false);
                    }
                    tablePopupMenu.show(table, e.getX(), e.getY());
                }
            }
        });

    }

    private void copyRowValues(boolean withHeader) {
        try {
            int[] rows = table.getSelectedRows();
            int[] columns;
            if (table.getRowSelectionAllowed()) {
                columns = new int[table.getColumnCount()];
                for (int a = 0; a < columns.length; a++) {
                    columns[a] = a;
                }
            } else {
                columns = table.getSelectedColumns();
            }
            if (rows != null && columns != null) {
                StringBuffer output = new StringBuffer();

                if (withHeader) {
                    for (int column = 0; column < columns.length; column++) {
                        if (column > 0) {
                            output.append('\t'); //NOI18N
                        }
                        Object o = table.getColumnModel().getColumn(column).getHeaderValue();
                        output.append(o != null ? o.toString() : ""); //NOI18N
                    }
                    output.append('\n'); //NOI18N
                }

                for (int row = 0; row < rows.length; row++) {
                    for (int column = 0; column < columns.length; column++) {
                        if (column > 0) {
                            output.append('\t'); //NOI18N
                        }
                        Object o = table.getValueAt(rows[row], columns[column]);
                        output.append(o != null ? o.toString() : ""); //NOI18N
                    }
                    output.append('\n'); //NOI18N
                }
                ExClipboard clipboard = (ExClipboard) Lookup.getDefault().lookup(ExClipboard.class);
                StringSelection strSel = new StringSelection(output.toString());
                clipboard.setContents(strSel, strSel);
            }
        } catch (ArrayIndexOutOfBoundsException exc) {
        }
    }

    public ResultSetTablePanel(DataOutputPanel panel) {
        this();
        this.dataPanel = panel;
    }

    /**
     * Constructs instance of SQLResultSetTableView to display the result sets contained
     * in the given Map.
     *
     * @param rsMap Map of ResultSets; possibly empty
     */
    public ResultSetTablePanel(Map rsMap) {
        this();
        setResultSet(rsMap);
    }

    /**
     * Constructs instance of SQLResultSetTableView to display the given result set.
     *
     * @param rsMap ResultSet
     */
    public ResultSetTablePanel(ResultSet rs) {
        this();
        setResultSet(rs);
    }

    public void clearView() {
        DataTableModel dtm = new DataTableModel();
        final TableSorter sorter = new TableSorter(dtm);
        sorter.setTableHeader(table.getTableHeader());

        Runnable run = new Runnable() {

            public void run() {
                table.setModel(sorter);
            }
        };
        SwingUtilities.invokeLater(run);
    }

    /**
     * Updates this view's data model with the results sets contained in the given Map.
     *
     * @param rsMap Map of ResultSets; possibly empty
     */
    public void setResultSet(Map rsMap) {
        if (rsMap == null) {
            throw new IllegalArgumentException("Must supply non-null Map reference for rsMap");
        }

        model = createModelFrom(rsMap);
        final TableModel tempModel = model;
        Runnable run = new Runnable() {

            public void run() {
                table.setModel(tempModel);
            }
        };
        SwingUtilities.invokeLater(run);
    }

    /**
     * Updates this view's data model to display the contents of the given ResultSet.
     *
     * @param rsMap new ResultSet to be displayed.
     */
    public void setResultSet(ResultSet rs) {
        this.setResultSet(rs, -1, 0);
    }

    /**
     * Updates this view's data model to display the contents of the given ResultSet.
     *
     * @param rsMap new ResultSet to be displayed.
     */
    public void setResultSet(ResultSet rs, int maxRowsToShow, int startFrom) {
        if (rs == null) {
            throw new IllegalArgumentException("Must supply non-null ResultSet reference for rs");
        }
        // Get RSMD before rsMap is iterated - DB2Connect Universal driver closes ResultSet
        // after rsMap.next() is advanced to end.
        List<Integer> columnWidthList = Collections.emptyList();
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            if (rsmd != null) {
                columnWidthList = getColumnWidthList(rsmd);
            }
        } catch (SQLException ignore) {
            // Could not obtain metadata - headers will not be displayed.
        }

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
    TableModel createModelFrom(ResultSet rs, int maxRowsToShow, int startFrom) {
        DataTableModel dtm = new DataTableModel();
        TableSorter sorter = new TableSorter(dtm);
        sorter.setTableHeader(table.getTableHeader());

        try {
            ResultSetMetaData md = rs.getMetaData();

            int colCt = md.getColumnCount();
            int[] colType = new int[colCt + 1];
            // Obtain display name
            for (int i = 1; i <= colCt; i++) {
                dtm.addColumn(md.getColumnLabel(i));
                int columnType = md.getColumnType(i);
                colType[i] = columnType;
            }

            Object[] row = new Object[colCt];
            int rowCnt = 0;

            boolean lastRowPicked = rs.next();
            while (lastRowPicked && rs.getRow() < (startFrom + 1)) {
                lastRowPicked = rs.next();
            }

            while (((maxRowsToShow == -1) || (maxRowsToShow > rowCnt)) && (lastRowPicked || rs.next())) {
                for (int i = 0; i < colCt; i++) {
                    row[i] = readResultSet(rs, colType[i + 1], i + 1);
                }
                dtm.addRow(row);
                rowCnt++;
                if (lastRowPicked) {
                    lastRowPicked = false;
                }
            }
        } catch (Exception e) {
            mLogger.errorNoloc(mLoc.t("EDIT146: Failed to set up table model({0})", LOG_CATEGORY), e);
        }

        return sorter;
    }

    /**
     * create a table model
     *
     * @param rsMap resultset
     * @return TableModel
     */
    TableModel createModelFrom(ResultSet rs, int maxRowsToShow) {
        return createModelFrom(rs, maxRowsToShow, 0);
    }

    @SuppressWarnings(value = "fallthrough")
    private Object readResultSet(ResultSet rs, int colType, int index) throws SQLException {
        switch (colType) {
            case Types.BIT:
            case Types.BOOLEAN: {
                boolean bdata = rs.getBoolean(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Boolean(bdata);
                }
            }
            case Types.TIME: {
                Time tdata = rs.getTime(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return tdata;
                }
            }
            case Types.DATE: {
                Date ddata = rs.getDate(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return ddata;
                }
            }
            case Types.TIMESTAMP:
            case -100: // -100 = Oracle timestamp
            {
                Timestamp tsdata = rs.getTimestamp(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return tsdata;
                }
            }
            case Types.BIGINT: {
                long ldata = rs.getLong(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Long(ldata);
                }
            }
            case Types.DOUBLE:
            case Types.FLOAT: {
                double fdata = rs.getDouble(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Double(fdata);
                }
            }
            case Types.REAL: {
                float rdata = rs.getFloat(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Float(rdata);
                }
            }
            case Types.DECIMAL:
            case Types.NUMERIC: {
                BigDecimal bddata = rs.getBigDecimal(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return bddata;
                }
            }
            case Types.INTEGER: {
                int idata = rs.getInt(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Integer(idata);
                }
            }
            case Types.SMALLINT: {
                short sidata = rs.getShort(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Short(sidata);
                }
            }
            case Types.TINYINT: {
                byte tidata = rs.getByte(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Byte(tidata);
                }
            }
            // JDBC/ODBC bridge JDK1.4 brings back -9 for nvarchar columns in
            // MS SQL Server tables.
            // -8 is ROWID in Oracle.
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case -9:
            case -8: {
                String sdata = rs.getString(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return sdata;
                }
            }
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY: {
                byte[] bdata = rs.getBytes(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    Byte[] internal = new Byte[bdata.length];
                    for (int i = 0; i < bdata.length; i++) {
                        internal[i] = new Byte(bdata[i]);
                    }
                    return BinaryToStringConverter.convertToString(internal, BinaryToStringConverter.HEX, false);
                }
            }
            case Types.BLOB: {
                // We always get the BLOB, even when we are not reading the contents.
                // Since the BLOB is just a pointer to the BLOB data rather than the
                // data itself, this operation should not take much time (as opposed
                // to getting all of the data in the blob).
                Blob blob = rs.getBlob(index);

                if (rs.wasNull()) {
                    return null;
                }
                // BLOB exists, so try to read the data from it
                byte[] blobData = null;
                if (blob != null) {
                    blobData = blob.getBytes(1, 255);
                }
                Byte[] internal = new Byte[blobData.length];
                for (int i = 0; i < blobData.length; i++) {
                    internal[i] = new Byte(blobData[i]);
                }
                return BinaryToStringConverter.convertToString(internal, BinaryToStringConverter.HEX, false);
            }
            case Types.CLOB: {
                // We always get the CLOB, even when we are not reading the contents.
                // Since the CLOB is just a pointer to the CLOB data rather than the
                // data itself, this operation should not take much time (as opposed
                // to getting all of the data in the clob).
                Clob clob = rs.getClob(index);

                if (rs.wasNull()) {
                    return null;
                }
                // CLOB exists, so try to read the data from it
                if (clob != null) {
                    return clob.getSubString(1, 255);
                }
            }
            case Types.OTHER:
            default:
                return rs.getObject(index);
        }
    }

    void setHeader(JTable table, List<Integer> columnWidthList) {
        try {
            TableColumnModel cModel = table.getColumnModel();
            for (int i = 0; i < columnWidthList.size(); i++) {
                TableColumn column = cModel.getColumn(i);
                column.setPreferredWidth(columnWidthList.get(i));
            }
            table.getTableHeader().setColumnModel(cModel);
        } catch (Exception e) {
            mLogger.errorNoloc(mLoc.t("EDIT147: Failed to set the size of the table headers({0})", LOG_CATEGORY), e);
        }
    }

    List<Integer> getColumnWidthList(ResultSetMetaData md) {
        List<Integer> columnWidthList = new ArrayList<Integer>();
        try {
            for (int i = 0; i < md.getColumnCount(); i++) {
                int fieldWidth = md.getColumnDisplaySize(i + 1);
                int labelWidth = md.getColumnLabel(i + 1).length();
                int colWidth = Math.max(fieldWidth, labelWidth) * multiplier;
                if (colWidth > MAX_COLUMN_WIDTH * multiplier) {
                    colWidth = MAX_COLUMN_WIDTH * multiplier;
                }
                columnWidthList.add(colWidth);
            }
        } catch (Exception e) {
            mLogger.errorNoloc(mLoc.t("EDIT147: Failed to set the size of the table headers({0})", LOG_CATEGORY), e);
        }
        return columnWidthList;
    }

    private TableModel createModelFrom(Map rsMap) {
        DataTableModel dtm = new DataTableModel();
        TableSorter sorter = new TableSorter(dtm);
        sorter.setTableHeader(table.getTableHeader());

        Object[] row = null;

        for (Object key : rsMap.keySet()) {
            String value = (String) rsMap.get(key);
            dtm.addColumn(key);
            row = new Object[value.length()];
        }

        int i = 0;
        for (Object value : rsMap.values()) {
            if (row != null) {
                row = new Object[rsMap.size()];
                row[i++] = value;
                dtm.addRow(row);
            }
        }

        return sorter;
    }

    void closeResources(PreparedStatement pstmt, Connection conn) {

        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException ex) {
            //ignore
        }

        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            //ignore
        }
    }

    private String generateWhereCondition(List<Integer> types, List values, int rowNum) {
        DBTableMetadata meta = dataPanel.meta;
        StringBuilder result = new StringBuilder();

        PrimaryKey key = meta.getTable().getPrimaryKey();
        if (key != null) {
            int j = 0;
            for (String keyName : key.getColumnNames()) {
                result.append((j++ != 0 ? " AND " : ""));
                for (int i = 0; i < table.getColumnCount(); i++) {
                    if (table.getColumnModel().getColumn(i).getHeaderValue().equals(keyName)) {
                        if (model.getValueAt(rowNum, i) != null) {
                            result.append(keyName + " = ? ");
                            values.add(model.getValueAt(rowNum, i));
                            types.add(meta.getColumnType(i));
                            break;
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < table.getColumnCount(); i++) {
                if (model.getValueAt(rowNum, i) != null) {
                    String columnName = meta.getColumnName(i);
                    result.append((i != 0 ? " AND " : ""));
                    result.append(columnName + " = ? ");
                    values.add(model.getValueAt(rowNum, i));
                    types.add(meta.getColumnType(i));
                }
            }
        }

        return result.toString();
    }
}
