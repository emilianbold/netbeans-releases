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

import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Nam Nguyen
 * @author rico
 */
public class WSDLElementFactoryProvider {
   
    public static class DefinitionsFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.DEFINITIONS.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            throw new UnsupportedOperationException("Root 'definitions' should be bootstrapped when WSDL model is created"); //NOI18N
        }
    }
    
    public static class BindingFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.BINDING.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new BindingImpl(context.getModel(), el);
        }
    }
    
    public static class DocumentationFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.DOCUMENTATION.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new DocumentationImpl(context.getModel(), el);
        }
    }
    
    public static class FaultFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.FAULT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            if (context instanceof BindingOperation) {
                return new BindingFaultImpl(context.getModel(), el);
            } else if (context instanceof Operation) {
                return new FaultImpl(context.getModel(), el);
            } else {
                throw new IllegalArgumentException("Wrong parent for 'fault'"); //NOI18N
            }
        }
    }
    
    public static class OperationFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.OPERATION.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), getImpliedQName(el));
            if (context instanceof Binding) {
                return new BindingOperationImpl(context.getModel(), el);
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
                ret = new NotificationOperationImpl(context.getModel(), el);
            } else if (in > 0 && out == 0) {
                ret = new OneWayOperationImpl(context.getModel(), el);
            } else if (in > out) {
                ret = new SolicitResponseOperationImpl(context.getModel(), el);
            } else if (in < out) {
                ret = new RequestResponseOperationImpl(context.getModel(), el);
            } 
            return ret;
        }
    }

    public static class InputFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.INPUT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            if (context instanceof BindingOperation) {
                return new BindingInputImpl(context.getModel(), el);
            } else if (context instanceof Operation) {
                return new InputImpl(context.getModel(), el);
            } else {
                throw new IllegalArgumentException("Wrong parent for 'input'"); //NOI18N
            }
        }
    }
    
    public static class ImportFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.IMPORT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new ImportImpl(context.getModel(), el);
        }
    }
    
    public static class MessageFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.MESSAGE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new MessageImpl(context.getModel(), el);
        }
    }
    
    public static class OutputFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.OUTPUT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            if (context instanceof BindingOperation) {
                return new BindingOutputImpl(context.getModel(), el);
            } else if (context instanceof Operation) {
                return new OutputImpl(context.getModel(), el);
            } else {
                throw new IllegalArgumentException("Wrong parent for 'output'"); //NOI18N
            }
        }
    }
    
    public static class PartFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.PART.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new PartImpl(context.getModel(), el);
        }
    }
    
    public static class PortFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.PORT.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new PortImpl(context.getModel(), el);
        }
    }
    
    public static class PortTypeFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.PORTTYPE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new PortTypeImpl(context.getModel(), el);
        }
    }
    
    public static class ServiceFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.SERVICE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new ServiceImpl(context.getModel(), el);
        }
    }
    
    public static class TypesFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(WSDLQNames.TYPES.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WSDLComponentBase) context));
            return new TypesImpl(context.getModel(), el);
        }
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
}
