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

package org.netbeans.modules.websvc.wsitmodelext.security;

import org.netbeans.modules.websvc.wsitmodelext.security.impl.*;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.impl.*;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;

public class SecurityPolicyFactories {

    public static class Wss11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.WSS11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Wss11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Wss11Impl(context.getModel(), element);
        }
    }

    public static class Wss10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.WSS10.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Wss10Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Wss10Impl(context.getModel(), element);
        }
    }

    public static class MustSupportRefEmbeddedTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.MUSTSUPPORTREFEMBEDDEDTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MustSupportRefEmbeddedTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefEmbeddedTokenImpl(context.getModel(), element);
        }
    }

    public static class MustSupportRefEncryptedKeyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.MUSTSUPPORTREFENCRYPTEDKEY.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MustSupportRefEncryptedKeyImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefEncryptedKeyImpl(context.getModel(), element);
        }
    }

    public static class MustSupportRefExternalURIFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.MUSTSUPPORTREFEXTERNALURI.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MustSupportRefExternalURIImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefExternalURIImpl(context.getModel(), element);
        }
    }

    public static class MustSupportRefIssuerSerialFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.MUSTSUPPORTREFISSUERSERIAL.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MustSupportRefIssuerSerialImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefIssuerSerialImpl(context.getModel(), element);
        }
    }

    public static class MustSupportRefKeyIdentifierFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.MUSTSUPPORTREFKEYIDENTIFIER.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MustSupportRefKeyIdentifierImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefKeyIdentifierImpl(context.getModel(), element);
        }
    }

    public static class MustSupportRefThumbprintFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.MUSTSUPPORTREFTHUMBPRINT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MustSupportRefThumbprintImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportRefThumbprintImpl(context.getModel(), element);
        }
    }

    public static class RequireSignatureConfirmationFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.REQUIRESIGNATURECONFIRMATION.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequireSignatureConfirmationImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireSignatureConfirmationImpl(context.getModel(), element);
        }
    }

    public static class RequestSecurityTokenTemplateFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequestSecurityTokenTemplateImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequestSecurityTokenTemplateImpl(context.getModel(), element);
        }
    }

    public static class Trust10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.TRUST10.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new Trust10Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new Trust10Impl(context.getModel(), element);
        }
    }

    public static class RequireServerEntropyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.REQUIRESERVERENTROPY.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequireServerEntropyImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireServerEntropyImpl(context.getModel(), element);
        }
    }

    public static class RequireClientEntropyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.REQUIRECLIENTENTROPY.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequireClientEntropyImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireClientEntropyImpl(context.getModel(), element);
        }
    }
 
    public static class MustSupportIssuedTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.MUSTSUPPORTISSUEDTOKENS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MustSupportIssuedTokensImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportIssuedTokensImpl(context.getModel(), element);
        }
    }

    public static class MustSupportClientChallengeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.MUSTSUPPORTCLIENTCHALLENGE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MustSupportClientChallengeImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportClientChallengeImpl(context.getModel(), element);
        }
    }

    public static class MustSupportServerChallengeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.MUSTSUPPORTSERVERCHALLENGE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new MustSupportServerChallengeImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MustSupportServerChallengeImpl(context.getModel(), element);
        }
    }
    
    public static class IncludeTimestampFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.INCLUDETIMESTAMP.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new IncludeTimestampImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new IncludeTimestampImpl(context.getModel(), element);
        }
    }

    public static class OnlySignEntireHeadersAndBodyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.ONLYSIGNENTIREHEADERSANDBODY.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new OnlySignEntireHeadersAndBodyImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new OnlySignEntireHeadersAndBodyImpl(context.getModel(), element);
        }
    }
    
    public static class EncryptSignatureFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.ENCRYPTSIGNATURE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new EncryptSignatureImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EncryptSignatureImpl(context.getModel(), element);
        }
    }

    public static class EncryptBeforeSigningFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.ENCRYPTBEFORESIGNING.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new EncryptBeforeSigningImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EncryptBeforeSigningImpl(context.getModel(), element);
        }
    }    
    
    public static class SignedElementsFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.SIGNEDELEMENTS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SignedElementsImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignedElementsImpl(context.getModel(), element);
        }
    }

    public static class SignedPartsFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.SIGNEDPARTS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SignedPartsImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignedPartsImpl(context.getModel(), element);
        }
    }

    public static class EncryptedElementsFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.ENCRYPTEDELEMENTS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new EncryptedElementsImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EncryptedElementsImpl(context.getModel(), element);
        }
    }

    public static class EncryptedPartsFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.ENCRYPTEDPARTS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new EncryptedPartsImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EncryptedPartsImpl(context.getModel(), element);
        }
    }

    public static class HeaderFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.HEADER.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new HeaderImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HeaderImpl(context.getModel(), element);
        }
    }

    public static class BodyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.BODY.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new BodyImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new BodyImpl(context.getModel(), element);
        }
    }

    public static class XPathFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.XPATH.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new XPathImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new XPathImpl(context.getModel(), element);
        }
    }
    
   public static class TransportBindingFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.TRANSPORTBINDING.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new TransportBindingImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TransportBindingImpl(context.getModel(), element);
        }
    }

   public static class SymmetricBindingFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.SYMMETRICBINDING.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SymmetricBindingImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SymmetricBindingImpl(context.getModel(), element);
        }
    }    

   public static class AsymmetricBindingFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.ASYMMETRICBINDING.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new AsymmetricBindingImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new AsymmetricBindingImpl(context.getModel(), element);
        }
    }

   public static class LayoutFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.LAYOUT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new LayoutImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LayoutImpl(context.getModel(), element);
        }
    }
   
   public static class StrictFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.STRICT.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new StrictImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new StrictImpl(context.getModel(), element);
        }
    }

   public static class LaxFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.LAX.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new LaxImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LaxImpl(context.getModel(), element);
        }
    }

   public static class LaxTsFirstFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.LAXTSFIRST.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new LaxTsFirstImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LaxTsFirstImpl(context.getModel(), element);
        }
    }

   public static class LaxTsLastFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.LAXTSLAST.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new LaxTsLastImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new LaxTsLastImpl(context.getModel(), element);
        }
    }

   public static class RequiredElementsFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.REQUIREDELEMENTS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequiredElementsImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequiredElementsImpl(context.getModel(), element);
        }
    }

   public static class BootstrapPolicyFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(SecurityPolicyQName.BOOTSTRAPPOLICY.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new BootstrapPolicyImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new BootstrapPolicyImpl(context.getModel(), element);
        }
    }
}
