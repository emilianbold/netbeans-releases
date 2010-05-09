/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensionAttrType;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensionElementType;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfiguratorFactory;

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
         ExtensibilityElementConfigurator configurator =
                ExtensibilityElementConfiguratorFactory.getDefault().getExtensibilityElementConfigurator(ee.getQName());
        if(attrs != null) {
            for(int i =0; i< attrs.length; i++) {
                ExtensionAttrType attr = attrs[i];
                String name = attr.getName();
                String defaultValue = attr.getDefaultValue();
                if (defaultValue == null && configurator != null) {
                    defaultValue = configurator.getDefaultValue(ee, ee.getQName(), name);
                }
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
