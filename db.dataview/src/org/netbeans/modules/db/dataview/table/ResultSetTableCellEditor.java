/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.dataview.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.renderer.JRendererCheckBox;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.netbeans.modules.db.dataview.util.JXDateTimePicker;
import org.netbeans.modules.db.dataview.util.TimestampType;
import org.openide.awt.StatusDisplayer;
import org.openide.windows.WindowManager;

/**
 * @author Ahimanikya Satapathy
 */
public class ResultSetTableCellEditor extends DefaultCellEditor {

    protected Object val;
    protected boolean editable = true;
    protected JTable table;
    static final boolean isGtk = "GTK".equals (UIManager.getLookAndFeel ().getID ()); //NOI18N

    public ResultSetTableCellEditor(final JTextField textField) {
        super(textField);
        delegate = new EditorDelegate() {

            @Override
            public void setValue(Object value) {
                val = value;
                textField.setText((value != null) ? value.toString() : "");
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                if (evt instanceof MouseEvent) {
                    return ((MouseEvent) evt).getClickCount() >= 2;
                }
                return true;
            }

            @Override
            public Object getCellEditorValue() {
                String txtVal = textField.getText();
                if (val == null && txtVal.equals("")) {
                    return null;
                } else {
                    try {
                        assert table != null;
                        int col = table.getEditingColumn();
                        //textField.addKeyListener(new TableKeyListener());
                        return DBReadWriteHelper.validate(txtVal, ((ResultSetJXTable) table).getDBColumn(col));
                    } catch (Exception ex) {
                        StatusDisplayer.getDefault().setStatusText(ex.getMessage());
                        return txtVal;
                    }
                }
            }
        };

        textField.addActionListener(delegate);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (DataViewUtils.isSQLConstantString(value)) {
            value = "";
        }
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    public ResultSetTableCellEditor(final JRendererCheckBox checkBox) {
        super(checkBox);
        delegate = new EditorDelegate() {

            @Override
            public void setValue(Object value) {
                val = value;
                checkBox.setSelected((value != null) ? checkBox.isSelected() : false);
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                if (evt instanceof MouseEvent) {
                    return ((MouseEvent) evt).getClickCount() >= 2;
                }
                return true;
            }

            @Override
            public Object getCellEditorValue() {
                Boolean bolVal = new Boolean(checkBox.isSelected());
                if (val == null && !checkBox.isSelected()) {
                    return null;
                } else {
                    return bolVal;
                }
            }
        };

        checkBox.addActionListener(delegate);
    }

    protected void setEditable(int column, Component c) {
        assert table != null;
        DBColumn dbCol = ((ResultSetJXTable) table).getDBColumn(column);
        if (dbCol.isGenerated()) {
            editable = false;
        }
        if (!((ResultSetJXTable) table).dView.isEditable()) {
            editable = false;
        } else {
            editable = dbCol.isEditable();
        }

        if (c instanceof JTextField) {
            ((JTextField) c).setEditable(editable);
        } else if (c instanceof JComponent) {
            ((JComponent) c).setEnabled(editable);
        }
    }
}

class BooleanTableCellEditor extends ResultSetTableCellEditor implements TableCellEditor {

    public BooleanTableCellEditor(JRendererCheckBox cb) {
        super(cb);
        cb.setHorizontalAlignment(0);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        setEditable(column, c);
        if (isGtk && c instanceof JComponent) {
            ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
        }
        return c;
    }
}

class StringTableCellEditor extends ResultSetTableCellEditor implements TableCellEditor, ActionListener {

    private JXButton customEditorButton = new JXButton("...");
    private int row, column;

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
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        final JComponent c = (JComponent) super.getTableCellEditorComponent(table, value, isSelected, row, column);
        setEditable(column, c);

        JXPanel panel = new JXPanel(new BorderLayout()) {

            @Override
            public void addNotify() {
                super.addNotify();
                c.requestFocus();
            }

            @Override
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
                InputMap map = c.getInputMap(condition);
                ActionMap am = c.getActionMap();

                if (map != null && am != null && isEnabled()) {
                    Object binding = map.get(ks);
                    Action action = (binding == null) ? null : am.get(binding);
                    if (action != null) {
                        return SwingUtilities.notifyAction(action, ks, e, c,
                                e.getModifiers());
                    }
                }
                return false;
            }
        };
        panel.add(c);
        if (isGtk) {
            c.setBorder(BorderFactory.createEmptyBorder());
        }
        panel.add(customEditorButton, BorderLayout.EAST);
        panel.revalidate();
        panel.repaint();

        this.row = row;
        this.column = column;
        return panel;
    }

    public final void actionPerformed(ActionEvent e) {
        assert table != null;
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
            int result = JOptionPane.showOptionDialog(parent, pane, table.getColumnName(column), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                table.setValueAt(textArea.getText(), row, column);
            }
        } else {
            JOptionPane.showMessageDialog(parent, pane, table.getColumnName(column), JOptionPane.PLAIN_MESSAGE, null);
        }
    }
}

