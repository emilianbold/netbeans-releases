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

/*
 * FileProperty.java
 *
 * Created on June 18, 2002, 11:53 AM
 */

import java.io.File;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.FileCustomEditorOperator;
import org.netbeans.jemmy.operators.ContainerOperator;

/** Operator serving property of type File
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class FileProperty extends Property {
    
    /** Creates a new instance of FileProperty
     * @param contOper ContainerOperator of parent container to search property in
     * @param name String property name 
     * @deprecated Use {@link #FileProperty(PropertySheetOperator, String)} instead
     */
    public FileProperty(ContainerOperator contOper, String name) {
        super(contOper, name);
    }
    
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
