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

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.IssuerImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.KeyTypeImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.CertAliasImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ContractImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.LifeTimeSTSImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.STSConfigurationServiceImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ServiceProviderImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.ServiceProvidersImpl;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl.TokenTypeImpl;

public class ProprietaryTrustServiceFactories {

    public static class CertAliasFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.CERTALIAS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new CertAliasImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new CertAliasImpl(context.getModel(), element);
        }
    }

    public static class ContractFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.CONTRACT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new ContractImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ContractImpl(context.getModel(), element);
        }
    }

    public static class STSIssuerFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.ISSUER.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new IssuerImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new IssuerImpl(context.getModel(), element);
        }
    }
    
    public static class STSConfigurationFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.STSCONFIGURATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new STSConfigurationServiceImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new STSConfigurationServiceImpl(context.getModel(), element);
        }
    }

    public static class ServiceProviderFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.SERVICEPROVIDER.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new ServiceProviderImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ServiceProviderImpl(context.getModel(), element);
        }
    }

    public static class ServiceProvidersFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.SERVICEPROVIDERS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new ServiceProvidersImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ServiceProvidersImpl(context.getModel(), element);
        }
    }
    
    public static class TokenTypeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.TOKENTYPE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new TokenTypeImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TokenTypeImpl(context.getModel(), element);
        }
    }

    public static class KeyTypeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.KEYTYPE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new KeyTypeImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new KeyTypeImpl(context.getModel(), element);
        }
    }
    
    public static class LifeTimeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(ProprietaryTrustServiceQName.LIFETIME.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new LifeTimeSTSImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LifeTimeSTSImpl(context.getModel(), element);
        }
    }
}
