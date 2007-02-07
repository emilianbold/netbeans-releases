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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service;

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.TimestampImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.CallbackHandlerConfigurationImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.CallbackHandlerImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.KeyStoreImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.TrustStoreImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ValidatorConfigurationImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ValidatorImpl;

public class ProprietarySecurityPolicyFactories {

    public static class KeyStoreServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.KEYSTORE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new KeyStoreImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new KeyStoreImpl(context.getModel(), element);
        }
    }

    public static class ValidatorConfigurationServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.VALIDATORCONFIGURATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new ValidatorConfigurationImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ValidatorConfigurationImpl(context.getModel(), element);
        }
    }

    public static class ValidatorServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.VALIDATOR.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new ValidatorImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ValidatorImpl(context.getModel(), element);
        }
    }

    public static class TimestampServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.TIMESTAMP.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new TimestampImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TimestampImpl(context.getModel(), element);
        }
    }
    
    public static class TrustStoreServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.TRUSTSTORE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new TrustStoreImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TrustStoreImpl(context.getModel(), element);
        }
    }

    public static class CallbackHandlerServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.CALLBACKHANDLER.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new CallbackHandlerImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new CallbackHandlerImpl(context.getModel(), element);
        }
    }

    public static class CallbackHandlerConfigurationServiceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietarySecurityPolicyServiceQName.CALLBACKHANDLERCONFIGURATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new CallbackHandlerConfigurationImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new CallbackHandlerConfigurationImpl(context.getModel(), element);
        }
    }

}
