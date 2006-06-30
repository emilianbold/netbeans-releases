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
import org.netbeans.jellytools.properties.editors.DimensionCustomEditorOperator;

/** Operator serving property of type Dimension
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class DimensionProperty extends Property {

    /** Creates a new instance of DimensionProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public DimensionProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return DimensionCustomEditorOperator */    
    public DimensionCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new DimensionCustomEditorOperator(getName());
    }
    
    /** setter for Dimension valuethrough Custom Editor
     * @param width String width
     * @param height String height */    
    public void setDimensionValue(String width, String height) {
        DimensionCustomEditorOperator customizer=invokeCustomizer();
        customizer.setDimensionValue(width, height);
        customizer.ok();
    }        
    
    /** getter for Dimension valuethrough Custom Editor
     * @return String[2] width and height */    
    public String[] getDimensionValue() {
        String[] value=new String[2];
        DimensionCustomEditorOperator customizer=invokeCustomizer();
        value[0]=customizer.getWidthValue();
        value[1]=customizer.getHeightValue();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }         
}
