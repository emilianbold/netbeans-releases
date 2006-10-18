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

package org.netbeans.modules.websvc.wsitmodelext.policy;

import org.netbeans.modules.websvc.wsitmodelext.policy.impl.AllImpl;
import org.netbeans.modules.websvc.wsitmodelext.policy.impl.ExactlyOneImpl;
import org.netbeans.modules.websvc.wsitmodelext.policy.impl.PolicyImpl;
import org.netbeans.modules.websvc.wsitmodelext.policy.impl.PolicyReferenceImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;


public class PolicyFactories {
            
    public static class PolicyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(PolicyQName.POLICY.getQName());
        }    
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new PolicyImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new PolicyImpl(context.getModel(), element);
        }
    }

    public static class AllFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(PolicyQName.ALL.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new AllImpl(context.getModel()));
            
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AllImpl(context.getModel(), element);
        }
    }

    public static class ExactlyOneFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(PolicyQName.EXACTLYONE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new ExactlyOneImpl(context.getModel()));
            
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ExactlyOneImpl(context.getModel(), element);
        }
    }

    public static class PolicyReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(PolicyQName.POLICYREFERENCE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new PolicyReferenceImpl(context.getModel()));
            
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new PolicyReferenceImpl(context.getModel(), element);
        }
    }

}
