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
import java.util.Set;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfileRegistry;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.TrustElement;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.*;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.security.AsymmetricBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.SymmetricBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.TransportBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.InitiatorToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RecipientToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.wsdl.model.*;
import java.util.Vector;

/**
 *
 * @author Martin Grebac
 */
public class ProfilesModelHelper {

    private static Vector<Vector> DEFAULT_TARGETS = new Vector();
    
    static {
        Vector row = new Vector();
        row.add(new MessageBody());
        row.add(Boolean.TRUE); row.add(Boolean.TRUE); row.add(Boolean.FALSE);
        DEFAULT_TARGETS.add(row);
        
        row = new Vector();
        row.add(new MessageHeader(MessageHeader.ADDRESSING_TO));
        row.add(Boolean.TRUE); row.add(Boolean.FALSE); row.add(Boolean.FALSE);
        DEFAULT_TARGETS.add(row);
        
        row = new Vector();
        row.add(new MessageHeader(MessageHeader.ADDRESSING_FROM));
        row.add(Boolean.TRUE); row.add(Boolean.FALSE); row.add(Boolean.FALSE);
        DEFAULT_TARGETS.add(row);
        
        row = new Vector();
        row.add(new MessageHeader(MessageHeader.ADDRESSING_FAULTTO));
        row.add(Boolean.TRUE); row.add(Boolean.FALSE); row.add(Boolean.FALSE);
        DEFAULT_TARGETS.add(row);

        row = new Vector();
        row.add(new MessageHeader(MessageHeader.ADDRESSING_REPLYTO));
        row.add(Boolean.TRUE); row.add(Boolean.FALSE); row.add(Boolean.FALSE);
        DEFAULT_TARGETS.add(row);

        row = new Vector();
        row.add(new MessageHeader(MessageHeader.ADDRESSING_MESSAGEID));
        row.add(Boolean.TRUE); row.add(Boolean.FALSE); row.add(Boolean.FALSE);
        DEFAULT_TARGETS.add(row);

        row = new Vector();
        row.add(new MessageHeader(MessageHeader.ADDRESSING_RELATESTO));
        row.add(Boolean.TRUE); row.add(Boolean.FALSE); row.add(Boolean.FALSE);
        DEFAULT_TARGETS.add(row);

        row = new Vector();
        row.add(new MessageHeader(MessageHeader.ADDRESSING_ACTION));
        row.add(Boolean.TRUE); row.add(Boolean.FALSE); row.add(Boolean.FALSE);
        DEFAULT_TARGETS.add(row);
    }
    
    /**
     * Creates a new instance of ProfilesModelHelper
     */
    public ProfilesModelHelper() { }
    
    /** 
     * Returns security profile for Binding or BindingOperation
     */
    public static String getSecurityProfile(WSDLComponent c) {
        assert ((c instanceof BindingOperation) || (c instanceof Binding));

        Set<SecurityProfile> profiles = SecurityProfileRegistry.getDefault().getSecurityProfiles();
        for (SecurityProfile profile : profiles) {
            if (profile.isCurrentProfile(c)) {
                return profile.getDisplayName();
            }
        }
        
        return ComboConstants.PROF_GENERIC;
    }

    /** 
     * Checks whether Secure Conversation is enabled
     */
    public static boolean isSCEnabled(WSDLComponent c) {
        assert ((c instanceof BindingOperation) || (c instanceof Binding));
        Policy p = PolicyModelHelper.getPolicyForElement(c);
        SymmetricBinding sb = (SymmetricBinding)PolicyModelHelper.getTopLevelElement(p, SymmetricBinding.class);
        if (sb == null) return false;
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(sb, ProtectionToken.class);
        if (protTokenKind == null) return false;
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
        if (protToken == null) return false;
        boolean secConv = (protToken instanceof SecureConversationToken);
        return secConv;        
    }

