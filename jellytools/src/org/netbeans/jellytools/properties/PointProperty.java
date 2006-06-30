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
import org.netbeans.jellytools.properties.editors.PointCustomEditorOperator;

/** Operator serving property of type Point */
public class PointProperty extends Property {

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
