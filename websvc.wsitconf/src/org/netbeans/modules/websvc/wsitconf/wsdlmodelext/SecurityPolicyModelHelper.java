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

import java.util.Collection;
import org.netbeans.modules.websvc.wsitconf.Utilities;
import org.netbeans.modules.websvc.wsitconf.ui.security.SecurityBindingOtherPanel;
import org.netbeans.modules.websvc.wsitconf.ui.security.SecurityBindingTokensPanel;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.*;
import org.netbeans.modules.websvc.wsitconf.ui.security.symmetric.tokens.TokensSymmetricPanel;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.addressing.AddressingQName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.All;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.rm.RMQName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.*;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.parameters.*;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.HttpsToken;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.RequireDerivedKeys;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.RequireEmbeddedTokenReference;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.RequireExternalReference;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.RequireExternalUriReference;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.RequireInternalReference;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.RequireIssuerSerialReference;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.RequireKeyIdentifierReference;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.RequireThumbprintReference;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.SC10SecurityContextToken;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.TokensQName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.tokens.TransportToken;
import org.netbeans.modules.xml.wsdl.model.*;
import org.openide.util.NbBundle;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import org.openide.ErrorManager;

/**
 *
 * @author Martin Grebac
 */
public class SecurityPolicyModelHelper {
    
    /**
     * Creates a new instance of SecurityPolicyModelHelper
     */
    public SecurityPolicyModelHelper() {
    }
    
