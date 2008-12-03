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
package org.netbeans.modules.db.dataview.output;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.openide.awt.StatusDisplayer;
import org.openide.windows.WindowManager;

/**
 *
 * @author Shankari
 */
class InsertRecordTableUI extends JTable {

    private DataView dView;
    private final int multiplier;
    private static final String data = "WE WILL EITHER FIND A WAY, OR MAKE ONE."; // NOI18N
    //private static final Color MAC_UNFOCUSED_UNSELECTED_VERTICAL_LINE_COLOR = new Color(0xd9d9d9);

    public InsertRecordTableUI(DataView dataView) {
        
        this.dView = dataView;
        getTableHeader().setReorderingAllowed(false);

        multiplier = getFontMetrics(getFont()).stringWidth(data) / data.length() + 3;
        setRowHeight(getFontMetrics(getFont()).getHeight() + 5);

        // addKeyListener(new Control0KeyListener());
        setDefaultEditor(Object.class, new ResultSetTableCellEditor(new JTextField()));
        setDefaultEditor(Number.class, new NumberEditor(new JTextField()));
        setDefaultEditor(String.class, new StringTableCellEditor(new JTextField()));

        DefaultTableModel model = (DefaultTableModel) getModel();
        for (int i = 0; i < dView.getDataViewDBTable().getColumnCount(); i++) {
            model.addColumn(dView.getDataViewDBTable().getColumnName(i));
        }
        //To adjust the table size to the scrollpane
        if (dView.getDataViewDBTable().getColumnCount() < 7) {
            setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        } else {
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
        Vector v = new Vector(dView.getDataViewDBTable().getColumnCount());
        model.addRow(v);
    }

    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        AWTEvent awtEvent = EventQueue.getCurrentEvent();
        if (awtEvent instanceof KeyEvent) {
            KeyEvent keyEvt = (KeyEvent) awtEvent;
            if (keyEvt.getSource() != this) {
                return;
            }
            if (rowIndex == 0 && columnIndex == 0 && KeyStroke.getKeyStrokeForEvent(keyEvt).equals(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))) {
                ((DefaultTableModel) getModel()).addRow(new Object[dView.getDataViewDBTable().getColumnCount()]);
                rowIndex = getRowCount() - 1; //Otherwise the selection switches to the first row
                editCellAt(rowIndex, 0);
            } else {
                editCellAt(rowIndex, columnIndex);
            }
        }
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return super.getCellRenderer(row, column);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();//super.getPreferredScrollableViewportSize();
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
                        } else {
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
                            DBColumn dbcol = dView.getDataViewDBTable().getColumn(col);
                            return DBReadWriteHelper.validate(txtVal, dbcol);
                        } catch (Exception ex) {
                            StatusDisplayer.getDefault().setStatusText(ex.getMessage());
                            return txtVal;
                        }
                    }
                }
            };

            textField.addActionListener(delegate);
        //textField.addKeyListener(new Control0KeyListener());
        }
    }

    private class NumberEditor extends ResultSetTableCellEditor {

        public NumberEditor(final JTextField textField) {
            super(textField);
            ((JTextField) getComponent()).setHorizontalAlignment(JTextField.RIGHT);
        }
    }

    public class StringTableCellEditor extends ResultSetTableCellEditor implements TableCellEditor, ActionListener {

        private JButton customEditorButton = new JButton("...");
        private JPanel panel = new JPanel(new BorderLayout());
        private JTable table;
        private int row, column;
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

            DBColumn dbCol = dView.getDataViewDBTable().getColumn(column);
            if (dbCol.isGenerated()) {
                editable = false;
            } else if (!dView.isEditable()) {
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
                    DBColumn dbcol = dView.getDataViewDBTable().getColumn(col);
                    if (dbcol.isGenerated() || !dbcol.isNullable()) {
                        Toolkit.getDefaultToolkit().beep();
                        editor.stopCellEditing();
                    } else {
                        editor.getTableCellEditorComponent(InsertRecordTableUI.this, null, rowSelectionAllowed, row, col);
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
}
