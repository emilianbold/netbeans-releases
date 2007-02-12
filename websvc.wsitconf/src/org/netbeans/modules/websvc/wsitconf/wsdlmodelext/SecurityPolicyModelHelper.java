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
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10QName;
import org.netbeans.modules.websvc.wsitmodelext.security.RequiredElements;
import org.netbeans.modules.websvc.wsitmodelext.security.TrustElement;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.*;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.KeyStore;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.TrustStore;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RequireDerivedKeys;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RequireEmbeddedTokenReference;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RequireExternalReference;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RequireExternalUriReference;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RequireInternalReference;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RequireIssuerSerialReference;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RequireKeyIdentifierReference;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RequireThumbprintReference;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SC10SecurityContextToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.TokensQName;
import org.netbeans.modules.websvc.wsitmodelext.security.AsymmetricBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.Body;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.EncryptedElements;
import org.netbeans.modules.websvc.wsitmodelext.security.EncryptedParts;
import org.netbeans.modules.websvc.wsitmodelext.security.Header;
import org.netbeans.modules.websvc.wsitmodelext.security.Lax;
import org.netbeans.modules.websvc.wsitmodelext.security.LaxTsFirst;
import org.netbeans.modules.websvc.wsitmodelext.security.LaxTsLast;
import org.netbeans.modules.websvc.wsitmodelext.security.Layout;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.SignedElements;
import org.netbeans.modules.websvc.wsitmodelext.security.SignedParts;
import org.netbeans.modules.websvc.wsitmodelext.security.Strict;
import org.netbeans.modules.websvc.wsitmodelext.security.SymmetricBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.TransportBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.Trust10;
import org.netbeans.modules.websvc.wsitmodelext.security.Wss10;
import org.netbeans.modules.websvc.wsitmodelext.security.Wss11;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.websvc.wsitmodelext.security.XPath;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.EncryptBeforeSigning;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.EncryptSignature;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.IncludeTimestamp;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportClientChallenge;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportIssuedTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportRefEmbeddedToken;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportRefEncryptedKey;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportRefExternalURI;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportRefIssuerSerial;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportRefKeyIdentifier;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportRefThumbprint;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.MustSupportServerChallenge;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.OnlySignEntireHeadersAndBody;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.RequireClientEntropy;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.RequireServerEntropy;
import org.netbeans.modules.websvc.wsitmodelext.security.parameters.RequireSignatureConfirmation;
import org.netbeans.modules.xml.wsdl.model.*;
import org.openide.util.NbBundle;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Martin Grebac
 */
public class SecurityPolicyModelHelper {
    
    /**
     * Creates a new instance of SecurityPolicyModelHelper
     */
    public SecurityPolicyModelHelper() { }

    // checks if Security is enabled in the config wsdl on specified element (Binding/Operation/Message)
    public static boolean isSecurityEnabled(WSDLComponent c) {
        WSDLModel model = c.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(c);
        if (p != null) {
            ExtensibilityElement secElem = getSecurityBindingTypeElement(c);
            return (secElem != null);
        }
        return false;
    }

    public static void disableSecurity(WSDLComponent c) {
        assert ((c instanceof Binding) || (c instanceof BindingOperation));
        WSDLModel model = c.getModel();
        setSecurityBindingType(c, null);
        SecurityTokensModelHelper.setSupportingTokens(c, null, SecurityTokensModelHelper.NONE);
        disableWss(c);
        disableTrust10(c);
        removeTargets(c);
        Policy p = PolicyModelHelper.getPolicyForElement(c);
        if (p != null) {
            KeyStore ks = PolicyModelHelper.getTopLevelElement(p, KeyStore.class);
            TrustStore ts = PolicyModelHelper.getTopLevelElement(p, TrustStore.class);
            if (ks != null) PolicyModelHelper.removeElement(ks);
            if (ts != null) PolicyModelHelper.removeElement(ts);
        }
        if (c instanceof Binding) {
            Binding b = (Binding)c;
            Collection<BindingOperation> ops = b.getBindingOperations();
            for (BindingOperation op : ops) {
                disableSecurity(op);
            }
        } else {
            BindingOperation bop = (BindingOperation)c;
            BindingInput bi = bop.getBindingInput();
            BindingOutput bo = bop.getBindingOutput();
            if (bi != null) PolicyModelHelper.removePolicyForElement(bi);
            if (bo != null) PolicyModelHelper.removePolicyForElement(bo);
        }
        PolicyModelHelper.cleanPolicies(c);
    }
    
