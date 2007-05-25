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
package org.netbeans.modules.sun.manager.jbi.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * A custom editor for editing typed environment variables.
 *
 * @author jqian
 */
public class EnvironmentVariablesCustomEditor extends SimpleTabularDataCustomEditor {
    
    private static TableCellRenderer booleanRenderer = new BooleanRenderer();
    private static TableCellRenderer doubleRenderer = new DoubleRenderer();
    private static TableCellRenderer passwordRenderer = new PasswordRenderer();
    
    private static TableCellEditor booleanEditor = new BooleanEditor();
    private static TableCellEditor doubleEditor = new NumberEditor();
    private static TableCellEditor passwordEditor = new PasswordEditor();
    private static TableCellEditor stringEditor = new StringEditor();
    
    
    public EnvironmentVariablesCustomEditor(SimpleTabularDataEditor editor) {
        super(editor);
    }
    
    @Override
    protected Vector createNewRow() {
        NewEnvironmentVariableTypeSelectionDialog dialog =
                new NewEnvironmentVariableTypeSelectionDialog(null, true);
        dialog.setVisible(true);
        
        if (dialog.getReturnStatus() == dialog.RET_OK) {
            String type = dialog.getTypeChoice();
            Vector row = super.createNewRow();
            row.add(type);
            
            return row;
        } else {
            return null;
        }
    }
    
