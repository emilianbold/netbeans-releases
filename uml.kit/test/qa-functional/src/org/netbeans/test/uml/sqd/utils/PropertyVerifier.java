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



package org.netbeans.test.uml.sqd.utils;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.test.umllib.exceptions.ElementVerificationException;




public class PropertyVerifier {
    
    protected PropertySheetOperator properties = null;
    
    public static final String STRING_RENDERER = Property.STRING_RENDERER;
    public static final String COMBOBOX_RENDERER = Property.COMBOBOX_RENDERER;
    public static final String CHECKBOX_RENDERER = Property.CHECKBOX_RENDERER;
    public static final String RADIOBUTTON_RENDERER = Property.RADIOBUTTON_RENDERER;
    public static final String CUSTOM_RENDERER = "CUSTOM_RENDERER";
    public static final String ANY_RENDERER = "ANY_RENDERER";
    //
    public static final int ID_RENDERER=1;
    public static final int ID_EDITABLE=2;
    public static final int ID_VALUE=3;
    
    public PropertyVerifier(String name) {
        properties = new PropertySheetOperator(name);
    }
    
    public boolean verifyProperty(String propertyName, String renderer, boolean editable, String oldValue, String newValue){        
        boolean result = false;
        
        result = checkProperty(propertyName, oldValue, renderer, editable);
        if (!result){
            //if (true) throw new RuntimeException("dfg - "+checkRenderer(renderer,new Property(properties, propertyName))+ "  "+checkEditable(editable,new Property(properties, propertyName))+ " "+checkValue(oldValue, new Property(properties, propertyName))+new Property(properties, propertyName).getRendererName());
            return false;
        }
        
        result = setNewValue(propertyName, newValue);
        if (!result){
            //if (true) throw new RuntimeException("dfg2");
            return false;
        }
        
        result = checkProperty(propertyName, newValue, renderer,  editable);
        if (!result){
            //if (true) throw new RuntimeException("dfg3");
            return false;
        }
        
        result = checkElement();
        if (!result){
            //if (true) throw new RuntimeException("dfg4");
            return false;
        }   
        
        return true;
    }
    
    /*
     * TBD
     */
    protected boolean checkElement(){
        return true;
    }
    
    protected boolean setNewValue(String propertyName, String newValue){
            Property prop = new Property(properties, propertyName);            
            
            final JTableOperator table = properties.tblSheet();   
            int rowIndex = prop.getRow();                              
            TableCellRenderer renderer = table.getCellRenderer(rowIndex, 1);
            Component comp = renderer.getTableCellRendererComponent(
                                                (JTable)table.getSource(), 
                                                table.getValueAt(rowIndex, 1), 
                                                false, 
                                                false, 
                                                rowIndex, 
                                                1
            );            
            
            if (prop.getRendererName().equals(Property.STRING_RENDERER)){
                table.clickOnCell(rowIndex,1);
                new EventTool().waitNoEvent(1000);
                JLabelOperator txt = new JLabelOperator((JLabel)comp);
                txt.setText("");
                for(int i=0;i<newValue.length();i++) {
                    txt.typeKey(newValue.charAt(i));
                }
                txt.typeKey('\n');                
            }else if (prop.getRendererName().equals(Property.COMBOBOX_RENDERER)){
                prop.setValue(newValue);
            }else{
                prop.setValue(newValue);
            }
            return true;
    }
    
    /**
     * Checks property name,value,editable
     */
    protected boolean checkProperty(String propertyName, String value, String renderer, boolean editable){        
        Property prop = new Property(properties, propertyName);
        return checkRenderer(renderer, prop)&&checkEditable(editable, prop)&&checkValue(value, prop);
    }    
        
    protected boolean checkRenderer(String rendererType, Property prop){        
        if (rendererType.equals(ANY_RENDERER)){
            return true;
        }
        if (rendererType.equals(CUSTOM_RENDERER) && prop.supportsCustomEditor()){
            return true;
        }
        if(prop.getRendererName().equals(rendererType))
        {
            return true;
        }
        throw new ElementVerificationException("Property renderer doesn't match #"+rendererType+"#, current is #"+prop.getRendererName()+"#",ID_RENDERER);
    }
    
    protected boolean checkEditable(boolean editable, Property prop){
        if(prop.canEditAsText()==editable)return true;
        else throw new ElementVerificationException("Editable state does not match expected #"+editable+"#, current is #"+prop.canEditAsText()+"#",ID_EDITABLE);
    }
    
    public boolean checkValue(String propName, String propValue){    
        Property prop = new Property(properties, propName);
        return checkValue(propValue,prop);
    }            
    protected boolean checkValue(String value, Property prop){                
        String propVal = prop.getValue();
        if (propVal!=null){
            if(propVal.equals(value))return true;
            else throw new ElementVerificationException("Value does not match expected #"+value+"#, current is #"+propVal+"#",ID_VALUE);
        }
        
        final JTableOperator table = properties.tblSheet();   
        int rowIndex = prop.getRow();

        TableCellRenderer renderer = table.getCellRenderer(rowIndex, 1);
        Component comp = renderer.getTableCellRendererComponent(
                                            (JTable)table.getSource(), 
                                            table.getValueAt(rowIndex, 1), 
                                            false, 
                                            false, 
                                            rowIndex, 
                                            1
        );            

        if (prop.getRendererName().equals(Property.COMBOBOX_RENDERER)){
            JComboBoxOperator cb = new JComboBoxOperator((JComboBox)comp);                
            String txt = cb.getItemAt(cb.getSelectedIndex()).toString();            
            /*
            if (!txt.equals(value)){
                throw new RuntimeException("df"+value+"|"+txt);
            }
            */
            if(txt.equals(value))return true;
            else throw new ElementVerificationException("Value does not match expected #"+value+"#, current is #"+txt+"#",ID_VALUE);
        }else{
            //!old code, strange way because !null alsready checked before, !!warning possible NPE
            if(prop.getValue().equals(value))return true;
            else throw new ElementVerificationException("Value does not match expected #"+value+"#, current is #"+prop.getValue()+"#",ID_VALUE);
        }
    }
            
}
