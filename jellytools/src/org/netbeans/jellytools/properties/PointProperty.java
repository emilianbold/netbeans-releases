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
import org.netbeans.jellytools.properties.editors.PointCustomEditorOperator;
import org.netbeans.jemmy.operators.ContainerOperator;

/** Operator serving property of type Point */
public class PointProperty extends Property {
    
    /** Creates a new instance of PointProperty
     * @param contOper ContainerOperator of parent container to search property in
     * @param name String property name 
     * @deprecated Use {@link #PointProperty(PropertySheetOperator, String)} instead
     */
    public PointProperty(ContainerOperator contOper, String name) {
        super(contOper, name);
    }
    
    /** Creates a new instance of PointProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name 
     */
    public PointProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return PointCustomEditorOperator */    
    public PointCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new PointCustomEditorOperator(getName());
    }
    
    /** setter for Point value through Custom Editor
     * @param x String x
     * @param y String y */    
    public void setPointValue(String x, String y) {
        PointCustomEditorOperator customizer=invokeCustomizer();
        customizer.setPointValue(x, y);
        customizer.ok();
    }        
    
    /** getter for Point value through Custom Editor
     * @return String[2] x and y coordinates */    
    public String[] getPointValue() {
        String[] value=new String[2];
        PointCustomEditorOperator customizer=invokeCustomizer();
        value[0]=customizer.getXValue();
        value[1]=customizer.getYValue();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
