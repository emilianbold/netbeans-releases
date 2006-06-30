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
import org.netbeans.jellytools.properties.editors.StringCustomEditorOperator;

/** Operator serving property of type String
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class StringProperty extends Property {//TextFieldProperty {

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
