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

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactoryProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Nam Nguyen
 * @author rico
 */
public class WSDLElementFactoryProvider implements ElementFactoryProvider{
    
    /** Creates a new instance of WSDLElementService */
    public WSDLElementFactoryProvider() {
    }
    
    private Collection<ElementFactory> factories;
    public Collection<ElementFactory> getElementFactories() {
        if (factories == null) {
            factories = new ArrayList<ElementFactory>();            
            factories.add(new BindingFactory());
            factories.add(new DocumentationFactory());
            factories.add(new FaultFactory());
            factories.add(new ImportFactory());
            factories.add(new InputFactory());
            factories.add(new MessageFactory());
            factories.add(new OperationFactory());
            factories.add(new OutputFactory());
            factories.add(new PartFactory());
            factories.add(new PortFactory());
            factories.add(new PortTypeFactory());
            factories.add(new ServiceFactory());
            factories.add(new TypesFactory());
        }
        return factories;
    }
    
    
    public Set<QName> getElementQNames() {
        return WSDLQNames.getQNames();
    }
    
    public static QName getQName(Element el) {
        return new QName(el.getNamespaceURI(), el.getLocalName());
    }
    
    private static QName getImpliedQName(Element el) {
        String ns = el.getNamespaceURI();
        if (ns == null) { // this can happen if new element has not added to xdm tree
            ns = WSDLQNames.WSDL_NS_URI;
        }
        return new QName(ns, el.getLocalName());
    }
    
    private static void checkArgument(Set<QName> wqnames, QName qname) {
        checkArgument(wqnames.iterator().next(), qname);
    }
    private static void checkArgument(QName wqname, QName qname) {
        if (! wqname.equals(qname)) {
            throw new IllegalArgumentException("Invalid element "+qname.getLocalPart()); //NOI18N
        }
    }
    