    public static String getWSITSecurityProfile(WSDLComponent c) {
        if ((c instanceof Binding) || (c instanceof BindingOperation)) {
            Policy p = PolicyModelHelper.getPolicyForElement(c);

            SymmetricBinding sb = (SymmetricBinding)PolicyModelHelper.getTopLevelElement(p, SymmetricBinding.class);
            WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(sb, ProtectionToken.class);
            WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
            WSDLComponent secConvSecBinding = null;
            boolean secConv = (protToken instanceof SecureConversationToken);

            WSDLComponent bootPolicy = null;
            
            if (secConv) {
                bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
                secConvSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            }
            
            TransportBinding tb = null;
            if (secConv && (secConvSecBinding instanceof TransportBinding)) {
                tb = (TransportBinding) secConvSecBinding;
            } else {
                tb = (TransportBinding)PolicyModelHelper.getTopLevelElement(p, TransportBinding.class);
            }
            if (tb != null) { // profiles 1,2,3
                // depends on message level policy
                if (c instanceof BindingOperation) {
                    BindingInput input = ((BindingOperation)c).getBindingInput();
                    WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(input, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                    String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
                    if (ComboConstants.SAML.equals(tokenType)) { // profile3
                        return ComboConstants.PROF_SAMLSSL;
                    } else if ((ComboConstants.USERNAME.equals(tokenType)) || (ComboConstants.X509.equals(tokenType))) {  // profile2
                        return ComboConstants.PROF_MSGAUTHSSL;
                    }
                    return ComboConstants.PROF_TRANSPORT;
                } else {
                    WSDLComponent tokenKind = null;
                    if (secConv) {
                        Policy pp = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class);
                        tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                    } else {
                        tokenKind = SecurityTokensModelHelper.getSupportingToken(c, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                    }
                    String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
                    if (ComboConstants.SAML.equals(tokenType)) { // profile3
                        return ComboConstants.PROF_SAMLSSL;
                    } else if ((ComboConstants.USERNAME.equals(tokenType)) || (ComboConstants.X509.equals(tokenType))) {  // profile2
                        return ComboConstants.PROF_MSGAUTHSSL;
                    }
                    return ComboConstants.PROF_TRANSPORT;
                }
            }

            if (secConv && (secConvSecBinding instanceof SymmetricBinding)) {
                sb = (SymmetricBinding) secConvSecBinding;
            } else {
                sb = (SymmetricBinding)PolicyModelHelper.getTopLevelElement(p, SymmetricBinding.class);
            }
            if (sb != null) { // profiles 4,6,9,10,12
                protToken = (ProtectionToken) SecurityTokensModelHelper.getTokenElement(sb, ProtectionToken.class);
                if (protToken != null) {
                    String tokenType = SecurityTokensModelHelper.getTokenType(protToken);
                    if (ComboConstants.ISSUED.equals(tokenType)) {  // profile 10
                        return ComboConstants.PROF_STSISSUED;
                    }
                    if (ComboConstants.KERBEROS.equals(tokenType)) {  // profile 9
                        return ComboConstants.PROF_KERBEROS;
                    }
                    if (ComboConstants.X509.equals(tokenType)) { // profile 12, 6, 4
                        WSDLComponent tokenKind = null;
                        if (secConv) {
                            Policy pp = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class);
                            tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, SecurityTokensModelHelper.ENDORSING);
                        } else {
                            tokenKind = SecurityTokensModelHelper.getSupportingToken(c, SecurityTokensModelHelper.ENDORSING);
                        }
                        
                        tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
                        if (ComboConstants.ISSUED.equals(tokenType)) { // profile 12
                            return ComboConstants.PROF_STSISSUEDENDORSE;
                        }
                        if (ComboConstants.X509.equals(tokenType)) { // profile 6
                            return ComboConstants.PROF_ENDORSCERT;
                        }
                        if (tokenType == null) {    // profile 4
                            return ComboConstants.PROF_USERNAME;
                        }
                    }
                }
            }

            AsymmetricBinding ab = null;
            if (secConv && (secConvSecBinding instanceof AsymmetricBinding)) {
                ab = (AsymmetricBinding) secConvSecBinding;
            } else {
                ab = (AsymmetricBinding)PolicyModelHelper.getTopLevelElement(p, AsymmetricBinding.class);
            }
            if (ab != null) { // profiles 5,7,8,11
                InitiatorToken initToken = (InitiatorToken) SecurityTokensModelHelper.getTokenElement(ab, InitiatorToken.class);
                RecipientToken recipToken = (RecipientToken) SecurityTokensModelHelper.getTokenElement(ab, RecipientToken.class);
                if ((initToken != null) && (recipToken!= null)) {
                    String initTokenType = SecurityTokensModelHelper.getTokenType(initToken);
                    String recipTokenType = SecurityTokensModelHelper.getTokenType(recipToken);
                    if ((ComboConstants.X509.equals(initTokenType)) && (ComboConstants.X509.equals(recipTokenType))) {  // profile 5, 7                       
                        if (c instanceof BindingOperation) {
                            BindingInput input = ((BindingOperation)c).getBindingInput();
                            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(input, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                            String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
                            if (ComboConstants.SAML.equals(tokenType)) { // profile7
                                return ComboConstants.PROF_SAMLSENDER;
                            } else if (tokenType == null) {  // profile5
                                return ComboConstants.PROF_MUTUALCERT;
                            }
                        } else {
                            WSDLComponent tokenKind = null;
                            if (secConv) {
                                Policy pp = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class);
                                tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                            } else {
                                tokenKind = SecurityTokensModelHelper.getSupportingToken(c, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                            }
                            String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
                            if (ComboConstants.SAML.equals(tokenType)) { // profile7
                                return ComboConstants.PROF_SAMLSENDER;
                            } else if (tokenType == null) {  // profile5
                                return ComboConstants.PROF_MUTUALCERT;
                            }
                        }
                    }
                    if ((ComboConstants.SAML.equals(initTokenType)) && (ComboConstants.X509.equals(recipTokenType))) {  // profile 8,
                        return ComboConstants.PROF_SAMLHOLDER;
                    }
                    if ((ComboConstants.ISSUED.equals(initTokenType)) && (ComboConstants.X509.equals(recipTokenType))) {  // profile 11
                        return ComboConstants.PROF_STSISSUEDCERT;
                    }
                }
            }
        }
        
        return ComboConstants.PROF_GENERIC;
    }

