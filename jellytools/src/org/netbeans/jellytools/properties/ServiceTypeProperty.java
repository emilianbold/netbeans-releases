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
import org.netbeans.jellytools.properties.editors.ServiceTypeCustomEditorOperator;

/** Operator serving property of type ServiceType
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ServiceTypeProperty extends Property {

    /** Creates a new instance of ServiceTypeProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public ServiceTypeProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return ServiceTypeCustomEditorOperator */    
    public ServiceTypeCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new ServiceTypeCustomEditorOperator(getName());
    }
    
    /** setter for ServiceType name value through Custom Editor
     * @param serviceName String service type name */    
    public void setServiceTypeValue(String serviceName) {
        ServiceTypeCustomEditorOperator customizer=invokeCustomizer();
        customizer.setServiceTypeValue(serviceName);
        customizer.ok();
    }        
    
    /** getter for ServiceType name value through Custom Editor
     * @return String service type name */    
    public String getServiceTypeValue() {
        String value;
        ServiceTypeCustomEditorOperator customizer=invokeCustomizer();
        value=customizer.getServiceTypeValue();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