class DateTimePickerCellEditor extends AbstractCellEditor implements TableCellEditor {

    private boolean editable = true;
    private JXDateTimePicker datePicker;
    private DateFormat dateFormat;
    private ActionListener pickerActionListener;
    private boolean ignoreAction;
    private JTable table;

    public DateTimePickerCellEditor() {
        this(new SimpleDateFormat (TimestampType.DEFAULT_FORMAT_PATTERN));
    }

    /**
     * Instantiates an editor with the given dateFormat. If
     * null, the datePickers default is used.
     * 
     * @param dateFormat
     */
    public DateTimePickerCellEditor(DateFormat dateFormat) {

        // JW: the copy is used to synchronize .. can 
        // we use something else?
        this.dateFormat = dateFormat != null ? dateFormat : new SimpleDateFormat (TimestampType.DEFAULT_FORMAT_PATTERN);
        datePicker = new JXDateTimePicker();
        // default border crushes the editor/combo
        datePicker.getEditor().setBorder(
                BorderFactory.createEmptyBorder(0, 1, 0, 1));
        // should be fixed by j2se 6.0
        datePicker.setFont(UIManager.getDefaults().getFont("TextField.font"));
        if (dateFormat != null) {
            datePicker.setFormats(dateFormat);
        }
        datePicker.addActionListener(getPickerActionListener());
    }

    public Timestamp getCellEditorValue() {
        return datePicker.getDateTime();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= 2;
        }
        return super.isCellEditable(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        ignoreAction = true;
        boolean canCommit = commitChange();
        ignoreAction = false;
        if (canCommit) {
            datePicker.setDateTime(null);
            return super.stopCellEditing();
        }
        return false;
    }

    public Component getTableCellEditorComponent(final JTable table, Object value,
            boolean isSelected, int row, int column) {
        this.table = table;
        ignoreAction = true;
        datePicker.setDateTime(getValueAsTimestamp(value));

        ignoreAction = false;
        setEditable(column, datePicker);
        return datePicker;
    }

    protected Timestamp getValueAsTimestamp(Object value) {
        if (isEmpty(value) || DataViewUtils.isSQLConstantString(value)) {
            return new Timestamp(System.currentTimeMillis());
        }

        if (value instanceof Timestamp) {
            return (Timestamp) value;
        }
        if (value instanceof Long) {
            return new Timestamp((Long) value);
        }
        if (value instanceof String) {
            try {

                return new Timestamp(dateFormat.parse((String) value).getTime());
            } catch (ParseException e) {
                //mLogger.log(Level.SEVERE, e.getMessage(), e.getMessage());
            }
        }

        return new Timestamp(System.currentTimeMillis());
    }

    protected boolean isEmpty(Object value) {
        return value == null || value instanceof String && ((String) value).length() == 0;
    }

    protected boolean commitChange() {
        try {
            datePicker.commitEdit();
            return true;
        } catch (ParseException e) {
        }
        return false;
    }

    public DateFormat[] getFormats() {
        return datePicker.getFormats();
    }

    public void setFormats(DateFormat... formats) {
        datePicker.setFormats(formats);
    }

    protected ActionListener getPickerActionListener() {
        if (pickerActionListener == null) {
            pickerActionListener = createPickerActionListener();
        }
        return pickerActionListener;
    }

    protected ActionListener createPickerActionListener() {
        ActionListener l = new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                // avoid duplicate trigger from
                // commit in stopCellEditing
                if (ignoreAction) {
                    return;
                }
                terminateEdit(e);
            }

            private void terminateEdit(final ActionEvent e) {
                if ((e != null) && (JXDatePicker.COMMIT_KEY.equals(e.getActionCommand()))) {
                    stopCellEditing();
                } else {
                    cancelCellEditing();
                }
            }
        };
        return l;
    }

    protected void setEditable(int column, JXDateTimePicker c) {
        assert table != null;
        DBColumn dbCol = ((ResultSetJXTable) table).getDBColumn(column);
        if (dbCol.isGenerated()) {
            editable = false;
        } else if (!((ResultSetJXTable) table).dView.isEditable()) {
            editable = false;
        } else {
            editable = dbCol.isEditable();
        }
        c.setEditable(editable);
    }

    protected void addKeyListener(KeyListener kl) {
        datePicker.addKeyListener(kl);
    }
}

class NumberFieldEditor extends ResultSetTableCellEditor {

    public NumberFieldEditor(final JTextField textField) {
        super(textField);
        ((JTextField) getComponent()).setHorizontalAlignment(JTextField.RIGHT);
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        if (isGtk && c instanceof JComponent) {
            ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
        }
        setEditable(column, c);
        return c;
    }
}