    /** Sets security profile on Binding or BindingOperation
     */
    public static void setSecurityProfile(WSDLComponent c, String profile, String oldProfile) {
        assert (c != null);
        assert (profile != null);
        assert ((c instanceof BindingOperation) || (c instanceof Binding));

        SecurityProfile newP = SecurityProfileRegistry.getDefault().getProfile(profile);
        SecurityProfile oldP = SecurityProfileRegistry.getDefault().getProfile(oldProfile);
        
        if (oldP != null) {
            oldP.profileDeselected(c);
        }
        newP.profileSelected(c);
        
        return;
    }
        
    /** Sets security profile on Binding or BindingOperation
     */
    public static void setSecurityProfile(WSDLComponent c, String profile) {
        WSDLModel model = c.getModel();
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        All a = PolicyModelHelper.createPolicy(c);
        try {
            // Profile #1
            if (ComboConstants.PROF_TRANSPORT.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.TRANSPORT);
                SecurityTokensModelHelper.setTokenType(bt, ComboConstants.TRANSPORT, ComboConstants.HTTPS);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.LAX);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, false);
                SecurityPolicyModelHelper.disableTrust10(c);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
            // Profile #2
            if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.TRANSPORT);
                SecurityTokensModelHelper.setTokenType(bt, ComboConstants.TRANSPORT, ComboConstants.HTTPS);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.LAX);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, false);
                SecurityPolicyModelHelper.disableTrust10(c);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                SecurityTokensModelHelper.setSupportingTokens(c, ComboConstants.USERNAME, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
            // Profile #3
            if (ComboConstants.PROF_SAMLSSL.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.TRANSPORT);
                SecurityTokensModelHelper.setTokenType(bt, ComboConstants.TRANSPORT, ComboConstants.HTTPS);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.LAX);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, false);
                SecurityPolicyModelHelper.disableTrust10(c);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                SecurityTokensModelHelper.setSupportingTokens(c, ComboConstants.SAML, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
            // Profile #4
            if (ComboConstants.PROF_USERNAME.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.X509);
//                SecurityPolicyModelHelper.enableRequireThumbprintReference(tokenType, true);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.STRICT);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, true);
                SecurityPolicyModelHelper.disableTrust10(c);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefThumbprint(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefEncryptedKey(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                SecurityTokensModelHelper.setSupportingTokens(c, ComboConstants.USERNAME, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
            // Profile #5
            if (ComboConstants.PROF_MUTUALCERT.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.ASYMMETRIC);
                WSDLComponent tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.INITIATOR, ComboConstants.X509);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
                tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.RECIPIENT, ComboConstants.X509);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.STRICT);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, false);
                SecurityPolicyModelHelper.disableTrust10(c);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
            // Profile #6
            if (ComboConstants.PROF_ENDORSCERT.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.X509);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
