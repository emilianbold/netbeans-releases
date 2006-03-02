/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.impl.Util;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactoryProvider;
import org.w3c.dom.Element;

/**
 * @authro Nam Nguyen
 * @author rico
 */
public class SOAPElementFactoryProvider implements ElementFactoryProvider{
    
    /** Creates a new instance of SOAPElementCreatorService */
    public SOAPElementFactoryProvider() {
    }

    private Collection<ElementFactory> factories;
    public Collection<ElementFactory> getElementFactories() {
        if (factories == null) {
            factories = new ArrayList<ElementFactory>();
            factories.add(new AddressFactory());
            factories.add(new BindingFactory());
            factories.add(new BodyFactory());
            factories.add(new FaultFactory());
            factories.add(new HeaderFaultFactory());
            factories.add(new OperationFactory());
        }
        return factories;
    }

    private static class BindingFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.BINDING.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(SOAPBinding.class, type);
            return type.cast(new SOAPBindingImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPBindingImpl(context.getWSDLModel(), element);
        }
    }

    private static class AddressFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.ADDRESS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(SOAPAddress.class, type);
            return type.cast(new SOAPAddressImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPAddressImpl(context.getWSDLModel(), element);
        }
    }

    private static class BodyFactory implements ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.BODY.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(SOAPBody.class, type);
            return type.cast(new SOAPBodyImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPBodyImpl(context.getWSDLModel(), element);
        }
    }

    private static class HeaderFactory implements ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.HEADER.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(SOAPHeader.class, type);
            return type.cast(new SOAPHeaderImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPHeaderImpl(context.getWSDLModel(), element);
        }
    }

    private static class HeaderFaultFactory implements ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.HEADER_FAULT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(SOAPHeaderFault.class, type);
            return type.cast(new SOAPHeaderFaultImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPHeaderFaultImpl(context.getWSDLModel(), element);
        }
    }

    private static class OperationFactory implements ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.OPERATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(SOAPOperation.class, type);
            return type.cast(new SOAPOperationImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPOperationImpl(context.getWSDLModel(), element);
        }
    }

    private static class FaultFactory implements ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(SOAPQName.FAULT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(SOAPFault.class, type);
            return type.cast(new SOAPFaultImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SOAPFaultImpl(context.getWSDLModel(), element);
        }
    }
}
