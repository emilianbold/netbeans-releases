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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.spi;


import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Port;

public abstract class ExtensibilityElementTemplateProvider {

    
    /**
     * Get the template input stream.
     * 
     * Create a input stream to the template xml document.
     * Make sure that the xml is compliant to the templates.xsd
     * defined in org.netbeans.modules.xml.wsdl.bindingsupport.template.xsd.templates.xsd
     * 
     * @return the xml document inputstream
     */
    public  abstract InputStream getTemplateInputStream();
    
    
    
    /**
     * Get localized text for keys defined in the template xml document.
     * 
     * @param key 
     * @param objects 
     * @return the localized message
     */
    public  abstract String getLocalizedMessage(String key, Object[] objects);
    
   
    /**
     * Do any post processing on Binding and its child elements based on
     * the information available in corresponding PortType. This will be called
     * when binding is about to be added to definition. Note this binding is not yet added to definition.
     * @param wsdlTargetNamespace targetNamespace of wsdl where this binding will be added.
     * @param binding Binding for portType
     */
    public void postProcess(String  wsdlTargetNamespace, Binding binding) {
        
    }
    
    /**
     * Do any post processing on Service Port and its child elements based on
     * the information available in corresponding Binding. This will be called
     * when binding is about to be added to definition. Note this port is not yet added to Service.
     * @param wsdlTargetNamespace targetNamespace of wsdl where this binding will be added.
     * @param port port 
     */
    public void postProcess(String  wsdlTargetNamespace, Port port) {
        
    }
    
    
    /**
     * validate Binding and its child elements based on
     * the information available in corresponding PortType. This will be called
     * when binding user goes from portType configuration wizard to binding
     * configuration or when user changes subtype of binding in binding configuration.
     * Note this binding is not yet added to definition.
     * @param binding Binding for portType
     * @return list of ValidationInfo
     */
    public List<ValidationInfo> validate(Binding binding) {
        return Collections.EMPTY_LIST;
    }
    
    
    /**
     * validate Binding and its child elements based on
     * the information available in corresponding PortType. This will be called
     * when binding user goes from portType configuration wizard to binding
     * configuration or when user changes subtype of binding in binding configuration.
     * Note this port is not yet added to Service.
     * @param port Port for a binding
     * @return list of ValidationInfo
     */
    public List<ValidationInfo> validate(Port port) {
        return Collections.EMPTY_LIST;
    }
    
    
    
}
