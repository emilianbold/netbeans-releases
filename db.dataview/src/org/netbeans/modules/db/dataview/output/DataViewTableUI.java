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

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.db.dataview.logger.Localizer;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExClipboard;

/**
 * Renders the current result page
 *
 * @author Ahimanikya Satapathy
 */
class DataViewTableUI extends JTable {

    private String[] columnToolTips;
    private JPopupMenu tablePopupMenu;
    private final int multiplier;
    private final DataViewTablePanel tablePanel;
    private static transient final Localizer mLoc = Localizer.get();
    private static final String data = "WE WILL EITHER FIND A WAY, OR MAKE ONE."; // NOI18N
    private static Logger mLogger = Logger.getLogger(DataViewTableUI.class.getName());
    
    public DataViewTableUI(final DataViewTablePanel tablePanel, final DataViewActionHandler handler, final DataView dataView) {
        this.tablePanel = tablePanel;
        addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyChar() == KeyEvent.VK_0) {
                    int row = getSelectedRow();
                    int col = getSelectedColumn();
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
                    }
                }
            }

            public void keyReleased(KeyEvent e) {
            }
        });

        // content popup menu on table with results
        tablePopupMenu = new JPopupMenu();
        String nbBundle15 = mLoc.t("RESC015: Print Table Data");
        JMenuItem printTable = new JMenuItem(nbBundle15.substring(15)); 

        printTable.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    if (!print()) {
                        String nbBundle16 = mLoc.t("RESC016: User cancelled printing");
                        System.err.println(nbBundle16.substring(15));
                    }
                } catch (java.awt.print.PrinterException ex) {
                    mLogger.infoNoloc(mLoc.t("LOGR023: Cannot print %s%n",ex.getMessage()));
                    System.err.format("Cannot print %s%n", ex.getMessage());
                }
            }
        });
        tablePopupMenu.add(printTable);

        String nbBundle17 = mLoc.t("RESC017: Refresh Records");
        JMenuItem miRefreshAction = new JMenuItem(nbBundle17.substring(15)); 
        miRefreshAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.refreshActionPerformed();
            }
        });
        tablePopupMenu.add(miRefreshAction);
        tablePopupMenu.addSeparator();

        String nbBundle18 = mLoc.t("RESC018: Insert Record");
        final JMenuItem miInsertAction = new JMenuItem(nbBundle18.substring(15)); 
        miInsertAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.insertActionPerformed();
            }
        });
        tablePopupMenu.add(miInsertAction);

        String nbBundle19 = mLoc.t("RESC019: Delete Record(s)");
        final JMenuItem miDeleteAction = new JMenuItem(nbBundle19.substring(15)); 
        miDeleteAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.deleteRecordActionPerformed();
            }
        });
        tablePopupMenu.add(miDeleteAction);

        String nbBundle20 = mLoc.t("RESC020: Commit Record(s)");
        final JMenuItem miCommitAction = new JMenuItem(nbBundle20.substring(15)); 
        miCommitAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.commitActionPerformed();
            }
        });
        tablePopupMenu.add(miCommitAction);


        String nbBundle21 = mLoc.t("RESC021: Cancel Edits");
        final JMenuItem miCancelEdits = new JMenuItem(nbBundle21.substring(15)); 
        miCancelEdits.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.cancelEditPerformed();
            }
        });
        tablePopupMenu.add(miCancelEdits);

        String nbBundle22 = mLoc.t("RESC022: Truncate Table");
        final JMenuItem miTruncateRecord = new JMenuItem(nbBundle22.substring(15)); 
        miTruncateRecord.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.truncateActionPerformed();
            }
        });
        tablePopupMenu.add(miTruncateRecord);
        tablePopupMenu.addSeparator();

        String nbBundle23 = mLoc.t("RESC023: Copy Cell Value");
        final JMenuItem miCopyValue = new JMenuItem(nbBundle23.substring(15)); 
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

        String nbBundle24 = mLoc.t("RESC024: Copy Row Values");
        final JMenuItem miCopyRowValues = new JMenuItem(nbBundle24.substring(15)); 
        miCopyRowValues.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                copyRowValues(false);
            }
        });
        tablePopupMenu.add(miCopyRowValues);

        String nbBundle25 = mLoc.t("RESC025: Copy Row Values(With Header)");
        final JMenuItem miCopyRowValuesH = new JMenuItem(nbBundle25.substring(15)); 
        miCopyRowValuesH.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                copyRowValues(true);
            }
        });
        tablePopupMenu.add(miCopyRowValuesH);
        tablePopupMenu.addSeparator();

        String nbBundle26 = mLoc.t("RESC026: Show SQL Script for INSERT");
        final JMenuItem miInsertSQLScript = new JMenuItem(nbBundle26.substring(15));
        miInsertSQLScript.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    int[] rows = getSelectedRows();
                    String insertSQL = "";
                    for (int j = 0; j < rows.length; j++) {
                        Object[] insertRow = dataView.getDataViewPageContext().getCurrentRows().get(rows[j]);
                        String sql = dataView.getSQLStatementGenerator().generateInsertStatement(insertRow)[1];
                        insertSQL += sql.replaceAll("\n", "").replaceAll("\t", "") + "\n"; // NOI18N
                    }
                    ShowSQLDialog dialog = new ShowSQLDialog();
                    dialog.setLocationRelativeTo(DataViewTableUI.this);
                    dialog.setText(insertSQL);
                    dialog.setVisible(true);
                } catch (DBException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        tablePopupMenu.add(miInsertSQLScript);

        String nbBundle27 = mLoc.t("RESC027: Show SQL Script for DELETE");
        final JMenuItem miDeleteSQLScript = new JMenuItem(nbBundle27.substring(15)); 
        miDeleteSQLScript.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int[] rows = getSelectedRows();
                String rawDeleteStmt = "";
                for (int j = 0; j < rows.length; j++) {

                    final List<Object> values = new ArrayList<Object>();
                    final List<Integer> types = new ArrayList<Integer>();

                    SQLStatementGenerator generator = dataView.getSQLStatementGenerator();
                    final String[] deleteStmt = generator.generateDeleteStatement(types, values, rows[j], getModel());
                    rawDeleteStmt += deleteStmt[1] + "\n"; // NOI18N
                }
                ShowSQLDialog dialog = new ShowSQLDialog();
                dialog.setLocationRelativeTo(DataViewTableUI.this);
                dialog.setText(rawDeleteStmt);
                dialog.setVisible(true);
            }
        });
        tablePopupMenu.add(miDeleteSQLScript);

        String nbBundle28 = mLoc.t("RESC028: Show SQL Script for UPDATE");
        final JMenuItem miCommitSQLScript = new JMenuItem(nbBundle28.substring(15));
        miCommitSQLScript.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String rawUpdateStmt = "";
                UpdatedRowContext tblContext = dataView.getUpdatedRowContext();
                if (tblContext.getUpdateKeys().isEmpty()) {
                    return;
                }
                for (String key : tblContext.getUpdateKeys()) {
                    rawUpdateStmt += tblContext.getRawUpdateStmt((key)) + "\n"; // NOI18N
                }
                ShowSQLDialog dialog = new ShowSQLDialog();
                dialog.setLocationRelativeTo(DataViewTableUI.this);
                dialog.setText(rawUpdateStmt);
                dialog.setVisible(true);
            }
        });
        tablePopupMenu.add(miCommitSQLScript);

        getTableHeader().setReorderingAllowed(false);
        setDefaultRenderer(Object.class, new ResultSetCellRenderer());
        setDefaultEditor(Object.class, new ResultSetTableCellEditor(new JTextField()));
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        multiplier = getFontMetrics(getFont()).stringWidth(data) / data.length() + 5;

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
                        miDeleteSQLScript.setEnabled(false);
                        miCommitAction.setEnabled(false);
                        miCancelEdits.setEnabled(false);
                        miCommitSQLScript.setEnabled(false);
                    }
                    tablePopupMenu.show(DataViewTableUI.this, e.getX(), e.getY());
                }
            }
        });
    }

    class ResultSetTableCellEditor extends DefaultCellEditor {

        Object val;

        public ResultSetTableCellEditor(final JTextField textField) {
            super(textField);
            delegate = new EditorDelegate() {

                @Override
                public void setValue(Object value) {
                    val = value;
                    textField.setText((value != null) ? value.toString() : ""); // NOI18N
                }

                @Override
                public Object getCellEditorValue() {
                    String txtVal = textField.getText();
                    if (val == null && textField.getText().equals("")) { // NOI18N
                        return null;
                    } else if(val != null && val.toString().equals(txtVal)){
                        return val;
                    } else {
                        return txtVal;
                    }
                }
            };
            textField.addActionListener(delegate);
            textField.addKeyListener(new KeyListener() {

                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                    if (e.isControlDown() && e.getKeyChar() == KeyEvent.VK_0) {
                        int col = getEditingColumn();
                        DBColumn dbcol = DataViewTableUI.this.tablePanel.getDataViewDBTable().getColumn(col);
                        if (dbcol.isGenerated() || !dbcol.isNullable()) {
                            Toolkit.getDefaultToolkit().beep();
                        } else {
                            delegate.setValue(null);
                            ResultSetTableCellEditor.this.stopCellEditing();
                        }
                    }
                }

                public void keyReleased(KeyEvent e) {
                }
            });
        }
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

    @Override
    public String getToolTipText(MouseEvent e) {
        return getColumnToolTipText(e);
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
            throw new RuntimeException(exc);
        }
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        DataViewTableSorter model =  (DataViewTableSorter)getModel();
        row = model.modelIndex(row);
        if (tablePanel.getDataViewDBTable().getColumn(column).isGenerated()) {
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
            c.setForeground(Color.GRAY);
            return c;
        }
    }

    private static class GeneratedResultSetCellRenderer extends DefaultTableCellRenderer {

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

        static final TableCellRenderer NULL_RENDERER = new NullObjectCellRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (null == value) {
                return NULL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
    }

    private static class UpdatedResultSetCellRenderer extends DefaultTableCellRenderer.UIResource {

        static Color green = new Color(0, 128, 0);
        static final TableCellRenderer NULL_RENDERER = new NullObjectCellRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c;
            if (null == value) {
                c = NULL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
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
}
