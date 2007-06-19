
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
/*
 * CustomizationComponentFactory.java
 *
 * Created on March 24, 2006, 11:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model;

import org.netbeans.modules.websvc.customization.model.impl.BindingCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.BindingOperationCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.DefinitionsCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.EnableAsyncMappingImpl;
import org.netbeans.modules.websvc.customization.model.impl.EnableMIMEContentImpl;
import org.netbeans.modules.websvc.customization.model.impl.EnableWrapperStyleImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaClassImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaDocImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaExceptionImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaMethodImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaPackageImpl;
import org.netbeans.modules.websvc.customization.model.impl.JavaParameterImpl;
import org.netbeans.modules.websvc.customization.model.impl.PortCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.PortTypeCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.PortTypeOperationCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.PortTypeOperationFaultCustomizationImpl;
import org.netbeans.modules.websvc.customization.model.impl.ProviderImpl;
import org.netbeans.modules.websvc.customization.model.impl.ServiceCustomizationImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author rico
 */
public class CustomizationComponentFactory {
    
    private static CustomizationComponentFactory factory =
            new CustomizationComponentFactory();
    /** Creates a new instance of CustomizationComponentFactory */
    private CustomizationComponentFactory() {
    }
    
    public static CustomizationComponentFactory getDefault(){
        return factory;
    }
    
    public BindingCustomization createBindingCustomization(WSDLModel model){
        return new BindingCustomizationImpl(model);
    }
    
    public BindingOperationCustomization createBindingOperationCustomization(WSDLModel model){
        return new BindingOperationCustomizationImpl(model);
    }
    
    public DefinitionsCustomization createDefinitionsCustomization(WSDLModel model){
        return new DefinitionsCustomizationImpl(model);
    }
    
    public EnableAsyncMapping createEnableAsyncMapping(WSDLModel model){
        return new EnableAsyncMappingImpl(model);
    }
    
    public EnableMIMEContent createEnableMIMEContent(WSDLModel model){
        return new EnableMIMEContentImpl(model);
    }
    
    public EnableWrapperStyle createEnableWrapperStyle(WSDLModel model){
        return new EnableWrapperStyleImpl(model);
    }
    
    public JavaClass createJavaClass(WSDLModel model){
        return new JavaClassImpl(model);
    }
    
    public JavaDoc createJavaDoc(WSDLModel model){
        return new JavaDocImpl(model);
    }
    
    public JavaException createJavaException(WSDLModel model){
        return new JavaExceptionImpl(model);
    }
    
    public JavaMethod createJavaMethod(WSDLModel model){
        return new JavaMethodImpl(model);
    }
    
    public JavaPackage createJavaPackage(WSDLModel model){
        return new JavaPackageImpl(model);
    }
    
    public JavaParameter createJavaParameter(WSDLModel model){
        return new JavaParameterImpl(model);
    }
    
    public PortCustomization createPortCustomization(WSDLModel model){
        return new PortCustomizationImpl(model);
    }
    
    public PortTypeCustomization createPortTypeCustomization(WSDLModel model){
        return new PortTypeCustomizationImpl(model);
    }
    
    public PortTypeOperationCustomization createPortTypeOperationCustomization(WSDLModel model){
        return new PortTypeOperationCustomizationImpl(model);
    }
    
    public PortTypeOperationFaultCustomization createPortTypeOperationFaultCustomization(WSDLModel model){
        return new PortTypeOperationFaultCustomizationImpl(model);
    }
    
    public Provider createProvider(WSDLModel model){
        return new ProviderImpl(model);
    }
    
    public ServiceCustomization createServiceCustomization(WSDLModel model){
        return new ServiceCustomizationImpl(model);
    }
}
