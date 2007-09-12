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
package org.netbeans.modules.sun.manager.jbi.editors;

import java.awt.Color;
import java.awt.Component;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
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
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * A custom editor for editing typed environment variables.
 * A typed environment variable is a triplet: [Name, Type, Value].
 * Four types are supported: STRING, NUMBER, BOOLEAN and PASSWORD.
 *
 * @author jqian
 */
public class EnvironmentVariablesCustomEditor extends SimpleTabularDataCustomEditor {
    
    public static final String STRING_TYPE = "STRING";
    public static final String NUMBER_TYPE = "NUMBER";
    public static final String BOOLEAN_TYPE = "BOOLEAN";
    public static final String PASSWORD_TYPE = "PASSWORD";
    
    public static final int NAME_COLUMN = 0;
    public static final int TYPE_COLUMN = 1;
    public static final int VALUE_COLUMN = 2;
    
    private static TableCellRenderer stringRenderer = new StringRenderer();
    private static TableCellRenderer doubleRenderer = new DoubleRenderer();
    private static TableCellRenderer booleanRenderer = new BooleanRenderer();
    private static TableCellRenderer passwordRenderer = new PasswordRenderer();
    
    private static TableCellEditor stringEditor = new StringEditor();
    private static TableCellEditor booleanEditor = new BooleanEditor();
    private static TableCellEditor doubleEditor = new NumberEditor();
    private static TableCellEditor passwordEditor = new PasswordEditor();
    
    private static Map<String, TableCellRenderer> rendererMap = new HashMap<String, TableCellRenderer>();
    private static Map<String, TableCellEditor> editorMap = new HashMap<String, TableCellEditor>();
    
    static {
        rendererMap.put(STRING_TYPE, stringRenderer);
        rendererMap.put(NUMBER_TYPE, doubleRenderer);
        rendererMap.put(BOOLEAN_TYPE, booleanRenderer);
        rendererMap.put(PASSWORD_TYPE, passwordRenderer);
        
        editorMap.put(STRING_TYPE, stringEditor);
        editorMap.put(NUMBER_TYPE, doubleEditor);
        editorMap.put(BOOLEAN_TYPE, booleanEditor);
        editorMap.put(PASSWORD_TYPE, passwordEditor);
    }
    
    public EnvironmentVariablesCustomEditor(SimpleTabularDataEditor editor) {
        super(editor);
    }
    
    @Override
    protected Vector<String> createRow() {
        NewEnvironmentVariableTypeSelectionPanel typeSelectionPanel =
                new NewEnvironmentVariableTypeSelectionPanel();
        
        DialogDescriptor dd = new DialogDescriptor(typeSelectionPanel,
                "Select Environment Variable Type");
        typeSelectionPanel.requestFocus();
        DialogDisplayer.getDefault().notify(dd);
        
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            String type = typeSelectionPanel.getTypeChoice();
            Vector<String> row = super.createRow();
            row.set(TYPE_COLUMN, type);
            
            // init default boolean value
            if (type.equals(BOOLEAN_TYPE)) {
                row.set(VALUE_COLUMN, Boolean.FALSE.toString()); 
            }
            
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
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row, int column) {
                        
                        if (column == NAME_COLUMN) {
                            // Highlight key columns
                            if (value != null) {
                                value = "<html><body><b>" + value + "</b></body></html>"; // NOI18N
                            }
                            Component component = super.getTableCellRendererComponent(
                                    table, value, isSelected, hasFocus, row, column);
                            ((JComponent)component).setBorder(myBorder);
                            return component;
                        } else {
                            DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                            Vector rowData = (Vector) tableModel.getDataVector().get(row);
                            String type = (String) rowData.get(TYPE_COLUMN);
                            TableCellRenderer renderer = rendererMap.get(type);
                            
                            if (renderer != null) {
                                return renderer.getTableCellRendererComponent(
                                        table, value, isSelected, hasFocus, row, column);
                            } else {
                                System.err.println("WARNING: Unknown TableCellRenderer for type of \"" + type + "\"");  // NOI18N
                                return super.getTableCellRendererComponent(
                                        table, value, isSelected, hasFocus, row, column);
                            }
                        }
                    }
                };
                return renderer;
            }
            
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == NAME_COLUMN) {
                    return stringEditor;
                } else {
                    DefaultTableModel tableModel = (DefaultTableModel) getModel();
                    Vector rowData = (Vector) tableModel.getDataVector().get(row);
                    String type = (String) rowData.get(TYPE_COLUMN);
                    TableCellEditor editor = editorMap.get(type);
                    if (type != null) {
                        return editor;
                    } else {
                        System.err.println("WARNING: Unknown TableCellEditor for type of \"" + type + "\"");  // NOI18N
                        return stringEditor;
                    }
                }
            }
        };
        
        return table;
    }
    
    @Override
    protected void configureTableColumns(JTable table) {
        super.configureTableColumns(table); 
        
        // Hide the type column in the table
        TableColumnModel columnModel = table.getColumnModel();
        TableColumn typeColumn = columnModel.getColumn(TYPE_COLUMN);
        columnModel.removeColumn(typeColumn);
    }
    
    @Override
    protected TableCellRenderer createTableHeaderRenderer() {
        return new EnvVarTableHeaderRenderer();   
    }
    
    class EnvVarTableHeaderRenderer extends TabularDataTableHeaderRenderer {
        // hide the type column
        protected int getColumnIndex(int column) {
            return column == NAME_COLUMN ? NAME_COLUMN : VALUE_COLUMN;
        }
    }    
    
    //=========================== RENDERERS ====================================
    
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
                setText(""); // NOI18N
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
            
            setSelected(value != null && 
                    value.toString().equalsIgnoreCase("true")); // NOI18N
            
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
            setEchoChar('*'); // NOI18N
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
            setText((value == null) ? "" : value.toString()); // NOI18N
        }
    }
    
    //============================= EDITORS ====================================
    
    static class GenericEditor extends DefaultCellEditor {
        
        Class[] argTypes = new Class[]{String.class};
        java.lang.reflect.Constructor constructor;
        Object value;
        
        public GenericEditor() {
            super(new JTextField());
            getComponent().setName("Table.editor"); // NOI18N
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
            if ("".equals(s)) {  // NOI18N
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
                            "Invalid number: " + value, // NOI18N
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
            
            return ""; // FIXME: restore the old value // NOI18N
        }
    }
    
    static class BooleanEditor extends DefaultCellEditor {
        public BooleanEditor() {
            super(new JCheckBox());
//            JCheckBox checkBox = (JCheckBox)getComponent();
        }
        
        public Object getCellEditorValue() {
            Boolean b = (Boolean) super.getCellEditorValue();
            return b ? "true" : "false"; // NOI18N
        }
    }
    
    static class PasswordEditor extends DefaultCellEditor {
        private static final Border myBorder = new EmptyBorder(1, 3, 1, 1);
        public PasswordEditor() {
            super(new JPasswordField());
            setClickCountToStart(1);
            ((JComponent)getComponent()).setBorder(myBorder);
            ((JPasswordField)getComponent()).setEchoChar('*'); // NOI18N
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
