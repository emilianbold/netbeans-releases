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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationDescriptor;
import org.netbeans.modules.sun.manager.jbi.util.StackTraceUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class TabularDataCellEditorFactory {
    
    private static final TableCellEditor STRING_EDITOR = new StringCellEditor();
    private static final TableCellEditor BOOLEAN_EDITOR = new BooleanCellEditor();
    private static final TableCellEditor DOUBLE_EDITOR = new NumberCellEditor();
    private static final TableCellEditor PASSWORD_EDITOR = new PasswordCellEditor();
//    private static final TableCellEditor TABULAR_DATA_EDITOR = 
//            new TabularDataActionTableCellEditor(new TabularDataCellEditor());
    
    private static final Map<Class, TableCellEditor> classMap = 
            new HashMap<Class, TableCellEditor>();
    
    private static final Map<ApplicationVariableType, TableCellEditor> avTypeMap = 
            new HashMap<ApplicationVariableType, TableCellEditor>();
    
    static {
        classMap.put(String.class, STRING_EDITOR);
        classMap.put(Number.class, DOUBLE_EDITOR);
        classMap.put(Boolean.class, BOOLEAN_EDITOR);
//        classMap.put(TabularData.class, TABULAR_DATA_EDITOR);
        
        avTypeMap.put(ApplicationVariableType.STRING, STRING_EDITOR);
        avTypeMap.put(ApplicationVariableType.NUMBER, DOUBLE_EDITOR);
        avTypeMap.put(ApplicationVariableType.BOOLEAN, BOOLEAN_EDITOR);
        avTypeMap.put(ApplicationVariableType.PASSWORD, PASSWORD_EDITOR);
    }
    
    public static TableCellEditor getEditor(Class clazz) {
        TableCellEditor ret = classMap.get(clazz);
        
        if (ret == null) {
            ret = STRING_EDITOR;
        }
        
        return ret;
    }
    
    public static TableCellEditor getEditor(Class clazz, 
            JBIComponentConfigurationDescriptor descriptor, boolean isWritable) {
        if (clazz.equals(TabularData.class)) {
            return new TabularDataActionTableCellEditor(
                    new TabularDataCellEditor(), descriptor, isWritable);
        } else {
            return getEditor(clazz);
        }
        
    }
    
    public static TableCellEditor getEditor(ApplicationVariableType avType) {
        TableCellEditor ret = avTypeMap.get(avType);
        
        if (ret == null) {
            ret = STRING_EDITOR;
        }
        
        return ret;
    }
    
    //==========================================================================
    
    /**
     * A generic editor.
     */
    private static class GenericCellEditor extends DefaultCellEditor {
        
        Class[] argTypes = new Class[]{String.class};
        Constructor constructor;
        Object value;
        
        public GenericCellEditor() {
            super(new JTextField());
            getComponent().setName("Table.editor"); // NOI18N
            setClickCountToStart(1);
        }
        
        @Override
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
        
        @Override
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
        
        @Override
        public Object getCellEditorValue() {
            return value;
        }
    }
    
    /**
     * A number editor
     */
    private static class NumberCellEditor extends GenericCellEditor {
        
        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);
                        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected,
                int row, int column) {
            Component component = super.getTableCellEditorComponent(
                    table, value, isSelected, row, column);
            ((JComponent)component).setBorder(myBorder);
            return component;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (value != null && ((String)value).trim().length() > 0) {
                try {
                    Double.parseDouble((String)value);
                    return value;
                } catch (NumberFormatException e) {
                    String msg = NbBundle.getMessage(
                            TabularDataCellEditorFactory.class, 
                            "MSG_INVALID_NUMBER", value); // NOI18N
                    if (StackTraceUtil.isCalledBy("*", "vetoableChange")) { // NOI18N
                        // called by SimpleTabularDataEditor$1.vetoableChange()
                        throw new RuntimeException(msg);
                    } else {
                        NotifyDescriptor d = new NotifyDescriptor.Message(msg,
                                NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                    }
                }
            }
            
            return ""; // FIXME: restore the old value // NOI18N
        }
    }
    
    /**
     * A boolean editor.
     */
    private static class BooleanCellEditor extends DefaultCellEditor {
        
        public BooleanCellEditor() {
            super(new JCheckBox());
        }
        
        @Override
        public Object getCellEditorValue() {
            Boolean b = (Boolean) super.getCellEditorValue();
            return b.toString();
        }
    }
    
    /**
     * A password editor.
     */
    private static class PasswordCellEditor extends DefaultCellEditor {
        
        private static final Border myBorder = new EmptyBorder(1, 3, 1, 1);
        
        public PasswordCellEditor() {
            super(new JPasswordField());
            setClickCountToStart(1);
            ((JComponent)getComponent()).setBorder(myBorder);
            ((JPasswordField)getComponent()).setEchoChar('*'); // NOI18N
        }
    }
    
    /**
     * A string editor.
     */
    private static class StringCellEditor extends DefaultCellEditor {
        
        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        
        public StringCellEditor() {
            super(new JTextField());
            setClickCountToStart(1);
            ((JComponent)getComponent()).setBorder(myBorder);
        }
    }
    
    /**
     * A tabular data editor.
     */
    private static class TabularDataCellEditor extends DefaultCellEditor { 
                
        public TabularDataCellEditor() {
            super(new JTextField());
            setClickCountToStart(1);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, 
                boolean isSelected, int row, int column) {

            StringBuilder sb = new StringBuilder();

            if (value != null) {
                TabularData tabularData = (TabularData) value;

                sb.append("{"); // NOI18N
                for (Object rowDataObj : tabularData.values()) {
                    CompositeData rowData = (CompositeData) rowDataObj;
                    String rowValues = getStringForRowData(rowData);
                    sb.append(rowValues);
                }
                sb.append("}"); //NOI18N
            }

            return super.getTableCellEditorComponent(table, sb.toString(), 
                    isSelected, row, column);
        }

        private String getStringForRowData(CompositeData rowData) {
            Collection rowValues = rowData.values();
            return rowValues.toString();
        }
    }
}