    public static WssElement enableWss(WSDLComponent c, boolean wss11) {
    
        if ((c instanceof Binding) || 
            (c instanceof BindingOperation) || 
            (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
            c = PolicyModelHelper.createPolicy(c);
        }
        
        if (wss11) {
            if (isWss10(c)) {
                disableWss(c);
            }
            if (!isWss11(c)) {
                return PolicyModelHelper.createElement(c, SecurityPolicyQName.WSS11.getQName(), Wss11.class, false);
            } else {
                return getWss11(c);
            }
        } else {
            if (isWss11(c)) {
                disableWss(c);
            }
            if (!isWss10(c)) {
                return PolicyModelHelper.createElement(c, SecurityPolicyQName.WSS10.getQName(), Wss10.class, false);
            } else {
                return getWss10(c);
            }
        }
    }
    
    public static TrustElement enableTrust10(WSDLComponent c) {
        if ((c instanceof Binding) || 
            (c instanceof BindingOperation) || 
            (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
            c = PolicyModelHelper.createPolicy(c);
        }
        if (!isTrust10(c)) {
            return PolicyModelHelper.createElement(c, SecurityPolicyQName.TRUST10.getQName(), Trust10.class, false);
        } else {
            return getTrust10(c);
        }
    }

    // disables Wss in the config wsdl on specified binding
    public static void disableWss(WSDLComponent c) {
        WSDLModel model = c.getModel();
        if ((c instanceof Binding) || 
            (c instanceof BindingOperation) || 
            (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
            c = PolicyModelHelper.createPolicy(c);
        }
        WssElement wss10 = getWss10(c);
        WssElement wss11 = getWss11(c);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (wss10 != null) {
                wss10.getParent().removeExtensibilityElement(wss10);
            }
            if (wss11 != null) {
                wss11.getParent().removeExtensibilityElement(wss11);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    /* Disables Trust10 in the config wsdl on specified component
     */
    public static void disableTrust10(WSDLComponent c) {
        WSDLModel model = c.getModel();
        if ((c instanceof Binding) || 
            (c instanceof BindingOperation) || 
            (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
            c = PolicyModelHelper.createPolicy(c);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        Trust10 trust = getTrust10(c);
        try {
            if (trust != null) {
                trust.getParent().removeExtensibilityElement(trust);
            } 
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static boolean isWss10(WSDLComponent c) {
        return getWss10(c) != null;
    }

    public static boolean isWss11(WSDLComponent c) {
        return getWss11(c) != null;
    }

    public static boolean isTrust10(WSDLComponent c) {
        return getTrust10(c) != null;
    }

    public static Wss10 getWss10(WSDLComponent c) {
        if ((c instanceof Binding) || (c instanceof BindingOperation)) {
            c = PolicyModelHelper.getPolicyForElement(c);
        }
        return PolicyModelHelper.getTopLevelElement(c, Wss10.class);
    }

    public static Wss11 getWss11(WSDLComponent c) {
        if ((c instanceof Binding) || (c instanceof BindingOperation)) {
            c = PolicyModelHelper.getPolicyForElement(c);
        }
        return PolicyModelHelper.getTopLevelElement(c, Wss11.class);
    }
    
    public static Trust10 getTrust10(WSDLComponent c) {
        if ((c instanceof Binding) || (c instanceof BindingOperation)) {
            c = PolicyModelHelper.getPolicyForElement(c);
        }
        return PolicyModelHelper.getTopLevelElement(c, Trust10.class);
    }

    // -------- WSS10 & 11 ELEMENTS -----------
    public static boolean isMustSupportRefEmbeddedToken(WSDLComponent comp) {
        Wss11 wss11 = getWss11(comp);
        Wss10 wss10 = getWss10(comp);
        return isAttributeEnabled(wss10, MustSupportRefEmbeddedToken.class) 
        || isAttributeEnabled(wss11, MustSupportRefEmbeddedToken.class);
    }

    public static boolean isMustSupportRefExternalURI(WSDLComponent comp) {
        Wss11 wss11 = getWss11(comp);
        Wss10 wss10 = getWss10(comp);
        return isAttributeEnabled(wss10, MustSupportRefExternalURI.class) 
        || isAttributeEnabled(wss11, MustSupportRefExternalURI.class);
    }

    public static boolean isMustSupportRefIssuerSerial(WSDLComponent comp) {
        Wss11 wss11 = getWss11(comp);
        Wss10 wss10 = getWss10(comp);
        return isAttributeEnabled(wss10, MustSupportRefIssuerSerial.class) 
        || isAttributeEnabled(wss11, MustSupportRefIssuerSerial.class);
    }

    public static boolean isMustSupportRefKeyIdentifier(WSDLComponent comp) {
        Wss11 wss11 = getWss11(comp);
        Wss10 wss10 = getWss10(comp);
        return isAttributeEnabled(wss10, MustSupportRefKeyIdentifier.class) 
        || isAttributeEnabled(wss11, MustSupportRefKeyIdentifier.class);
    }

    // ----------- WSS11 ONLY ELEMENTS -----------
    public static boolean isMustSupportRefEncryptedKey(WSDLComponent comp) {
        Wss11 wss11 = getWss11(comp);
        return isAttributeEnabled(wss11, MustSupportRefEncryptedKey.class);
    }

    public static boolean isMustSupportRefThumbprint(WSDLComponent comp) {
        Wss11 wss11 = getWss11(comp);
        return isAttributeEnabled(wss11, MustSupportRefThumbprint.class);
    }

    public static boolean isRequireSignatureConfirmation(WSDLComponent comp) {
        Wss11 wss11 = getWss11(comp);
        return isAttributeEnabled(wss11, RequireSignatureConfirmation.class);
    }

    // -------- TRUST ELEMENTS -----------
    public static boolean isRequireServerEntropy(WSDLComponent comp) {
        Trust10 trust = getTrust10(comp);
        return isAttributeEnabled(trust, RequireServerEntropy.class);
    }
    
    public static boolean isRequireClientEntropy(WSDLComponent comp) {
        Trust10 trust = getTrust10(comp);
        return isAttributeEnabled(trust, RequireClientEntropy.class);
    }
    
    public static boolean isMustSupportIssuedTokens(WSDLComponent comp) {
        Trust10 trust = getTrust10(comp);
        return isAttributeEnabled(trust, MustSupportIssuedTokens.class);
    }
    
    public static boolean isMustSupportClientChallenge(WSDLComponent comp) {
        Trust10 trust = getTrust10(comp);
        return isAttributeEnabled(trust, MustSupportClientChallenge.class);
    }

    public static boolean isMustSupportServerChallenge(WSDLComponent comp) {
        Trust10 trust = getTrust10(comp);
        return isAttributeEnabled(trust, MustSupportServerChallenge.class);
    }

    /* Used to get values of attributes defined in WSS10/WSS11/TRUST10 assertions, for tokens, ...
     * first retrieves the Policy element and then element of class a underneath
     */
    public static boolean isAttributeEnabled(ExtensibilityElement element, Class a) {
        if (element != null) {
            Policy p = PolicyModelHelper.getTopLevelElement(element, Policy.class);
            return (PolicyModelHelper.getTopLevelElement(p, a) != null);
        }
        return false;
    }

    public static void enableIncludeTimestamp(WSDLComponent secBinding, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(secBinding, SecurityPolicyQName.INCLUDETIMESTAMP.getQName(), IncludeTimestamp.class, true);
        } else {
            PolicyModelHelper.removeElement(secBinding, IncludeTimestamp.class, true);
        }
    }
    
    public static void enableEncryptSignature(WSDLComponent secBinding, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(secBinding, SecurityPolicyQName.ENCRYPTSIGNATURE.getQName(), EncryptSignature.class, true);
        } else {
            PolicyModelHelper.removeElement(secBinding, EncryptSignature.class, true);
        }
    }

    public static void enableSignEntireHeadersAndBody(WSDLComponent secBinding, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(secBinding, SecurityPolicyQName.ONLYSIGNENTIREHEADERSANDBODY.getQName(), OnlySignEntireHeadersAndBody.class, true);
        } else {
            PolicyModelHelper.removeElement(secBinding, OnlySignEntireHeadersAndBody.class, true);
        }
    }

    public static void enableEncryptBeforeSigning(WSDLComponent secBinding, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(secBinding, SecurityPolicyQName.ENCRYPTBEFORESIGNING.getQName(), EncryptBeforeSigning.class, true);
        } else {
            PolicyModelHelper.removeElement(secBinding, EncryptBeforeSigning.class, true);
        }
    }

    public static void enableMustSupportRefEmbeddedToken(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(wss, SecurityPolicyQName.MUSTSUPPORTREFEMBEDDEDTOKEN.getQName(), MustSupportRefEmbeddedToken.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, MustSupportRefEmbeddedToken.class, true);
        }
    }
    
    public static void enableMustSupportRefExternalURI(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(wss,  SecurityPolicyQName.MUSTSUPPORTREFEXTERNALURI.getQName(), MustSupportRefExternalURI.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, MustSupportRefExternalURI.class, true);
        }
    }
    
    public static void enableMustSupportRefIssuerSerial(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(wss, SecurityPolicyQName.MUSTSUPPORTREFISSUERSERIAL.getQName(), MustSupportRefIssuerSerial.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, MustSupportRefIssuerSerial.class, true);
        }
    }    
    
    public static void enableMustSupportRefKeyIdentifier(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(wss, SecurityPolicyQName.MUSTSUPPORTREFKEYIDENTIFIER.getQName(), MustSupportRefKeyIdentifier.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, MustSupportRefKeyIdentifier.class, true);
        }
    }

    public static boolean isRequireDerivedKeys(WSDLComponent token) {
        return isAttributeEnabled((ExtensibilityElement) token, RequireDerivedKeys.class);
    }

    public static void enableRequireDerivedKeys(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(tokenType, TokensQName.REQUIREDERIVEDKEYS.getQName(), RequireDerivedKeys.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, RequireDerivedKeys.class, true);
        }
    }

