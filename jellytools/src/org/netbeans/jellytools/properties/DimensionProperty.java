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
import org.netbeans.jellytools.properties.editors.DimensionCustomEditorOperator;
import org.netbeans.jemmy.operators.ContainerOperator;

/** Operator serving property of type Dimension
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class DimensionProperty extends Property {
    
    /** Creates a new instance of DimensionProperty
     * @param contOper ContainerOperator of parent container to search property in
     * @param name String property name 
     * @deprecated Use {@link #DimensionProperty(PropertySheetOperator, String)} instead
     */
    public DimensionProperty(ContainerOperator contOper, String name) {
        super(contOper, name);
    }
    
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