    /* enables Wss10 in the config wsdl on specified binding, 
     * creates all the important elements if needed
    */
    public static void enableWss10(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        Wss10 wss10Assertion = (Wss10) PolicyModelHelper.getTopLevelElement(p, Wss10.class);
        if (wss10Assertion == null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();

                All all = null;
                if (p == null) {
                    all = PolicyModelHelper.createTopLevelPolicy(b, model, wcf);
                } else {
                    all = PolicyModelHelper.createTopExactlyOne(p, model, wcf);
                }

                enableWss10(all);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }

    public static void enableWss10(WSDLComponent c) {
        
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            if (!isWss10(c)) {
                Wss10 wss = (Wss10)wcf.create(c, SecurityPolicyQName.WSS10.getQName());
                c.addExtensibilityElement(wss);

                // set default values
                enableAttribute(wss,
                    SecurityPolicyQName.MUSTSUPPORTREFKEYIDENTIFIER.getQName(), 
                    MustSupportRefKeyIdentifier.class);
                enableAttribute(wss,
                    SecurityPolicyQName.MUSTSUPPORTREFISSUERSERIAL.getQName(), 
                    MustSupportRefIssuerSerial.class);
            }
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
    
    /* enables Wss11 in the config wsdl on specified binding, 
     * creates all the important elements if needed
    */
    public static void enableWss11(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        Wss11 wss11Assertion = (Wss11) PolicyModelHelper.getTopLevelElement(p, Wss11.class);
        if (wss11Assertion == null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            WSDLComponentFactory wcf = model.getFactory();

            All all = null;
            if (p == null) {
                all = PolicyModelHelper.createTopLevelPolicy(b, model, wcf);
            } else {
                all = PolicyModelHelper.createTopExactlyOne(p, model, wcf);
            }
            
            enableWss11(all);

                if (!isTransaction) {
                    model.endTransaction();
                }
        }
    }

    public static void enableWss11(WSDLComponent c) {
        
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            if (!isWss11(c)) {
                Wss11 wss = (Wss11)wcf.create(c, SecurityPolicyQName.WSS11.getQName());
                c.addExtensibilityElement(wss);

                // set default values
                enableAttribute(wss,
                    SecurityPolicyQName.MUSTSUPPORTREFKEYIDENTIFIER.getQName(), 
                    MustSupportRefKeyIdentifier.class);
                enableAttribute(wss,
                    SecurityPolicyQName.MUSTSUPPORTREFISSUERSERIAL.getQName(), 
                    MustSupportRefIssuerSerial.class);
                enableAttribute(wss,
                    SecurityPolicyQName.MUSTSUPPORTREFTHUMBPRINT.getQName(), 
                    MustSupportRefThumbprint.class);
                enableAttribute(wss,
                    SecurityPolicyQName.MUSTSUPPORTREFENCRYPTEDKEY.getQName(), 
                    MustSupportRefEncryptedKey.class);
                enableAttribute(wss,
                    SecurityPolicyQName.REQUIRESIGNATURECONFIRMATION.getQName(), 
                    RequireSignatureConfirmation.class);
            }
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
    
    /* enables Wss11 in the config wsdl on specified binding, 
     * creates all the important elements if needed
    */
    public static void enableTrust10(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        Trust10 trustAssertion = (Trust10) PolicyModelHelper.getTopLevelElement(p, Trust10.class);
        if (trustAssertion == null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();

                All all = null;
                if (p == null) {
                    all = PolicyModelHelper.createTopLevelPolicy(b, model, wcf);
                } else {
                    all = PolicyModelHelper.createTopExactlyOne(p, model, wcf);
                }

                enableTrust10(all);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }
    
    public static void enableTrust10(WSDLComponent c) {
        
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            if (!isTrust10(c)) {
                Trust10 trust = (Trust10)wcf.create(c, SecurityPolicyQName.TRUST10.getQName());
                c.addExtensibilityElement(trust);

                // set default values
                enableAttribute(trust,
                    SecurityPolicyQName.MUSTSUPPORTISSUEDTOKENS.getQName(), 
                    MustSupportIssuedTokens.class);
                enableAttribute(trust,
                    SecurityPolicyQName.REQUIRECLIENTENTROPY.getQName(), 
                    RequireClientEntropy.class);
                enableAttribute(trust,
                    SecurityPolicyQName.REQUIRESERVERENTROPY.getQName(), 
                    RequireServerEntropy.class);
            }
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    // disables Wss in the config wsdl on specified binding
    public static void disableWss(Binding b, boolean wss11) {
        WSDLModel model = b.getModel();
        WssElement wss = wss11 ? getWss11(b, model) : getWss10(b, model);
        if (wss != null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                wss.getParent().removeExtensibilityElement(wss);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }

    // disables Wss in the config wsdl on specified binding
    public static void disableWss(WSDLComponent c, boolean wss11) {
        WSDLModel model = c.getModel();
        WssElement wss = wss11 ? getWss11(c) : getWss10(c);
        if (wss != null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                wss.getParent().removeExtensibilityElement(wss);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }
    
    /* disables Trust10 in the config wsdl on specified binding
     */
    public static void disableTrust10(Binding b, WSDLModel model) {
        Trust10 trust = getTrust10(b, model);
        if (trust != null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                trust.getParent().removeExtensibilityElement(trust);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }
    
    /* disables Trust10 in the config wsdl on specified component
     */
    public static void disableTrust10(WSDLComponent c) {
        Trust10 trust = getTrust10(c);
        if (trust != null) {
            WSDLModel model = c.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                trust.getParent().removeExtensibilityElement(trust);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }

    // checks if Wss10 is enabled in the config wsdl on specified binding
    public static boolean isWss10(Binding b, WSDLModel model) {
        Wss10 wss10 = getWss10(b, model);
        return (wss10 != null);
    }
    
    public static boolean isWss10(WSDLComponent c) {
        Wss10 wss = getWss10(c);
        return (wss != null);
    }

    // checks if Wss11 is enabled in the config wsdl on specified binding
    public static boolean isWss11(Binding b, WSDLModel model) {
        Wss11 wss11 = getWss11(b, model);
        return (wss11 != null);
    }

    public static boolean isWss11(WSDLComponent c) {
        Wss11 wss = getWss11(c);
        return (wss != null);
    }

    // checks if Trust10 is enabled in the config wsdl on specified binding
    public static boolean isTrust10(Binding b, WSDLModel model) {
        Trust10 trust = getTrust10(b, model);
        return (trust != null);
    }

    public static boolean isTrust10(WSDLComponent c) {
        Trust10 trust = getTrust10(c);
        return (trust != null);
    }

    public static Wss10 getWss10(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        return (Wss10)PolicyModelHelper.getTopLevelElement(p, Wss10.class);
    }

    public static Wss10 getWss10(WSDLComponent c) {
        List<Wss10> elems = c.getExtensibilityElements(Wss10.class);
        if ((elems != null) && (!elems.isEmpty())) {
            return elems.get(0);
        }
        return null;
    }

    public static Wss11 getWss11(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        return (Wss11)PolicyModelHelper.getTopLevelElement(p, Wss11.class);
    }

    public static Wss11 getWss11(WSDLComponent c) {
        List<Wss11> elems = c.getExtensibilityElements(Wss11.class);
        if ((elems != null) && (!elems.isEmpty())) {
            return elems.get(0);
        }
        return null;
    }
    
    public static Trust10 getTrust10(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        return (Trust10)PolicyModelHelper.getTopLevelElement(p, Trust10.class);
    }
    
    public static Trust10 getTrust10(WSDLComponent c) {
        List<Trust10> elems = c.getExtensibilityElements(Trust10.class);
        if ((elems != null) && (!elems.isEmpty())) {
            return elems.get(0);
        }
        return null;
    }

    // -------- WSS10 & 11 ELEMENTS -----------
    public static boolean isMustSupportRefEmbeddedToken(Binding b, WSDLModel model) {
        Wss11 wss11 = getWss11(b, model);
        Wss10 wss10 = getWss10(b, model);
        return isAttributeEnabled(wss10, MustSupportRefEmbeddedToken.class) 
        || isAttributeEnabled(wss11, MustSupportRefEmbeddedToken.class);
    }

    public static boolean isMustSupportRefExternalURI(Binding b, WSDLModel model) {
        Wss11 wss11 = getWss11(b, model);
        Wss10 wss10 = getWss10(b, model);
        return isAttributeEnabled(wss10, MustSupportRefExternalURI.class) 
        || isAttributeEnabled(wss11, MustSupportRefExternalURI.class);
    }

    public static boolean isMustSupportRefIssuerSerial(Binding b, WSDLModel model) {
        Wss11 wss11 = getWss11(b, model);
        Wss10 wss10 = getWss10(b, model);
        return isAttributeEnabled(wss10, MustSupportRefIssuerSerial.class) 
        || isAttributeEnabled(wss11, MustSupportRefIssuerSerial.class);
    }

    public static boolean isMustSupportRefKeyIdentifier(Binding b, WSDLModel model) {
        Wss11 wss11 = getWss11(b, model);
        Wss10 wss10 = getWss10(b, model);
        return isAttributeEnabled(wss10, MustSupportRefKeyIdentifier.class) 
        || isAttributeEnabled(wss11, MustSupportRefKeyIdentifier.class);
    }

    // ----------- WSS11 ONLY ELEMENTS -----------
    public static boolean isMustSupportRefEncryptedKey(Binding b, WSDLModel model) {
        Wss11 wss11 = getWss11(b, model);
        return isAttributeEnabled(wss11, MustSupportRefEncryptedKey.class);
    }

    public static boolean isMustSupportRefThumbprint(Binding b, WSDLModel model) {
        Wss11 wss11 = getWss11(b, model);
        return isAttributeEnabled(wss11, MustSupportRefThumbprint.class);
    }

    public static boolean isRequireSignatureConfirmation(Binding b, WSDLModel model) {
        Wss11 wss11 = getWss11(b, model);
        return isAttributeEnabled(wss11, RequireSignatureConfirmation.class);
    }

    // -------- TRUST ELEMENTS -----------
    public static boolean isRequireServerEntropy(Binding b, WSDLModel model) {
        Trust10 trust = getTrust10(b, model);
        return isAttributeEnabled(trust, RequireServerEntropy.class);
    }
    
    public static boolean isRequireClientEntropy(Binding b, WSDLModel model) {
        Trust10 trust = getTrust10(b, model);
        return isAttributeEnabled(trust, RequireClientEntropy.class);
    }
    
    public static boolean isMustSupportIssuedTokens(Binding b, WSDLModel model) {
        Trust10 trust = getTrust10(b, model);
        return isAttributeEnabled(trust, MustSupportIssuedTokens.class);
    }
    
    public static boolean isMustSupportClientChallenge(Binding b, WSDLModel model) {
        Trust10 trust = getTrust10(b, model);
        return isAttributeEnabled(trust, MustSupportClientChallenge.class);
    }

    public static boolean isMustSupportServerChallenge(Binding b, WSDLModel model) {
        Trust10 trust = getTrust10(b, model);
        return isAttributeEnabled(trust, MustSupportServerChallenge.class);
    }

    /* Used to get values of attributes defined in WSS10/WSS11/TRUST10 assertions, for tokens, ...
     * first retrieves the Policy element and then element of class a underneath
     */
    public static boolean isAttributeEnabled(ExtensibilityElement element, Class a) {
        if (element != null) {
            List<Policy> policies = element.getExtensibilityElements(Policy.class);
            if ((policies != null) && !(policies.isEmpty())) {
                Policy p = policies.get(0);
                if (p != null) {
                    List l = p.getExtensibilityElements(a);
                    if ((l != null) && !(l.isEmpty())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void enableIncludeTimestamp(WSDLComponent secBinding, boolean enable) {
        if (enable) {
            enableAttribute(secBinding,
                SecurityPolicyQName.INCLUDETIMESTAMP.getQName(), 
                IncludeTimestamp.class);
        } else {
            disableAttribute(secBinding, IncludeTimestamp.class);
        }
    }
    
    public static void enableEncryptSignature(Binding b, WSDLModel model, WSDLComponent secBinding, boolean enable) {
        if (enable) {
            enableAttribute(secBinding,
                SecurityPolicyQName.ENCRYPTSIGNATURE.getQName(), 
                EncryptSignature.class);
        } else {
            disableAttribute(secBinding, EncryptSignature.class);
        }
    }

    public static void enableSignEntireHeadersAndBody(Binding b, WSDLModel model, WSDLComponent secBinding, boolean enable) {
        if (enable) {
            enableAttribute(secBinding,
                SecurityPolicyQName.ONLYSIGNENTIREHEADERSANDBODY.getQName(), 
                OnlySignEntireHeadersAndBody.class);
        } else {
            disableAttribute(secBinding, OnlySignEntireHeadersAndBody.class);
        }
    }

    public static void enableEncryptBeforeSigning(Binding b, WSDLModel model, WSDLComponent secBinding, boolean enable) {
        if (enable) {
            enableAttribute(secBinding,
                SecurityPolicyQName.ENCRYPTBEFORESIGNING.getQName(), 
                EncryptBeforeSigning.class);
        } else {
            disableAttribute(secBinding, EncryptBeforeSigning.class);
        }
    }

    public static void enableMustSupportRefEmbeddedToken(Binding b, WSDLModel model, boolean wss11, boolean enable) {
        WssElement wss = null; 
        if (wss11) {
            wss = getWss11(b, model);
        } else {
            wss = getWss10(b, model);
        }
        if (enable) {
            enableAttribute(wss,
                SecurityPolicyQName.MUSTSUPPORTREFEMBEDDEDTOKEN.getQName(), 
                MustSupportRefEmbeddedToken.class);
        } else {
            disableAttribute(wss, MustSupportRefEmbeddedToken.class);
        }
    }
    
    public static void enableMustSupportRefExternalURI(Binding b, WSDLModel model, boolean wss11, boolean enable) {
        WssElement wss = null; 
        if (wss11) {
            wss = getWss11(b, model);
        } else {
            wss = getWss10(b, model);
        }
        if (enable) {
            enableAttribute(wss, 
                SecurityPolicyQName.MUSTSUPPORTREFEXTERNALURI.getQName(), 
                MustSupportRefExternalURI.class);
        } else {
            disableAttribute(wss, MustSupportRefExternalURI.class);
        }
    }
    
    public static void enableMustSupportRefIssuerSerial(Binding b, WSDLModel model, boolean wss11, boolean enable) {
        WssElement wss = null; 
        if (wss11) {
            wss = getWss11(b, model);
        } else {
            wss = getWss10(b, model);
        }
        if (enable) {
            enableAttribute(wss, 
                SecurityPolicyQName.MUSTSUPPORTREFISSUERSERIAL.getQName(), 
                MustSupportRefIssuerSerial.class);
        } else {
            disableAttribute(wss, MustSupportRefIssuerSerial.class);
        }
    }    
    
    public static void enableMustSupportRefKeyIdentifier(Binding b, WSDLModel model, boolean wss11, boolean enable) {
        WssElement wss = null; 
        if (wss11) {
            wss = getWss11(b, model);
        } else {
            wss = getWss10(b, model);
        }
        if (enable) {
            enableAttribute(wss, 
                SecurityPolicyQName.MUSTSUPPORTREFKEYIDENTIFIER.getQName(), 
                MustSupportRefKeyIdentifier.class);
        } else {
            disableAttribute(wss, MustSupportRefKeyIdentifier.class);
        }
    }

    public static void enableRequireDerivedKeys(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            enableAttribute(tokenType, 
                TokensQName.REQUIREDERIVEDKEYS.getQName(), 
                RequireDerivedKeys.class);
        } else {
            disableAttribute(tokenType, RequireDerivedKeys.class);
        }
    }

    public static void enableRequireExternalUri(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            enableAttribute(tokenType, 
                TokensQName.REQUIREEXTERNALURIREFERENCE.getQName(), 
                RequireExternalUriReference.class);
        } else {
            disableAttribute(tokenType, RequireExternalUriReference.class);
        }
    }

    public static void enableRequireKeyIdentifierReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            enableAttribute(tokenType, 
                TokensQName.REQUIREKEYIDENTIFIERREFERENCE.getQName(), 
                RequireKeyIdentifierReference.class);
        } else {
            disableAttribute(tokenType, RequireKeyIdentifierReference.class);
        }
    }
    
    public static void enableRequireSecurityContextToken(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            enableAttribute(tokenType, 
                TokensQName.SC10SECURITYCONTEXTTOKEN.getQName(), 
                SC10SecurityContextToken.class);
        } else {
            disableAttribute(tokenType, SC10SecurityContextToken.class);
        }
    }

    public static void enableRequireIssuerSerialReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            enableAttribute(tokenType, 
                TokensQName.REQUIREISSUERSERIALREFERENCE.getQName(), 
                RequireIssuerSerialReference.class);
        } else {
            disableAttribute(tokenType, RequireIssuerSerialReference.class);
        }
    }

    public static void enableRequireEmbeddedTokenReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            enableAttribute(tokenType, 
                TokensQName.REQUIREEMBEDDEDTOKENREFERENCE.getQName(),
                RequireEmbeddedTokenReference.class);
        } else {
            disableAttribute(tokenType, RequireEmbeddedTokenReference.class);
        }
    }

    public static void enableRequireThumbprintReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            enableAttribute(tokenType, 
                TokensQName.REQUIRETHUMBPRINTREFERENCE.getQName(), 
                RequireThumbprintReference.class);
        } else {
            disableAttribute(tokenType, RequireThumbprintReference.class);
        }
    }
    
    public static void enableRequireExternalReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            enableAttribute(tokenType, 
                TokensQName.REQUIREEXTERNALREFERENCE.getQName(), 
                RequireExternalReference.class);
        } else {
            disableAttribute(tokenType, RequireExternalReference.class);
        }
    }

    public static void enableRequireInternalReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            enableAttribute(tokenType, 
                TokensQName.REQUIREINTERNALREFERENCE.getQName(), 
                RequireInternalReference.class);
        } else {
            disableAttribute(tokenType, RequireInternalReference.class);
        }
    }

    public static void enableMustSupportRefEncryptedKey(Binding b, WSDLModel model, boolean enable) {
        Wss11 wss = getWss11(b, model);
        if (enable) {
            enableAttribute(wss, 
                SecurityPolicyQName.MUSTSUPPORTREFENCRYPTEDKEY.getQName(), 
                MustSupportRefEncryptedKey.class);
        } else {
            disableAttribute(wss, MustSupportRefEncryptedKey.class);
        }
    }

    public static void enableMustSupportRefThumbprint(Binding b, WSDLModel model, boolean enable) {
        Wss11 wss = getWss11(b, model);
        if (enable) {
            enableAttribute(wss, 
                SecurityPolicyQName.MUSTSUPPORTREFTHUMBPRINT.getQName(), 
                MustSupportRefThumbprint.class);
        } else {
            disableAttribute(wss, MustSupportRefThumbprint.class);
        }
    }
    
    public static void enableRequireSignatureConfirmation(Binding b, WSDLModel model, boolean enable) {
        Wss11 wss = getWss11(b, model);
        if (enable) {
            enableAttribute(wss, 
                SecurityPolicyQName.REQUIRESIGNATURECONFIRMATION.getQName(), 
                RequireSignatureConfirmation.class);
        } else {
            disableAttribute(wss, RequireSignatureConfirmation.class);
        }
    }

    // ----------- TRUST -------------------
    public static void enableRequireClientEntropy(Binding b, WSDLModel model, boolean enable) {
        Trust10 trust = getTrust10(b, model);
        if (trust == null) {
            enableTrust10(b, model);
            trust = getTrust10(b, model);
        }
        if (enable) {
            enableAttribute(trust, 
                SecurityPolicyQName.REQUIRECLIENTENTROPY.getQName(), 
                RequireClientEntropy.class);
        } else {
            disableAttribute(trust, RequireClientEntropy.class);
        }
    }
    
    public static void enableRequireServerEntropy(Binding b, WSDLModel model, boolean enable) {
        Trust10 trust = getTrust10(b, model);
        if (trust == null) {
            enableTrust10(b, model);
            trust = getTrust10(b, model);
        }
        if (enable) {
            enableAttribute(trust, 
                SecurityPolicyQName.REQUIRESERVERENTROPY.getQName(), 
                RequireServerEntropy.class);
        } else {
            disableAttribute(trust, RequireServerEntropy.class);
        }
    }

    public static void enableMustSupportIssuedTokens(Binding b, WSDLModel model, boolean enable) {
        Trust10 trust = getTrust10(b, model);
        if (trust == null) {
            enableTrust10(b, model);
            trust = getTrust10(b, model);
        }
        if (enable) {
            enableAttribute(trust, 
                SecurityPolicyQName.MUSTSUPPORTISSUEDTOKENS.getQName(), 
                MustSupportIssuedTokens.class);
        } else {
            disableAttribute(trust, MustSupportIssuedTokens.class);
        }
    }

    public static void enableMustSupportClientChallenge(Binding b, WSDLModel model, boolean enable) {
        Trust10 trust = getTrust10(b, model);
        if (trust == null) {
            enableTrust10(b, model);
            trust = getTrust10(b, model);
        }
        if (enable) {
            enableAttribute(trust, 
                SecurityPolicyQName.MUSTSUPPORTCLIENTCHALLENGE.getQName(), 
                MustSupportClientChallenge.class);
        } else {
            disableAttribute(trust, MustSupportClientChallenge.class);
        }
    }
    
    public static void enableMustSupportServerChallenge(Binding b, WSDLModel model, boolean enable) {
        Trust10 trust = getTrust10(b, model);
        if (trust == null) {
            enableTrust10(b, model);
            trust = getTrust10(b, model);
        }
        if (enable) {
            enableAttribute(trust, 
                SecurityPolicyQName.MUSTSUPPORTSERVERCHALLENGE.getQName(), 
                MustSupportServerChallenge.class);
        } else {
            disableAttribute(trust, MustSupportServerChallenge.class);
        }
    }

    /* Used to enable attributes (empty tags) on some elements 
     */
    public static void enableAttribute(WSDLComponent element, QName attrQname, Class attributeClass) {
        if (element != null) {
            WSDLModel model = element.getModel();
            WSDLComponentFactory wcf = model.getFactory();
            
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }

            try {
                List<Policy> policies = element.getExtensibilityElements(Policy.class);
                Policy p = null;
                if ((policies == null) || (policies.isEmpty())) {
                    p = (Policy)wcf.create(element, PolicyQName.POLICY.getQName());
                    element.addExtensibilityElement(p);
                } else {
                    p = policies.get(0);
                }

                List elems = p.getExtensibilityElements(attributeClass);
                if ((elems == null) || (elems.isEmpty())) {
                    WSDLComponent token = wcf.create(p, attrQname);
                    p.addExtensibilityElement((ExtensibilityElement) token);
                }
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }
    
    /* Used to disable attributes on an element
     */
    public static void disableAttribute(WSDLComponent element, Class attributeElement) {
        if (element != null) {
            WSDLModel model = element.getModel();
            WSDLComponentFactory wcf = model.getFactory();

            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                List<Policy> policies = element.getExtensibilityElements(Policy.class);
                Policy p = null;
                if ((policies != null) && (!policies.isEmpty())) {
                    p = policies.get(0);
                    List l = p.getExtensibilityElements(attributeElement);
                    if ((l != null) && (!l.isEmpty())) {
                        ExtensibilityElement tok = (ExtensibilityElement)l.get(0);
                        tok.getParent().removeExtensibilityElement(tok);
                    }
                }
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }
        
    /*************** SIGN ENCRYPT TARGETS PARTS *******************/
    public static Vector<MessageListElement> getSignedTargets(Object msg, WSDLModel model) {
        Vector elements = new Vector();
        Policy p = null;
        if (msg instanceof InputMessage) {
            p = Utilities.getPolicyForElement(((InputMessage)msg).getInputMsg(), model);
        } else if (msg instanceof OutputMessage) {
            p = Utilities.getPolicyForElement(((OutputMessage)msg).getOutputMsg(), model);
        } else if (msg instanceof FaultMessage) {
            p = Utilities.getPolicyForElement(((FaultMessage)msg).getFaultMsg(), model);
        }
        if (p == null) {
            return elements;
        }
        
        List<Body> bodies = Collections.EMPTY_LIST;
        List<Header> headers = Collections.EMPTY_LIST;
        SignedParts signedParts = (SignedParts)PolicyModelHelper.getTopLevelElement(p, SignedParts.class);
        if (signedParts != null) {
            bodies = signedParts.getExtensibilityElements(Body.class);
            headers = signedParts.getExtensibilityElements(Header.class);
        }

        List<XPath> xpaths = Collections.EMPTY_LIST;
        SignedElements signedElements = (SignedElements)PolicyModelHelper.getTopLevelElement(p, SignedElements.class);
        if (signedElements != null) {
            xpaths = signedElements.getExtensibilityElements(XPath.class);
        }

        if ((bodies != null) && (!bodies.isEmpty())) {
            elements.add(new MessageBody(MessageBody.BODY));
        }
        
        for (Header h : headers) {
            MessageHeader headerStr = getListModelForHeader(h);
            if (headerStr != null) {
                elements.add(headerStr);
            }
        }

        for (XPath x : xpaths) {
            MessageElement e = getListModelForXPath(x);
            if (e != null) {
                elements.add(e);
            }
        }

        return elements;
    }

    public static Vector<MessageListElement> getEncryptedTargets(Object msg, WSDLModel model) {
        Vector elements = new Vector();
        Policy p = null;
        if (msg instanceof InputMessage) {
            p = Utilities.getPolicyForElement(((InputMessage)msg).getInputMsg(), model);
        } else if (msg instanceof OutputMessage) {
            p = Utilities.getPolicyForElement(((OutputMessage)msg).getOutputMsg(), model);
        } else if (msg instanceof FaultMessage) {
            p = Utilities.getPolicyForElement(((FaultMessage)msg).getFaultMsg(), model);
        }
        if (p == null) {
            return elements;
        }

        List<Body> bodies = Collections.EMPTY_LIST;
        List<Header> headers = Collections.EMPTY_LIST;
        EncryptedParts encryptedParts = (EncryptedParts)PolicyModelHelper.getTopLevelElement(p, EncryptedParts.class);
        if (encryptedParts != null) {
            bodies = encryptedParts.getExtensibilityElements(Body.class);
            headers = encryptedParts.getExtensibilityElements(Header.class);
        }

        List<XPath> xpaths = Collections.EMPTY_LIST;
        EncryptedElements encryptedElements = (EncryptedElements)PolicyModelHelper.getTopLevelElement(p, EncryptedElements.class);
        if (encryptedElements != null) {
            xpaths = encryptedElements.getExtensibilityElements(XPath.class);
        }

        if ((bodies != null) && (!bodies.isEmpty())) {
            elements.add(new MessageBody(MessageBody.BODY));
        }
        for (Header h : headers) {
            MessageHeader headerStr = getListModelForHeader(h);
            if (headerStr != null) {
                elements.add(headerStr);
            }
        }
        
        for (XPath x : xpaths) {
            MessageElement e = getListModelForXPath(x);
            if (e != null) {
                elements.add(e);
            }
        }        
        return elements;
    }
    
    public static void setEncryptedTargets(Binding b, WSDLComponent o, WSDLModel model, Vector<MessageListElement> elements) {

        if (elements == null) {
            Utilities.removePolicyForElement(o);
            return;
        }

        Policy p = Utilities.getPolicyForElement((WSDLComponent) o, model);
        EncryptedParts encryptedParts = (EncryptedParts) PolicyModelHelper.getTopLevelElement(p, EncryptedParts.class);
        EncryptedElements encryptedElements = (EncryptedElements) PolicyModelHelper.getTopLevelElement(p, EncryptedElements.class);
        WSDLComponentFactory wcf = model.getFactory();
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            All all = null;
            if (encryptedParts != null) {
                all = (All) encryptedParts.getParent();
                all.removeExtensibilityElement(encryptedParts);
            } 
            if (encryptedElements != null) {
                all = (All) encryptedElements.getParent();
                all.removeExtensibilityElement(encryptedElements);
            } 

            if (p == null) {
                all = PolicyModelHelper.createMessageLevelPolicy(b, o, model, wcf);
            } else {
                all = PolicyModelHelper.createTopExactlyOne(p, model, wcf);
            }

            encryptedParts = (EncryptedParts)wcf.create(all, SecurityPolicyQName.ENCRYPTEDPARTS.getQName());
            encryptedElements = (EncryptedElements)wcf.create(all, SecurityPolicyQName.ENCRYPTEDELEMENTS.getQName());
            all.addExtensibilityElement(encryptedParts);
            all.addExtensibilityElement(encryptedElements);

            for (MessageListElement e : elements) {
                if (e instanceof MessageHeader) {
                    addHeaderElementForListItem(e.toString(), encryptedParts, wcf);
                } else if (e instanceof MessageElement) {
                    addElementForListItem(e.toString(), encryptedElements, wcf);
                } else if (e instanceof MessageBody) {
                    addBodyForListItem(e.toString(), encryptedParts, wcf);
                }
            }
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setSignedTargets(Binding b, WSDLComponent o, WSDLModel model, Vector<MessageListElement> elements) {

        if (elements == null) {
            Utilities.removePolicyForElement(o);
            return;
        }

        Policy p = Utilities.getPolicyForElement(o, model);
        SignedParts signedParts = (SignedParts) PolicyModelHelper.getTopLevelElement(p, SignedParts.class);
        SignedElements signedElements = (SignedElements) PolicyModelHelper.getTopLevelElement(p, SignedElements.class);
        WSDLComponentFactory wcf = model.getFactory();
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            All all = null;
            if (signedParts != null) {
                all = (All) signedParts.getParent();
                all.removeExtensibilityElement(signedParts);
            } 
            if (signedElements != null) {
                all = (All) signedElements.getParent();
                all.removeExtensibilityElement(signedElements);
            } 

            if (p == null) {
                all = PolicyModelHelper.createMessageLevelPolicy(b, o, model, wcf);
            } else {
                all = PolicyModelHelper.createTopExactlyOne(p, model, wcf);
            }

            signedParts = (SignedParts)wcf.create(all, SecurityPolicyQName.SIGNEDPARTS.getQName());
            signedElements = (SignedElements)wcf.create(all, SecurityPolicyQName.SIGNEDELEMENTS.getQName());
            all.addExtensibilityElement(signedParts);
            all.addExtensibilityElement(signedElements);

            for (MessageListElement e : elements) {
                if (e instanceof MessageHeader) {
                    addHeaderElementForListItem(e.toString(), signedParts, wcf);
                } else if (e instanceof MessageElement) {
                    addElementForListItem(e.toString(), signedElements, wcf);
                } else if (e instanceof MessageBody) {
                    addBodyForListItem(e.toString(), signedParts, wcf);
                }
            }
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
    
    public static MessageHeader getListModelForHeader(Header h) {
        String name = h.getName();
        if ("To".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_TO);               //NOI18N
        if ("From".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_FROM);           //NOI18N
        if ("FaultTo".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_FAULTTO);     //NOI18N
        if ("ReplyTo".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_REPLYTO);     //NOI18N
        if ("MessageId".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_MESSAGEID); //NOI18N
        if ("RelatesTo".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_RELATESTO); //NOI18N
        if ("Action".equals(name)) return new MessageHeader(MessageHeader.ADDRESSING_ACTION);       //NOI18N
        if ("AckRequested".equals(name)) return new MessageHeader(MessageHeader.RM_ACKREQUESTED);   //NOI18N
        if ("SequenceAcknowledgement".equals(name)) return new MessageHeader(MessageHeader.RM_SEQUENCEACK);   //NOI18N
        if ("Sequence".equals(name)) return new MessageHeader(MessageHeader.RM_SEQUENCE);           //NOI18N
        return null;
    }

    public static MessageElement getListModelForXPath(XPath x) {
        String xpath = x.getXPath();
        return new MessageElement(xpath);
    }
    
    public static ExtensibilityElement addHeaderElementForListItem(String item, WSDLComponent c, WSDLComponentFactory wcf) {
        Header h = null;
        h = (Header)wcf.create(c, SecurityPolicyQName.HEADER.getQName());
        if (MessageHeader.ADDRESSING_TO.equals(item)) {
            h.setName("To");        //NOI18N
            h.setNamespace(AddressingQName.ADDRESSING_NS_URI);
        }
        if (MessageHeader.ADDRESSING_FROM.equals(item)) {
            h.setName("From");      //NOI18N
            h.setNamespace(AddressingQName.ADDRESSING_NS_URI);
        }
        if (MessageHeader.ADDRESSING_FAULTTO.equals(item)) {
            h.setName("FaultTo");      //NOI18N
            h.setNamespace(AddressingQName.ADDRESSING_NS_URI);
        }
        if (MessageHeader.ADDRESSING_REPLYTO.equals(item)) {
            h.setName("ReplyTo");   //NOI18N
            h.setNamespace(AddressingQName.ADDRESSING_NS_URI);
        }
        if (MessageHeader.ADDRESSING_MESSAGEID.equals(item)) {
            h.setName("MessageId"); //NOI18N
            h.setNamespace(AddressingQName.ADDRESSING_NS_URI);
        }
        if (MessageHeader.ADDRESSING_RELATESTO.equals(item)) {
            h.setName("RelatesTo"); //NOI18N
            h.setNamespace(AddressingQName.ADDRESSING_NS_URI);
        }
        if (MessageHeader.ADDRESSING_ACTION.equals(item)) {
            h.setName("Action");    //NOI18N
            h.setNamespace(AddressingQName.ADDRESSING_NS_URI);
        }
        if (MessageHeader.RM_ACKREQUESTED.equals(item)) {
            h.setName("AckRequested");  //NOI18N
            h.setNamespace(RMQName.RM_NS_URI);
        }
        if (MessageHeader.RM_SEQUENCEACK.equals(item)) {
            h.setName("SequenceAcknowledgement");   //NOI18N
            h.setNamespace(RMQName.RM_NS_URI);
        }
        if (MessageHeader.RM_SEQUENCE.equals(item)) {
            h.setName("Sequence");  //NOI18N
            h.setNamespace(RMQName.RM_NS_URI);
        }
        if (h != null) {
            c.addExtensibilityElement(h);
        }
        return h;
    }
    
    public static ExtensibilityElement addElementForListItem(String item, WSDLComponent c, WSDLComponentFactory wcf) {
        XPath x = null;
        x = (XPath)wcf.create(c, SecurityPolicyQName.XPATH.getQName());
        x.setXPath(item);
        c.addExtensibilityElement(x);
        return x;
    }    

    public static ExtensibilityElement addBodyForListItem(String item, WSDLComponent c, WSDLComponentFactory wcf) {
        Body b = null;
        b = (Body)wcf.create(c, SecurityPolicyQName.BODY.getQName());
        c.addExtensibilityElement(b);
        return b;
    }    

    /**************************** SECURITY BINDING TYPE *********************/
    
    public static String getSecurityBindingType(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        ExtensibilityElement e = getSecurityBindingTypeElement(b, model);
        if (e instanceof SymmetricBinding) return SecurityBindingTokensPanel.SYMMETRIC;
        if (e instanceof AsymmetricBinding) return SecurityBindingTokensPanel.ASYMMETRIC;
        if (e instanceof TransportBinding) return SecurityBindingTokensPanel.TRANSPORT;
        return SecurityBindingTokensPanel.NOSECURITY;
    }

    public static ExtensibilityElement getSecurityBindingTypeElement(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);

        SymmetricBinding sb = (SymmetricBinding)PolicyModelHelper.getTopLevelElement(p, SymmetricBinding.class);
        if (sb != null) return sb;
        
        AsymmetricBinding ab = (AsymmetricBinding)PolicyModelHelper.getTopLevelElement(p, AsymmetricBinding.class);
        if (ab != null) return ab;
        
        TransportBinding tb = (TransportBinding)PolicyModelHelper.getTopLevelElement(p, TransportBinding.class);
        if (tb != null) return tb;

        return null;
    }

    public static String getSecurityBindingType(WSDLComponent c) {
        WSDLModel model = c.getModel();
        ExtensibilityElement e = getSecurityBindingTypeElement(c);
        if (e instanceof SymmetricBinding) return SecurityBindingTokensPanel.SYMMETRIC;
        if (e instanceof AsymmetricBinding) return SecurityBindingTokensPanel.ASYMMETRIC;
        if (e instanceof TransportBinding) return SecurityBindingTokensPanel.TRANSPORT;
        return SecurityBindingTokensPanel.NOSECURITY;
    }

    public static ExtensibilityElement getSecurityBindingTypeElement(WSDLComponent c) {
        WSDLModel model = c.getModel();
        
        List<Policy> policies = c.getExtensibilityElements(Policy.class);
        if ((policies != null) && !(policies.isEmpty())) {
            Policy p = policies.get(0);
            if (p != null) {
                if (isAttributeEnabled((ExtensibilityElement) c, BootstrapPolicy.class)) {
                    List l = p.getExtensibilityElements(BootstrapPolicy.class);
                    if ((l != null) && !(l.isEmpty())) {
                        BootstrapPolicy b = (BootstrapPolicy) l.get(0);
                        policies = b.getExtensibilityElements(Policy.class);
                        if ((policies != null) && !(policies.isEmpty())) {
                            p = policies.get(0);
                        }
                    }
                }
                if (p != null) {
                    List l = p.getExtensibilityElements(SymmetricBinding.class);
                    if ((l != null) && !(l.isEmpty())) {
                        return (ExtensibilityElement) l.get(0);
                    }
                    l = p.getExtensibilityElements(AsymmetricBinding.class);
                    if ((l != null) && !(l.isEmpty())) {
                        return (ExtensibilityElement) l.get(0);
                    }
                    l = p.getExtensibilityElements(TransportBinding.class);
                    if ((l != null) && !(l.isEmpty())) {
                        return (ExtensibilityElement) l.get(0);
                    }
                }
            }
        }
        return null;
    }

    public static WSDLComponent setSecurityBindingType(Binding b, WSDLModel model, String bindingType) {
        Policy p = Utilities.getPolicyForElement(b, model);
        WSDLComponentFactory wcf = model.getFactory();
        WSDLComponent secBindingType = null;
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            SymmetricBinding sb = (SymmetricBinding)PolicyModelHelper.getTopLevelElement(p, SymmetricBinding.class);
            AsymmetricBinding ab = (AsymmetricBinding)PolicyModelHelper.getTopLevelElement(p, AsymmetricBinding.class);
            TransportBinding tb = (TransportBinding)PolicyModelHelper.getTopLevelElement(p, TransportBinding.class);

            if (sb != null) {
                if (SecurityBindingTokensPanel.SYMMETRIC.equals(bindingType)) {
                    return sb;
                }
                sb.getParent().removeExtensibilityElement(sb);
            }
            if (ab != null) {
                if (SecurityBindingTokensPanel.ASYMMETRIC.equals(bindingType)) {
                    return ab;
                }
                ab.getParent().removeExtensibilityElement(ab);
            }
            if (tb != null) {
                if (SecurityBindingTokensPanel.TRANSPORT.equals(bindingType)) {
                    return tb;
                }
                tb.getParent().removeExtensibilityElement(tb);
            }

            All all = null;
            if (p == null) {
                all = PolicyModelHelper.createTopLevelPolicy(b, model, wcf);
            } else {
                all = PolicyModelHelper.createTopExactlyOne(p, model, wcf);
            }

            secBindingType = setSecurityBindingType(all, bindingType, b);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
        return secBindingType;
    }

    /* Binding may be null - if it's not, default values for encrypted/signed targets may be set
     */
    public static WSDLComponent setSecurityBindingType(WSDLComponent c, String bindingType, Binding b) {
        
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        WSDLComponent secBindingType = null;
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {    
            if (SecurityBindingTokensPanel.SYMMETRIC.equals(bindingType)) {
                // Create symmetric binding and set some default values in it
                SymmetricBinding sb = (SymmetricBinding)wcf.create(c, SecurityPolicyQName.SYMMETRICBINDING.getQName());
                c.addExtensibilityElement(sb);

                SecurityTokensModelHelper.setTokenType(sb, TokensSymmetricPanel.PROTECTION, TokensSymmetricPanel.X509);

                AlgoSuiteModelHelper.setAlgorithmSuite(sb, SecurityBindingOtherPanel.BASIC128);
                setLayout(sb, SecurityBindingOtherPanel.STRICT);
                enableIncludeTimestamp(sb, true);

                if (!isWss10(c) && (!isWss11(c))) {
                    enableWss10(c);
                    if (b != null) {
                        setDefaultTargets(b, false);
                    }
                }
                if (!isTrust10(c)) {
                    enableTrust10(c);
                }

                secBindingType = sb;

            } else if (SecurityBindingTokensPanel.ASYMMETRIC.equals(bindingType)) {
                AsymmetricBinding ab = (AsymmetricBinding)wcf.create(c, SecurityPolicyQName.ASYMMETRICBINDING.getQName());
                c.addExtensibilityElement(ab);

                SecurityTokensModelHelper.setTokenType(ab, TokensSymmetricPanel.INITIATOR, TokensSymmetricPanel.X509);
                SecurityTokensModelHelper.setTokenType(ab, TokensSymmetricPanel.RECIPIENT, TokensSymmetricPanel.X509);

                AlgoSuiteModelHelper.setAlgorithmSuite(ab, SecurityBindingOtherPanel.BASIC128);
                setLayout(ab, SecurityBindingOtherPanel.STRICT);
                enableIncludeTimestamp(ab, true);

                if (!isWss10(c) && (!isWss11(c))) {
                    enableWss10(c);
                    if (b != null) {
                        setDefaultTargets(b, false);
                    }
                }
                if (!isTrust10(c)) {
                    enableTrust10(c);
                }

                secBindingType = ab;

            } else if (SecurityBindingTokensPanel.TRANSPORT.equals(bindingType)) {
                TransportBinding tb = (TransportBinding)wcf.create(c, SecurityPolicyQName.TRANSPORTBINDING.getQName());
                c.addExtensibilityElement(tb);

                Policy p = (Policy)wcf.create(tb, PolicyQName.POLICY.getQName());
                tb.setPolicy(p);
                TransportToken tt = (TransportToken)wcf.create(p, TokensQName.TRANSPORTTOKEN.getQName());
                p.addExtensibilityElement(tt);
                p = (Policy)wcf.create(tt, PolicyQName.POLICY.getQName());
                tt.setPolicy(p);
                HttpsToken ht = (HttpsToken)wcf.create(p, TokensQName.HTTPSTOKEN.getQName());
                ht.setRequireClientCertificate(false);
                p.addExtensibilityElement(ht);

                setLayout(tb, SecurityBindingOtherPanel.STRICT);
                AlgoSuiteModelHelper.setAlgorithmSuite(tb, SecurityBindingOtherPanel.BASIC128);
                enableIncludeTimestamp(tb, true);

                removeTargets(b);

                if (isWss10(c)) {
                    disableWss(c, false);
                }
                if (isWss11(c)) {
                    disableWss(c, true);
                }
                if (isTrust10(c)) {
                    disableTrust10(c);
                }
                secBindingType = tb;

            } else if (SecurityBindingTokensPanel.NOSECURITY.equals(bindingType)) {
                if (isWss10(c)) {
                    disableWss(c, false);
                }
                if (isWss11(c)) {
                    disableWss(c, true);
                }
                if (isTrust10(c)) {
                    disableTrust10(c);
                }
                removeTargets(b);
            }        
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
        return secBindingType;
    }
    
    public static void setDefaultTargets(Binding b, boolean wss11) {
        
        WSDLModel model = b.getModel();
        Collection<BindingOperation> operations = b.getBindingOperations();

        Vector<MessageListElement> signedElems = new Vector();
        Vector<MessageListElement> encElems = new Vector();

        MessageBody body = new MessageBody(MessageBody.BODY);
        signedElems.add(body);
        encElems.add(body);

        if (wss11) {
            for (String s : MessageHeader.ALL_HEADERS) {
                MessageHeader h = new MessageHeader(s);
                signedElems.add(h);
                encElems.add(h);
            }
        }
        
        for (BindingOperation o : operations) {
            BindingInput input = o.getBindingInput();
            if (input != null) {
                setEncryptedTargets(b, input, model, encElems);
                setSignedTargets(b, input, model, signedElems);
            }
            BindingOutput output = o.getBindingOutput();
            if (output != null) {
                setEncryptedTargets(b, output, model, encElems);
                setSignedTargets(b, output, model, signedElems);
            }
            Collection<BindingFault> faults = o.getBindingFaults();
            for (BindingFault f : faults) {
                if (f != null) {
                    setEncryptedTargets(b, f, model, encElems);
                    setSignedTargets(b, f, model, signedElems);
                }
            }
        }
    }

    public static void removeTargets(Binding b) {
        
        WSDLModel model = b.getModel();
        Collection<BindingOperation> operations = b.getBindingOperations();

        for (BindingOperation o : operations) {
            BindingInput input = o.getBindingInput();
            if (input != null) {
                setEncryptedTargets(b, input, model, null);
                setSignedTargets(b, input, model, null);
            }
            BindingOutput output = o.getBindingOutput();
            if (output != null) {
                setEncryptedTargets(b, output, model, null);
                setSignedTargets(b, output, model, null);
            }
            Collection<BindingFault> faults = o.getBindingFaults();
            for (BindingFault f : faults) {
                if (f != null) {
                    setEncryptedTargets(b, f, model, null);
                    setSignedTargets(b, f, model, null);
                }
            }
        }
    }
    
    /********** Other binding attributes ****************/

    public static String getComboItemForElement(WSDLComponent wc) {
        String cName = wc.getClass().getSimpleName();
        String msg = "COMBO_" + cName.substring(0, cName.length()-4);  //NOI18N
        return NbBundle.getMessage(SecurityBindingOtherPanel.class, msg);
    }

    public static String getMessageLayout(Binding b, WSDLModel model) {
        WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(b, model);
        if (secBinding != null) {
            WSDLComponent wc = getMessageLayoutForSecurityBinding(b, model, secBinding);
            if (wc instanceof Strict) return SecurityBindingOtherPanel.STRICT;
            if (wc instanceof Lax) return SecurityBindingOtherPanel.LAX;
            if (wc instanceof LaxTsFirst) return SecurityBindingOtherPanel.LAXTSFIRST;
            if (wc instanceof LaxTsLast) return SecurityBindingOtherPanel.LAXTSLAST;
        }
        return null;
    }

    public static WSDLComponent getMessageLayoutForSecurityBinding(Binding b, WSDLModel model, WSDLComponent secBinding) {
        List<Policy> policies = secBinding.getExtensibilityElements(Policy.class);
        if ((policies != null) && !(policies.isEmpty())) {
            Policy p = policies.get(0);
            List<Layout> layouts = p.getExtensibilityElements(Layout.class);
            if ((layouts != null) && !(layouts.isEmpty())) {
                Layout ls = layouts.get(0);
                policies = ls.getExtensibilityElements(Policy.class);
                if ((policies != null) && !(policies.isEmpty())) {
                    p = policies.get(0);
                    if (p != null) {
                        List<ExtensibilityElement> elements = p.getExtensibilityElements();
                        if ((elements != null) && !(elements.isEmpty())) {
                            ExtensibilityElement e = elements.get(0);
                            return e;
                        }
                    }
                }
            }
        }
        return null;
    }    
    
    public static boolean isIncludeTimestamp(WSDLComponent c) {
        if (c != null) {
            return isAttributeEnabled((ExtensibilityElement) c, IncludeTimestamp.class);
        }
        return false;
    }

    public static boolean isIncludeTimestamp(Binding b, WSDLModel model) {
        ExtensibilityElement e = getSecurityBindingTypeElement(b, model);
        if (e != null) {
            return isAttributeEnabled(e, IncludeTimestamp.class);
        }
        return false;
    }
    
    public static boolean isEncryptBeforeSigning(Binding b, WSDLModel model) {
        ExtensibilityElement e = getSecurityBindingTypeElement(b, model);
        if (e != null) {
            return isAttributeEnabled(e, EncryptBeforeSigning.class);
        }
        return false;
    }

    public static boolean isEncryptSignature(Binding b, WSDLModel model) {
        ExtensibilityElement e = getSecurityBindingTypeElement(b, model);
        if (e != null) {
            return isAttributeEnabled(e, EncryptSignature.class);
        }
        return false;
    }
    
    public static boolean isSignEntireHeadersAndBody(Binding b, WSDLModel model) {
        ExtensibilityElement e = getSecurityBindingTypeElement(b, model);
        if (e != null) {
            return isAttributeEnabled(e, OnlySignEntireHeadersAndBody.class);
        }
        return false;
    }

   public static void setLayout(WSDLComponent c, String msgLayout) {
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();

        WSDLComponent topElem = c;

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (!(topElem instanceof Policy)) {
                List<Policy> policies = topElem.getExtensibilityElements(Policy.class);
                Policy p = null;
                if ((policies == null) || (policies.isEmpty())) {
                    p = (Policy) wcf.create(topElem, PolicyQName.POLICY.getQName());
                    topElem.addExtensibilityElement(p);
                } else {
                    p = policies.get(0);
                }
                topElem = p;
            }

            QName qnameToCreate = null;

            if (SecurityBindingOtherPanel.STRICT.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.STRICT.getQName();
            } else if (SecurityBindingOtherPanel.LAX.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.LAX.getQName();
            } else if (SecurityBindingOtherPanel.LAXTSFIRST.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.LAXTSFIRST.getQName();
            } else if (SecurityBindingOtherPanel.LAXTSLAST.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.LAXTSLAST.getQName();
            }

            List<Layout> layouts = topElem.getExtensibilityElements(Layout.class);

            Layout layout = null;
            if ((layouts == null) || (layouts.isEmpty())) {
                layout = (Layout) wcf.create(topElem, SecurityPolicyQName.LAYOUT.getQName());
                topElem.addExtensibilityElement(layout);
            } else {
                layout = layouts.get(0);
            }

            List<Policy> policies = layout.getExtensibilityElements(Policy.class);
            if ((policies != null) && (!policies.isEmpty())) {
                for (Policy pol : policies) {
                    layout.removeExtensibilityElement(pol);
                }
            }        
            Policy p = (Policy) wcf.create(layout, PolicyQName.POLICY.getQName());
            layout.setPolicy(p);
            ExtensibilityElement e = (ExtensibilityElement) wcf.create(p, qnameToCreate);
            p.addExtensibilityElement(e);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }    
    
}
