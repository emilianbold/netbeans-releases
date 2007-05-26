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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.openide.DialogDescriptor;
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
    private static TableCellRenderer stringRenderer = new StringRenderer();
    
    private static TableCellEditor booleanEditor = new BooleanEditor();
    private static TableCellEditor doubleEditor = new NumberEditor();
    private static TableCellEditor passwordEditor = new PasswordEditor();
    private static TableCellEditor stringEditor = new StringEditor();
    
    
    public EnvironmentVariablesCustomEditor(SimpleTabularDataEditor editor) {
        super(editor);
    }
    
    @Override
    protected Vector createNewRow() {
        NewEnvironmentVariableTypeSelectionPanel typeSelectionPanel =
                new NewEnvironmentVariableTypeSelectionPanel();
        
        DialogDescriptor dd = new DialogDescriptor(typeSelectionPanel,
                "Select Environment Variable Type");
        typeSelectionPanel.requestFocus();
        DialogDisplayer.getDefault().notify(dd);
        
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            String type = typeSelectionPanel.getTypeChoice();
            Vector row = super.createNewRow();
            row.add(type);
            
            return row;
        } else {
            return null;
        }
    }
    
    @Override
    protected JTable createTable(DefaultTableModel tableModel) {
        final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        
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
                            ((JComponent)component).setBorder(myBorder);
                            return component;
                        } else {
                            String type = null;
                            
                            try {
                                DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                                Vector rowData = (Vector) tableModel.getDataVector().get(row);
                                type = (String) rowData.get(column + 1);
                            } catch (Exception e) {
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
                                return stringRenderer.getTableCellRendererComponent(
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
        
    static class StringRenderer extends DefaultTableCellRenderer {
        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        
        public StringRenderer() {
            super();
            setHorizontalAlignment(JLabel.LEFT);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            setBorder(myBorder);
            return component;
        }
    }
    
    static class DoubleRenderer extends DefaultTableCellRenderer {
        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        
        NumberFormat formatter;
        public DoubleRenderer() {
            super();
            setHorizontalAlignment(JLabel.LEFT);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            setBorder(myBorder);
            return component;
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
        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        
        public BooleanRenderer() {
            super();
            setBorder(myBorder);
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
            
            return this;
        }
    }
    
    static class PasswordRenderer extends JPasswordField
            implements TableCellRenderer, Serializable {
        
        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        
        public PasswordRenderer() {
            super();
            setOpaque(true);
            setBorder(myBorder);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            
            setValue(value);
            
            return this;
        }
        
        protected void setValue(Object value) {
            setText((value == null) ? "" : value.toString());
        }
    }
    
    static class GenericEditor extends DefaultCellEditor {
        
        Class[] argTypes = new Class[]{String.class};
        java.lang.reflect.Constructor constructor;
        Object value;
        
        public GenericEditor() {
            super(new JTextField());
            getComponent().setName("Table.editor");
            setClickCountToStart(1);
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
        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        public NumberEditor() {
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected,
                int row, int column) {
            Component component = super.getTableCellEditorComponent(
                    table, value, isSelected, row, column);
            ((JComponent)component).setBorder(myBorder);
            return component;
        }
        
        public Object getCellEditorValue() {
            if (value != null && ((String)value).trim().length() > 0) {
                try {
                    Double.parseDouble((String)value);
                    return value;
                } catch (Exception e) {
                    NotifyDescriptor d = new NotifyDescriptor.Message(//e.getMessage(),
                            "Invalid number: " + value,
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
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
        private static final Border myBorder = new EmptyBorder(1, 3, 1, 1);
        public PasswordEditor() {
            super(new JPasswordField());
            setClickCountToStart(1);
            ((JComponent)getComponent()).setBorder(myBorder);
        }
    }
    
    static class StringEditor extends DefaultCellEditor {
        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        
        public StringEditor() {
            super(new JTextField());
            setClickCountToStart(1);
            ((JComponent)getComponent()).setBorder(myBorder);
        }
    }
}