    private static class BindingFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.BINDING.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(Binding.class, type);
            return type.cast(new BindingImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            return new BindingImpl(context.getWSDLModel(), el);
        }
    }
    
    private static class DocumentationFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.DOCUMENTATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(Documentation.class, type);
            return type.cast(new DocumentationImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            return new DocumentationImpl(context.getWSDLModel(), el);
        }
    }
    
    private static class FaultFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.FAULT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            if (context instanceof BindingOperation) {
                Util.checkType(BindingFault.class, type);
                return type.cast(new BindingFaultImpl(context.getWSDLModel()));
            } else if (context instanceof Operation) {
                Util.checkType(Fault.class, type);
                return type.cast(new FaultImpl(context.getWSDLModel()));
            } else {
                throw new IllegalArgumentException("Wrong parent for 'fault'"); //NOI18N
            }
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            if (context instanceof BindingOperation) {
                return new BindingFaultImpl(context.getWSDLModel());
            } else if (context instanceof Operation) {
                return new FaultImpl(context.getWSDLModel(), el);
            } else {
                throw new IllegalArgumentException("Wrong parent for 'fault'"); //NOI18N
            }
        }
    }
    
    private static class OperationFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.OPERATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            if (context instanceof Binding) {
                Util.checkType(BindingOperation.class, type);
                return type.cast(new BindingOperationImpl(context.getWSDLModel()));
            } else if (context instanceof PortType) {
                if (OneWayOperation.class.isAssignableFrom(type)) {
                    return type.cast(new OneWayOperationImpl(context.getWSDLModel()));
                } else if (RequestResponseOperation.class.isAssignableFrom(type)) {
                    return type.cast(new RequestResponseOperationImpl(context.getWSDLModel()));
                } else if (SolicitResponseOperation.class.isAssignableFrom(type)) {
                    return type.cast(new SolicitResponseOperationImpl(context.getWSDLModel()));
                } else if (NotificationOperation.class.isAssignableFrom(type)) {
                    return type.cast(new NotificationOperationImpl(context.getWSDLModel()));
                } else {
                    throw new IllegalArgumentException("Invalid type of operation"); //NOI18N
                }
            } else {
                throw new IllegalArgumentException("Wrong parent for 'operation'"); //NOI18N
            }
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getImpliedQName(el));
            if (context instanceof Binding) {
                return new BindingOperationImpl(context.getWSDLModel(), el);
            } else if (! (context instanceof PortType)) {
                throw new IllegalArgumentException("Wrong parent for 'operation'"); //NOI18N
            }
            
            //portType/operation
            NodeList list = el.getChildNodes();
            int in = 0, out = 0;
            for (int i=0; i<list.getLength(); i++) {
                if (in > 0 && out > 0) {
                    break;
                }
                Node n = list.item(i);
                if (!(n instanceof Element)) {
                    continue;
                }
                if (n.getLocalName().equals(WSDLQNames.INPUT.getQName().getLocalPart())) {
                    in = out == 0 ? 1 : 2;
                } else if (n.getLocalName().equals(WSDLQNames.OUTPUT.getQName().getLocalPart())) {
                    out = in == 0 ? 1 : 2;
                }
            }
            
            WSDLComponent ret = null;
            if (in == 0 && out > 0) {
                ret = new NotificationOperationImpl(context.getWSDLModel(), el);
            } else if (in > 0 && out == 0) {
                ret = new OneWayOperationImpl(context.getWSDLModel(), el);
            } else if (in > out) {
                ret = new SolicitResponseOperationImpl(context.getWSDLModel(), el);
            } else if (in < out) {
                ret = new RequestResponseOperationImpl(context.getWSDLModel(), el);
            } //TODO should we support temporary empty Operation
            return ret;
        }
    }

    private static class InputFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.INPUT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            if (context instanceof BindingOperation) {
                Util.checkType(BindingInput.class, type);
                return type.cast(new BindingInputImpl(context.getWSDLModel()));
            } else if (context instanceof Operation) {
                Util.checkType(Input.class, type);
                return type.cast(new InputImpl(context.getWSDLModel()));
            } else {
                throw new IllegalArgumentException("Wrong parent for 'input'"); //NOI18N
            }
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            if (context instanceof BindingOperation) {
                return new BindingInputImpl(context.getWSDLModel(), el);
            } else if (context instanceof Operation) {
                return new InputImpl(context.getWSDLModel(), el);
            } else {
                throw new IllegalArgumentException("Wrong parent for 'input'"); //NOI18N
            }
        }
    }
    
    private static class ImportFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.IMPORT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(Import.class, type);
            return type.cast(new ImportImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            return new ImportImpl(context.getWSDLModel(), el);
        }
    }
    
    private static class MessageFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.MESSAGE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(Message.class, type);
            return type.cast(new MessageImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            return new MessageImpl(context.getWSDLModel(), el);
        }
    }
    
    private static class OutputFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.OUTPUT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            if (context instanceof BindingOperation) {
                Util.checkType(BindingOutput.class, type);
                return type.cast(new BindingOutputImpl(context.getWSDLModel()));
            } else if (context instanceof Operation) {
                Util.checkType(Output.class, type);
                return type.cast(new OutputImpl(context.getWSDLModel()));
            } else {
                throw new IllegalArgumentException("Wrong parent for 'output'"); //NOI18N
            }
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            if (context instanceof BindingOperation) {
                return new BindingOutputImpl(context.getWSDLModel(), el);
            } else if (context instanceof Operation) {
                return new OutputImpl(context.getWSDLModel(), el);
            } else {
                throw new IllegalArgumentException("Wrong parent for 'output'"); //NOI18N
            }
        }
    }
    
    private static class PartFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.PART.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(Part.class, type);
            return type.cast(new PartImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            return new PartImpl(context.getWSDLModel(), el);
        }
    }
    
    private static class PortFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.PORT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(Port.class, type);
            return type.cast(new PortImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            return new PortImpl(context.getWSDLModel(), el);
        }
    }
    
    private static class PortTypeFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.PORTTYPE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(PortType.class, type);
            return type.cast(new PortTypeImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            return new PortTypeImpl(context.getWSDLModel(), el);
        }
    }
    
    private static class ServiceFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.SERVICE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(Service.class, type);
            return type.cast(new ServiceImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            return new ServiceImpl(context.getWSDLModel(), el);
        }
    }
    
    private static class TypesFactory implements ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.TYPES.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            Util.checkType(Types.class, type);
            return type.cast(new TypesImpl(context.getWSDLModel()));
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getQName(el));
            return new TypesImpl(context.getWSDLModel(), el);
        }
    }
}
