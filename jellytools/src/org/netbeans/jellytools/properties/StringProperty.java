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
/*
 * StringProperty.java
 *
 * Created on June 18, 2002, 11:28 AM
 */

import org.netbeans.jellytools.properties.editors.StringCustomEditorOperator;
import org.netbeans.jemmy.operators.ContainerOperator;

/** Operator serving property of type String
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class StringProperty extends Property {//TextFieldProperty {
    
    /** Creates a new instance of StringProperty
     * @param contOper ContainerOperator of parent container to search property in
     * @param name String property name 
     * @deprecated Use {@link #StringProperty(PropertySheetOperator, String)} instead
     */
    public StringProperty(ContainerOperator contOper, String name) {
        super(contOper, name);
    }
    
    /** Creates a new instance of StringProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name 
     */
    public StringProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return StringCustomEditorOperator */    
    public StringCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new StringCustomEditorOperator(getName());
    }
    
    /** setter for String value through Custom Editor
     * @param value String */    
    public void setStringValue(String value) {
        StringCustomEditorOperator customizer=invokeCustomizer();
        customizer.setStringValue(value);
        customizer.ok();
    }    
    
    /** getter for String value through Custom Editor
     * @return String */    
    public String getStringValue() {
        StringCustomEditorOperator customizer=invokeCustomizer();
        String s=customizer.getStringValue();
        customizer.close();
        return s;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }    
}