abstract class AbstractActionTableCellEditorDecorator 
        implements TableCellEditor, ActionListener {

    private TableCellEditor realEditor;
    private JButton editButton = new JButton("..."); // NOI18N
    protected JTable table;
    protected int row;
    protected int column;

    public AbstractActionTableCellEditorDecorator(TableCellEditor realEditor) {
        this.realEditor = realEditor;
        editButton.addActionListener(this);

        editButton.setFocusable(false);
        editButton.setFocusPainted(false);
        editButton.setMargin(new Insets(0, 0, 0, 0));
    }

    public Component getTableCellEditorComponent(JTable table, Object value, 
            boolean isSelected, int row, int column) {
        JPanel panel = new JPanel(new BorderLayout());
        
        Component editorComponent = realEditor.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        panel.add(editorComponent);
        panel.add(editButton, BorderLayout.EAST);
        this.table = table;
        this.row = row;
        this.column = column;
        
        return panel;
    }

    public Object getCellEditorValue() {
        return realEditor.getCellEditorValue();
    }

    public boolean isCellEditable(EventObject anEvent) {
        return realEditor.isCellEditable(anEvent);
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return realEditor.shouldSelectCell(anEvent);
    }

    public boolean stopCellEditing() {
        return realEditor.stopCellEditing();
    }

    public void cancelCellEditing() {
        realEditor.cancelCellEditing();
    }

    public void addCellEditorListener(CellEditorListener l) {
        realEditor.addCellEditorListener(l);
    }

    public void removeCellEditorListener(CellEditorListener l) {
        realEditor.removeCellEditorListener(l);
    }

    public final void actionPerformed(ActionEvent e) {
        realEditor.cancelCellEditing();
        editCell(table, row, column);
    }

    protected abstract void editCell(JTable table, int row, int column);
}

class TabularDataActionTableCellEditor 
        extends AbstractActionTableCellEditorDecorator {
    
    private JBIComponentConfigurationDescriptor descriptor;
    private boolean isWritable;

    public TabularDataActionTableCellEditor(TableCellEditor realEditor,
            boolean isWritable) {
        this(realEditor, null, isWritable);
    }
    
    public TabularDataActionTableCellEditor(TableCellEditor realEditor,
            JBIComponentConfigurationDescriptor descriptor,
            boolean isWritable) {
        super(realEditor);
        this.descriptor = descriptor;
        this.isWritable = isWritable;
    }

    protected void editCell(JTable table, int row, int column) {
        Object value = table.getValueAt(row, column);
        TabularData tabularData = (TabularData)value;
        
        SimpleTabularDataCustomEditor editorPanel = 
                new SimpleTabularDataCustomEditor(tabularData,
                "title", "description", descriptor, isWritable); 
        
        int result = JOptionPane.showOptionDialog(table, 
                    editorPanel, 
                    table.getColumnName(column), 
                    JOptionPane.OK_CANCEL_OPTION, 
                    JOptionPane.PLAIN_MESSAGE, 
                    null, null, null);
        if (result == JOptionPane.OK_OPTION) {
            table.setValueAt(editorPanel.getPropertyValue(), row, column);
        }
    }
}
