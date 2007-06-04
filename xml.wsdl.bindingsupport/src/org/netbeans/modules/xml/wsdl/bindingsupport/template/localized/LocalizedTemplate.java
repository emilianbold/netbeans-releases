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

/*
 * LocalizedTemplate.java
 *
 * Created on September 1, 2006, 12:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.template.localized;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementTemplateProvider;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType;

/**
 *
 * @author radval
 */
public class LocalizedTemplate {
    
    public static final String TEMPLATE = "TEMPLATE"; //NOI18N
    
    private LocalizedTemplateGroup mParent;
    
    private TemplateType mTemplateType;
    
    private ExtensibilityElementTemplateProvider mProvider;
    
    /** Creates a new instance of LocalizedTemplate */
    public LocalizedTemplate(LocalizedTemplateGroup parent, TemplateType template, ExtensibilityElementTemplateProvider provider) {
        this.mParent = parent;
        this.mTemplateType = template;
        this.mProvider = provider;
    }
    
    public LocalizedTemplateGroup getTemplateGroup() {
        return this.mParent;
    }
    
    public String getName() {
        String lName = null;
        try {
         String name = TEMPLATE + "_name_" + this.mTemplateType.getName(); //NOI18N
         lName = this.mProvider.getLocalizedMessage(name, null);
        } catch (Exception ex) {
            lName = this.mTemplateType.getName();
        }
        
        return lName;
    }
    
    public TemplateType getDelegate() {
        return this.mTemplateType;
    }

    public ExtensibilityElementTemplateProvider getMProvider() {
        return mProvider;
    }
    
    /**
     * get WSDLElementType 
     * @param wsdlElementName name attribute of <wsdlElement> 
     */
    public WsdlElementType getWSDLElementType(String wsdlElementName) {
        WsdlElementType wsdlElement = null;
        
        WsdlElementType[] wsdlElements = this.mTemplateType.getWsdlElement();
        if(wsdlElements != null) {
            for(int i = 0; i < wsdlElements.length; i++) {
                WsdlElementType w = wsdlElements[i];
                if(wsdlElementName.equals(w.getName())) {
                    wsdlElement = w;
                    break;
                }
            }
        }
        
        return wsdlElement;
    }
}
