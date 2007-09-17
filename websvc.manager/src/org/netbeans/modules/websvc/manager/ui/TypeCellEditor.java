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

package org.netbeans.modules.websvc.manager.ui;

import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.NodeRowModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.net.URI;
import java.net.URISyntaxException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.JComboBox;
import java.awt.event.ActionEvent;
import org.openide.ErrorManager;

/**
 *
 * @author  david
 */
public class TypeCellEditor extends DefaultCellEditor implements TableCellEditor {
    Component lastComponent;
    String type;
    private ClassLoader classLoader;
    
    /** Creates a new instance of TypeCellRenderer */
    public TypeCellEditor(ClassLoader loader) {
        super(new JTextField());
        this.setClickCountToStart(1);
        this.classLoader = loader;
    }
    
    public void cancelCellEditing() {
        return;
    }
    
    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }
    
    /**
     * return the value of the last component.
     */
    public Object getCellEditorValue() {
        if(null == type) {
            return ((JTextField)lastComponent).getText();
        } else {
            if(lastComponent instanceof JTextField) {
                String valueString = ((JTextField)lastComponent).getText();
                Object value = createValue(valueString);
                return value;
            } else if(lastComponent instanceof JComboBox) {
                return ((JComboBox)lastComponent).getSelectedItem();
            } else return null;
        }
        
    }
    
    
    
    public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
        /**
         * We need to create the correct editing component for the type of field we have.
         *  JavaSimpleTypes all except Date and Calendar - JTextField()
         *  JavaEnumerationType - JComboBox
         */
        
        /**
         *  First, we need to get the JavaType for the node of the object to be edited.
         */
        
        NodeRowModel rowModel = ((OutlineModel)table.getModel()).getRowNodeModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)rowModel.getNodeForRow(row);
        /**
         * Now depending on the type, create a component to edit/display the type.
         */
        if(null == node.getUserObject()) {
            JTextField txtField = new JTextField();
            txtField.setText((String)value);
            lastComponent = (Component)txtField;
            
        } else {
            TypeNodeData data = (TypeNodeData)node.getUserObject();
            type = data.getTypeClass();
            
            if (ReflectionHelper.isSimpleType(type, classLoader)) {
                /**
                 * If the type is boolean or Boolean, create a JComboBox with true,false
                 */
                if (type.equalsIgnoreCase(boolean.class.getName()) ||
                        type.equalsIgnoreCase(Boolean.class.getName())) {
                    JComboBox combo = new JComboBox();
                    lastComponent = (Component)combo;
                    combo.addItem(new Boolean(true));
                    combo.addItem(new Boolean(false));
                    
                    /**
                     * Set the value as the current Enumeration value.
                     */
                    
                    Object parameterValue = data.getTypeValue();
                    
                    combo.setSelectedItem(parameterValue);
                    combo.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            comboActionPerformed(evt);
                        }
                    });
                } else {
                    
                    JTextField txtField = new JTextField();
                    /**
                     * figure out what kind of simple field this is to set the value.
                     */
                    if(null != value) {
                        txtField.setText(value.toString());
                    }
                    lastComponent = (Component)txtField;
                }
                
            } else if (ReflectionHelper.isEnumeration(type, classLoader)) {
                try {
                    JComboBox combo = new JComboBox();
                    List<String> enumTypes = ReflectionHelper.getEnumerationValues(type, classLoader);
                    for (String enumType : enumTypes) {
                        Object nextEnum = ReflectionHelper.getEnumeration(type, enumType, classLoader);
                        combo.addItem(nextEnum);
                    }
                    lastComponent = combo;

                    combo.setSelectedItem(data.getTypeValue());
                    combo.addActionListener(new java.awt.event.ActionListener() {

                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            comboActionPerformed(evt);
                        }
                    });
                } catch (WebServiceReflectionException wsre) {
                    Throwable cause = wsre.getCause();
                    ErrorManager.getDefault().notify(cause);
                    ErrorManager.getDefault().log(this.getClass().getName() + ": Error retrieving Enum type on: " + "WebServiceReflectionException=" + cause);
                }
            }
        }
        
        return lastComponent;
    }
    
    private void comboActionPerformed(ActionEvent evt) {
        JComboBox combo = (JComboBox)evt.getSource();
        this.fireEditingStopped();
        
    }
    
    private Object createValue(String inValue) {
        Object returnValue = null;
        String currentType = type;
        
        if(currentType.equalsIgnoreCase("int") ||
        currentType.equalsIgnoreCase("java.lang.Integer")) {
            try {
                returnValue = new Integer(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = new Integer(0);
            }
        } else if(currentType.equalsIgnoreCase("byte") ||
        currentType.equalsIgnoreCase("java.lang.Byte")) {
            try {
                returnValue = new Byte(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = new Byte("0");
            }
        } else if(currentType.equalsIgnoreCase("boolean") ||
        currentType.equalsIgnoreCase("java.lang.Boolean")) {
            try {
                returnValue = new Boolean(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = new Boolean(false);
            }
        } else if(currentType.equalsIgnoreCase("float") ||
        currentType.equalsIgnoreCase("java.lang.Float")) {
            try {
                returnValue = new Float(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = new Float(0);
            }
        } else if(currentType.equalsIgnoreCase("double") ||
        currentType.equalsIgnoreCase("java.lang.Double")) {
            try {
                returnValue = new Double(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = new Double(0);
            }
        } else if(currentType.equalsIgnoreCase("long") ||
        currentType.equalsIgnoreCase("java.lang.Long")) {
            try {
                returnValue = new Long(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = new Long(0);
            }
        } else if(currentType.equalsIgnoreCase("short") ||
        currentType.equalsIgnoreCase("java.lang.Short")) {
            try {
                returnValue = new Short(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = new Short(" ");
            }
        } else if(currentType.equalsIgnoreCase("java.lang.String")) {
            returnValue = inValue;
        } else if(currentType.equalsIgnoreCase("java.math.BigDecimal")) {
            try {
                returnValue = new BigDecimal(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = new BigDecimal(0);
            }
        } else if(currentType.equalsIgnoreCase("java.math.BigInteger")) {
            try {
                returnValue = new BigInteger(inValue);
            } catch(NumberFormatException nfe) {
                returnValue = new BigInteger("0");
            }
        } else if(currentType.equalsIgnoreCase("java.net.URI")) {
            try {
                returnValue = new URI(inValue);
            } catch(URISyntaxException uri) {
                try {
                    returnValue = new URI("http://java.sun.com");
                } catch(URISyntaxException uri2) {}
            }
        } else if(currentType.equalsIgnoreCase("java.util.Calendar")) {
            returnValue = Calendar.getInstance();
        } else if(currentType.equalsIgnoreCase("java.util.Date")) {
            try {
                returnValue = DateFormat.getInstance().parse(inValue);
            } catch(ParseException pe) {
                returnValue = new Date();
            }
        }
        
        return returnValue;
        
    }
    
}
