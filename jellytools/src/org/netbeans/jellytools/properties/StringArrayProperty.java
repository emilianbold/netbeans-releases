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
import org.netbeans.jellytools.properties.editors.StringArrayCustomEditorOperator;

/** Operator serving property of type String[]
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class StringArrayProperty extends Property {

    /** Creates a new instance of StringArrayProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public StringArrayProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return StringArrayCustomEditorOperator */    
    public StringArrayCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new StringArrayCustomEditorOperator(getName());
    }
    
    /** setter for StringArray value through Custom Editor
     * @param value String[] array of strings */    
    public void setStringArrayValue(String[] value) {
        StringArrayCustomEditorOperator customizer=invokeCustomizer();
        customizer.setStringArrayValue(value);
        customizer.ok();
    }        
    
    /** getter for StringArray value through Custom Editor
     * @return String[] array of strings */    
    public String[] getStringArrayValue() {
        String[] value;
        StringArrayCustomEditorOperator customizer=invokeCustomizer();
        value=customizer.getStringArrayValue();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
