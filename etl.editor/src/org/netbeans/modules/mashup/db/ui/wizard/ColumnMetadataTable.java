/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mashup.db.ui.wizard;

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

/**
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class ColumnMetadataTable extends JTable {

    static class MyBooleanRenderer extends JCheckBox implements TableCellRenderer {
        protected static Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        private JPanel myPanel;

        /**
         * Creates a default MyBooleanRenderer.
         */
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

            if (hasFocus) { // NOI18N this scope block
                myPanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                if (table.isCellEditable(row, column)) {
                    super.setForeground(UIManager.getColor("Table.focusCellForeground"));
                    super.setBackground(UIManager.getColor("Table.focusCellBackground"));
                }
                myPanel.setForeground(UIManager.getColor("Table.focusCellForeground"));
                myPanel.setBackground(UIManager.getColor("Table.focusCellBackground"));
            } else {
                myPanel.setBorder(noFocusBorder);
            }

            setSelected((value != null && ((Boolean) value).booleanValue()));
            return myPanel;
        }

        /**
         * Overrides <code>JComponent.setBackground</code> to assign the
         * unselected-background color to the specified color.
         * 
         * @param c set the background color to this value
         */
        public void setBackground(Color c) {
            super.setBackground(c);
        }

        /**
         * Overrides <code>JComponent.setForeground</code> to assign the
         * unselected-foreground color to the specified color.
         * 
         * @param c set the foreground color to this value
         */
        public void setForeground(Color c) {
            super.setForeground(c);
        }
    }

    /**
     * Creates a new instance of ColumnMetadataTable using the given TableModel
     * 
     * @param model data model containing record layout information
     */
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
            public Object getCellEditorValue() {
                return new Integer(intField.getValue());
            }

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

    /**
     * Creates combobox renderer for the given column, and using the given List of option
     * strings as selection choices.
     * 
     * @param column index of table column to use this combobox
     * @param optionStrings List of strings to use as options
     */
    public JComboBox setComboBoxRenderer(int column, List optionStrings) {
        if (column < 0 || column > getModel().getColumnCount()) {
            throw new IndexOutOfBoundsException("Value of column exceeds range of column values.");
        }

        if (optionStrings == null || optionStrings.size() == 0) {
            throw new IllegalArgumentException("optionStrings must be non-null and contain at least one String.");
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

