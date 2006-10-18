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

package org.netbeans.modules.websvc.wsitmodelext.security.algosuite;

import org.netbeans.modules.websvc.wsitmodelext.security.algosuite.impl.*;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;

public class AlgorithmSuiteFactories {

    public static class AlgorithmSuiteFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.ALGORITHMSUITE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new AlgorithmSuiteImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AlgorithmSuiteImpl(context.getModel(), element);
        }
    }

    public static class Basic128Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC128.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic128Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic128Impl(context.getModel(), element);
        }
    }

    public static class Basic192Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC192.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic192Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic192Impl(context.getModel(), element);
        }
    }

    public static class Basic256Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC256.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic256Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic256Impl(context.getModel(), element);
        }
    }

    public static class TripleDesFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.TRIPLEDES.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new TripleDesImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TripleDesImpl(context.getModel(), element);
        }
    }

    /* rsa15 */ 
    public static class Basic128Rsa15Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC128RSA15.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic128Rsa15Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic128Rsa15Impl(context.getModel(), element);
        }
    }

    public static class Basic192Rsa15Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC192RSA15.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic192Rsa15Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic192Rsa15Impl(context.getModel(), element);
        }
    }

    public static class Basic256Rsa15Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC256RSA15.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic256Rsa15Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic256Rsa15Impl(context.getModel(), element);
        }
    }

    public static class TripleDesRsa15Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.TRIPLEDESRSA15.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new TripleDesRsa15Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TripleDesRsa15Impl(context.getModel(), element);
        }
    }

    /* sha256 */ 
    public static class Basic128Sha256Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC128SHA256.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic128Sha256Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic128Sha256Impl(context.getModel(), element);
        }
    }

    public static class Basic192Sha256Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC192SHA256.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic192Sha256Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic192Sha256Impl(context.getModel(), element);
        }
    }

    public static class Basic256Sha256Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC256SHA256.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic256Sha256Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic256Sha256Impl(context.getModel(), element);
        }
    }

    public static class TripleDesSha256Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.TRIPLEDESSHA256.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new TripleDesSha256Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TripleDesSha256Impl(context.getModel(), element);
        }
    }

    /* sha256rsa15 */ 
    public static class Basic128Sha256Rsa15Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC128SHA256RSA15.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic128Sha256Rsa15Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic128Sha256Rsa15Impl(context.getModel(), element);
        }
    }

    public static class Basic192Sha256Rsa15Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC192SHA256RSA15.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic192Sha256Rsa15Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic192Sha256Rsa15Impl(context.getModel(), element);
        }
    }

    public static class Basic256Sha256Rsa15Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.BASIC256SHA256RSA15.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Basic256Sha256Rsa15Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Basic256Sha256Rsa15Impl(context.getModel(), element);
        }
    }

    public static class TripleDesSha256Rsa15Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(AlgorithmSuiteQName.TRIPLEDESSHA256RSA15.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new TripleDesSha256Rsa15Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TripleDesSha256Rsa15Impl(context.getModel(), element);
        }
    }    
}
