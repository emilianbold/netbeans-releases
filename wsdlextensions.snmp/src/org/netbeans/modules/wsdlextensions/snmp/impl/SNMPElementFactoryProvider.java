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

package org.netbeans.modules.wsdlextensions.snmp.impl;

import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.wsdlextensions.snmp.SNMPQName;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

public class SNMPElementFactoryProvider {
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class BindingFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SNMPQName.BINDING.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SNMPBindingImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class AddressFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SNMPQName.ADDRESS.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SNMPAddressImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class OperationFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SNMPQName.OPERATION.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SNMPOperationImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class MessageFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SNMPQName.MESSAGE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SNMPMessageImpl(context.getModel(), element);
        }
    }
    
}