    @Override
    protected JTable createTable(DefaultTableModel tableModel) {
        JTable table = new JTable(tableModel) {
            public TableCellRenderer getCellRenderer(int row, int column) {
                TableCellRenderer renderer = new DefaultTableCellRenderer() {
                    public Component getTableCellRendererComponent(JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row, int column) {
                        
                        if (column == 0) {
                            // Highlight key columns
                            if (value != null) {
                                value = "<html><body><b>" + value + "</b></body></html>";
                            }
                            Component component = super.getTableCellRendererComponent(
                                    table, value, isSelected, hasFocus, row, column);
                            //((JLabel) component).setHorizontalAlignment(JLabel.CENTER);
                            return component;
                        } else {
                            String type = null;
                            
                            try {
                                DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                                Vector rowData = (Vector) tableModel.getDataVector().get(row);
                                type = (String) rowData.get(column + 1);
                            } catch (Exception e) {
                            }
                            
                            if (type == null) {
                                // TMP for testing purpose
                                if (row % 4 == 0) type = "Boolean";
                                else if (row % 4 == 1) type = "Password";
                                else if (row % 4 == 2) type = "Number";
                                else if (row % 4== 3) type = "String";
                            }
                            
                            if (type.equalsIgnoreCase("Boolean")) {
                                return booleanRenderer.getTableCellRendererComponent(
                                        table, value, isSelected, hasFocus, row, column);
                            } else if (type.equalsIgnoreCase("Password")) {
                                return passwordRenderer.getTableCellRendererComponent(
                                        table, value, isSelected, hasFocus, row, column);
                            } else if (type.equalsIgnoreCase("Number")) {
                                return doubleRenderer.getTableCellRendererComponent(
                                        table, value, isSelected, hasFocus, row, column);
                            } else if (type.equalsIgnoreCase("String")) {
                                return super.getTableCellRendererComponent(
                                        table, value, isSelected, hasFocus, row, column);
                            } else {
                                throw new RuntimeException("Unknown type: " + type);
                            }
                        }
                    }
                };
                return renderer;
            }
            
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 0) {
                    return stringEditor;
                } else {
                    String type = null;
                    try {
                        DefaultTableModel tableModel = (DefaultTableModel) getModel();
                        Vector rowData = (Vector) tableModel.getDataVector().get(row);
                        type = (String) rowData.get(column + 1);
                    } catch (Exception e) {
                    }
                    
                    if (type == null) {
                        // TMP for testing purpose
                        if (row % 4 == 0) type = "Boolean";
                        else if (row % 4 == 1) type = "Password";
                        else if (row % 4 == 2) type = "Number";
                        else if (row % 4 == 3) type = "String";
                    }
                    
                    if (type.equalsIgnoreCase("Boolean")) {
                        return booleanEditor;
                    } else if (type.equalsIgnoreCase("Password")) {
                        return passwordEditor;
                    } else if (type.equalsIgnoreCase("Number")) {
                        return doubleEditor;
                    } else if (type.equalsIgnoreCase("String")) {
                        return stringEditor;
                    } else {
                        throw new RuntimeException("Unknown type: " + type);
                    }
                }
            }
        };
        
        return table;
    }
    
    static class NumberRenderer extends DefaultTableCellRenderer.UIResource {
        public NumberRenderer() {
            super();
            setHorizontalAlignment(JLabel.RIGHT);
        }
    }
    
    static class DoubleRenderer extends NumberRenderer {
        NumberFormat formatter;
        public DoubleRenderer() {
            super();
            setHorizontalAlignment(JTextField.LEFT);
        }
        
        public void setValue(Object value) {
            String oldValue = getText();
            if (formatter == null) {
                formatter = NumberFormat.getInstance();
            }
            if (value == null || ((String)value).trim().length() == 0) {
                setText("");
            } else {
                double d = Double.parseDouble((String)value);
                setText(formatter.format(d));
            }
        }
    }
    
    static class BooleanRenderer extends JCheckBox implements TableCellRenderer {
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        
        public BooleanRenderer() {
            super();
            //            setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setSelected((value != null && ((Boolean)value).booleanValue()));
            
            if (hasFocus) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            } else {
                setBorder(noFocusBorder);
            }
            
            return this;
        }
    }
    
    static class PasswordRenderer extends JPasswordField
            implements TableCellRenderer, Serializable {
        
        protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
        
        // We need a place to store the color the JLabel should be returned
        // to after its foreground and background colors have been set
        // to the selection background color.
        // These ivars will be made protected when their names are finalized.
        //        private Color unselectedForeground;
        //        private Color unselectedBackground;
        
        /**
         * Creates a default table cell renderer.
         */
        public PasswordRenderer() {
            super();
            setOpaque(true);
            setBorder(getNoFocusBorder());
        }
        
        private static Border getNoFocusBorder() {
            if (System.getSecurityManager() != null) {
                return SAFE_NO_FOCUS_BORDER;
            } else {
                return noFocusBorder;
            }
        }
        
        //        /**
        //         * Overrides <code>JComponent.setForeground</code> to assign
        //         * the unselected-foreground color to the specified color.
        //         *
        //         * @param c set the foreground color to this value
        //         */
        //        public void setForeground(Color c) {
        //            super.setForeground(c);
        //            unselectedForeground = c;
        //        }
        //
        //        /**
        //         * Overrides <code>JComponent.setBackground</code> to assign
        //         * the unselected-background color to the specified color.
        //         *
        //         * @param c set the background color to this value
        //         */
        //        public void setBackground(Color c) {
        //            super.setBackground(c);
        //            unselectedBackground = c;
        //        }
        
        /**
         *
         * Returns the default table cell renderer.
         *
         * @param table  the <code>JTable</code>
         * @param value  the value to assign to the cell at
         *			<code>[row, column]</code>
         * @param isSelected true if cell is selected
         * @param hasFocus true if cell has focus
         * @param row  the row of the cell to render
         * @param column the column of the cell to render
         * @return the default table cell renderer
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                /*super.*/setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            
            if (hasFocus) {
                Border border = null;
                if (isSelected) {
                    border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
                }
                if (border == null) {
                    border = UIManager.getBorder("Table.focusCellHighlightBorder");
                }
                setBorder(border);
            } else {
                setBorder(getNoFocusBorder());
            }
            
            //            } else {
            //                super.setForeground((unselectedForeground != null) ? unselectedForeground
            //                        : table.getForeground());
            //                super.setBackground((unselectedBackground != null) ? unselectedBackground
            //                        : table.getBackground());
            //            }
            //
            //            setFont(table.getFont());
            //
            //            if (hasFocus) {
            //                Border border = null;
            //                if (isSelected) {
            //                    border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
            //                }
            //                if (border == null) {
            //                    border = UIManager.getBorder("Table.focusCellHighlightBorder");
            //                }
            //                setBorder(border);
            //
            //                if (!isSelected && table.isCellEditable(row, column)) {
            //                    Color col;
            //                    col = UIManager.getColor("Table.focusCellForeground");
            //                    if (col != null) {
            //                        super.setForeground(col);
            //                    }
            //                    col = UIManager.getColor("Table.focusCellBackground");
            //                    if (col != null) {
            //                        super.setBackground(col);
            //                    }
            //                }
            //            } else {
            //                setBorder(getNoFocusBorder());
            //            }
            
            setValue(value);
            
            return this;
        }
        
        
        /**
         * Sets the <code>String</code> object for the cell being rendered to
         * <code>value</code>.
         *
         * @param value  the string value for this cell; if value is
         *		<code>null</code> it sets the text value to an empty string
         * @see JLabel#setText
         *
         */
        protected void setValue(Object value) {
            setText((value == null) ? "" : value.toString());
        }
    }
    
    /**
     * Default Editors
     */
    static class GenericEditor extends DefaultCellEditor {
        
        Class[] argTypes = new Class[]{String.class};
        java.lang.reflect.Constructor constructor;
        Object value;
        
        public GenericEditor() {
            super(new JTextField());
            getComponent().setName("Table.editor");
        }
        
        public boolean stopCellEditing() {
            String s = (String)super.getCellEditorValue();
            // Here we are dealing with the case where a user
            // has deleted the string value in a cell, possibly
            // after a failed validation. Return null, so that
            // they have the option to replace the value with
            // null or use escape to restore the original.
            // For Strings, return "" for backward compatibility.
            if ("".equals(s)) {
                if (constructor.getDeclaringClass() == String.class) {
                    value = s;
                }
                super.stopCellEditing();
            }
            
            try {
                value = constructor.newInstance(new Object[]{s});
            } catch (Exception e) {
                ((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
                return false;
            }
            return super.stopCellEditing();
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected,
                int row, int column) {
            this.value = null;
            ((JComponent)getComponent()).setBorder(new LineBorder(Color.black));
            try {
                Class type = table.getColumnClass(column);
                // Since our obligation is to produce a value which is
                // assignable for the required type it is OK to use the
                // String constructor for columns which are declared
                // to contain Objects. A String is an Object.
                if (type == Object.class) {
                    type = String.class;
                }
                constructor = type.getConstructor(argTypes);
            } catch (Exception e) {
                return null;
            }
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
        
        public Object getCellEditorValue() {
            return value;
        }
    }
    
    static class NumberEditor extends GenericEditor {
        public NumberEditor() {
            //((JTextField)getComponent()).setHorizontalAlignment(JTextField.LEFT);
        }
        
        public Object getCellEditorValue() {
            try {
                Double.parseDouble((String)value);
                return value;
            } catch (Exception e) {
                NotifyDescriptor d = new NotifyDescriptor.Message(//e.getMessage(),
                        "Invalid number: " + value,
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
            
            return ""; // FIXME: restore the old value
        }
    }
    
    static class BooleanEditor extends DefaultCellEditor {
        public BooleanEditor() {
            super(new JCheckBox());
            JCheckBox checkBox = (JCheckBox)getComponent();
        }
    }
    
    static class PasswordEditor extends DefaultCellEditor {
        public PasswordEditor() {
            super(new JPasswordField());
        }
    }
    
    static class StringEditor extends DefaultCellEditor {
        public StringEditor() {
            super(new JTextField());
        }
    }
}
