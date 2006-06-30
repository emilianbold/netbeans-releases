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

import java.awt.Color;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.ColorCustomEditorOperator;

/** Operator serving property of type Color
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ColorProperty extends Property {

    /** Creates a new instance of ColorProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public ColorProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
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
