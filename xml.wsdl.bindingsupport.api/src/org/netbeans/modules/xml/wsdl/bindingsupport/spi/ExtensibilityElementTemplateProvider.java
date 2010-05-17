/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

    /**
     * Implementation of this is required if there are wsdl template files defined in the template.xml
     * filePath parameter will provide the relative path specified in the wsdlTemplate file attribute.
     * Not abstract because, some implementation may not use this.
     * 
     * @param filePath relative path of the file
     * @return InputStream of the file
     */
     public InputStream getTemplateFileInputStream(String filePath) {
         return null;
     }
    
     /**
      * Implementation of this is required for customizing the wsdl wizard. If you do not want to add custom panels,
      * do not implement it the default wsdl iterator will be used.
      * 
      * 
      * @param context the WSDLWizardContext object
      * @return the iterator.
      */
     public WSDLWizardExtensionIterator getWSDLWizardExtensionIterator(WSDLWizardContext context) {
         return null;
     }
     
    
}
