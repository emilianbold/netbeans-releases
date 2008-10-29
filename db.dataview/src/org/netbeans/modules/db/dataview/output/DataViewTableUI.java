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

import java.util.EventObject;
import org.netbeans.modules.db.dataview.util.ExtendedJTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.windows.WindowManager;

/**
 * Renders the current result page
 *
 * @author Ahimanikya Satapathy
 */
class DataViewTableUI extends ExtendedJTable {

    private String[] columnToolTips;
    private JPopupMenu tablePopupMenu;
    private final int multiplier;
    private final DataViewTablePanel tablePanel;
    private static final String data = "WE WILL EITHER FIND A WAY, OR MAKE ONE."; // NOI18N
    private static Logger mLogger = Logger.getLogger(DataViewTableUI.class.getName());

    public DataViewTableUI(final DataViewTablePanel tablePanel, final DataViewActionHandler handler, final DataView dataView) {
        this.tablePanel = tablePanel;
        addKeyListener(new Control0KeyListener());
        getTableHeader().setReorderingAllowed(false);

        setDefaultRenderer(Object.class, new ResultSetCellRenderer());
        setDefaultRenderer(String.class, new ResultSetCellRenderer());
        setDefaultRenderer(Number.class, new ResultSetCellRenderer());
        setDefaultRenderer(java.util.Date.class, new ResultSetCellRenderer());

        setDefaultEditor(Object.class, new ResultSetTableCellEditor(new JTextField()));
        setDefaultEditor(Number.class, new NumberEditor(new JTextField()));
        setDefaultEditor(String.class, new StringTableCellEditor(new JTextField()));

        TableSelectionListener listener = new TableSelectionListener(this);
        this.getSelectionModel().addListSelectionListener(listener);
        this.getColumnModel().getSelectionModel().addListSelectionListener(listener);

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        multiplier = getFontMetrics(getFont()).stringWidth(data) / data.length() + 3;
        setRowHeight(getFontMetrics(getFont()).getHeight() + 5);

        createPopupMenu(handler, dataView);
    }

