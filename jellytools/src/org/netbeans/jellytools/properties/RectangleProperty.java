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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.properties;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.RectangleCustomEditorOperator;

/** Operator serving property of type Rectangle
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class RectangleProperty extends Property {

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
