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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.websvc.wsitconf.ui.security.SecurityBindingTokensPanel;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.MessageBody;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.MessageHeader;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.MessageListElement;
import org.netbeans.modules.websvc.wsitconf.ui.security.symmetric.tokens.SamlPanel;
import org.netbeans.modules.websvc.wsitconf.ui.security.symmetric.tokens.TokensSymmetricPanel;
import org.netbeans.modules.websvc.wsitconf.ui.security.symmetric.tokens.UsernamePanel;
import org.netbeans.modules.websvc.wsitconf.ui.security.symmetric.tokens.X509Panel;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.addressing.Address;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.addressing.AddressingQName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.addressing.EndpointReference;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.EncryptedParts;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.RequestSecurityTokenTemplate;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.SignedParts;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.*;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.trust.KeySize;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.trust.KeyType;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.trust.TokenType;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.trust.TrustQName;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class SecurityTokensModelHelper {

    public static final int SUPPORTING = 0;     
    public static final int SIGNED_SUPPORTING = 1;
    public static final int ENDORSING = 2;
    public static final int SIGNED_ENDORSING = 3;
    
    /**
     * Creates a new instance of SecurityTokensModelHelper
     */
    public SecurityTokensModelHelper() { }

    public static boolean isSameToken(WSDLComponent wc) {
        return SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) wc, ProtectionToken.class);
    }

    public static boolean isRequireClientCertificate(HttpsToken token) {
        return token.getRequireClientCertificate();
    }

    public static void setRequireClientCertificate(HttpsToken token, boolean require) {
        WSDLModel model = token.getModel();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            token.setRequireClientCertificate(require);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static String getProtectionTokenType(WSDLComponent secBinding) {
        return getTokenType(secBinding, ProtectionToken.class);
    }

    public static String getSignatureTokenType(WSDLComponent secBinding) {
        return getTokenType(secBinding, SignatureToken.class);
    }

    public static String getEncryptionTokenType(WSDLComponent secBinding) {
        return getTokenType(secBinding, EncryptionToken.class);
    }

    public static String getInitiatorTokenType(WSDLComponent secBinding) {
        return getTokenType(secBinding, InitiatorToken.class);
    }

    public static String getRecipientTokenType(WSDLComponent secBinding) {
        return getTokenType(secBinding, RecipientToken.class);
    }

    public static String getTokenType(WSDLComponent enclosingAssertion, Class tokenKindClass) {
        WSDLComponent pt = getTokenKindElement(enclosingAssertion, tokenKindClass);
        return getTokenType(pt);
    }
        
    public static String getTokenType(WSDLComponent tokenKind) {
        if (tokenKind != null) {
            WSDLComponent wc = null;
            wc = getTokenTypeElement(tokenKind, UsernameToken.class);
            if (wc != null) return TokensSymmetricPanel.USERNAME;
            wc = getTokenTypeElement(tokenKind, X509Token.class);
            if (wc != null) return TokensSymmetricPanel.X509;
            wc = getTokenTypeElement(tokenKind, SamlToken.class);
            if (wc != null) return TokensSymmetricPanel.SAML;
            wc = getTokenTypeElement(tokenKind, RelToken.class);
            if (wc != null) return TokensSymmetricPanel.REL;
            wc = getTokenTypeElement(tokenKind, KerberosToken.class);
            if (wc != null) return TokensSymmetricPanel.KERBEROS;
            wc = getTokenTypeElement(tokenKind, SecurityContextToken.class);
            if (wc != null) return TokensSymmetricPanel.SECURITYCONTEXT;
            wc = getTokenTypeElement(tokenKind, SecureConversationToken.class);
            if (wc != null) return TokensSymmetricPanel.SECURECONVERSATION;
            wc = getTokenTypeElement(tokenKind, IssuedToken.class);
            if (wc != null) return TokensSymmetricPanel.ISSUED;
        }
        return null;
    }

    public static WSDLComponent getTokenTypeElement(WSDLComponent tokenKind) {
        if (tokenKind != null) {
            WSDLComponent wc = null;
            wc = getTokenTypeElement(tokenKind, UsernameToken.class);
            if (wc != null) return wc;
            wc = getTokenTypeElement(tokenKind, X509Token.class);
            if (wc != null) return wc;
            wc = getTokenTypeElement(tokenKind, SamlToken.class);
            if (wc != null) return wc;
            wc = getTokenTypeElement(tokenKind, RelToken.class);
            if (wc != null) return wc;
            wc = getTokenTypeElement(tokenKind, KerberosToken.class);
            if (wc != null) return wc;
            wc = getTokenTypeElement(tokenKind, SecurityContextToken.class);
            if (wc != null) return wc;
            wc = getTokenTypeElement(tokenKind, SecureConversationToken.class);
            if (wc != null) return wc;
            wc = getTokenTypeElement(tokenKind, IssuedToken.class);
            return wc;
        }
        return null;
    }
    
    public static String getTokenInclusionLevel(WSDLComponent tokenType) {
        String incLevelStr = ((ExtensibilityElement)tokenType).getAnyAttribute(TokensQName.INCLUDETOKENATTRIBUTE.getQName());
        if (incLevelStr != null) {
            incLevelStr = incLevelStr.substring(incLevelStr.lastIndexOf("/")+1, incLevelStr.length()); //NOI18N
            return NbBundle.getMessage(UsernamePanel.class, "COMBO_" + incLevelStr); //NOI18N
        } else {
            return UsernamePanel.NONE;
        }
    }

    public static String getTokenProfileVersion(WSDLComponent tokenType) {
        if (tokenType instanceof UsernameToken) {
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssUsernameToken10.class)) {
                return UsernamePanel.WSS10;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssUsernameToken11.class)) {
                return UsernamePanel.WSS11;
            }
        }
        if (tokenType instanceof SamlToken) {
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssSamlV10Token11.class)) {
                return SamlPanel.SAML_V1011;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssSamlV11Token10.class)) {
                return SamlPanel.SAML_V1110;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssSamlV11Token11.class)) {
                return SamlPanel.SAML_V1111;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssSamlV20Token11.class)) {
                return SamlPanel.SAML_V2011;
            }
        }
        if (tokenType instanceof X509Token) {
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssX509V1Token10.class)) {
                return X509Panel.X509_V110;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssX509V1Token11.class)) {
                return X509Panel.X509_V111;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssX509V3Token10.class)) {
                return X509Panel.X509_V310;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssX509V3Token11.class)) {
                return X509Panel.X509_V311;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssX509Pkcs7Token10.class)) {
                return X509Panel.X509_PKCS710;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssX509Pkcs7Token11.class)) {
                return X509Panel.X509_PKCS711;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssX509PkiPathV1Token10.class)) {
                return X509Panel.X509_PKIPATHV110;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssX509PkiPathV1Token11.class)) {
                return X509Panel.X509_PKIPATHV111;
            }
        }
        return UsernamePanel.NONE;
    }

    public static WSDLComponent getBootstrapPolicy(WSDLComponent tokenType) {
        List<Policy> policies = tokenType.getExtensibilityElements(Policy.class);
        if ((policies != null) && (!policies.isEmpty())) {
            Policy p = policies.get(0);
            List<BootstrapPolicy> bpolicies = p.getExtensibilityElements(BootstrapPolicy.class);
            if ((bpolicies != null) && (!bpolicies.isEmpty())) {
                return bpolicies.get(0);
            }
        }
        return null;
    }

    public static WSDLComponent getTokenKindElement(WSDLComponent e, Class tokenClass) {
        List<Policy> policies = e.getExtensibilityElements(Policy.class);
        if ((policies != null) && (!policies.isEmpty())) {
            Policy p = policies.get(0);
            List<WSDLComponent> ptokens = p.getExtensibilityElements(tokenClass);
            if ((ptokens != null) && (!ptokens.isEmpty())) {
                return ptokens.get(0);
            }
        }
        return null;
    }
    
    public static WSDLComponent getTokenTypeElement(WSDLComponent tokenKind, Class tokenClass) {
        List<Policy> policies = tokenKind.getExtensibilityElements(Policy.class);
        if ((policies != null) && (!policies.isEmpty())) {
            Policy p = policies.get(0);
            List<WSDLComponent> tokens = p.getExtensibilityElements(tokenClass);
            if ((tokens != null) && (!tokens.isEmpty())) {
                return tokens.get(0);
            }
        }
        return null;
    }
    
    public static WSDLComponent setTokenType(WSDLComponent secBinding, String tokenKindStr, String tokenTypeStr) {
        WSDLModel model = secBinding.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        WSDLComponent tokenType = null;

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            List<Policy> policies = secBinding.getExtensibilityElements(Policy.class);
            Policy p = null;
            if ((policies == null) || (policies.isEmpty())) {
                p = (Policy) wcf.create(secBinding, PolicyQName.POLICY.getQName());
                secBinding.addExtensibilityElement(p);
            } else {
                p = policies.get(0);
            }

            WSDLComponent tokenKind = null;
            if (TokensSymmetricPanel.PROTECTION.equals(tokenKindStr)) {
                List<ExtensibilityElement> tokenKinds = p.getExtensibilityElements();
                if ((tokenKinds != null) && (!tokenKinds.isEmpty())) {
                    for (ExtensibilityElement tkind : tokenKinds) {
                        if (tkind instanceof ProtectionToken ||
                            tkind instanceof SignatureToken ||
                            tkind instanceof EncryptionToken ||
                            tkind instanceof InitiatorToken ||
                            tkind instanceof RecipientToken) {
                                p.removeExtensibilityElement(tkind);
                        }
                    }
                }
                tokenKind = wcf.create(p, TokensQName.PROTECTIONTOKEN.getQName());
            }
            if (TokensSymmetricPanel.SIGNATURE.equals(tokenKindStr)) {
                List<ExtensibilityElement> tokenKinds = p.getExtensibilityElements();
                if ((tokenKinds != null) && (!tokenKinds.isEmpty())) {
                    for (ExtensibilityElement tkind : tokenKinds) {
    //                    if (tkind instanceof ProtectionToken) {
    //                        List<WSDLComponent> children = tkind.getChildren();
    //                        tokenKind = wcf.create(p, TokensQName.ENCRYPTIONTOKEN.getQName());
    //                        for (WSDLComponent c : children) {
    //                            tkind.removeExtensibilityElement((ExtensibilityElement) c);
    //                            tokenKind.addExtensibilityElement((ExtensibilityElement) c);
    //                        }
    //                        p.addExtensibilityElement((ExtensibilityElement) tokenKind);
    //                    }
                        if (!(tkind instanceof EncryptionToken)) p.removeExtensibilityElement(tkind);
                    }
                }
                tokenKind = wcf.create(p, TokensQName.SIGNATURETOKEN.getQName());
            }
            if (TokensSymmetricPanel.ENCRYPTION.equals(tokenKindStr)) {
                List<ExtensibilityElement> tokenKinds = p.getExtensibilityElements();
                if ((tokenKinds != null) && (!tokenKinds.isEmpty())) {
                    for (ExtensibilityElement tkind : tokenKinds) {
    //                    if (tkind instanceof ProtectionToken) {
    //                        List<WSDLComponent> children = tkind.getChildren();
    //                        tokenKind = wcf.create(p, TokensQName.SIGNATURETOKEN.getQName());
    //                        for (WSDLComponent c : children) {
    //                            tkind.removeExtensibilityElement((ExtensibilityElement) c);
    //                            tokenKind.addExtensibilityElement((ExtensibilityElement) c);
    //                        }
    //                        p.addExtensibilityElement((ExtensibilityElement) tokenKind);
    //                    }
                        if (!(tkind instanceof SignatureToken)) p.removeExtensibilityElement(tkind);
                    }
                }
                tokenKind = wcf.create(p, TokensQName.ENCRYPTIONTOKEN.getQName());
            }
            if (TokensSymmetricPanel.INITIATOR.equals(tokenKindStr)) {
                List<ExtensibilityElement> tokenKinds = p.getExtensibilityElements();
                if ((tokenKinds != null) && (!tokenKinds.isEmpty())) {
                    for (ExtensibilityElement tkind : tokenKinds) {
                        if (!(tkind instanceof RecipientToken)) p.removeExtensibilityElement(tkind);
                    }
                }
                tokenKind = wcf.create(p, TokensQName.INITIATORTOKEN.getQName());
            }
            if (TokensSymmetricPanel.RECIPIENT.equals(tokenKindStr)) {
                List<ExtensibilityElement> tokenKinds = p.getExtensibilityElements();
                if ((tokenKinds != null) && (!tokenKinds.isEmpty())) {
                    for (ExtensibilityElement tkind : tokenKinds) {
                        if (!(tkind instanceof InitiatorToken)) p.removeExtensibilityElement(tkind);
                    }
                }
                tokenKind = wcf.create(p, TokensQName.RECIPIENTTOKEN.getQName());
            }
            p.addExtensibilityElement((ExtensibilityElement) tokenKind);

            Policy pinner = (Policy) wcf.create(tokenKind, PolicyQName.POLICY.getQName());
            tokenKind.addExtensibilityElement(pinner);

            if (TokensSymmetricPanel.USERNAME.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.USERNAMETOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, UsernamePanel.WSS10);
            }
            if (TokensSymmetricPanel.X509.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.X509TOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, X509Panel.X509_V310);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
            }
            if (TokensSymmetricPanel.SAML.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.SAMLTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, SamlPanel.SAML_V1110);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);            
            }
            if (TokensSymmetricPanel.REL.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.RELTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
            }
            if (TokensSymmetricPanel.SECURECONVERSATION.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.SECURECONVERSATIONTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
                setBootstrapPolicy(tokenType, 
                        SecurityBindingTokensPanel.SYMMETRIC, 
                        TokensSymmetricPanel.X509, 
                        TokensSymmetricPanel.X509,
                        UsernamePanel.WSS10);
            }
            if (TokensSymmetricPanel.SECURITYCONTEXT.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.SECURITYCONTEXTTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
            }
            if (TokensSymmetricPanel.SPNEGOCONTEXT.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.SPNEGOCONTEXTTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
            }
            if (TokensSymmetricPanel.ISSUED.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.ISSUEDTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                RequestSecurityTokenTemplate template = 
                        (RequestSecurityTokenTemplate) wcf.create(tokenType, SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName());

                SecurityPolicyModelHelper.enableRequireInternalReference(tokenType, true);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);

                tokenType.addExtensibilityElement(template);
                TokenType trustTokenType = (TokenType) wcf.create(template, TrustQName.TOKENTYPE.getQName());
                template.addExtensibilityElement(trustTokenType);
                trustTokenType.setContent("urn:oasis:names:tc:SAML:1.0:assertion"); //NOI18N
                KeyType trustKeyType = (KeyType) wcf.create(template, TrustQName.KEYTYPE.getQName());
                template.addExtensibilityElement(trustKeyType);
                trustKeyType.setContent("http://schemas.xmlsoap.org/ws/2005/02/trust/SymmetricKey"); //NOI18N
                KeySize trustKeySize = (KeySize) wcf.create(template, TrustQName.KEYSIZE.getQName());
                template.addExtensibilityElement(trustKeySize);
                trustKeySize.setContent("256"); //NOI18N
            }
            if (TokensSymmetricPanel.KERBEROS.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.KERBEROSTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
            }

            setTokenInclusionLevel(tokenType, UsernamePanel.ALWAYSRECIPIENT);

        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
        return tokenType;
    }

    public static void setTokenInclusionLevel(WSDLComponent tokenType, String incLevel) {
        WSDLModel model = tokenType.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            String levelStr = null;
            if (UsernamePanel.NEVER.equals(incLevel)) {
                levelStr = UsernamePanel.NEVER_POLICYSTR;
            } else if (UsernamePanel.ALWAYS.equals(incLevel)) {
                levelStr = UsernamePanel.ALWAYS_POLICYSTR;
            } else if (UsernamePanel.ALWAYSRECIPIENT.equals(incLevel)) {
                levelStr = UsernamePanel.ALWAYSRECIPIENT_POLICYSTR;
            } else if (UsernamePanel.ONCE.equals(incLevel)) {
                levelStr = UsernamePanel.ONCE_POLICYSTR;
            }
            ((ExtensibilityElement)tokenType).setAnyAttribute(TokensQName.INCLUDETOKENATTRIBUTE.getQName(), levelStr);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
   }

    public static void setTokenProfileVersion(WSDLComponent tokenType, String profileVersion) {
        WSDLModel model = tokenType.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            List<Policy> policies = tokenType.getExtensibilityElements(Policy.class);
            Policy p = null;
            if ((policies == null) || (policies.isEmpty())) {
                p = (Policy) wcf.create(tokenType, PolicyQName.POLICY.getQName());
                tokenType.addExtensibilityElement(p);
            } else {
                p = policies.get(0);
            }

            WSDLComponent profileVersionAssertion = null;
            if (tokenType instanceof UsernameToken) {
                List<ExtensibilityElement> tokenAssertions = p.getExtensibilityElements();
                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if ((e instanceof WssUsernameToken10) ||
                            (e instanceof WssUsernameToken11)) {                     
                                p.removeExtensibilityElement(e);
                        }
                    }
                }
                if (UsernamePanel.WSS10.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSUSERNAMETOKEN10.getQName());
                if (UsernamePanel.WSS11.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSUSERNAMETOKEN11.getQName());
            }
            if (tokenType instanceof SamlToken) {
                List<ExtensibilityElement> tokenAssertions = p.getExtensibilityElements();
                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if ((e instanceof WssSamlV10Token11) ||
                            (e instanceof WssSamlV11Token10) || 
                            (e instanceof WssSamlV11Token11) || 
                            (e instanceof WssSamlV20Token11)) {                     
                                p.removeExtensibilityElement(e);
                        }
                    }
                }
                if (SamlPanel.SAML_V1011.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSSAMLV10TOKEN11.getQName());
                if (SamlPanel.SAML_V1110.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSSAMLV11TOKEN10.getQName());
                if (SamlPanel.SAML_V1111.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSSAMLV11TOKEN11.getQName());
                if (SamlPanel.SAML_V2011.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSSAMLV20TOKEN11.getQName());
            }

            if (tokenType instanceof X509Token) {
                List<ExtensibilityElement> tokenAssertions = p.getExtensibilityElements();

                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if ((e instanceof WssX509V1Token10) ||
                            (e instanceof WssX509V3Token10) || 
                            (e instanceof WssX509V1Token11) || 
                            (e instanceof WssX509V3Token11) || 
                            (e instanceof WssX509Pkcs7Token10) || 
                            (e instanceof WssX509Pkcs7Token11) || 
                            (e instanceof WssX509PkiPathV1Token10) || 
                            (e instanceof WssX509PkiPathV1Token11)) {                     
                                p.removeExtensibilityElement(e);
                        }
                    }
                }

                if (X509Panel.X509_V110.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509V1TOKEN10.getQName());
                if (X509Panel.X509_V310.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509V3TOKEN11.getQName());
                if (X509Panel.X509_V111.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509V1TOKEN11.getQName());
                if (X509Panel.X509_V311.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509V3TOKEN11.getQName());
                if (X509Panel.X509_PKCS710.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509PKCS7TOKEN10.getQName());
                if (X509Panel.X509_PKCS711.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509PKCS7TOKEN11.getQName());
                if (X509Panel.X509_PKIPATHV110.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509PKIPATHV1TOKEN10.getQName());
                if (X509Panel.X509_PKIPATHV111.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509PKIPATHV1TOKEN11.getQName());
            }

            if (profileVersionAssertion != null) p.addExtensibilityElement((ExtensibilityElement) profileVersionAssertion);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
 
    public static WSDLComponent setBootstrapPolicy(WSDLComponent secConvToken, 
            String secBinding, String protToken, String authToken, String wssVersion) {
        
        WSDLModel model = secConvToken.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        BootstrapPolicy bootPol = null;
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            List<Policy> policies = secConvToken.getExtensibilityElements(Policy.class);
            Policy p = null;
            if ((policies == null) || (policies.isEmpty())) {
                p = (Policy) wcf.create(secConvToken, PolicyQName.POLICY.getQName());
                secConvToken.addExtensibilityElement(p);
            } else {
                p = policies.get(0);
            }

            List<BootstrapPolicy> bpolicies = p.getExtensibilityElements(BootstrapPolicy.class);
            if ((bpolicies != null) && (!bpolicies.isEmpty())) {
                for (ExtensibilityElement bpol : bpolicies) {
                    p.removeExtensibilityElement(bpol);
                }
            }
            bootPol = (BootstrapPolicy) wcf.create(p, SecurityPolicyQName.BOOTSTRAPPOLICY.getQName());
            p.addExtensibilityElement((ExtensibilityElement) bootPol);

            p = (Policy) wcf.create(bootPol, PolicyQName.POLICY.getQName());
            bootPol.addExtensibilityElement(p);

            //add binding
            WSDLComponent secBindingElem = SecurityPolicyModelHelper.setSecurityBindingType(p, secBinding, null);        
            setSupportingTokens(p, authToken, SIGNED_SUPPORTING);

            if (SecurityBindingTokensPanel.SYMMETRIC.equals(secBinding)) {
                WSDLComponent token = setTokenType(secBindingElem, TokensSymmetricPanel.PROTECTION, protToken);
                if (TokensSymmetricPanel.X509.equals(protToken)) {
                    setTokenInclusionLevel(token, UsernamePanel.NEVER);
                    SecurityPolicyModelHelper.enableRequireDerivedKeys(token, true);
                    SecurityPolicyModelHelper.enableRequireEmbeddedTokenReference(token, false);
                    SecurityPolicyModelHelper.enableRequireThumbprintReference(token, false);
                    SecurityPolicyModelHelper.enableRequireIssuerSerialReference(token, false);
                    SecurityPolicyModelHelper.enableRequireKeyIdentifierReference(token, false);                
                }
            }

    //      add wss11
            if (UsernamePanel.WSS10.equals(wssVersion)) {
                SecurityPolicyModelHelper.enableWss10(p);
            } else {
                SecurityPolicyModelHelper.enableWss11(p);
            }
    //      add trust10
            SecurityPolicyModelHelper.enableTrust10(p);

    //      add message parts for policy bootstrap
            SignedParts signedParts = (SignedParts) wcf.create(p, SecurityPolicyQName.SIGNEDPARTS.getQName());
            EncryptedParts encryptedParts = (EncryptedParts) wcf.create(p, SecurityPolicyQName.ENCRYPTEDPARTS.getQName());

            p.addExtensibilityElement(signedParts);
            p.addExtensibilityElement(encryptedParts);

            Vector<MessageListElement> signedElems = new Vector();
            Vector<MessageListElement> encElems = new Vector();

            MessageBody body = new MessageBody(MessageBody.BODY);
            signedElems.add(body);
            encElems.add(body);

            MessageHeader h = new MessageHeader(MessageHeader.ADDRESSING_TO);
            signedElems.add(h);
            h = new MessageHeader(MessageHeader.ADDRESSING_FROM);
            signedElems.add(h);
            h = new MessageHeader(MessageHeader.ADDRESSING_FAULTTO);
            signedElems.add(h);
            h = new MessageHeader(MessageHeader.ADDRESSING_REPLYTO);
            signedElems.add(h);
            h = new MessageHeader(MessageHeader.ADDRESSING_MESSAGEID);
            signedElems.add(h);
            h = new MessageHeader(MessageHeader.ADDRESSING_RELATESTO);
            signedElems.add(h);
            h = new MessageHeader(MessageHeader.ADDRESSING_ACTION);
            signedElems.add(h);

            for (MessageListElement e : signedElems) {
                if (e instanceof MessageHeader) {
                    SecurityPolicyModelHelper.addHeaderElementForListItem(e.toString(), signedParts, wcf);
                } else if (e instanceof MessageBody) {
                    SecurityPolicyModelHelper.addBodyForListItem(e.toString(), signedParts, wcf);
                }
            }

            for (MessageListElement e : encElems) {
                if (e instanceof MessageHeader) {
                    SecurityPolicyModelHelper.addHeaderElementForListItem(e.toString(), encryptedParts, wcf);
                } else if (e instanceof MessageBody) {
                    SecurityPolicyModelHelper.addBodyForListItem(e.toString(), encryptedParts, wcf);
                }
            }
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
        return bootPol;
    }
    
    public static WSDLComponent getSupportingToken(WSDLComponent c, int supportingType) {

        List elems = null;
        
        if (SUPPORTING == supportingType) {
            elems = c.getExtensibilityElements(SupportingTokens.class);
        }
        if (SIGNED_SUPPORTING == supportingType) {
            elems = c.getExtensibilityElements(SignedSupportingTokens.class);
        }
        if (ENDORSING == supportingType) {
            elems = c.getExtensibilityElements(EndorsingSupportingTokens.class);
        }
        if (SIGNED_ENDORSING == supportingType) {
            elems = c.getExtensibilityElements(SignedEndorsingSupportingTokens.class);
        }
        
        if ((elems != null) && (elems.size() > 0)) {
            return (WSDLComponent) elems.get(0);
        }
        return null;
    }
    
    public static WSDLComponent setTopLevelSupportingTokens(WSDLComponent c, String authToken, int supportingType) {
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();

        WSDLComponent suppToken = getSupportingToken(c, supportingType);
        WSDLComponent tokenType = null;
        
        // remove token if none selection
        if (UsernamePanel.NONE.equals(authToken)) {
            if (suppToken == null) {
                return null;
            } else {
                boolean isTransaction = model.isIntransaction();
                if (!isTransaction) {
                    model.startTransaction();
                }
                try {
                    c.removeExtensibilityElement((ExtensibilityElement) suppToken);
                } finally {
                    if (!isTransaction) {
                            model.endTransaction();
                    }
                }
                return null;
            }
        }

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            if (suppToken == null ) {
                if (SUPPORTING == supportingType) {
                    suppToken = wcf.create(c, TokensQName.SUPPORTINGTOKENS.getQName());
                }
                if (SIGNED_SUPPORTING == supportingType) {
                    suppToken = wcf.create(c, TokensQName.SIGNEDSUPPORTINGTOKENS.getQName());
                }
                if (ENDORSING == supportingType) {
                    suppToken = wcf.create(c, TokensQName.ENDORSINGSUPPORTINGTOKENS.getQName());
                }
                if (SIGNED_ENDORSING == supportingType) {
                    suppToken = wcf.create(c, TokensQName.SIGNEDENDORSINGSUPPORTINGTOKENS.getQName());
                }            
                c.addExtensibilityElement((ExtensibilityElement) suppToken);
            }

            List<Policy> policies = suppToken.getExtensibilityElements(Policy.class);
            if ((policies != null) && (!policies.isEmpty())) {
                for (Policy pol : policies) {
                    suppToken.removeExtensibilityElement(pol);
                }
            }        
            Policy p = (Policy) wcf.create(suppToken, PolicyQName.POLICY.getQName());
            suppToken.addExtensibilityElement(p);

            if (TokensSymmetricPanel.USERNAME.equals(authToken)) {
                tokenType = wcf.create(p, TokensQName.USERNAMETOKEN.getQName());
                p.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, UsernamePanel.WSS10);
            }
            if (TokensSymmetricPanel.X509.equals(authToken)) {
                tokenType = wcf.create(p, TokensQName.X509TOKEN.getQName());
                p.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, X509Panel.X509_V310);
                SecurityPolicyModelHelper.enableRequireThumbprintReference(tokenType, true);
            }
            if (TokensSymmetricPanel.SAML.equals(authToken)) {
                tokenType = wcf.create(p, TokensQName.SAMLTOKEN.getQName());
                p.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, SamlPanel.SAML_V1110);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
            }
            if (TokensSymmetricPanel.REL.equals(authToken)) {
                tokenType = wcf.create(p, TokensQName.RELTOKEN.getQName());
                p.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
            }
            if (TokensSymmetricPanel.SECURECONVERSATION.equals(authToken)) {
                tokenType = wcf.create(p, TokensQName.SECURECONVERSATIONTOKEN.getQName());
                p.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
                setBootstrapPolicy(tokenType, 
                        SecurityBindingTokensPanel.SYMMETRIC, 
                        TokensSymmetricPanel.X509, 
                        TokensSymmetricPanel.X509,
                        UsernamePanel.WSS10);
            }
            if (TokensSymmetricPanel.ISSUED.equals(authToken)) {
                tokenType = wcf.create(p, TokensQName.ISSUEDTOKEN.getQName());
                p.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireInternalReference(tokenType, true);            

                RequestSecurityTokenTemplate template = 
                        (RequestSecurityTokenTemplate) wcf.create(tokenType, SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName());
                tokenType.addExtensibilityElement(template);
                TokenType trustTokenType = (TokenType) wcf.create(template, TrustQName.TOKENTYPE.getQName());
                template.addExtensibilityElement(trustTokenType);
                trustTokenType.setContent("urn:oasis:names:tc:SAML:1.0:assertion"); //NOI18N
                KeyType trustKeyType = (KeyType) wcf.create(template, TrustQName.KEYTYPE.getQName());
                template.addExtensibilityElement(trustKeyType);
                trustKeyType.setContent("http://schemas.xmlsoap.org/ws/2005/02/trust/SymmetricKey"); //NOI18N
                KeySize trustKeySize = (KeySize) wcf.create(template, TrustQName.KEYSIZE.getQName());
                template.addExtensibilityElement(trustKeySize);
                trustKeySize.setContent("256"); //NOI18N
            }
            setTokenInclusionLevel(tokenType, UsernamePanel.ALWAYSRECIPIENT);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
        return tokenType;
    }    
    
    public static WSDLComponent setSupportingTokens(WSDLComponent c, String authToken, int supportingType) {
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        WSDLComponent tokenType = null;

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            WSDLComponent suppToken = getSupportingToken(c, supportingType);
            if (suppToken == null ) {
                if (SUPPORTING == supportingType) {
                    suppToken = wcf.create(c, TokensQName.SUPPORTINGTOKENS.getQName());
                }
                if (SIGNED_SUPPORTING == supportingType) {
                    suppToken = wcf.create(c, TokensQName.SIGNEDSUPPORTINGTOKENS.getQName());
                }
                if (ENDORSING == supportingType) {
                    suppToken = wcf.create(c, TokensQName.ENDORSINGSUPPORTINGTOKENS.getQName());
                }
                if (SIGNED_ENDORSING == supportingType) {
                    suppToken = wcf.create(c, TokensQName.SIGNEDENDORSINGSUPPORTINGTOKENS.getQName());
                }
                c.addExtensibilityElement((ExtensibilityElement) suppToken);
            }

            List<Policy> policies = suppToken.getExtensibilityElements(Policy.class);
            if ((policies != null) && (!policies.isEmpty())) {
                for (Policy pol : policies) {
                    suppToken.removeExtensibilityElement(pol);
                }
            }        
            Policy p = (Policy) wcf.create(suppToken, PolicyQName.POLICY.getQName());
            suppToken.addExtensibilityElement(p);

            if (TokensSymmetricPanel.USERNAME.equals(authToken)) {
                tokenType = wcf.create(p, TokensQName.USERNAMETOKEN.getQName());
                p.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, UsernamePanel.WSS10);
                setTokenInclusionLevel(tokenType, UsernamePanel.ALWAYSRECIPIENT);
            }
            if (TokensSymmetricPanel.X509.equals(authToken)) {
                tokenType = wcf.create(p, TokensQName.X509TOKEN.getQName());
                p.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, X509Panel.X509_V310);
                SecurityPolicyModelHelper.enableRequireThumbprintReference(tokenType, true);
                setTokenInclusionLevel(tokenType, UsernamePanel.ALWAYSRECIPIENT);
            }
            if (TokensSymmetricPanel.ISSUED.equals(authToken)) {
                tokenType = wcf.create(p, TokensQName.ISSUEDTOKEN.getQName());
                p.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireInternalReference(tokenType, true);            
                setTokenInclusionLevel(tokenType, UsernamePanel.ALWAYSRECIPIENT);

                RequestSecurityTokenTemplate template = 
                        (RequestSecurityTokenTemplate) wcf.create(tokenType, SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName());
                tokenType.addExtensibilityElement(template);
                TokenType trustTokenType = (TokenType) wcf.create(template, TrustQName.TOKENTYPE.getQName());
                template.addExtensibilityElement(trustTokenType);
                trustTokenType.setContent("urn:oasis:names:tc:SAML:1.0:assertion"); //NOI18N
                KeyType trustKeyType = (KeyType) wcf.create(template, TrustQName.KEYTYPE.getQName());
                template.addExtensibilityElement(trustKeyType);
                trustKeyType.setContent("http://schemas.xmlsoap.org/ws/2005/02/trust/SymmetricKey"); //NOI18N
                KeySize trustKeySize = (KeySize) wcf.create(template, TrustQName.KEYSIZE.getQName());
                template.addExtensibilityElement(trustKeySize);
                trustKeySize.setContent("256"); //NOI18N
            }
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
        return tokenType;
    }    
    
    public static String getTokenIssuer(WSDLComponent tokenType) {
        if (tokenType != null) {
            List<Issuer> issuerList = tokenType.getExtensibilityElements(Issuer.class);
            if ((issuerList != null) && (!issuerList.isEmpty())) {
                Issuer issuer = issuerList.get(0);
                List eRefs = issuer.getExtensibilityElements(EndpointReference.class);
                if ((eRefs != null) && (!eRefs.isEmpty())) {
                    EndpointReference e = (EndpointReference) eRefs.get(0);
                    List addresses = e.getExtensibilityElements(Address.class);
                    if ((addresses != null) && (!addresses.isEmpty())) {
                        Address a = (Address) addresses.get(0);
                        if (a != null) {
                            return a.getAddress();
                        }
                    }
                }
            }
        }
        return null;
    }

   public static void setTokenIssuer(WSDLComponent tokenType, String url) {
        WSDLModel model = tokenType.getModel();
        WSDLComponentFactory wcf = model.getFactory();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            List<Issuer> issuers = tokenType.getExtensibilityElements(Issuer.class);
            Issuer i = null;
            if ((issuers == null) || (issuers.isEmpty())) {
                i = (Issuer) wcf.create(tokenType, TokensQName.ISSUER.getQName());
                tokenType.addExtensibilityElement(i);
            } else {
                i = issuers.get(0);
            }

            List<EndpointReference> eRefs = i.getExtensibilityElements(EndpointReference.class);
            EndpointReference er = null;
            if ((eRefs == null) || (eRefs.isEmpty())) {
                er = (EndpointReference) wcf.create(i, AddressingQName.ENDPOINTREFERENCE.getQName());
                i.addExtensibilityElement(er);
            } else {
                er = eRefs.get(0);
            }

            List<Address> ads = er.getExtensibilityElements(Address.class);
            Address a = null;
            if ((ads == null) || (ads.isEmpty())) {
                a = (Address) wcf.create(er, AddressingQName.ADDRESS.getQName());
                er.addExtensibilityElement(a);
            } else {
                a = ads.get(0);
            }
            a.setAddress(url);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
}
