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
 * ClasspathProperty.java
 *
 * Created on June 19, 2002, 3:18 PM
 */

import org.netbeans.jellytools.properties.editors.ClasspathCustomEditorOperator;
import org.netbeans.jemmy.operators.ContainerOperator;

/** Operator serving property of type NbClasspath
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ClasspathProperty extends Property {
    
    /** Creates a new instance of ClasspathProperty
     * @param contOper ContainerOperator of parent container to search property in
     * @param name String property name 
     * @deprecated Use {@link #ClasspathProperty(PropertySheetOperator, String)} instead
     */
    public ClasspathProperty(ContainerOperator contOper, String name) {
        super(contOper, name);
    }
    
    /** Creates a new instance of ClasspathProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name 
     */
    public ClasspathProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return ClassPathCustomEditorOperator */    
    public ClasspathCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new ClasspathCustomEditorOperator(getName());
    }
    
    /** getter for Classpath value through Custom Editor
     * @return String[] array of directory paths or JAR or ZIP file paths */    
    public String[] getClasspathValue() {
        String[] value;
        ClasspathCustomEditorOperator customizer=invokeCustomizer();
        value=customizer.getClasspathValue();
        customizer.close();
        return value;
    }
    
    /** setter for Classpath value through Custom Editor
     * @param classPathElements String[] array of directory paths or JAR or ZIP file paths */    
    public void setClasspathValue(String[] classPathElements) {
        ClasspathCustomEditorOperator customizer=invokeCustomizer();
        customizer.setClasspathValue(classPathElements);
        customizer.ok();
    }        

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }    
}
