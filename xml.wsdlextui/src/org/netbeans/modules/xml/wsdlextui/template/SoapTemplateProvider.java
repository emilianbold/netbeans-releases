/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.wsdlextui.template;

import java.io.InputStream;
import java.util.List;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementTemplateProvider;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ValidationInfo;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.openide.util.NbBundle;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementTemplateProvider.class)
public class SoapTemplateProvider extends ExtensibilityElementTemplateProvider {
    
    static final String soapTemplateUrl = "/org/netbeans/modules/xml/wsdlextui/template/template.xml";

    
    public InputStream getTemplateInputStream() {
        return SoapTemplateProvider.class.getResourceAsStream(soapTemplateUrl);
    }

    
    public String getLocalizedMessage(String str, Object[] objects) {
        return NbBundle.getMessage(SoapTemplateProvider.class, str, objects);
    }
    
    /**
     * Do any post processing on Binding and its child elements based on
     * the information available in corresponding PortType. This will be called
     * when binding is about to be added to definition. Note this binding is not yet added to definition.
     * @param wsdlTargetNamespace targetNamespace of wsdl where this binding will be added.
     * @param binding Binding for portType
     */
    public void postProcess(String  wsdlTargetNamespace, Binding binding) {
        SoapBindingPostProcessor processor = new SoapBindingPostProcessor();
        processor.postProcess(wsdlTargetNamespace, binding);
    }
    
    public void postProcess(String wsdlTargetNamespace, Port port) {
        SoapBindingPostProcessor processor = new SoapBindingPostProcessor();
        processor.postProcess(wsdlTargetNamespace, port);
    }
    
    /**
     * validate Binding and its child elements based on
     * the information available in corresponding PortType. This will be called
     * when binding user goes from portType configuration wizard to binding
     * configuration or when user changes subtype of binding in binding configuration.
     * Note this binding is not yet added to definition.
     * @param binding Binding for portType
     */
    public List<ValidationInfo> validate(Binding binding) {
        SoapBindingValidator validator = new SoapBindingValidator();
        return validator.validate(binding);
    }

    public List<ValidationInfo> validate(Port port) {
        return null;
    }
    

    
    

}
