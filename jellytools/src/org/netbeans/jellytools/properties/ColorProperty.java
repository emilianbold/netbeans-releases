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

import java.awt.Color;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.ColorCustomEditorOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.*;

/** Operator serving property of type Color
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ColorProperty extends Property {
    
    /** Creates a new instance of ColorProperty
     * @param contOper ContainerOperator of parent container to search property in
     * @param name String property name 
     * @deprecated Use {@link #ColorProperty(PropertySheetOperator, String)} instead
     */
    public ColorProperty(ContainerOperator contOper, String name) {
        super(contOper, name);
    }

    /** Creates a new instance of ColorProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name 
     */
    public ColorProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** getter for text filed of property (if available)
     * @return JTextFieldOperator 
     * @deprecated Use {@link #setValue} to change property value
     */    
    public JTextFieldOperator textField() {
        throw new JemmyException("Don't use this! Property sheet uses JTable instead of SheetButton.");
        /*
        startEditing();
        return new JTextFieldOperator(contOper);
         */
    }
    
    /** Sets value of the property. It makes property editable, finds
     * JTextField, sets its value and pushes Enter key.
     * @param value new value of property
     * @deprecated Use {@link #setValue} to change property value
     */
    public void setTextValue(String value) {
        setValue(value);
        /*
        JTextFieldOperator textOper = textField();
        textOper.enterText(value);
         */
    }

    /** getter for combo box of property (if available)
     * @return JComboBoxOperator 
     * @deprecated Use {@link #setValue} to change property value
     */
    public JComboBoxOperator comboBox() {
        throw new JemmyException("Don't use this! Property sheet uses JTable instead of SheetButton.");
        /*
        startEditing();
        return new JComboBoxOperator(contOper);
         */
    }        

    /** Sets value of the property. It makes property editable, finds
     * JComboBox and selects specified item.
     * @param value item to be selected
     * @deprecated Use {@link #setValue(String)} to change property value
     */
    public void setComboValue(String value) {
        setValue(value);
        /*
        JComboBoxOperator comboOper = comboBox();
        comboOper.setSelectedItem(value);
         */
    }
    
    /** Sets value of the property. It makes property editable, finds
     * JComboBox and selects index-th item.
     * @param index index of item to be selected (Start at 0)
     * @deprecated Use {@link #setValue(int)} to change property value
     */
    public void setComboValue(int index) {
        setValue(index);
        /*
        JComboBoxOperator comboOper = comboBox();
        comboOper.setSelectedIndex(index);
         */
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
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
