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

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

/**
 * @authro Nam Nguyen
 * @author rico
 */
public class SOAPElementFactoryProvider {
    
    public static class BindingFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.BINDING.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPBindingImpl(context.getModel(), element);
        }
    }

    public static class AddressFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.ADDRESS.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPAddressImpl(context.getModel(), element);
        }
    }

    public static class BodyFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.BODY.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPBodyImpl(context.getModel(), element);
        }
    }

    public static class HeaderFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.HEADER.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPHeaderImpl(context.getModel(), element);
        }
    }

    public static class HeaderFaultFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.HEADER_FAULT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPHeaderFaultImpl(context.getModel(), element);
        }
    }

    public static class OperationFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.OPERATION.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPOperationImpl(context.getModel(), element);
        }
    }

    public static class FaultFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.FAULT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPFaultImpl(context.getModel(), element);
        }
    }
}
