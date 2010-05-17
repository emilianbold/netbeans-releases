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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.registry.ui;

import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import com.sun.xml.rpc.processor.model.java.JavaEnumerationType;
import com.sun.xml.rpc.processor.model.java.JavaEnumerationEntry;
import com.sun.xml.rpc.processor.model.java.JavaSimpleType;
import com.sun.xml.rpc.processor.model.java.JavaType;

import org.openide.util.NbBundle;

import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.NodeRowModel;

import javax.swing.tree.DefaultMutableTreeNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
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

/**
 *
 * @author  david
 */
public class TypeCellEditor extends DefaultCellEditor implements TableCellEditor {
    Component lastComponent;
    JavaType type;
    
    /** Creates a new instance of TypeCellRenderer */
    public TypeCellEditor() {
        super(new JTextField());
        this.setClickCountToStart(1);
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
            type = data.getParameterType();
            
            if(type instanceof JavaSimpleType) {
                /**
                 * If the type is boolean or Boolean, create a JComboBox with true,false
                 */
                if(type.getRealName().equalsIgnoreCase("boolean") ||
                type.getRealName().equalsIgnoreCase("java.lang.Boolean")) {
                    JComboBox combo = new JComboBox();
                    lastComponent = (Component)combo;
                    combo.addItem(new Boolean(true));
                    combo.addItem(new Boolean(false));
                    
                    /**
                     * Set the value as the current Enumeration value.
                     */
                    
                    Object parameterValue = data.getParameterValue();
                    
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
                    txtField.setText(value != null ? value.toString() : ""); // NOI18N
                    lastComponent = (Component)txtField;
                }
                
            }  else if(type instanceof JavaEnumerationType) {
                JComboBox combo = new JComboBox();
                lastComponent = (Component)combo;
                JavaEnumerationType enumType = (JavaEnumerationType)type;
                Iterator iterator = enumType.getEntries();
                while(iterator.hasNext()) {
                    JavaEnumerationEntry entry = (JavaEnumerationEntry)iterator.next();
                    combo.addItem(entry.getLiteralValue());
                }
                
                /**
                 * Set the value as the current Enumeration value.
                 */
                
                Object parameterValue = data.getParameterValue();
                
                combo.setSelectedItem(parameterValue);
                
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
        String currentType = type.getRealName();
        
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
                returnValue = new Byte(" ");
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
            try {
                Date date = DateFormat.getDateTimeInstance().parse(inValue);
                returnValue = Calendar.getInstance();
                ((Calendar)returnValue).setTime(date);             
            } catch (java.text.ParseException ex) {
                returnValue = Calendar.getInstance();
            }
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
