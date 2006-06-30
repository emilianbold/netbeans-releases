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

import java.io.File;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.FileCustomEditorOperator;

/** Operator serving property of type File
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class FileProperty extends Property {

    /** Creates a new instance of FileProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public FileProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return FileCustomEditorOperator */    
    public FileCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new FileCustomEditorOperator(getName());
    }
    
    /** setter for File value through Custom Editor
     * @param filePath String file path */    
    public void setFileValue(String filePath) {
        FileCustomEditorOperator customizer=invokeCustomizer();
        customizer.setFileValue(filePath);
        customizer.ok();
    }        
    
    /** setter for File value through Custom Editor
     * @param value File */    
    public void setFileValue(File value) {
        FileCustomEditorOperator customizer=invokeCustomizer();
        customizer.setFileValue(value);
        customizer.ok();
    }        
    
    /** getter for File value through Custom Editor
     * @return File */    
    public File getFileValue() {
        File value;
        FileCustomEditorOperator customizer=invokeCustomizer();
        value=customizer.getFileValue();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
