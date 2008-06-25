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
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
    private static final String data = "WE WILL EITHER FIND A WAY, OR MAKE ONE.";

    public DataViewTableUI(DataViewTablePanel tablePanel, final DataViewActionHandler handler, final DataView dataView) {
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
        JMenuItem printTable = new JMenuItem("Print Table Data"); //NOI18N

        printTable.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    if (!print()) {
                        System.err.println("User cancelled printing");
                    }
                } catch (java.awt.print.PrinterException ex) {
                    System.err.format("Cannot print %s%n", ex.getMessage());
                }
            }
        });
        tablePopupMenu.add(printTable);

        JMenuItem miRefreshAction = new JMenuItem("Refresh Records"); //NOI18N
        miRefreshAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.refreshActionPerformed();
            }
        });
        tablePopupMenu.add(miRefreshAction);
        tablePopupMenu.addSeparator();

        JMenuItem miInsertAction = new JMenuItem("Insert Record"); //NOI18N
        miInsertAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.insertActionPerformed();
            }
        });
        tablePopupMenu.add(miInsertAction);

        JMenuItem miDeleteAction = new JMenuItem("Delete Record(s)"); //NOI18N
        miDeleteAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.deleteRecordActionPerformed();
            }
        });
        tablePopupMenu.add(miDeleteAction);

        JMenuItem miCommitAction = new JMenuItem("Commit Record(s)"); //NOI18N
        miCommitAction.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.commitActionPerformed();
            }
        });
        tablePopupMenu.add(miCommitAction);


        JMenuItem miCancelEdits = new JMenuItem("Cancel Edits"); //NOI18N
        miCancelEdits.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.cancelEditPerformed();
            }
        });
        tablePopupMenu.add(miCancelEdits);

        JMenuItem miTruncateRecord = new JMenuItem("Truncate Table"); //NOI18N
        miTruncateRecord.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.truncateActionPerformed();
            }
        });
        tablePopupMenu.add(miTruncateRecord);
        tablePopupMenu.addSeparator();
        if (!tablePanel.isEditable()) {
            miInsertAction.setEnabled(false);
            miDeleteAction.setEnabled(false);
            miTruncateRecord.setEnabled(false);
        }

        JMenuItem miCopyValue = new JMenuItem("Copy Cell Value"); //NOI18N
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

        JMenuItem miCopyRowValues = new JMenuItem("Copy Row Values"); //NOI18N
        miCopyRowValues.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                copyRowValues(false);
            }
        });
        tablePopupMenu.add(miCopyRowValues);

        JMenuItem miCopyRowValuesH = new JMenuItem("Copy Row Values(With Header)"); //NOI18N
        miCopyRowValuesH.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                copyRowValues(true);
            }
        });
        tablePopupMenu.add(miCopyRowValuesH);
        tablePopupMenu.addSeparator();

        JMenuItem miInsertSQLScript = new JMenuItem("Show SQL Script for INSERT"); //NOI18N
        miInsertSQLScript.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    Object[] insertRow = dataView.getDataViewPageContext().getCurrentRows().get(getSelectedRow());
                    String insertSQL = dataView.getSQLStatementGenerator().generateInsertStatement(insertRow)[1];
                    NotifyDescriptor nd = new NotifyDescriptor.Message(insertSQL);
                    //  JOptionPane.showMessageDialog(new JEditorPane(), insertSQL);
                    DialogDisplayer.getDefault().notify(nd);
                } catch (DBException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        tablePopupMenu.add(miInsertSQLScript);

        JMenuItem miDeleteSQLScript = new JMenuItem("Show SQL Script for DELETE"); //NOI18N
        miDeleteSQLScript.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int[] rows = getSelectedRows();
                String rawDeleteStmt = "";
                for (int j = 0; j < rows.length; j++) {

                    final List<Object> values = new ArrayList<Object>();
                    final List<Integer> types = new ArrayList<Integer>();

                    SQLStatementGenerator generator = dataView.getSQLStatementGenerator();
                    final String[] deleteStmt = generator.generateDeleteStatement(types, values, getSelectedRow(), getModel());
                    rawDeleteStmt += deleteStmt[1];
                }
                NotifyDescriptor nd = new NotifyDescriptor.Message(rawDeleteStmt);
                DialogDisplayer.getDefault().notify(nd);

            }
        });
        tablePopupMenu.add(miDeleteSQLScript);

        JMenuItem miCommitSQLScript = new JMenuItem("Show SQL Script for UPDATE"); //NOI18N
        miCommitSQLScript.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String rawUpdateStmt = "";
                UpdatedRowContext tblContext = dataView.getUpdatedRowContext();
                for (String key : tblContext.getUpdateKeys()) {
                    rawUpdateStmt += tblContext.getRawUpdateStmt((key)) + "\n";
                }
                NotifyDescriptor nd = new NotifyDescriptor.Message(rawUpdateStmt);
                DialogDisplayer.getDefault().notify(nd);

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
                    textField.setText((value != null) ? value.toString() : "");
                }

                @Override
                public Object getCellEditorValue() {
                    String txtVal = textField.getText();
                    if (val == null && textField.getText().equals("")) {
                        return null;
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
        if (tablePanel.getDataViewDBTable().getColumn(column).isGenerated()) {
            return new GeneratedResultSetCellRenderer();
        } else if (getResultSetRowContext().getValueList((row + 1) + ";" + (column + 1)) != null) {
            return new UpdatedResultSetCellRenderer();
        }
        return super.getCellRenderer(row, column);
    }

    private static class NullObjectCellRenderer extends DefaultTableCellRenderer {

        static final String NULL_LABEL = "<NULL>";

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
