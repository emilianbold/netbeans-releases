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

package org.netbeans.modules.websvc.wsitmodelext.security.tokens;

import org.netbeans.modules.websvc.wsitmodelext.security.tokens.impl.*;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;

public class TokenFactories {

    public static class SupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.SUPPORTINGTOKENS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SupportingTokensImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SupportingTokensImpl(context.getModel(), element);
        }
    }

    public static class SignedSupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.SIGNEDSUPPORTINGTOKENS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SignedSupportingTokensImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignedSupportingTokensImpl(context.getModel(), element);
        }
    }

    public static class EndorsingSupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.ENDORSINGSUPPORTINGTOKENS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new EndorsingSupportingTokensImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EndorsingSupportingTokensImpl(context.getModel(), element);
        }
    }

    public static class SignedEndorsingSupportingTokensFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.SIGNEDENDORSINGSUPPORTINGTOKENS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SignedEndorsingSupportingTokensImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignedEndorsingSupportingTokensImpl(context.getModel(), element);
        }
    }

    public static class HttpsTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.HTTPSTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new HttpsTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new HttpsTokenImpl(context.getModel(), element);
        }
    }

    public static class InitiatorTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.INITIATORTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new InitiatorTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new InitiatorTokenImpl(context.getModel(), element);
        }
    }

    public static class SignatureTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.SIGNATURETOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SignatureTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SignatureTokenImpl(context.getModel(), element);
        }
    }

    public static class EncryptionTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.ENCRYPTIONTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new EncryptionTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new EncryptionTokenImpl(context.getModel(), element);
        }
    }
    
    public static class IssuedTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.ISSUEDTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new IssuedTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new IssuedTokenImpl(context.getModel(), element);
        }
    }
    
    public static class KerberosTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.KERBEROSTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new KerberosTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new KerberosTokenImpl(context.getModel(), element);
        }
    }
    
    public static class ProtectionTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.PROTECTIONTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new ProtectionTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new ProtectionTokenImpl(context.getModel(), element);
        }
    }
    
    public static class TransportTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.TRANSPORTTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new TransportTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new TransportTokenImpl(context.getModel(), element);
        }
    }

    public static class RecipientTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.RECIPIENTTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RecipientTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RecipientTokenImpl(context.getModel(), element);
        }
    }
    
    public static class RelTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.RELTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RelTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RelTokenImpl(context.getModel(), element);
        }
    }
    
    public static class SamlTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.SAMLTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SamlTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SamlTokenImpl(context.getModel(), element);
        }
    }
    
    public static class SecureConversationTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.SECURECONVERSATIONTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SecureConversationTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SecureConversationTokenImpl(context.getModel(), element);
        }
    }
    
    public static class SecurityContextTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.SECURITYCONTEXTTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SecurityContextTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SecurityContextTokenImpl(context.getModel(), element);
        }
    }
    
    public static class SpnegoContextTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.SPNEGOCONTEXTTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SpnegoContextTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SpnegoContextTokenImpl(context.getModel(), element);
        }
    }

    public static class UsernameTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.USERNAMETOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new UsernameTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new UsernameTokenImpl(context.getModel(), element);
        }
    }
    
    public static class X509TokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.X509TOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new X509TokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new X509TokenImpl(context.getModel(), element);
        }
    }

    public static class WssUsernameToken10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSUSERNAMETOKEN10.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssUsernameToken10Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssUsernameToken10Impl(context.getModel(), element);
        }
    }

    public static class WssUsernameToken11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSUSERNAMETOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssUsernameToken11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssUsernameToken11Impl(context.getModel(), element);
        }
    }
    
    public static class IssuerFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.ISSUER.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new IssuerImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new IssuerImpl(context.getModel(), element);
        }
    }    

    public static class RequireDerivedKeysFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.REQUIREDERIVEDKEYS.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequireDerivedKeysImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireDerivedKeysImpl(context.getModel(), element);
        }
    }    

    public static class RequireExternalReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.REQUIREEXTERNALREFERENCE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequireExternalReferenceImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireExternalReferenceImpl(context.getModel(), element);
        }
    }    

    public static class RequireInternalReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.REQUIREINTERNALREFERENCE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequireInternalReferenceImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireInternalReferenceImpl(context.getModel(), element);
        }
    }

    public static class RequireKeyIdentifierReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.REQUIREKEYIDENTIFIERREFERENCE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequireKeyIdentifierReferenceImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireKeyIdentifierReferenceImpl(context.getModel(), element);
        }
    }

    public static class RequireIssuerSerialReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.REQUIREISSUERSERIALREFERENCE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequireIssuerSerialReferenceImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireIssuerSerialReferenceImpl(context.getModel(), element);
        }
    }

    public static class RequireThumbprintReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.REQUIRETHUMBPRINTREFERENCE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequireThumbprintReferenceImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireThumbprintReferenceImpl(context.getModel(), element);
        }
    }

    public static class RequireEmbeddedTokenReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.REQUIREEMBEDDEDTOKENREFERENCE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequireEmbeddedTokenReferenceImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireEmbeddedTokenReferenceImpl(context.getModel(), element);
        }
    }

    public static class RequireExternalUriReferenceFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.REQUIREEXTERNALURIREFERENCE.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new RequireExternalUriReferenceImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RequireExternalUriReferenceImpl(context.getModel(), element);
        }
    }

    public static class SC10SecurityContextTokenFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.SC10SECURITYCONTEXTTOKEN.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new SC10SecurityContextTokenImpl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new SC10SecurityContextTokenImpl(context.getModel(), element);
        }
    }

    public static class WssX509V1Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSX509V1TOKEN10.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssX509V1Token10Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509V1Token10Impl(context.getModel(), element);
        }
    }

    public static class WssX509V3Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSX509V3TOKEN10.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssX509V3Token10Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509V3Token10Impl(context.getModel(), element);
        }
    }

    public static class WssX509Pkcs7Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSX509PKCS7TOKEN10.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssX509Pkcs7Token10Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509Pkcs7Token10Impl(context.getModel(), element);
        }
    }

    public static class WssX509PkiPathV1Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSX509PKIPATHV1TOKEN10.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssX509PkiPathV1Token10Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509PkiPathV1Token10Impl(context.getModel(), element);
        }
    }

    public static class WssX509V1Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSX509V1TOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssX509V1Token11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509V1Token11Impl(context.getModel(), element);
        }
    }

    public static class WssX509V3Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSX509V3TOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssX509V3Token11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509V3Token11Impl(context.getModel(), element);
        }
    }

    public static class WssX509Pkcs7Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSX509PKCS7TOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssX509Pkcs7Token11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509Pkcs7Token11Impl(context.getModel(), element);
        }
    }

    public static class WssX509PkiPathV1Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSX509PKIPATHV1TOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssX509PkiPathV1Token11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssX509PkiPathV1Token11Impl(context.getModel(), element);
        }
    }

    public static class WssSamlV10Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSSAMLV10TOKEN10.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssSamlV10Token10Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssSamlV10Token10Impl(context.getModel(), element);
        }
    }

    public static class WssSamlV11Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSSAMLV11TOKEN10.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssSamlV11Token10Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssSamlV11Token10Impl(context.getModel(), element);
        }
    }

    public static class WssSamlV10Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSSAMLV10TOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssSamlV10Token11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssSamlV10Token11Impl(context.getModel(), element);
        }
    }

    public static class WssSamlV11Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSSAMLV11TOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssSamlV11Token11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssSamlV11Token11Impl(context.getModel(), element);
        }
    }

    public static class WssSamlV20Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSSAMLV20TOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssSamlV20Token11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssSamlV20Token11Impl(context.getModel(), element);
        }
    }
    
    public static class WssRelV10Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSRELV10TOKEN10.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssRelV10Token10Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssRelV10Token10Impl(context.getModel(), element);
        }
    }    

    public static class WssRelV20Token10Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSRELV20TOKEN10.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssRelV20Token10Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssRelV20Token10Impl(context.getModel(), element);
        }
    }    

    public static class WssRelV10Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSRELV10TOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssRelV10Token11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssRelV10Token11Impl(context.getModel(), element);
        }
    }    

    public static class WssRelV20Token11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSRELV20TOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssRelV20Token11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssRelV20Token11Impl(context.getModel(), element);
        }
    }    

    public static class WssKerberosV5ApReqToken11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSKERBEROSV5APREQTOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssKerberosV5ApReqToken11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssKerberosV5ApReqToken11Impl(context.getModel(), element);
        }
    }    

    public static class WssGssKerberosV5ApReqToken11Factory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(TokensQName.WSSGSSKERBEROSV5APREQTOKEN11.getQName());
        }
        public <C extends WSDLComponent> C create(WSDLComponent context, Class<C> type) {
            return type.cast(new WssGssKerberosV5ApReqToken11Impl(context.getModel()));
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new WssGssKerberosV5ApReqToken11Impl(context.getModel(), element);
        }
    }    
}