    @Override
    //Implement table header tool tips.
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeaderImpl(columnModel);
    }

    protected int getMultiplier() {
        return multiplier;
    }

    protected void setColumnToolTips(String[] columnToolTips) {
        this.columnToolTips = columnToolTips;
    }

    private String getColumnToolTipText(MouseEvent e) {
        java.awt.Point p = e.getPoint();
        int index = columnModel.getColumnIndexAtX(p.x);
        try {
            int realIndex = columnModel.getColumn(index).getModelIndex();
            return columnToolTips[realIndex];
        } catch (ArrayIndexOutOfBoundsException aio) {
            return null;
        }
    }

    private UpdatedRowContext getResultSetRowContext() {
        return tablePanel.getUpdatedRowContext();
    }

    private void createPopupMenu(final DataViewActionHandler handler, final DataView dataView) {
        // content popup menu on table with results
        tablePopupMenu = new JPopupMenu();
        final JMenuItem miInsertAction = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_insert"));
        miInsertAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.insertActionPerformed();
            }
        });
        tablePopupMenu.add(miInsertAction);


        final JMenuItem miDeleteAction = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_deleterow"));
        miDeleteAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.deleteRecordActionPerformed();
            }
        });
        tablePopupMenu.add(miDeleteAction);

        final JMenuItem miCommitAction = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_commit"));
        miCommitAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.commitActionPerformed(true);
            }
        });
        tablePopupMenu.add(miCommitAction);

        final JMenuItem miCancelEdits = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_cancel_edits"));
        miCancelEdits.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.cancelEditPerformed();
            }
        });
        tablePopupMenu.add(miCancelEdits);

        final JMenuItem miTruncateRecord = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_truncate_table"));
        miTruncateRecord.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.truncateActionPerformed();
            }
        });
        tablePopupMenu.add(miTruncateRecord);
        tablePopupMenu.addSeparator();

        final JMenuItem miCopyValue = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_copy_cell_value"));
        miCopyValue.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    Object o = getValueAt(getSelectedRow(), getSelectedColumn());
                    String output = (o != null) ? o.toString() : ""; //NOI18N

                    ExClipboard clipboard = (ExClipboard) Lookup.getDefault().lookup(ExClipboard.class);
                    StringSelection strSel = new StringSelection(output);
                    clipboard.setContents(strSel, strSel);
                } catch (ArrayIndexOutOfBoundsException exc) {
                }
            }
        });
        tablePopupMenu.add(miCopyValue);

        final JMenuItem miCopyRowValues = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_copy_row_value"));
        miCopyRowValues.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                copyRowValues(false);
            }
        });
        tablePopupMenu.add(miCopyRowValues);

        final JMenuItem miCopyRowValuesH = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_copy_row_header"));
        miCopyRowValuesH.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                copyRowValues(true);
            }
        });
        tablePopupMenu.add(miCopyRowValuesH);
        tablePopupMenu.addSeparator();

        final JMenuItem miCreateSQLScript = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_show_create_sql"));
        miCreateSQLScript.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    DBTable table = dataView.getDataViewDBTable().geTable(0);
                    String createSQL = dataView.getSQLStatementGenerator().generateCreateStatement(table);
                    ShowSQLDialog dialog = new ShowSQLDialog();
                    dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                    dialog.setText(createSQL + ";\n"); // NOI18N
                    dialog.setVisible(true);
                } catch (Exception ex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage());
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        });
        tablePopupMenu.add(miCreateSQLScript);

        final JMenuItem miInsertSQLScript = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_show_insert_sql"));
        miInsertSQLScript.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    int[] rows = getSelectedRows();
                    String insertSQL = "";
                    for (int j = 0; j < rows.length; j++) {
                        Object[] insertRow = dataView.getDataViewPageContext().getCurrentRows().get(rows[j]);
                        String sql = dataView.getSQLStatementGenerator().generateInsertStatement(insertRow)[1];
                        insertSQL += sql.replaceAll("\n", "").replaceAll("\t", "") + ";\n"; // NOI18N
                    }
                    ShowSQLDialog dialog = new ShowSQLDialog();
                    dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                    dialog.setText(insertSQL);
                    dialog.setVisible(true);
                } catch (DBException ex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage());
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        });
        tablePopupMenu.add(miInsertSQLScript);

        final JMenuItem miDeleteSQLScript = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_show_delete_sql"));
        miDeleteSQLScript.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int[] rows = getSelectedRows();
                String rawDeleteStmt = "";
                for (int j = 0; j < rows.length; j++) {

                    final List<Object> values = new ArrayList<Object>();
                    final List<Integer> types = new ArrayList<Integer>();

                    SQLStatementGenerator generator = dataView.getSQLStatementGenerator();
                    final String[] deleteStmt = generator.generateDeleteStatement(types, values, rows[j], getModel());
                    rawDeleteStmt += deleteStmt[1] + ";\n"; // NOI18N
                }
                ShowSQLDialog dialog = new ShowSQLDialog();
                dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                dialog.setText(rawDeleteStmt);
                dialog.setVisible(true);
            }
        });
        tablePopupMenu.add(miDeleteSQLScript);

        final JMenuItem miCommitSQLScript = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_show_update_sql"));
        miCommitSQLScript.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String rawUpdateStmt = "";
                UpdatedRowContext tblContext = dataView.getUpdatedRowContext();
                if (tblContext.getUpdateKeys().isEmpty()) {
                    return;
                }
                for (String key : tblContext.getUpdateKeys()) {
                    rawUpdateStmt += tblContext.getRawUpdateStmt((key)) + ";\n"; // NOI18N
                }
                ShowSQLDialog dialog = new ShowSQLDialog();
                dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                dialog.setText(rawUpdateStmt);
                dialog.setVisible(true);
            }
        });
        tablePopupMenu.add(miCommitSQLScript);

        tablePopupMenu.addSeparator();

        JMenuItem printTable = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_print_data"));

        printTable.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    if (!print()) {
                        mLogger.log(Level.INFO, NbBundle.getMessage(DataViewTableUI.class, "MSG_cancel_printing"));
                    }
                } catch (java.awt.print.PrinterException ex) {
                    mLogger.log(Level.INFO, NbBundle.getMessage(DataViewTableUI.class, "MSG_failure_to_print" + ex.getMessage()));
                }
            }
        });
        tablePopupMenu.add(printTable);

        JMenuItem miRefreshAction = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_refresh"));
        miRefreshAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.refreshActionPerformed();
            }
        });
        tablePopupMenu.add(miRefreshAction);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int row = rowAtPoint(e.getPoint());
                    int column = columnAtPoint(e.getPoint());
                    boolean inSelection = false;
                    int[] rows = getSelectedRows();
                    for (int a = 0; a < rows.length; a++) {
                        if (rows[a] == row) {
                            inSelection = true;
                            break;
                        }
                    }
                    if (!getRowSelectionAllowed()) {
                        inSelection = false;
                        int[] columns = getSelectedColumns();
                        for (int a = 0; a < columns.length; a++) {
                            if (columns[a] == column) {
                                inSelection = true;
                                break;
                            }
                        }
                    }
                    if (!inSelection) {
                        changeSelection(row, column, false, false);
                    }
                    if (!tablePanel.isEditable()) {
                        miInsertAction.setEnabled(false);
                        miDeleteAction.setEnabled(false);
                        miTruncateRecord.setEnabled(false);
                        miInsertSQLScript.setEnabled(false);
                        miCreateSQLScript.setEnabled(false);
                        miDeleteSQLScript.setEnabled(false);
                    }

                    if (!tablePanel.isCommitEnabled()) {
                        miCommitAction.setEnabled(false);
                        miCancelEdits.setEnabled(false);
                        miCommitSQLScript.setEnabled(false);
                    } else {
                        miCommitAction.setEnabled(true);
                        miCancelEdits.setEnabled(true);
                        miCommitSQLScript.setEnabled(true);
                    }
                    tablePopupMenu.show(DataViewTableUI.this, e.getX(), e.getY());
                }
            }
        });
    }

    private void copyRowValues(boolean withHeader) {
        try {
            int[] rows = getSelectedRows();
            int[] columns;
            if (getRowSelectionAllowed()) {
                columns = new int[getColumnCount()];
                for (int a = 0; a < columns.length; a++) {
                    columns[a] = a;
                }
            } else {
                columns = getSelectedColumns();
            }
            if (rows != null && columns != null) {
                StringBuffer output = new StringBuffer();

                if (withHeader) {
                    for (int column = 0; column < columns.length; column++) {
                        if (column > 0) {
                            output.append('\t'); //NOI18N

                        }
                        Object o = getColumnModel().getColumn(column).getHeaderValue();
                        output.append(o != null ? o.toString() : ""); //NOI18N

                    }
                    output.append('\n'); //NOI18N

                }

                for (int row = 0; row < rows.length; row++) {
                    for (int column = 0; column < columns.length; column++) {
                        if (column > 0) {
                            output.append('\t'); //NOI18N

                        }
                        Object o = getValueAt(rows[row], columns[column]);
                        output.append(o != null ? o.toString() : ""); //NOI18N

                    }
                    output.append('\n'); //NOI18N

                }
                ExClipboard clipboard = (ExClipboard) Lookup.getDefault().lookup(ExClipboard.class);
                StringSelection strSel = new StringSelection(output.toString());
                clipboard.setContents(strSel, strSel);
            }
        } catch (ArrayIndexOutOfBoundsException exc) {
            Exceptions.printStackTrace(exc);
        }
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        DataViewTableSorter model = (DataViewTableSorter) getModel();
        row = model.modelIndex(row);
        DBColumn dbCol = tablePanel.getDataViewDBTable().getColumn(column);
        if (dbCol.isGenerated()) {
            return new GeneratedResultSetCellRenderer();
        } else if (getResultSetRowContext().getValueList((row + 1) + ";" + (column + 1)) != null) { // NOI18N
            return new UpdatedResultSetCellRenderer();
        }
        return super.getCellRenderer(row, column);
    }

    private static class NullObjectCellRenderer extends DefaultTableCellRenderer {

        static final String NULL_LABEL = "<NULL>"; // NOI18N

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setValue(NULL_LABEL);
            setToolTipText(NULL_LABEL);
            c.setForeground(Color.GRAY);
            return c;
        }
    }

    private static class NumberObjectCellRenderer extends DefaultTableCellRenderer.UIResource {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            ((JLabel) c).setHorizontalAlignment(JLabel.RIGHT);
            setToolTipText(value.toString());
            return c;
        }
    }

    private static final class StringRenderer extends DefaultTableCellRenderer.UIResource {

        public StringRenderer() {
            super();
            super.putClientProperty("html.disable", Boolean.TRUE);
        }
    }

    private static class DateTimeRenderer extends DefaultTableCellRenderer.UIResource {

        DateFormat formatter;

        public DateTimeRenderer() {
            super();
        }

        @Override
        public void setValue(Object value) {
            if (formatter == null) {
                formatter = DateFormat.getDateTimeInstance();
            }
            setText((value == null) ? "" : formatter.format(value)); // NOI18N
            setToolTipText(getText());
        }
    }

    private static class TimeRenderer extends DefaultTableCellRenderer.UIResource {

        DateFormat formatter;

        public TimeRenderer() {
            super();
        }

        @Override
        public void setValue(Object value) {
            if (formatter == null) {
                formatter = DateFormat.getTimeInstance();
            }
            setText((value == null) ? "" : formatter.format(value)); // NOI18N
            setToolTipText(getText());
        }
    }

    private static class DateRenderer extends DefaultTableCellRenderer.UIResource {

        DateFormat formatter;

        public DateRenderer() {
            super();
        }

        @Override
        public void setValue(Object value) {
            if (formatter == null) {
                formatter = DateFormat.getDateInstance();
            }
            setText((value == null) ? "" : formatter.format(value)); // NOI18N
            setToolTipText(getText());
        }
    }

    private static class GeneratedResultSetCellRenderer extends ResultSetCellRenderer {

        static Color gray = new Color(245, 245, 245);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(gray);
            } else {
                c.setBackground(table.getSelectionBackground());
            }
            return c;
        }
    }

    private static class ResultSetCellRenderer extends DefaultTableCellRenderer.UIResource {

        final TableCellRenderer NULL_RENDERER = new NullObjectCellRenderer();
        final TableCellRenderer NUMNBER_RENDERER = new NumberObjectCellRenderer();
        final TableCellRenderer TIME_RENDERER = new TimeRenderer();
        final TableCellRenderer DATE_RENDERER = new DateRenderer();
        final TableCellRenderer DATETIME_RENDERER = new DateTimeRenderer();
        final TableCellRenderer STRING_RENDERER = new StringRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (null == value) {
                return NULL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else if (value instanceof Number) {
                return STRING_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else if (value instanceof Number) {
                return NUMNBER_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else if (value instanceof Timestamp) {
                return DATETIME_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else if (value instanceof Date) {
                return DATE_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else if (value instanceof Time) {
                return TIME_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JComponent) {
                    String tooltip = "<html><table border=0 cellspacing=0 cellpadding=0 width=40><tr><td>"+ 
                    DataViewUtils.escapeHTML(value.toString()).replaceAll("\\n", "<br>")
                            .replaceAll(" ", "&nbsp;") + "</td></tr></table></html>";
                    ((JComponent) c).setToolTipText(tooltip);
                }
                return c;
            }
        }
    }

    private static class UpdatedResultSetCellRenderer extends ResultSetCellRenderer {

        static Color green = new Color(0, 128, 0);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                c.setForeground(Color.ORANGE);
            } else {
                c.setForeground(green);
            }
            return c;
        }
    }

    private class JTableHeaderImpl extends JTableHeader {

        public JTableHeaderImpl(TableColumnModel cm) {
            super(cm);
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            return getColumnToolTipText(e);
        }
    }

    private class ResultSetTableCellEditor extends DefaultCellEditor {

        Object val;

        public ResultSetTableCellEditor(final JTextField textField) {
            super(textField);
            textField.setFont(getFont());
            delegate = new EditorDelegate() {

                @Override
                public void setValue(Object value) {
                    val = value;
                    textField.setText((value != null) ? value.toString() : ""); // NOI18N
                }

                @Override
                public boolean isCellEditable(EventObject evt) {                    
                    int clickcount;
                    if (evt instanceof MouseEvent) {
                        if (System.getProperty("os.name").contains("Mac")) {
                            clickcount = 1;
                            return ((MouseEvent) evt).getClickCount() >= clickcount;
                        }else{
                            clickcount = 2;
                            return ((MouseEvent) evt).getClickCount() >= clickcount;
                        }
                    }
                    return true;
                }

                @Override
                public Object getCellEditorValue() {
                    String txtVal = textField.getText();
                    if (val == null && txtVal.equals("")) { // NOI18N
                        return null;
                    } else {
                        try {
                            int col = getEditingColumn();
                            DBColumn dbcol = DataViewTableUI.this.tablePanel.getDataViewDBTable().getColumn(col);
                            return DBReadWriteHelper.validate(txtVal, dbcol);
                        } catch (Exception ex) {
                            StatusDisplayer.getDefault().setStatusText(ex.getMessage());
                            return txtVal;
                        }
                    }
                }
            };

            textField.addActionListener(delegate);
            textField.addKeyListener(new Control0KeyListener());
        }
    }

    private class NumberEditor extends ResultSetTableCellEditor {

        public NumberEditor(final JTextField textField) {
            super(textField);
            ((JTextField) getComponent()).setHorizontalAlignment(JTextField.RIGHT);
        }
    }

    private class Control0KeyListener implements KeyListener {

        public Control0KeyListener() {
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            if (e.isControlDown() && e.getKeyChar() == KeyEvent.VK_0) {

                int row = getSelectedRow();
                int col = getSelectedColumn();
                if (row == -1) {
                    return;
                }
                editCellAt(row, col);

                TableCellEditor editor = getCellEditor();
                if (editor != null) {
                    DBColumn dbcol = DataViewTableUI.this.tablePanel.getDataViewDBTable().getColumn(col);
                    if (dbcol.isGenerated() || !dbcol.isNullable()) {
                        Toolkit.getDefaultToolkit().beep();
                        editor.stopCellEditing();
                    } else {
                        editor.getTableCellEditorComponent(DataViewTableUI.this, null, rowSelectionAllowed, row, col);
                        setValueAt(null, row, col);
                        editor.stopCellEditing();
                    }
                    setRowSelectionInterval(row, row);
                }
            }
        }

        public void keyReleased(KeyEvent e) {
        }
    }

    public class TableSelectionListener implements ListSelectionListener {

        JTable table;

        TableSelectionListener(JTable table) {
            this.table = table;
        }

        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
                int first = e.getFirstIndex();
                if (first >= 0 && tablePanel.isEditable()) {
                    tablePanel.enableDeleteBtn(true);
                } else {
                    tablePanel.enableDeleteBtn(false);
                }
            }
        }
    }

    public class StringTableCellEditor extends ResultSetTableCellEditor implements TableCellEditor, ActionListener {

        private JButton customEditorButton = new JButton("...");
        private JPanel panel = new JPanel(new BorderLayout());
        private JTable table;
        private int row,  column;
        private boolean editable = true;

        public StringTableCellEditor(final JTextField textField) {
            super(textField);
            customEditorButton.addActionListener(this);

            // ui-tweaking
            customEditorButton.setFocusable(false);
            customEditorButton.setFocusPainted(false);
            customEditorButton.setMargin(new Insets(0, 0, 0, 0));
            customEditorButton.setPreferredSize(new Dimension(20, 10));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            panel.removeAll();
            Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);

            DBColumn dbCol = tablePanel.getDataViewDBTable().getColumn(column);
            if (dbCol.isGenerated()) {
                editable = false;
            } else if (!tablePanel.isEditable()) {
                editable = false;
            } else {
                editable = dbCol.isEditable();
            }

            c.setEnabled(editable);

            panel.add(c);
            panel.add(customEditorButton, BorderLayout.EAST);
            panel.revalidate();
            panel.repaint();

            this.table = table;
            this.row = row;
            this.column = column;
            return panel;
        }

        public final void actionPerformed(ActionEvent e) {
            super.cancelCellEditing();
            editCell(table, row, column);
        }

        protected void editCell(JTable table, int row, int column) {
            JTextArea textArea = new JTextArea(10, 50);
            Object value = table.getValueAt(row, column);
            if (value != null) {
                textArea.setText((String) value);
                textArea.setCaretPosition(0);
                textArea.setEditable(editable);
            }
            JScrollPane pane = new JScrollPane(textArea);
            Component parent = WindowManager.getDefault().getMainWindow();

            if (editable) {
                int result = JOptionPane.showOptionDialog(parent, pane, (String) table.getColumnName(column), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
                if (result == JOptionPane.OK_OPTION) {
                    table.setValueAt(textArea.getText(), row, column);
                }
            } else {
                JOptionPane.showMessageDialog(parent, pane, (String) table.getColumnName(column), JOptionPane.PLAIN_MESSAGE, null);
            }
        }
    }
}
