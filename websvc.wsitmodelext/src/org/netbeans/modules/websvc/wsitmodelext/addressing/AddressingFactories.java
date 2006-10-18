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

import org.netbeans.modules.websvc.wsitmodelext.addressing.impl.*;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;


public class AddressingFactories {

    public static class EndpointReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AddressingQName.ENDPOINTREFERENCE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new EndpointReferenceImpl(context.getModel()));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EndpointReferenceImpl(context.getModel(), element);
        }
    }

    public static class AddressFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AddressingQName.ADDRESS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new AddressImpl(context.getModel()));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AddressImpl(context.getModel(), element);
        }
    }

    public static class AddressingServiceNameFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AddressingQName.SERVICENAME.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new AddressingServiceNameImpl(context.getModel()));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AddressingServiceNameImpl(context.getModel(), element);
        }
    }

    public static class AddressingPortTypeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AddressingQName.PORTTYPE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new AddressingPortTypeImpl(context.getModel()));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AddressingPortTypeImpl(context.getModel(), element);
        }
    }

    public static class ReferenceParametersFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AddressingQName.REFERENCEPARAMETERS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new ReferenceParametersImpl(context.getModel()));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ReferenceParametersImpl(context.getModel(), element);
        }
    }

    public static class ReferencePropertiesFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AddressingQName.REFERENCEPROPERTIES.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new ReferencePropertiesImpl(context.getModel()));
        }

        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ReferencePropertiesImpl(context.getModel(), element);
        }
    }
    
}
