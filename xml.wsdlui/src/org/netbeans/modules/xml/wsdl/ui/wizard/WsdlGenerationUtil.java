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
 * WsdlGenerationUtil.java
 *
 * Created on September 6, 2006, 5:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.ExtensionAttrType;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.ExtensionElementType;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.WsdlElementType;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.localized.LocalizedTemplate;

/**
 *
 * @author radval
 */
public class WsdlGenerationUtil {
    
    private WSDLModel mModel;
    
    /** Creates a new instance of WsdlGenerationUtil */
    public WsdlGenerationUtil(WSDLModel model) {
        this.mModel = model;
    }
    
    
    public ExtensibilityElement createAndAddExtensionElementAndAttribute(String wsdlElementNameInTemplate, LocalizedTemplate bindingSubType, WSDLComponent parentWSDLComponent) {
         ExtensibilityElement e = null;
         String namespace = bindingSubType.getTemplateGroup().getNamespace();
         String prefix =   bindingSubType.getTemplateGroup().getPrefix();
        
         WsdlElementType wsdlElement = bindingSubType.getWSDLElementType(wsdlElementNameInTemplate);
         
         if(wsdlElement != null) {
             ExtensionElementType[] ees = wsdlElement.getExtensionElement();
             if(ees != null) {
                 for(int i=0; i < ees.length; i++) {
                     ExtensionElementType ee = ees[i];
                     String name = ee.getName();
                     if(name != null) {
                        e = createExtensibilityElement(name, prefix, namespace, parentWSDLComponent);
                        if(e != null) {
                            parentWSDLComponent.addExtensibilityElement(e);
                            createAndAddExtensibilityElementAttributes(e, ee.getExtensionAttr());
                        }
                     }
                 }
             }
         }
         
         return e;
    }
    
     public void createAndAddExtensibilityElementAttributes(ExtensibilityElement ee, ExtensionAttrType[] attrs) {
        if(attrs != null) {
            for(int i =0; i< attrs.length; i++) {
                ExtensionAttrType attr = attrs[i];
                String name = attr.getName();
                String defaultValue = attr.getDefaultValue();
                if(name != null) {
                    ee.setAttribute(name, defaultValue);
                }
            }
        }
     }
    
     public ExtensibilityElement createExtensibilityElement(String elementName, 
            String prefix, 
            String targetNamespace,
            WSDLComponent parent) {
        QName qName = null;
        
        if(prefix != null) {
            qName = new QName(targetNamespace, elementName, prefix);
        } else {
            qName = new QName(targetNamespace, elementName);
        }
        
        //create extensibility element
        //set all its attribute as defined in schema element
        ExtensibilityElement exElement = (ExtensibilityElement) this.mModel.getFactory().create(parent, qName);
        
        return exElement;
        
    }
}
