/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.properties;

/*
 * ColorProperty.java
 *
 * Created on June 18, 2002, 11:53 AM
 */

import java.awt.Color;
import org.netbeans.jellytools.properties.editors.ColorCustomEditorOperator;
import org.netbeans.jemmy.operators.*;

/** Operator serving property of type Color
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ColorProperty extends Property {
    
    /** Creates a new instance of ColorProperty
     * @param contOper ContainerOperator of parent container to search property in
     * @param name String property name */
    public ColorProperty(ContainerOperator contOper, String name) {
        super(contOper, name);
    }

    /** getter for text filed of property (if available)
     * @return JTextFieldOperator */    
    public JTextFieldOperator textField() {
        startEditing();
        return new JTextFieldOperator(contOper);
    }
    
    /** Sets value of the property. It makes property editable, finds
     * JTextField, sets its value and pushes Enter key.
     * @param value new value of property
     */
    public void setTextValue(String value) {
        JTextFieldOperator textOper = textField();
        textOper.enterText(value);
    }

    /** getter for combo box of property (if available)
     * @return JComboBoxOperator */    
    public JComboBoxOperator comboBox() {
        startEditing();
        return new JComboBoxOperator(contOper);
    }        

    /** Sets value of the property. It makes property editable, finds
     * JComboBox and selects specified item.
     * @param value item to be selected
     */
    public void setComboValue(String value) {
        JComboBoxOperator comboOper = comboBox();
        comboOper.setSelectedItem(value);
    }
    
    /** Sets value of the property. It makes property editable, finds
     * JComboBox and selects index-th item.
     * @param index index of item to be selected (Start at 0)
     */
    public void setComboValue(int index) {
        JComboBoxOperator comboOper = comboBox();
        comboOper.setSelectedIndex(index);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return ColorCustomEditorOperator */    
    public ColorCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new ColorCustomEditorOperator(getName());
    }
    
    /** getter for RGB value through Custom Editor
     * @param r int red
     * @param g int green
     * @param b int blue */    
    public void setRGBValue(int r, int g, int b) {
        ColorCustomEditorOperator customizer=invokeCustomizer();
        customizer.setRGBValue(r, g, b);
        customizer.ok();
    }        
    
    /** setter for Color value through Custom Editor
     * @param value Color */    
    public void setColorValue(Color value) {
        ColorCustomEditorOperator customizer=invokeCustomizer();
        customizer.setColorValue(value);
        customizer.ok();
    }        
    
    /** getter for Color value through Custom Editor
     * @return Color */    
    public Color getColorValue() {
        Color value;
        ColorCustomEditorOperator customizer=invokeCustomizer();
        value=customizer.getColorValue();
        customizer.cancel();
        return value;
    }
    
}
