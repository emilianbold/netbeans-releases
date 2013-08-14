/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.table.celleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXPanel;
import org.netbeans.modules.db.dataview.table.ResultSetTableCellEditor;
import org.openide.windows.WindowManager;

public class StringTableCellEditor extends ResultSetTableCellEditor implements TableCellEditor, ActionListener {

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
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, final int column) {
        this.table = table;
        final JComponent c = (JComponent) super.getTableCellEditorComponent(table, value, isSelected, row, column);        
        final JTextComponent tc = c instanceof JTextComponent ? (JTextComponent) c : null;

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
                
                if (tc != null && ks.isOnKeyRelease()) {
                    table.getModel().setValueAt(tc.getText(), row, column);
                }

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
        if (suppressEditorBorder) {
            c.setBorder(BorderFactory.createEmptyBorder());
        }
        panel.add(customEditorButton, BorderLayout.EAST);
        panel.revalidate();
        panel.repaint();

        this.row = row;
        this.column = column;
        return panel;
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        assert table != null;
        super.cancelCellEditing();
        editCell(table, row, column);
    }

    protected void editCell(JTable table, int row, int column) {
        JTextArea textArea = new JTextArea(20, 80);
        // Work aroung JDK bugs 7027598 (this bug suggests this work-around) #233347
        textArea.setDropTarget(null);
        TableModel tm = table.getModel();
        int modelRow = table.convertRowIndexToModel(row);
        int modelColumn = table.convertColumnIndexToModel(column);
        boolean editable = tm.isCellEditable(modelRow, modelColumn);
        Object value = tm.getValueAt(modelRow, modelColumn);
        if (value != null) {
            textArea.setText(value.toString());
            textArea.setCaretPosition(0);
            textArea.setEditable(editable);
        }
        JScrollPane pane = new JScrollPane(textArea);
        pane.addHierarchyListener(new MakeResizableListener(pane));
        Component parent = WindowManager.getDefault().getMainWindow();

        if (editable) {
            int result = JOptionPane.showOptionDialog(parent, pane, table.getColumnName(column), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                tm.setValueAt(textArea.getText(), modelRow, modelColumn);
            }
        } else {
            JOptionPane.showMessageDialog(parent, pane, table.getColumnName(column), JOptionPane.PLAIN_MESSAGE, null);
        }
    }

    /**
     * Hack to make JOptionPane resizable.
     * https://blogs.oracle.com/scblog/entry/tip_making_joptionpane_dialog_resizable
     */
    static class MakeResizableListener implements HierarchyListener {

        private Component pane;

        public MakeResizableListener(Component pane) {
            this.pane = pane;
        }

        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            Window window = SwingUtilities.getWindowAncestor(pane);
            if (window instanceof Dialog) {
                Dialog dialog = (Dialog) window;
                if (!dialog.isResizable()) {
                    dialog.setResizable(true);
                }
            }
        }
    }
}
