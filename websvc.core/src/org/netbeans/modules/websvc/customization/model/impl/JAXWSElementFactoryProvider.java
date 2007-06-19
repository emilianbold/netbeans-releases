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
 * JAXWSElementFactoryProvider.java
 *
 * Created on February 22, 2006, 9:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

import org.netbeans.modules.websvc.customization.model.JAXWSQName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.customization.model.BindingCustomization;
import org.netbeans.modules.websvc.customization.model.BindingOperationCustomization;
import org.netbeans.modules.websvc.customization.model.DefinitionsCustomization;
import org.netbeans.modules.websvc.customization.model.EnableAsyncMapping;
import org.netbeans.modules.websvc.customization.model.EnableMIMEContent;
import org.netbeans.modules.websvc.customization.model.EnableWrapperStyle;
import org.netbeans.modules.websvc.customization.model.JavaClass;
import org.netbeans.modules.websvc.customization.model.JavaDoc;
import org.netbeans.modules.websvc.customization.model.JavaException;
import org.netbeans.modules.websvc.customization.model.JavaMethod;
import org.netbeans.modules.websvc.customization.model.JavaPackage;
import org.netbeans.modules.websvc.customization.model.JavaParameter;
import org.netbeans.modules.websvc.customization.model.PortCustomization;
import org.netbeans.modules.websvc.customization.model.PortTypeCustomization;
import org.netbeans.modules.websvc.customization.model.PortTypeOperationCustomization;
import org.netbeans.modules.websvc.customization.model.PortTypeOperationFaultCustomization;
import org.netbeans.modules.websvc.customization.model.Provider;
import org.netbeans.modules.websvc.customization.model.ServiceCustomization;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class JAXWSElementFactoryProvider {
    
    private Collection<ElementFactory> factories;
    /** Creates a new instance of JAXWSElementFactoryProvider */
    public JAXWSElementFactoryProvider() {
    }
    
    public Set<QName> getElementQNames() {
        return JAXWSQName.getQNames();
    }
    
    public Collection<ElementFactory> getElementFactories() {
        if(factories == null){
            factories = new ArrayList<ElementFactory>();
            factories.add(new BindingsFactory());
            factories.add(new EnableAsyncMappingFactory());
            factories.add(new EnableWrapperStyleFactory());
            factories.add(new JavaClassFactory());
            factories.add(new JavaDocFactory());
            factories.add(new EnableMIMEContentFactory());
            factories.add(new JavaExceptionFactory());
            factories.add(new JavaMethodFactory());
            factories.add(new JavaPackageFactory());
            factories.add(new JavaParameterFactory());
            factories.add(new ProviderFactory());
        }
        return factories;
    }
    
    public static class BindingsFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JAXWSQName.BINDINGS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            WSDLModel model = context.getModel();
            if(context instanceof Definitions){
                checkType(DefinitionsCustomization.class, type);
                return type.cast(new DefinitionsCustomizationImpl(model));
            } else if(context instanceof PortType){
                checkType(PortTypeCustomization.class, type);
                return type.cast(new PortTypeCustomizationImpl(model));
            } else if(context instanceof Operation){
                checkType(PortTypeOperationCustomization.class, type);
                return type.cast(new PortTypeOperationCustomizationImpl(model));
            } else if (context instanceof BindingOperation){
                checkType(BindingOperationCustomization.class, type);
                return type.cast(new BindingOperationCustomizationImpl(model));
            } else if (context instanceof Fault){
                checkType(PortTypeOperationFaultCustomization.class, type);
                return type.cast(new PortTypeOperationFaultCustomizationImpl(model));
            } else if(context instanceof Binding){
                checkType(BindingCustomization.class, type);
                return type.cast(new BindingCustomizationImpl(model));
            } else if (context instanceof Service){
                checkType(ServiceCustomization.class, type);
                return type.cast(new ServiceCustomizationImpl(model));
            } else if (context instanceof Port){
                checkType(PortCustomization.class, type);
                return type.cast(new PortCustomizationImpl(model));
            }
            return null;
        }
        
        public WSDLComponent create(WSDLComponent context, Element element) {
            WSDLModel model = context.getModel();
            if(context instanceof Definitions){
                return new DefinitionsCustomizationImpl(model, element);
            } else if(context instanceof PortType){
                return new PortTypeCustomizationImpl(model, element);
            } else if(context instanceof Operation){
                return new PortTypeOperationCustomizationImpl(model, element);
            } else if (context instanceof BindingOperation){
                return new BindingOperationCustomizationImpl(model, element);
            } else if (context instanceof Fault){
                return new PortTypeOperationFaultCustomizationImpl(model, element);
            } else if(context instanceof Binding){
                return new BindingCustomizationImpl(model, element);
            } else if (context instanceof Service){
                return new ServiceCustomizationImpl(model, element);
            } else if (context instanceof Port){
                return new PortCustomizationImpl(model, element);
            }
            return null;
        }
    }
    
    public static class JavaExceptionFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JAXWSQName.JAVAEXCEPTION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            checkType(JavaException.class, type);
            return type.cast(new JavaExceptionImpl(context.getModel()));
            
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JavaExceptionImpl(context.getModel(), element);
        }
    }
    public static class EnableAsyncMappingFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JAXWSQName.ENABLEASYNCMAPPING.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            checkType(EnableAsyncMapping.class, type);
            return type.cast(new EnableAsyncMappingImpl(context.getModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EnableAsyncMappingImpl(context.getModel(), element);
        }
    }
    
    public static class EnableWrapperStyleFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JAXWSQName.ENABLEWRAPPERSTYLE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            checkType(EnableWrapperStyle.class, type);
            return type.cast(new EnableWrapperStyleImpl(context.getModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EnableWrapperStyleImpl(context.getModel(), element);
        }
    }
    
    public static class EnableMIMEContentFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JAXWSQName.ENABLEMIMECONTENT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            checkType(EnableMIMEContent.class, type);
            return type.cast(new EnableMIMEContentImpl(context.getModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EnableMIMEContentImpl(context.getModel(), element);
        }
    }
    
    public static class JavaClassFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JAXWSQName.CLASS.getQName());
        }
        public <C extends WSDLComponent> C  create(WSDLComponent context, Class<C> type) {
            checkType(JavaClass.class, type);
            return type.cast(new JavaClassImpl(context.getModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JavaClassImpl(context.getModel(), element);
        }
    }
    
    public static class JavaDocFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JAXWSQName.JAVADOC.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            checkType(JavaDoc.class, type);
            return type.cast(new JavaDocImpl(context.getModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JavaDocImpl(context.getModel(), element);
        }
    }
    
    public static class JavaMethodFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JAXWSQName.METHOD.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            checkType(JavaMethod.class, type);
            return type.cast(new JavaMethodImpl(context.getModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JavaMethodImpl(context.getModel(), element);
        }
    }
    public static class JavaPackageFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JAXWSQName.PACKAGE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            checkType(JavaPackage.class, type);
            return type.cast(new JavaPackageImpl(context.getModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JavaPackageImpl(context.getModel(), element);
        }
    }
    
    public static class JavaParameterFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JAXWSQName.PARAMETER.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            checkType(JavaParameter.class, type);
            return type.cast(new JavaParameterImpl(context.getModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JavaParameterImpl(context.getModel(), element);
        }
    }
    
    public static class ProviderFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JAXWSQName.PROVIDER.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            checkType(Provider.class, type);
            return type.cast(new ProviderImpl(context.getModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ProviderImpl(context.getModel(), element);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static void checkType(Class type1, Class type2) {
        if (! type1.isAssignableFrom(type2)) {
            throw new IllegalArgumentException("Invalid requested component type");
        }
    }
}
