/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.compapp.casaeditor.properties.extension;

import org.netbeans.modules.compapp.casaeditor.properties.spi.TabularDataDescriptor;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import javax.xml.namespace.QName;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class TabularDataCellEditorFactory {    
    
    // For Application Variable:
    private static final TableCellEditor STRING_EDITOR = new StringCellEditor(); //DefaultCellEditor(new JTextField());
    private static final TableCellEditor STRING_TYPE_BOOLEAN_EDITOR = new StringTypeBooleanCellEditor();
    private static final TableCellEditor STRING_TYPE_NUMBER_EDITOR = new StringTypeNumberCellEditor();
    private static final TableCellEditor PASSWORD_EDITOR = new PasswordCellEditor();
    
    // For Applicaiton Configuration:
    private static final TableCellEditor INTEGER_EDITOR = new IntegerCellEditor();
    private static final TableCellEditor BOOLEAN_EDITOR = new DefaultCellEditor(new JCheckBox()); 
//    private static final TableCellEditor TABULAR_DATA_EDITOR = 
//            new TabularDataActionTableCellEditor(new TabularDataCellEditor());
    
    private static final Map<Class, TableCellEditor> classMap = 
            new HashMap<Class, TableCellEditor>();
    
    static {        
        classMap.put(String.class, STRING_EDITOR);
        classMap.put(Integer.class, INTEGER_EDITOR);
        classMap.put(Boolean.class, BOOLEAN_EDITOR);
//        classMap.put(Number.class, DOUBLE_EDITOR);
//        classMap.put(TabularData.class, TABULAR_DATA_EDITOR);
    }       
    
    public static TableCellEditor getEditor(Class clazz, 
            TabularDataDescriptor descriptor) {
        /*if (clazz.equals(TabularData.class)) {
            return new TabularDataActionTableCellEditor(
                    new TabularDataCellEditor(), descriptor, isWritable);
        } else */if (descriptor != null) {
            return getEditor(descriptor);
        } else {
            return getEditor(clazz);
        }        
    }
    
    public static TableCellEditor getEditor(Class clazz) {
        TableCellEditor ret = classMap.get(clazz);
        
        if (ret == null) {
            ret = STRING_EDITOR;
        }
        
        return ret;
    }
    
    private static TableCellEditor getEditor(TabularDataDescriptor descriptor) {
        
        QName typeQName = descriptor.getTypeQName();

//        List<JBIComponentConfigurationConstraint> constraints =
//                descriptor.getConstraints();
   
        if (descriptor.isEncrypted()) {
            return PASSWORD_EDITOR;
        } else if (TabularDataDescriptor.XSD_STRING.equals(typeQName)) {
//            if (constraints.size() == 0) {
                return STRING_EDITOR;
//            } else if (constraints.size() == 1 &&
//                    constraints.get(0) instanceof EnumerationConstraint) {
//                List<String> options =
//                        ((EnumerationConstraint) constraints.get(0)).getOptions();
//                return new DefaultCellEditor(new JComboBox(options.toArray()));
//            }
        } else if (TabularDataDescriptor.XSD_INT.equals(typeQName) ||
                TabularDataDescriptor.XSD_BYTE.equals(typeQName) ||
                TabularDataDescriptor.XSD_SHORT.equals(typeQName) ||
                TabularDataDescriptor.XSD_POSITIVE_INTEGER.equals(typeQName) ||
                TabularDataDescriptor.XSD_NEGATIVE_INTEGER.equals(typeQName) ||
                TabularDataDescriptor.XSD_NON_POSITIVE_INTEGER.equals(typeQName) ||
                TabularDataDescriptor.XSD_NON_NEGATIVE_INTEGER.equals(typeQName)) {

            int minInc, maxInc;

//            if (JBIComponentConfigurationDescriptor.XSD_BYTE.equals(typeQName)) {
//                minInc = Byte.MIN_VALUE;
//                maxInc = Byte.MAX_VALUE;
//            } else if (JBIComponentConfigurationDescriptor.XSD_SHORT.equals(typeQName)) {
//                minInc = Short.MIN_VALUE;
//                maxInc = Short.MAX_VALUE;
//            } else if (JBIComponentConfigurationDescriptor.XSD_INT.equals(typeQName)) {
                minInc = Integer.MIN_VALUE;
                maxInc = Integer.MAX_VALUE;
//            } else if (JBIComponentConfigurationDescriptor.XSD_POSITIVE_INTEGER.equals(typeQName)) {
//                minInc = 1;
//                maxInc = Integer.MAX_VALUE;
//            } else if (JBIComponentConfigurationDescriptor.XSD_NEGATIVE_INTEGER.equals(typeQName)) {
//                minInc = Integer.MIN_VALUE;
//                maxInc = -1;
//            } else if (JBIComponentConfigurationDescriptor.XSD_NON_POSITIVE_INTEGER.equals(typeQName)) {
//                minInc = Integer.MIN_VALUE;
//                maxInc = 0;
//            } else { //if (JBIComponentConfigurationDescriptor.XSD_NON_NEGATIVE_INTEGER.equals(typeQName)) {
//                minInc = 0;
//                maxInc = Integer.MAX_VALUE;
//            }

//            for (JBIComponentConfigurationConstraint constraint : constraints) {
//                if (constraint instanceof MinInclusiveConstraint) {
//                    minInc = Math.max(minInc,
//                            (int) ((MinInclusiveConstraint) constraint).getValue());
//                } else if (constraint instanceof MaxInclusiveConstraint) {
//                    maxInc = Math.min(maxInc,
//                            (int) ((MaxInclusiveConstraint) constraint).getValue());
//                } else if (constraint instanceof MinExclusiveConstraint) {
//                    minInc = Math.max(minInc,
//                            (int) ((MinInclusiveConstraint) constraint).getValue() + 1);
//                } else if (constraint instanceof MaxExclusiveConstraint) {
//                    maxInc = Math.min(maxInc,
//                            (int) ((MaxInclusiveConstraint) constraint).getValue() - 1);
//                } else {
//                    throw new RuntimeException("Constraint not supported yet: " +
//                            constraint.getClass().getName());
//                }
//            }
            return new IntegerCellEditor(minInc, maxInc);
        } else if (TabularDataDescriptor.XSD_BOOLEAN.equals(typeQName)) {
            return BOOLEAN_EDITOR;
        }

        System.err.println("WARNING: (schema-based) TabularDataCellEditorFactory: Unsupported type: " + typeQName);
        return STRING_EDITOR;
    }
        
    //==========================================================================
    
    /**
     * A generic editor.
     */
    private static class GenericCellEditor extends DefaultCellEditor {
                
        public GenericCellEditor() {
            super(new JTextField());
            getComponent().setName("Table.editor"); // NOI18N
            setClickCountToStart(1);
        }
    }
    
    /**
     * A (string type) number editor for Application Variable.
     */
    private static class StringTypeNumberCellEditor extends GenericCellEditor {
        
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
        public String getCellEditorValue() {
            String value = (String)super.getCellEditorValue();
            
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
     * An integer editor.
     */
    private static class IntegerCellEditor extends GenericCellEditor {
        
        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        private int minInclusiveValue;
        private int maxInclusiveValue;
                
        IntegerCellEditor() {
            this(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        
        IntegerCellEditor(int minInclusiveValue, int maxInclusiveValue) {
            this.minInclusiveValue = minInclusiveValue;
            this.maxInclusiveValue = maxInclusiveValue;            
        }
        
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
            String stringValue = (String)super.getCellEditorValue();
            
            Integer intValue = 0;
            
            try {
                intValue = Integer.parseInt((String)stringValue);
            } catch (NumberFormatException e) {
                String msg = NbBundle.getMessage(
                        TabularDataCellEditorFactory.class, 
                        "MSG_INVALID_NUMBER", stringValue); // NOI18N
                if (StackTraceUtil.isCalledBy("*", "vetoableChange")) { // NOI18N
                    // called by SimpleTabularDataEditor$1.vetoableChange()
                    throw new RuntimeException(msg);
                } else {
                    NotifyDescriptor d = new NotifyDescriptor.Message(msg,
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
                return stringValue; // keep the invalid value in the cell
            }
            
             if (intValue < minInclusiveValue || intValue > maxInclusiveValue) {
                 String msg = NbBundle.getMessage(TabularDataCellEditorFactory.class, // FIXME
                        "MSG_INVALID_INTEGER", intValue, // NOI18N
                        minInclusiveValue, maxInclusiveValue);
                 if (StackTraceUtil.isCalledBy("*", "vetoableChange")) { // NOI18N
                    // called by SimpleTabularDataEditor$1.vetoableChange()
                    throw new RuntimeException(msg);
                } else {
                    NotifyDescriptor d = new NotifyDescriptor.Message(msg,
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
                return stringValue; // keep the invalid value in the cell
            }
            
            return intValue;
        }
    }
            
    /**
     * A (string type) boolean editor.
     */
    private static class StringTypeBooleanCellEditor extends DefaultCellEditor {
        
        public StringTypeBooleanCellEditor() {
            super(new JCheckBox());
        }
        
        @Override
        public String getCellEditorValue() {
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
//    
//    /**
//     * A tabular data editor.
//     */
//    private static class TabularDataCellEditor extends DefaultCellEditor { 
//                
//        public TabularDataCellEditor() {
//            super(new JTextField());
//            setClickCountToStart(1);
//        }
//        
//        @Override
//        public Component getTableCellEditorComponent(JTable table, Object value, 
//                boolean isSelected, int row, int column) {
//
//            StringBuilder sb = new StringBuilder();
//
//            if (value != null) {
//                TabularData tabularData = (TabularData) value;
//
//                sb.append("{"); // NOI18N
//                for (Object rowDataObj : tabularData.values()) {
//                    CompositeData rowData = (CompositeData) rowDataObj;
//                    String rowValues = getStringForRowData(rowData);
//                    sb.append(rowValues);
//                }
//                sb.append("}"); //NOI18N
//            }
//
//            return super.getTableCellEditorComponent(table, sb.toString(), 
//                    isSelected, row, column);
//        }
//
//        private String getStringForRowData(CompositeData rowData) {
//            Collection rowValues = rowData.values();
//            return rowValues.toString();
//        }
//    }
}

class StackTraceUtil {

    public static boolean isCalledBy(String className, String methodName) {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        for (StackTraceElement element : elements) {
            String cName = element.getClassName();
            String mName = element.getMethodName();
            if ((cName.equals(className) || className == null || className.equals("*")) &&
                    (mName.equals(methodName) || methodName == null || methodName.equals("*"))) {
                return true;
            }
        }
        
        return false;
    }
}


