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

package org.netbeans.modules.wsdlextensions.jms.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.wsdlextensions.jms.JMSQName;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

/**
 * JMSElementFactoryProvider 
 */
public class JMSElementFactoryProvider {
    
    public static class BindingFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.BINDING.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSBindingImpl(context.getModel(), element);
        }
    }

    public static class AddressFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.ADDRESS.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSAddressImpl(context.getModel(), element);
        }
    }

    public static class OperationFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.OPERATION.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSOperationImpl(context.getModel(), element);
        }
    }

    public static class MessageFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.MESSAGE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSMessageImpl(context.getModel(), element);
        }
    }
    
    public static class OptionsFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.OPTIONS.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSOptionsImpl(context.getModel(), element);
        }
    }

    public static class OptionFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.OPTION.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSOptionImpl(context.getModel(), element);
        }
    }

    public static class JNDIEnvFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.JNDIENV.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSJNDIEnvImpl(context.getModel(), element);
        }
    }

    public static class JNDIEnvEntryFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.JNDIENVENTRY.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSJNDIEnvEntryImpl(context.getModel(), element);
        }
    }
    
    public static class MapMessageFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.MAPMESSAGE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSMapMessageImpl(context.getModel(), element);
        }
    }

    public static class MapMessagePartFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.MAPPART.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSMapMessagePartImpl(context.getModel(), element);
        }
    }

    public static class PropertiesFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.PROPERTIES.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSPropertiesImpl(context.getModel(), element);
        }
    }

    public static class PropertyFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(JMSQName.PROPERTY.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new JMSPropertyImpl(context.getModel(), element);
        }
    }
    
}
