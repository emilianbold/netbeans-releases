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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitmodelext.addressing;

import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Address10Impl;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing10EndpointReferenceImpl;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing10MetadataImpl;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing10ReferencePropertiesImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing10AnonymousImpl;
import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.Addressing10WsdlUsingAddressingImpl;


public class Addressing10Factories {

    public static class EndpointReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(Addressing10QName.ENDPOINTREFERENCE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Addressing10EndpointReferenceImpl(context.getModel()));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Addressing10EndpointReferenceImpl(context.getModel(), element);
        }
    }

    public static class AnonymousFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(Addressing10QName.ANONYMOUS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Addressing10AnonymousImpl(context.getModel()));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Addressing10AnonymousImpl(context.getModel(), element);
        }
    }
    
    public static class Address10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(Addressing10QName.ADDRESS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Address10Impl(context.getModel()));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Address10Impl(context.getModel(), element);
        }
    }
    
    public static class Addressing10MetadataFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(Addressing10QName.ADDRESS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Addressing10MetadataImpl(context.getModel()));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Addressing10MetadataImpl(context.getModel(), element);
        }
    }
    
    public static class Addressing10ReferencePropertiesFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(Addressing10QName.REFERENCEPROPERTIES.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Addressing10ReferencePropertiesImpl(context.getModel()));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Addressing10ReferencePropertiesImpl(context.getModel(), element);
        }
    }    
}
