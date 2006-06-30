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
import org.netbeans.jellytools.properties.editors.ProcessDescriptorCustomEditorOperator;

/** Operator serving property of type ProcessDescriptor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ProcessDescriptorProperty extends Property {

    /** Creates a new instance of ProcessDescriptorProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public ProcessDescriptorProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return ProcessDescriptorCustomEditorOperator */    
    public ProcessDescriptorCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new ProcessDescriptorCustomEditorOperator(getName());
    }
    
    /** setter for ProcessDescriptor value through Custom Editor
     * @param process String process command line
     * @param arguments String process arguments */    
    public void setProcessDescriptorValue(String process, String arguments) {
        ProcessDescriptorCustomEditorOperator customizer=invokeCustomizer();
        customizer.setProcess(process);
        customizer.setArguments(arguments);
        customizer.ok();
    }        
    
    /** getter for ProcessDescriptor value through Custom Editor
     * @return String[3] process command line, process arguments, arguments keys */    
    public String[] getProcessDescriptorValue() {
        String[] value=new String[3];
        ProcessDescriptorCustomEditorOperator customizer=invokeCustomizer();
        value[0]=customizer.getProcess();
        value[1]=customizer.getArguments();
        value[2]=customizer.getArgumentKey();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