    public static void enableRequireExternalUri(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(tokenType, TokensQName.REQUIREEXTERNALURIREFERENCE.getQName(), RequireExternalUriReference.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, RequireExternalUriReference.class, true);
        }
    }

    public static void enableRequireKeyIdentifierReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(tokenType, TokensQName.REQUIREKEYIDENTIFIERREFERENCE.getQName(), RequireKeyIdentifierReference.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, RequireKeyIdentifierReference.class, true);
        }
    }
    
    public static void enableRequireSecurityContextToken(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(tokenType, TokensQName.SC10SECURITYCONTEXTTOKEN.getQName(), SC10SecurityContextToken.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, SC10SecurityContextToken.class, true);
        }
    }

    public static void enableRequireIssuerSerialReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(tokenType, TokensQName.REQUIREISSUERSERIALREFERENCE.getQName(), RequireIssuerSerialReference.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, RequireIssuerSerialReference.class, true);
        }
    }

    public static void enableRequireEmbeddedTokenReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(tokenType, TokensQName.REQUIREEMBEDDEDTOKENREFERENCE.getQName(), RequireEmbeddedTokenReference.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, RequireEmbeddedTokenReference.class, true);
        }
    }

    public static void enableRequireThumbprintReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(tokenType, TokensQName.REQUIRETHUMBPRINTREFERENCE.getQName(), RequireThumbprintReference.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, RequireThumbprintReference.class, true);
        }
    }
    
    public static void enableRequireExternalReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(tokenType, TokensQName.REQUIREEXTERNALREFERENCE.getQName(), RequireExternalReference.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, RequireExternalReference.class, true);
        }
    }

    public static void enableRequireInternalReference(WSDLComponent tokenType, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(tokenType, TokensQName.REQUIREINTERNALREFERENCE.getQName(), RequireInternalReference.class, true);
        } else {
            PolicyModelHelper.removeElement(tokenType, RequireInternalReference.class, true);
        }
    }

    public static void enableMustSupportRefEncryptedKey(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(wss, SecurityPolicyQName.MUSTSUPPORTREFENCRYPTEDKEY.getQName(), MustSupportRefEncryptedKey.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, MustSupportRefEncryptedKey.class, true);
        }
    }

    public static void enableMustSupportRefThumbprint(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(wss, SecurityPolicyQName.MUSTSUPPORTREFTHUMBPRINT.getQName(), MustSupportRefThumbprint.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, MustSupportRefThumbprint.class, true);
        }
    }
    
    public static void enableRequireSignatureConfirmation(WssElement wss, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(wss, SecurityPolicyQName.REQUIRESIGNATURECONFIRMATION.getQName(), RequireSignatureConfirmation.class, true);
        } else {
            PolicyModelHelper.removeElement(wss, RequireSignatureConfirmation.class, true);
        }
    }

    // ----------- TRUST -------------------
    public static void enableRequireClientEntropy(TrustElement trust, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(trust, SecurityPolicyQName.REQUIRECLIENTENTROPY.getQName(), RequireClientEntropy.class, true);
        } else {
            PolicyModelHelper.removeElement(trust, RequireClientEntropy.class, true);
        }
    }
    
    public static void enableRequireServerEntropy(TrustElement trust, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(trust, SecurityPolicyQName.REQUIRESERVERENTROPY.getQName(), RequireServerEntropy.class, true);
        } else {
            PolicyModelHelper.removeElement(trust, RequireServerEntropy.class, true);
        }
    }

    public static void enableMustSupportIssuedTokens(TrustElement trust, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(trust, SecurityPolicyQName.MUSTSUPPORTISSUEDTOKENS.getQName(), MustSupportIssuedTokens.class, true);
        } else {
            PolicyModelHelper.removeElement(trust, MustSupportIssuedTokens.class, true);
        }
    }

    public static void enableMustSupportClientChallenge(TrustElement trust, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(trust, SecurityPolicyQName.MUSTSUPPORTCLIENTCHALLENGE.getQName(), MustSupportClientChallenge.class, true);
        } else {
            PolicyModelHelper.removeElement(trust, MustSupportClientChallenge.class, true);
        }
    }
    
    public static void enableMustSupportServerChallenge(TrustElement trust, boolean enable) {
        if (enable) {
            PolicyModelHelper.createElement(trust, SecurityPolicyQName.MUSTSUPPORTSERVERCHALLENGE.getQName(), MustSupportServerChallenge.class, true);
        } else {
            PolicyModelHelper.removeElement(trust, MustSupportServerChallenge.class, true);
        }
    }
        
    /*************** SIGN ENCRYPT TARGETS PARTS *******************/
    
    public static Vector<Vector> getTargets(WSDLComponent comp) {
        
        WSDLModel model = comp.getModel();
        Vector<Vector> rows = new Vector();
        
        Policy p = null;
        p = PolicyModelHelper.getPolicyForElement(comp);
        if (p == null) {
            return rows;
        }

        // ENCRYPTED PARTS FIRST
        List<Body> bodies = Collections.EMPTY_LIST;
        List<Header> headers = Collections.EMPTY_LIST;
        List<XPath> xpaths = Collections.EMPTY_LIST;
        EncryptedParts encryptedParts = (EncryptedParts)PolicyModelHelper.getTopLevelElement(p, EncryptedParts.class);
        EncryptedElements encryptedElements = (EncryptedElements)PolicyModelHelper.getTopLevelElement(p, EncryptedElements.class);
        if (encryptedParts != null) {
            bodies = encryptedParts.getExtensibilityElements(Body.class);
            headers = encryptedParts.getExtensibilityElements(Header.class);
        }
        if (encryptedElements != null) {
            xpaths = encryptedElements.getExtensibilityElements(XPath.class);
        }
        // BODY
        if ((bodies != null) && (!bodies.isEmpty())) {
            Vector columns = new Vector();
            columns.add(TargetElement.DATA, new MessageBody());
            columns.add(TargetElement.SIGN, Boolean.FALSE);
            columns.add(TargetElement.ENCRYPT, Boolean.TRUE);
            columns.add(TargetElement.REQUIRE, Boolean.FALSE);
            rows.add(columns);
        }
        // HEADERS
        for (Header h : headers) {
            MessageHeader header = getListModelForHeader(h);
            if (header != null) {
                Vector columns = new Vector();
                columns.add(TargetElement.DATA, header);
                columns.add(TargetElement.SIGN, Boolean.FALSE);
                columns.add(TargetElement.ENCRYPT, Boolean.TRUE);
                columns.add(TargetElement.REQUIRE, Boolean.FALSE);
                rows.add(columns);
            }
        }
        // XPATH ELEMENTS
        for (XPath x : xpaths) {
            MessageElement e = getListModelForXPath(x);
            if (e != null) {
                Vector columns = new Vector();
                columns.add(TargetElement.DATA, e);
                columns.add(TargetElement.SIGN, Boolean.FALSE);
                columns.add(TargetElement.ENCRYPT, Boolean.TRUE);
                columns.add(TargetElement.REQUIRE, Boolean.FALSE);
                rows.add(columns);
            }
        }
        
        SignedParts signedParts = (SignedParts)PolicyModelHelper.getTopLevelElement(p, SignedParts.class);
        SignedElements signedElements = (SignedElements)PolicyModelHelper.getTopLevelElement(p, SignedElements.class);
        if (signedParts != null) {
            bodies = signedParts.getExtensibilityElements(Body.class);
            headers = signedParts.getExtensibilityElements(Header.class);
        }
        if (signedElements != null) {
            xpaths = signedElements.getExtensibilityElements(XPath.class);
        }

        if ((bodies != null) && (!bodies.isEmpty())) {
            MessageBody body = new MessageBody();
            Vector existing = targetExists(rows, body);
            if (existing != null) {
                existing.set(TargetElement.SIGN, Boolean.TRUE);
            } else {
                Vector columns = new Vector();
                columns.add(TargetElement.DATA, body);
                columns.add(TargetElement.SIGN, Boolean.TRUE);
                columns.add(TargetElement.ENCRYPT, Boolean.FALSE);
                columns.add(TargetElement.REQUIRE, Boolean.FALSE);
                rows.add(columns);
            }
        }
        for (Header h : headers) {
            MessageHeader header = getListModelForHeader(h);
            if (header != null) {
                Vector existing = targetExists(rows, header);
                if (existing != null) {
                    existing.set(TargetElement.SIGN, Boolean.TRUE);
                } else {
                    Vector columns = new Vector();
                    columns.add(TargetElement.DATA, header);
                    columns.add(TargetElement.SIGN, Boolean.TRUE);
                    columns.add(TargetElement.ENCRYPT, Boolean.FALSE);
                    columns.add(TargetElement.REQUIRE, Boolean.FALSE);
                    rows.add(columns);
                }
            }
        }
        for (XPath x : xpaths) {
            MessageElement e = getListModelForXPath(x);
            if (e != null) {
                Vector existing = targetExists(rows, e);
                if (existing != null) {
                    existing.set(TargetElement.SIGN, Boolean.TRUE);
                } else {
                    Vector columns = new Vector();
                    columns.add(TargetElement.DATA, e);
                    columns.add(TargetElement.SIGN, Boolean.TRUE);
                    columns.add(TargetElement.ENCRYPT, Boolean.FALSE);
                    columns.add(TargetElement.REQUIRE, Boolean.FALSE);
                    rows.add(columns);
                }
            }
        }

        RequiredElements requiredElements = (RequiredElements)PolicyModelHelper.getTopLevelElement(p, RequiredElements.class);
        if (requiredElements != null) {
            xpaths = requiredElements.getExtensibilityElements(XPath.class);
        }
        for (XPath x : xpaths) {
            MessageElement e = getListModelForXPath(x);
            if (e != null) {
                Vector existing = targetExists(rows, e);
                if (existing != null) {
                    existing.set(TargetElement.REQUIRE, Boolean.TRUE);
                } else {
                    Vector columns = new Vector();
                    columns.add(TargetElement.DATA, e);
                    columns.add(TargetElement.SIGN, Boolean.FALSE);
                    columns.add(TargetElement.ENCRYPT, Boolean.FALSE);
                    columns.add(TargetElement.REQUIRE, Boolean.TRUE);
                    rows.add(columns);
                }
            }
        }

        return rows;
    }

    public static Vector targetExists(Vector<Vector> rows, TargetElement e) {
        for (Vector row : rows) {
            TargetElement te = (TargetElement) row.get(TargetElement.DATA);
            if (te.equals(e)) {
                return row;
            }
        }
        return null;
    }

    public static void setTargets(WSDLComponent comp, Vector<Vector> targetModel) {

        if (comp == null) return;
        
        WSDLModel model = comp.getModel();

        Policy p = PolicyModelHelper.getPolicyForElement(comp);
        EncryptedParts encryptedParts = (EncryptedParts) PolicyModelHelper.getTopLevelElement(p, EncryptedParts.class);
        SignedParts signedParts = (SignedParts) PolicyModelHelper.getTopLevelElement(p, SignedParts.class);
        EncryptedElements encryptedElements = (EncryptedElements) PolicyModelHelper.getTopLevelElement(p, EncryptedElements.class);
        SignedElements signedElements = (SignedElements) PolicyModelHelper.getTopLevelElement(p, SignedElements.class);
        RequiredElements requiredElements = (RequiredElements) PolicyModelHelper.getTopLevelElement(p, RequiredElements.class);
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
            if (signedParts != null) {
                all = (All) signedParts.getParent();
                all.removeExtensibilityElement(signedParts);
            }
            if (encryptedElements != null) {
                all = (All) encryptedElements.getParent();
                all.removeExtensibilityElement(encryptedElements);
            }
            if (signedElements != null) {
                all = (All) signedElements.getParent();
                all.removeExtensibilityElement(signedElements);
            }
            if (requiredElements != null) {
                all = (All) requiredElements.getParent();
                all.removeExtensibilityElement(requiredElements);
            }

            if (targetModel == null) {
                return;
            }
            
            if (p == null) {
                all = PolicyModelHelper.createPolicy(comp);
            } else {
                all = PolicyModelHelper.createTopExactlyOneAll(p);
            }

            encryptedParts = PolicyModelHelper.createElement(all, SecurityPolicyQName.ENCRYPTEDPARTS.getQName(), EncryptedParts.class, false);
            signedParts = PolicyModelHelper.createElement(all, SecurityPolicyQName.SIGNEDPARTS.getQName(), SignedParts.class, false);

            for (Vector v : targetModel) {
                TargetElement te = (TargetElement) v.get(TargetElement.DATA);
                boolean encrypt = ((Boolean)v.get(TargetElement.ENCRYPT)).booleanValue();
                boolean sign = ((Boolean)v.get(TargetElement.SIGN)).booleanValue();
                boolean require = ((Boolean)v.get(TargetElement.REQUIRE)).booleanValue();
                
                if (te instanceof MessageHeader) {    
                    if (encrypt) {
                        addHeaderElementForListItem(te.toString(), encryptedParts, wcf);
                    }
                    if (sign) {
                        addHeaderElementForListItem(te.toString(), signedParts, wcf);
                    }
                } else if (te instanceof MessageElement) {
                    
                    if (encrypt) {
                        if (encryptedElements == null) {
                            encryptedElements = PolicyModelHelper.createElement(all, SecurityPolicyQName.ENCRYPTEDELEMENTS.getQName(), EncryptedElements.class, false);
                        } 
                        addElementForListItem(te.toString(), encryptedElements, wcf);
                    }
                    if (sign) {
                        if (signedElements == null) {
                            signedElements = PolicyModelHelper.createElement(all, SecurityPolicyQName.SIGNEDELEMENTS.getQName(), SignedElements.class, false);
                        }
                        addElementForListItem(te.toString(), signedElements, wcf);
                    }
                    if (require) {
                        if (requiredElements == null) {
                            requiredElements = PolicyModelHelper.createElement(all, SecurityPolicyQName.REQUIREDELEMENTS.getQName(), RequiredElements.class, false);            
                        }
                        addElementForListItem(te.toString(), requiredElements, wcf);
                    }
                } else if (te instanceof MessageBody) {
                    if (encrypt) {
                        addBody(encryptedParts, wcf);
                    }
                    if (sign) {
                        addBody(signedParts, wcf);
                    }
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
        
        //TODO - get addressing namespace
        
        Header h = null;
        h = (Header)wcf.create(c, SecurityPolicyQName.HEADER.getQName());
        if (MessageHeader.ADDRESSING_TO.equals(item)) {
            h.setName("To");        //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_FROM.equals(item)) {
            h.setName("From");      //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_FAULTTO.equals(item)) {
            h.setName("FaultTo");      //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_REPLYTO.equals(item)) {
            h.setName("ReplyTo");   //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_MESSAGEID.equals(item)) {
            h.setName("MessageId"); //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_RELATESTO.equals(item)) {
            h.setName("RelatesTo"); //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
        }
        if (MessageHeader.ADDRESSING_ACTION.equals(item)) {
            h.setName("Action");    //NOI18N
            h.setNamespace(Addressing10QName.ADDRESSING10_NS_URI);
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

    public static ExtensibilityElement addBody(WSDLComponent c, WSDLComponentFactory wcf) {
        Body b = null;
        b = (Body)wcf.create(c, SecurityPolicyQName.BODY.getQName());
        c.addExtensibilityElement(b);
        return b;
    }

    /**************************** SECURITY BINDING TYPE *********************/

    /** 
     * Returns string representation of security binding type
     * TODO: maybe this method is not required with the new UI, as there's no mention of it
     */
    public static String getSecurityBindingType(WSDLComponent c) {
        assert c != null;
        WSDLModel model = c.getModel();
        ExtensibilityElement e = getSecurityBindingTypeElement(c);
        if (e instanceof SymmetricBinding) return ComboConstants.SYMMETRIC;
        if (e instanceof AsymmetricBinding) return ComboConstants.ASYMMETRIC;
        if (e instanceof TransportBinding) return ComboConstants.TRANSPORT;
        return ComboConstants.NOSECURITY;
    }

    /**
     * Returns security binding type element for specified element which can be either top level Binding, BindingOperation, ...
     * or sub-level like SecureConversationToken
     */ 
    public static ExtensibilityElement getSecurityBindingTypeElement(WSDLComponent c) {
        assert c != null;
        WSDLModel model = c.getModel();
        WSDLComponent p = c;
        
        if ((c instanceof Binding) || (c instanceof BindingOperation) || 
            (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
            p = PolicyModelHelper.getPolicyForElement(c);
        } else if (c instanceof BootstrapPolicy) {
            p = PolicyModelHelper.getTopLevelElement(c, Policy.class);
        }
        
        ExtensibilityElement ee = PolicyModelHelper.getTopLevelElement(p, SymmetricBinding.class);
        if (ee != null) return ee;
        ee = (AsymmetricBinding)PolicyModelHelper.getTopLevelElement(p, AsymmetricBinding.class);
        if (ee != null) return ee;
        ee = (TransportBinding)PolicyModelHelper.getTopLevelElement(p, TransportBinding.class);
        if (ee != null) return ee;
        
        return null;
    }

    public static WSDLComponent setSecurityBindingType(WSDLComponent c, String bindingType) {
        assert (c!=null);
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        WSDLComponent secBindingType = null;
               
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        All a = PolicyModelHelper.createPolicy(c);
        
        try {
            SymmetricBinding sb = (SymmetricBinding)PolicyModelHelper.getTopLevelElement(a, SymmetricBinding.class);
            AsymmetricBinding ab = (AsymmetricBinding)PolicyModelHelper.getTopLevelElement(a, AsymmetricBinding.class);
            TransportBinding tb = (TransportBinding)PolicyModelHelper.getTopLevelElement(a, TransportBinding.class);

            if (sb != null) sb.getParent().removeExtensibilityElement(sb);
            if (ab != null) ab.getParent().removeExtensibilityElement(ab);
            if (tb != null) tb.getParent().removeExtensibilityElement(tb);

            if (ComboConstants.SYMMETRIC.equals(bindingType)) {
                sb = PolicyModelHelper.createElement(a, SecurityPolicyQName.SYMMETRICBINDING.getQName(), SymmetricBinding.class, false);
                secBindingType = sb;
            }
            if (ComboConstants.ASYMMETRIC.equals(bindingType)) {
                ab = PolicyModelHelper.createElement(a, SecurityPolicyQName.ASYMMETRICBINDING.getQName(), AsymmetricBinding.class, false);
                secBindingType = ab;
            }
            if (ComboConstants.TRANSPORT.equals(bindingType)) {
                tb = PolicyModelHelper.createElement(a, SecurityPolicyQName.TRANSPORTBINDING.getQName(), TransportBinding.class, false);
                secBindingType = tb;
            }

        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
        
        return secBindingType;
    }

    public static void setDefaultTargets(WSDLComponent c, boolean wss11) {

        WSDLModel model = c.getModel();
        Vector<Vector> targets = new Vector();

        Vector row = new Vector();
        MessageBody body = new MessageBody();
        row.add(TargetElement.DATA, body);
        row.add(TargetElement.SIGN, Boolean.TRUE);
        row.add(TargetElement.ENCRYPT, Boolean.TRUE);
        row.add(TargetElement.REQUIRE, Boolean.FALSE);
        targets.add(row);

        if (wss11) {
            for (String s : MessageHeader.ALL_HEADERS) {
                row = new Vector();
                MessageHeader h = new MessageHeader(s);
                row.add(TargetElement.DATA, h);
                row.add(TargetElement.SIGN, Boolean.TRUE);
                row.add(TargetElement.ENCRYPT, Boolean.TRUE);
                row.add(TargetElement.REQUIRE, Boolean.FALSE);
                targets.add(row);
            }
        }

        setTargets(c, targets);
    }

    public static void removeTargets(WSDLComponent c) {
        WSDLModel model = c.getModel();
        setTargets(c, null);
    }
    
    /********** Other binding attributes ****************/

    public static String getComboItemForElement(WSDLComponent wc) {
        String cName = wc.getClass().getSimpleName();
        String msg = "COMBO_" + cName.substring(0, cName.length()-4);  //NOI18N
        return NbBundle.getMessage(ComboConstants.class, msg);
    }

    public static String getMessageLayout(WSDLComponent comp) {
        WSDLComponent layout = getMessageLayoutElement(comp);
        if (layout != null) {
            if (layout instanceof Strict) return ComboConstants.STRICT;
            if (layout instanceof Lax) return ComboConstants.LAX;
            if (layout instanceof LaxTsFirst) return ComboConstants.LAXTSFIRST;
            if (layout instanceof LaxTsLast) return ComboConstants.LAXTSLAST;            
        }
        return null;
    }
    
    public static WSDLComponent getMessageLayoutElement(WSDLComponent comp) {
        if ((comp instanceof Binding) || (comp instanceof BindingOperation)) {
            comp = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
        }
        if (comp == null) return null;
        Policy p = PolicyModelHelper.getTopLevelElement(comp, Policy.class);
        Layout l = PolicyModelHelper.getTopLevelElement(p, Layout.class);
        p = PolicyModelHelper.getTopLevelElement(l, Policy.class);
        if (p != null) {
            List<ExtensibilityElement> elements = p.getExtensibilityElements();
            if ((elements != null) && !(elements.isEmpty())) {
                ExtensibilityElement e = elements.get(0);
                return e;
            }
        }
        return null;
    }
    
    public static boolean isIncludeTimestamp(WSDLComponent c) {
        ExtensibilityElement e = getSecurityBindingTypeElement(c);
        if (e != null) {
            return isAttributeEnabled(e, IncludeTimestamp.class);
        }
        return false;
    }
    
    public static boolean isEncryptBeforeSigning(WSDLComponent c) {
        ExtensibilityElement e = getSecurityBindingTypeElement(c);
        if (e != null) {
            return isAttributeEnabled(e, EncryptBeforeSigning.class);
        }
        return false;
    }

    public static boolean isEncryptSignature(WSDLComponent c) {
        ExtensibilityElement e = getSecurityBindingTypeElement(c);
        if (e != null) {
            return isAttributeEnabled(e, EncryptSignature.class);
        }
        return false;
    }
    
    public static boolean isSignEntireHeadersAndBody(WSDLComponent c) {
        ExtensibilityElement e = getSecurityBindingTypeElement(c);
        if (e != null) {
            return isAttributeEnabled(e, OnlySignEntireHeadersAndBody.class);
        }
        return false;
    }

   public static void setLayout(WSDLComponent c, String msgLayout) {
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            QName qnameToCreate = null;
            if (ComboConstants.STRICT.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.STRICT.getQName();
            } else if (ComboConstants.LAX.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.LAX.getQName();
            } else if (ComboConstants.LAXTSFIRST.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.LAXTSFIRST.getQName();
            } else if (ComboConstants.LAXTSLAST.equals(msgLayout)) {
                qnameToCreate = SecurityPolicyQName.LAXTSLAST.getQName();
            }

            Layout layout = PolicyModelHelper.createElement(c, SecurityPolicyQName.LAYOUT.getQName(), Layout.class, true);

            List<Policy> policies = layout.getExtensibilityElements(Policy.class);
            if ((policies != null) && (!policies.isEmpty())) {
                for (Policy pol : policies) {
                    layout.removeExtensibilityElement(pol);
                }
            }        
            Policy p = PolicyModelHelper.createElement(layout, PolicyQName.POLICY.getQName(), Policy.class, false);
            ExtensibilityElement e = (ExtensibilityElement) wcf.create(p, qnameToCreate);
            p.addExtensibilityElement(e);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }    
    
}