//                SecurityPolicyModelHelper.enableRequireThumbprintReference(tokenType, true);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.LAX);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, true);
                SecurityPolicyModelHelper.disableTrust10(c);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefThumbprint(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefEncryptedKey(wss, true);
                //endorsing supporting token
                SecurityTokensModelHelper.removeSupportingTokens(c);
                tokenType = SecurityTokensModelHelper.setSupportingTokens(c, ComboConstants.X509, SecurityTokensModelHelper.ENDORSING);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
            // Profile #7
            if (ComboConstants.PROF_SAMLSENDER.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.ASYMMETRIC);
                WSDLComponent tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.INITIATOR, ComboConstants.X509);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
                tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.RECIPIENT, ComboConstants.X509);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.STRICT);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, false);
                SecurityPolicyModelHelper.disableTrust10(c);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                SecurityTokensModelHelper.setSupportingTokens(c, ComboConstants.SAML, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
            // Profile #8
            if (ComboConstants.PROF_SAMLHOLDER.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.ASYMMETRIC);
                WSDLComponent tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.INITIATOR, ComboConstants.SAML);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
                tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.RECIPIENT, ComboConstants.X509);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.STRICT);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, false);
                SecurityPolicyModelHelper.disableTrust10(c);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
            // Profile #9
            if (ComboConstants.PROF_KERBEROS.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.KERBEROS);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.ONCE);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.LAX);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, true);
                SecurityPolicyModelHelper.disableTrust10(c);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefThumbprint(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefEncryptedKey(wss, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
            // Profile #10
            if (ComboConstants.PROF_STSISSUED.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.ISSUED);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.LAX);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, true);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefThumbprint(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefEncryptedKey(wss, true);
                //trust10
                TrustElement trust = SecurityPolicyModelHelper.enableTrust10(c);
                SecurityPolicyModelHelper.enableMustSupportIssuedTokens(trust, true);
                SecurityPolicyModelHelper.enableRequireClientEntropy(trust, true);
                SecurityPolicyModelHelper.enableRequireServerEntropy(trust, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
            // Profile #11
            if (ComboConstants.PROF_STSISSUEDCERT.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.ASYMMETRIC);
                WSDLComponent tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.INITIATOR, ComboConstants.ISSUED);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
                tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.RECIPIENT, ComboConstants.X509);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.NEVER);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.LAX);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, true);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefThumbprint(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefEncryptedKey(wss, true);
                //trust10
                TrustElement trust = SecurityPolicyModelHelper.enableTrust10(c);
                SecurityPolicyModelHelper.enableMustSupportIssuedTokens(trust, true);
                SecurityPolicyModelHelper.enableRequireClientEntropy(trust, true);
                SecurityPolicyModelHelper.enableRequireServerEntropy(trust, true);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
            // Profile #12
            if (ComboConstants.PROF_STSISSUEDENDORSE.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                WSDLComponent tokenType = SecurityTokensModelHelper.setTokenType(bt, ComboConstants.PROTECTION, ComboConstants.X509);
                SecurityTokensModelHelper.setTokenInclusionLevel(tokenType, ComboConstants.ALWAYS);
//                SecurityPolicyModelHelper.enableRequireThumbprintReference(tokenType, true);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.LAX);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                //wss
                WssElement wss = SecurityPolicyModelHelper.enableWss(c, true);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefThumbprint(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefEncryptedKey(wss, true);
                //trust10
                TrustElement trust = SecurityPolicyModelHelper.enableTrust10(c);
                SecurityPolicyModelHelper.enableMustSupportIssuedTokens(trust, true);
                SecurityPolicyModelHelper.enableRequireClientEntropy(trust, true);
                SecurityPolicyModelHelper.enableRequireServerEntropy(trust, true);
                //endorsing supporting token
                SecurityTokensModelHelper.removeSupportingTokens(c);
                tokenType = SecurityTokensModelHelper.setSupportingTokens(c, ComboConstants.ISSUED, SecurityTokensModelHelper.ENDORSING);
                setMessageLevelSecurityProfilePolicies(c, profile);
                return;
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setMessageLevelSecurityProfilePolicies(WSDLComponent c, String profile) {
        assert ((c instanceof BindingOperation) || (c instanceof Binding));
        
        if (c instanceof Binding) {
            Collection<BindingOperation> ops = ((Binding)c).getBindingOperations();
            for (BindingOperation o : ops) {
                if (!SecurityPolicyModelHelper.isSecurityEnabled(o)) {
                    setMessageLevelSecurityProfilePolicies(o, profile);
                }
            }
        } else {
            setMessageLevelSecurityProfilePolicies((BindingOperation)c, profile);
        }
    }
    
    public static void setMessageLevelSecurityProfilePolicies(BindingOperation o, String profile) {
        assert (o != null);
        
        WSDLModel model = o.getModel();
        
        BindingInput input = o.getBindingInput();
        BindingOutput output = o.getBindingOutput();
               
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            if (input != null) PolicyModelHelper.removePolicyForElement(input);
            if (output != null) PolicyModelHelper.removePolicyForElement(output);

            // Profile #1
            if (ComboConstants.PROF_TRANSPORT.equals(profile)) {
                // do nothing, there are no msg level policies
                return;
            }
            // Profile #2
            if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
//                SecurityTokensModelHelper.setSupportingTokens(input, null, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                return;
            }
            // Profile #3
            if (ComboConstants.PROF_SAMLSSL.equals(profile)) {
//                SecurityTokensModelHelper.setSupportingTokens(input, null, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                return;
            }
            // Profile #4
            if (ComboConstants.PROF_USERNAME.equals(profile)) {
//                SecurityTokensModelHelper.setSupportingTokens(input, null, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                SecurityPolicyModelHelper.setTargets(input, DEFAULT_TARGETS);
                SecurityPolicyModelHelper.setTargets(output, DEFAULT_TARGETS);
                return;
            }
            // Profile #5
            if (ComboConstants.PROF_MUTUALCERT.equals(profile)) {
                SecurityPolicyModelHelper.setTargets(input, DEFAULT_TARGETS);
                SecurityPolicyModelHelper.setTargets(output, DEFAULT_TARGETS);
                return;
            }
            // Profile #6
            if (ComboConstants.PROF_ENDORSCERT.equals(profile)) {
                SecurityPolicyModelHelper.setTargets(input, DEFAULT_TARGETS);
                SecurityPolicyModelHelper.setTargets(output, DEFAULT_TARGETS);
                return;
            }
            // Profile #7
            if (ComboConstants.PROF_SAMLSENDER.equals(profile)) {
//                SecurityTokensModelHelper.setSupportingTokens(input, null, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                SecurityPolicyModelHelper.setTargets(input, DEFAULT_TARGETS);
                SecurityPolicyModelHelper.setTargets(output, DEFAULT_TARGETS);
                return;
            }
            // Profile #8
            if (ComboConstants.PROF_SAMLHOLDER.equals(profile)) {
                SecurityPolicyModelHelper.setTargets(input, DEFAULT_TARGETS);
                SecurityPolicyModelHelper.setTargets(output, DEFAULT_TARGETS);
                return;
            }
            // Profile #9
            if (ComboConstants.PROF_KERBEROS.equals(profile)) {
                SecurityPolicyModelHelper.setTargets(input, DEFAULT_TARGETS);
                SecurityPolicyModelHelper.setTargets(output, DEFAULT_TARGETS);
                return;
            }
            // Profile #10
            if (ComboConstants.PROF_STSISSUED.equals(profile)) {
                SecurityPolicyModelHelper.setTargets(input, DEFAULT_TARGETS);
                SecurityPolicyModelHelper.setTargets(output, DEFAULT_TARGETS);
                return;
            }
            // Profile #11
            if (ComboConstants.PROF_STSISSUEDCERT.equals(profile)) {
                SecurityPolicyModelHelper.setTargets(input, DEFAULT_TARGETS);
                SecurityPolicyModelHelper.setTargets(output, DEFAULT_TARGETS);
                return;
            }
            // Profile #12
            if (ComboConstants.PROF_STSISSUEDENDORSE.equals(profile)) {
                SecurityPolicyModelHelper.setTargets(input, DEFAULT_TARGETS);
                SecurityPolicyModelHelper.setTargets(output, DEFAULT_TARGETS);
                return;
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void enableSecureConversation(WSDLComponent c, boolean enable, String profile) {
        assert (c != null);
        assert ((c instanceof BindingOperation) || (c instanceof Binding));

        WSDLModel model = c.getModel();        
        WSDLComponentFactory wcf = model.getFactory();
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            if (enable) {
                WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(c);
                WSDLComponent par = secBinding.getParent();
                
                boolean onlySign = SecurityPolicyModelHelper.isSignEntireHeadersAndBody(c);
                boolean includeTimestamp = SecurityPolicyModelHelper.isSignEntireHeadersAndBody(c);
                String algoSuite = AlgoSuiteModelHelper.getAlgorithmSuite(c);
                        
                BootstrapPolicy bp = (BootstrapPolicy) wcf.create(par, SecurityPolicyQName.BOOTSTRAPPOLICY.getQName());
                par.addExtensibilityElement(bp);
                Policy p = PolicyModelHelper.createElement(bp, PolicyQName.POLICY.getQName(), Policy.class, false);
                p.addExtensibilityElement((ExtensibilityElement) secBinding.copy(p));

                for (int suppTokenType=0; suppTokenType < 3; suppTokenType++) {
                    ExtensibilityElement suppToken = 
                            (ExtensibilityElement) SecurityTokensModelHelper.getSupportingToken(c, suppTokenType);
                    if (suppToken == null) continue;
                    p.addExtensibilityElement((ExtensibilityElement) suppToken.copy(p));
                    suppToken.getParent().removeExtensibilityElement(suppToken);
                }

                WSDLComponent bType = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.SYMMETRIC);
                SecureConversationToken tType = (SecureConversationToken) SecurityTokensModelHelper.setTokenType(
                        bType, ComboConstants.PROTECTION, ComboConstants.SECURECONVERSATION);                    
                SecurityTokensModelHelper.setTokenInclusionLevel(tType, ComboConstants.ALWAYSRECIPIENT);
                p = PolicyModelHelper.createElement(tType, PolicyQName.POLICY.getQName(), Policy.class, false);
                ExtensibilityElement bpcopy = (ExtensibilityElement) bp.copy(p);
                p.addExtensibilityElement(bpcopy);
                par.removeExtensibilityElement(bp);
                p = PolicyModelHelper.getTopLevelElement(bpcopy, Policy.class);
                WSDLComponent wss10 = SecurityPolicyModelHelper.getWss10(par);
                if (wss10 != null) {
                    p.addExtensibilityElement((ExtensibilityElement) wss10.copy(p));
                }
                WssElement wss11 = SecurityPolicyModelHelper.getWss11(par);
                if (wss11 != null) {
                    p.addExtensibilityElement((ExtensibilityElement) wss11.copy(p));
                }
                TrustElement trust = SecurityPolicyModelHelper.getTrust10(par);
                if (trust != null) {
                    p.addExtensibilityElement((ExtensibilityElement) trust.copy(p));
                }

                // set top level secure conversation policy
                SecurityPolicyModelHelper.setLayout(bType, ComboConstants.STRICT);
                if (algoSuite != null) {
                    AlgoSuiteModelHelper.setAlgorithmSuite(bType, algoSuite);
                } else {
                    AlgoSuiteModelHelper.setAlgorithmSuite(bType, ComboConstants.BASIC128);
                }
                if (includeTimestamp) {
                    SecurityPolicyModelHelper.enableIncludeTimestamp(bType, true);
                }
                if (onlySign) {
                    SecurityPolicyModelHelper.enableSignEntireHeadersAndBody(bType, true);
                }
                
                SecurityPolicyModelHelper.setTargets(p, DEFAULT_TARGETS);
                
                SecurityPolicyModelHelper.disableWss(par);
                WssElement wss = SecurityPolicyModelHelper.enableWss(par, true);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefThumbprint(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefEncryptedKey(wss, true);

                SecurityPolicyModelHelper.disableTrust10(par);
                trust = SecurityPolicyModelHelper.enableTrust10(par);
                SecurityPolicyModelHelper.enableRequireClientEntropy(trust, true);
                SecurityPolicyModelHelper.enableRequireServerEntropy(trust, true);
                SecurityPolicyModelHelper.enableMustSupportIssuedTokens(trust, true);

            } else {
                WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(c);
                WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
                WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
                WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
                WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);

                WSDLComponent par = topSecBinding.getParent().getParent();

                par.addExtensibilityElement((ExtensibilityElement) secBinding.copy(par));

                for (int suppTokenType=0; suppTokenType < 3; suppTokenType++) {
                    ExtensibilityElement suppToken = 
                            (ExtensibilityElement) SecurityTokensModelHelper.getSupportingToken(secBinding.getParent(), suppTokenType);
                    if (suppToken == null) continue;
                    par.addExtensibilityElement((ExtensibilityElement) suppToken.copy(par));
                    suppToken.getParent().removeExtensibilityElement(suppToken);
                }
                
                WssElement wss10 = SecurityPolicyModelHelper.getWss10(secBinding.getParent());
                if (wss10 != null) {
                    par.addExtensibilityElement((ExtensibilityElement) wss10.copy(par));
                }
                WssElement wss11 = SecurityPolicyModelHelper.getWss11(secBinding.getParent());
                if (wss11 != null) {
                    par.addExtensibilityElement((ExtensibilityElement) wss11.copy(par));
                }
                TrustElement trust = SecurityPolicyModelHelper.getTrust10(secBinding.getParent());
                if (trust != null) {
                    par.addExtensibilityElement((ExtensibilityElement) trust.copy(par));
                }
                
                SecurityPolicyModelHelper.setSecurityBindingType(c, null);
                SecurityPolicyModelHelper.disableWss(c);
                SecurityPolicyModelHelper.disableTrust10(c);
                
                WSDLComponent copyto = PolicyModelHelper.getTopLevelElement(par, All.class);
                WSDLComponent bType = SecurityPolicyModelHelper.getSecurityBindingTypeElement(par);
                copyto.addExtensibilityElement((ExtensibilityElement) bType.copy(copyto));
                bType.getParent().removeExtensibilityElement((ExtensibilityElement) bType);
                wss10 = SecurityPolicyModelHelper.getWss10(par);
                if (wss10 != null) {
                    copyto.addExtensibilityElement((ExtensibilityElement) wss10.copy(copyto));
                    wss10.getParent().removeExtensibilityElement(wss10);
                }
                wss11 = SecurityPolicyModelHelper.getWss11(par);
                if (wss11 != null) {
                    copyto.addExtensibilityElement((ExtensibilityElement) wss11.copy(copyto));
                    wss11.getParent().removeExtensibilityElement(wss11);
                }
                trust = SecurityPolicyModelHelper.getTrust10(par);
                if (trust != null) {
                    copyto.addExtensibilityElement((ExtensibilityElement) trust.copy(copyto));
                    trust.getParent().removeExtensibilityElement(trust);
                }                
                for (int suppTokenType=0; suppTokenType < 3; suppTokenType++) {
                    ExtensibilityElement suppToken = 
                            (ExtensibilityElement) SecurityTokensModelHelper.getSupportingToken(par, suppTokenType);
                    if (suppToken == null) continue;
                    copyto.addExtensibilityElement((ExtensibilityElement) suppToken.copy(copyto));
                    suppToken.getParent().removeExtensibilityElement(suppToken);
                }
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
        
    }    
}
