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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.RectangleCustomEditorOperator;
import org.netbeans.jemmy.operators.ContainerOperator;

/** Operator serving property of type Rectangle
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class RectangleProperty extends TextFieldProperty {
    
    /** Creates a new instance of RectangleProperty
     * @param contOper ContainerOperator of parent container to search property in
     * @param name String property name 
     * @deprecated Use {@link #RectangleProperty(PropertySheetOperator, String)} instead
     */
    public RectangleProperty(ContainerOperator contOper, String name) {
        super(contOper, name);
    }
    
    /** Creates a new instance of RectangleProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name 
     */
    public RectangleProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return RectangleCustomEditorOperator */    
    public RectangleCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new RectangleCustomEditorOperator(getName());
    }
    
    /** setter for Rectangle value through Custom Editor
     * @param x String x
     * @param y String y
     * @param width String width
     * @param height String height */    
    public void setRectangleValue(String x, String y, String width, String height) {
        RectangleCustomEditorOperator customizer=invokeCustomizer();
        customizer.setRectangleValue(x, y, width, height);
        customizer.ok();
    }        
    
    /** getter for Rectangle value through Custom Editor
     * @return String[4] x, y, width and height */    
    public String[] getRectangleValue() {
        String[] value=new String[4];
        RectangleCustomEditorOperator customizer=invokeCustomizer();
        value[0]=customizer.getXValue();
        value[1]=customizer.getYValue();
        value[2]=customizer.getWidthValue();
        value[3]=customizer.getHeightValue();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
