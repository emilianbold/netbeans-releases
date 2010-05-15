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
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.openide.util.NbBundle;

/**
 * @author Ahimanikya Satapathy
 */
public class ColumnMetadataTable extends JTable {

    static class MyBooleanRenderer extends JCheckBox implements TableCellRenderer {

        protected static Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        private JPanel myPanel;

        public MyBooleanRenderer() {
            super();
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            myPanel = new JPanel();
            myPanel.setLayout(new BorderLayout());
            myPanel.add(this, BorderLayout.CENTER);
            myPanel.setOpaque(true);
            myPanel.setBorder(noFocusBorder);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());

                myPanel.setForeground(table.getSelectionForeground());
                myPanel.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());

                myPanel.setForeground(table.getForeground());
                myPanel.setBackground(table.getBackground());
            }

            if (hasFocus) {
                myPanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder")); // NOI18N
                if (table.isCellEditable(row, column)) {
                    super.setForeground(UIManager.getColor("Table.focusCellForeground")); // NOI18N
                    super.setBackground(UIManager.getColor("Table.focusCellBackground")); // NOI18N
                }
                myPanel.setForeground(UIManager.getColor("Table.focusCellForeground")); // NOI18N
                myPanel.setBackground(UIManager.getColor("Table.focusCellBackground")); // NOI18N
            } else {
                myPanel.setBorder(noFocusBorder);
            }

            setSelected((value != null && ((Boolean) value).booleanValue()));
            return myPanel;
        }

        @Override
        public void setBackground(Color c) {
            super.setBackground(c);
        }

        @Override
        public void setForeground(Color c) {
            super.setForeground(c);
        }
    }

    public ColumnMetadataTable(TableModel model) {
        super(model);

        // WT #65119: Fix problem with edited cell not committing its contents once
        // user clicks a dialog control button.
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        setSurrendersFocusOnKeystroke(true);

        setCellSelectionEnabled(true);
        setRowSelectionAllowed(false);
        setColumnSelectionAllowed(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final ColumnSizeTextField intField = new ColumnSizeTextField(0, 5);
        intField.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultCellEditor integerEditor = new DefaultCellEditor(intField) {
            // Override DefaultCellEditor's getCellEditorValue method
            // to return an Integer, not a String:
            @Override
            public Object getCellEditorValue() {
                return new Integer(intField.getValue());
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
                if (isSelected) {
                    ((ColumnSizeTextField) c).selectAll();
                }

                return c;
            }
        };
        setDefaultEditor(Integer.class, integerEditor);

        setDefaultRenderer(Boolean.class, new MyBooleanRenderer());
    }

    public JComboBox setComboBoxRenderer(int column, List optionStrings) {
        if (column < 0 || column > getModel().getColumnCount()) {
            throw new IndexOutOfBoundsException(NbBundle.getMessage(ColumnMetadataTable.class, "MSG_exceeds_colRange"));
        }

        if (optionStrings == null || optionStrings.size() == 0) {
            throw new IllegalArgumentException(NbBundle.getMessage(ColumnMetadataTable.class, "MSG_Null_optionStrings"));
        }

        JComboBox comboBox = new JComboBox();
        Iterator iter = optionStrings.iterator();
        while (iter.hasNext()) {
            comboBox.addItem(iter.next());
        }

        TableColumn theColumn = getColumnModel().getColumn(column);
        theColumn.setCellEditor(new DefaultCellEditor(comboBox));

        return comboBox;
    }
}

