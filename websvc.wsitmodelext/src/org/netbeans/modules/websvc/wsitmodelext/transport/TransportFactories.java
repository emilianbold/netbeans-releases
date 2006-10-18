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

package org.netbeans.modules.websvc.wsitmodelext.transport;

import org.netbeans.modules.websvc.wsitmodelext.transport.impl.OptimizedTCPTransportImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.transport.impl.AutomaticallySelectFastInfosetImpl;
import org.netbeans.modules.websvc.wsitmodelext.transport.impl.AutomaticallySelectOptimalTransportImpl;
import org.netbeans.modules.websvc.wsitmodelext.transport.impl.OptimizedFastInfosetSerializationImpl;

public class TransportFactories {

    public static class OptimizedFastInfosetSerialization extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(FIQName.OPTIMIZEDFASTINFOSETSERIALIZATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new OptimizedFastInfosetSerializationImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new OptimizedFastInfosetSerializationImpl(context.getModel(), element);
        }
    }   

    public static class AutomaticallySelectFastInfoset extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(FIQName.AUTOMATICALLYSELECTFASTINFOSET.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new AutomaticallySelectFastInfosetImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AutomaticallySelectFastInfosetImpl(context.getModel(), element);
        }
    }

    public static class OptimizedTCPTransport extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TCPQName.OPTIMIZEDTCPTRANSPORT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new OptimizedTCPTransportImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new OptimizedTCPTransportImpl(context.getModel(), element);
        }
    }   

    public static class AutomaticallySelectOptimalTransport extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TCPQName.AUTOMATICALLYSELECTOPTIMALTRANSPORT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new AutomaticallySelectOptimalTransportImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AutomaticallySelectOptimalTransportImpl(context.getModel(), element);
        }
    }
    
}
