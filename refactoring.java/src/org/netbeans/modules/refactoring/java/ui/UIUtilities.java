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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.refactoring.java.ui;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.refactoring.java.api.MemberInfo;


/** Class containing various utility methods and inner classes
 * useful when creating refactoring UI.
 *
 * @author Martin Matula, Jan Becicka
 */
public final class UIUtilities {
    // not to be instantiated
    private UIUtilities() {
    }

    /** Returns the same string as passed in or " " if the passed string was an empty string.
     * This method is used as a workaround for issue #58302.
     * @param name Original table column name.
     * @return "Fixed" column name.
     */
    public static String getColumnName(String name) {
        return name == null || name.length() == 0 ? " " : name; // NOI18N
    }
    
    /** Initializes preferred (and eventually maximum) width of a table column based on
     * the size of its header and the estimated longest value.
     * @param table Table to adjust the column width for.
     * @param index Index of the column.
     * @param longValue Estimated long value for the column.
     * @param padding Number of pixes for padding.
     */
    public static void initColumnWidth(JTable table, int index, Object longValue, int padding) {
        TableColumn column = table.getColumnModel().getColumn(index);
        
        // get preferred size of the header
        TableCellRenderer headerRenderer = column.getHeaderRenderer();
        if (headerRenderer == null) {
            headerRenderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = headerRenderer.getTableCellRendererComponent(
                null, column.getHeaderValue(), false, false, 0, 0);
        int width = comp.getPreferredSize().width;
        
        // get preferred size of the long value (remeber max of the pref. size for header and long value)
        comp = table.getDefaultRenderer(table.getModel().getColumnClass(index)).getTableCellRendererComponent(
                table, longValue, false, false, 0, index);
        width = Math.max(width, comp.getPreferredSize().width) + 2 * padding;
        
        // set preferred width of the column
        column.setPreferredWidth(width);
        // if the column contains boolean values, the preferred width
        // should also be its max width
        if (longValue instanceof Boolean) {
            column.setMaxWidth(width);
        }
    }

    /** Table cell renderer that renders Java elements (instances of NamedElement and its subtypes).
     * When rendering the elements it displays element's icon (if available) and display text.
     */
    public static class JavaElementTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, extractText(value), isSelected, hasFocus, row, column);
            if (value instanceof MemberInfo) {
                Icon i = ((MemberInfo) value).getIcon();
                setIcon(i); 
            }
            return this;
        }
        
        
        /** Can be overriden to return alter the standard display text returned for elements.
         * @param value Cell value.
         * @return Display text.
         */
        protected String extractText(Object value) {
            if (value==null)
                return null;
            if (value instanceof MemberInfo) {
                return ((MemberInfo) value).getHtmlText();
            } else {
                return value.toString();
            }
        }
    }
    
    /** Table cell renderer that renders Java elements (instances of NamedElement and its subtypes).
     * When rendering the elements it displays element's icon (if available) and display text.
     */
    public static class JavaElementListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, extractText(value), index, isSelected, cellHasFocus);
            if (value instanceof MemberInfo) {
                Icon i = ((MemberInfo) value).getIcon();
                setIcon(i); 
            }
            return this;
        }

        
        /** Can be overriden to return alter the standard display text returned for elements.
         * @param value Cell value.
         * @return Display text.
         */
        protected String extractText(Object value) {
            if (value instanceof MemberInfo) {
                return ((MemberInfo) value).getHtmlText();
            } else {
                return value.toString();
            }
        }
    }

    /** Table cell renderer for boolean values (a little more advanced that the
     * standard one). Enables hiding the combo box in case the value is <code>null</code>
     * rather than <code>Boolean.TRUE</code> or <code>Boolean.FALSE</code>
     * and disables the combo box for read-only cells to give a better visual feedback
     * that the cells cannot be edited.
     */
    public static class BooleanTableCellRenderer extends JCheckBox implements TableCellRenderer {
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        private final JLabel emptyLabel = new JLabel();

	public BooleanTableCellRenderer() {
	    super();
	    setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
            emptyLabel.setBorder(noFocusBorder);
            emptyLabel.setOpaque(true);
	}

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComponent result;
            if (value == null) {
                result = emptyLabel;
            } else {
                setSelected(((Boolean)value).booleanValue());
                setEnabled(table.getModel().isCellEditable(row, column));
                result = this;
            }

            result.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            result.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            result.setBorder(hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder); // NOI18N
            
            return result;
        }
    }
}
