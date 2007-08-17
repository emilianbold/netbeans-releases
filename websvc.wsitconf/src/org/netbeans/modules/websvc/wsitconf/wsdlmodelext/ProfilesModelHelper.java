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
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.AuthConstraint;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.UserDataConstraint;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfileRegistry;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.TrustElement;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.*;
import org.netbeans.modules.websvc.wsitconf.util.Util;
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
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Grebac
 */
public class ProfilesModelHelper {

    public static final String XWS_SECURITY_SERVER = "xws-security-server";
    public static final String XWS_SECURITY_CLIENT = "xws-security-client";
    public static final String DEFAULT_PASSWORD = "wsit";
    public static final String DEFAULT_USERNAME = "wsit";

    private static final Logger logger = Logger.getLogger(ProfilesModelHelper.class.getName());
    
    /**
     * Creates a new instance of ProfilesModelHelper
     */
    public ProfilesModelHelper() { }

    public static boolean isSSLProfile(String s) {
        if (ComboConstants.PROF_MSGAUTHSSL.equals(s) || 
            ComboConstants.PROF_SAMLSSL.equals(s) ||
            ComboConstants.PROF_TRANSPORT.equals(s)) {
            return true;
        }
        return false;
    }
    
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
    }
    
    public static boolean isServiceDefaultSetupUsed(String profile, Binding binding, Project project) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        return p.isServiceDefaultSetupUsed(binding, project);
    }

    public static boolean isClientDefaultSetupUsed(String profile, Binding binding, WSDLComponent serviceBinding, Project project) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        return p.isClientDefaultSetupUsed(binding, (Binding)serviceBinding, project);
    }
    
    public static void setClientDefaults(String profile, Binding binding, WSDLComponent serviceBinding, Project project) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        p.setClientDefaults(binding, serviceBinding, project);
    }

    public static void setServiceDefaults(String profile, Binding binding, Project project) {
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(profile);
        p.setServiceDefaults(binding, project);
    }
    
    /** Sets security profile on Binding or BindingOperation
     */
    public static void setSecurityProfile(WSDLComponent c, String profile) {
        WSDLModel model = c.getModel();
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        PolicyModelHelper.createPolicy(c, true);
        try {
            // Profile #1
            if (ComboConstants.PROF_TRANSPORT.equals(profile)) {
                WSDLComponent bt = SecurityPolicyModelHelper.setSecurityBindingType(c, ComboConstants.TRANSPORT);
                SecurityTokensModelHelper.setTokenType(bt, ComboConstants.TRANSPORT, ComboConstants.HTTPS);
                SecurityPolicyModelHelper.setLayout(bt, ComboConstants.LAX);
                SecurityPolicyModelHelper.enableIncludeTimestamp(bt, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(bt, ComboConstants.BASIC128);
                SecurityPolicyModelHelper.enableWss(c, false);
                SecurityPolicyModelHelper.disableTrust10(c);
                SecurityTokensModelHelper.removeSupportingTokens(c);
                setMessageLevelSecurityProfilePolicies(c, profile);
            } else if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) { // Profile #2
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
            } else if (ComboConstants.PROF_SAMLSSL.equals(profile)) {   // Profile #3
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
            } else if (ComboConstants.PROF_USERNAME.equals(profile)) {   // Profile #4
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
            } else if (ComboConstants.PROF_MUTUALCERT.equals(profile)) {         // #5
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
            } else if (ComboConstants.PROF_ENDORSCERT.equals(profile)) {               //#6
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
            } else if (ComboConstants.PROF_SAMLSENDER.equals(profile)) {        //#7
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
            } else if (ComboConstants.PROF_SAMLHOLDER.equals(profile)) {        // #8
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
            } else if (ComboConstants.PROF_KERBEROS.equals(profile)) {          //#9
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
            } else if (ComboConstants.PROF_STSISSUED.equals(profile)) {         //#10
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
            } else if (ComboConstants.PROF_STSISSUEDCERT.equals(profile)) {     //#11
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
            } else if (ComboConstants.PROF_STSISSUEDENDORSE.equals(profile)) {  //#12
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
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    private static FileObject getDDFO(WSDLComponent c) {
        if (c != null) {
            WSDLModel model = c.getModel();
            FileObject fo = Util.getFOForModel(model);
            if (fo != null) {
                Project p = FileOwnerQuery.getOwner(fo);
                if (Util.isWebProject(p)) {
                    WebModule wm = WebModule.getWebModule(fo);
                    return wm.getDeploymentDescriptor();
                } else {
                    return Util.getSunDDFO(p);
                }
            }
        }
        return null;
    }
    
    private static SecurityConstraint getSecurityConstraint(WSDLComponent c) {
        FileObject webXmlFO = getDDFO(c);
        if (webXmlFO != null) {
            WebApp webXmlDD = null;
            try {
                webXmlDD = DDProvider.getDefault().getDDRoot(webXmlFO);
            } catch (IOException ioe) {
                logger.log(Level.FINE, null, ioe); //ignore
            }

            String urlPattern = null;

            if (c instanceof Binding) {
                Collection<Service> ss = c.getModel().getDefinitions().getServices();
                for (Service s : ss) {
                    Collection<Port> pp = s.getPorts();
                    for (Port port : pp) {
                        QName qname = port.getBinding().getQName();
                        String bName = ((Binding)c).getName();
                        if (bName.equals(qname.getLocalPart())) {
                            urlPattern = s.getName();
                        }
                    }
                }
            }   

            if ((webXmlDD != null) && (webXmlDD.getStatus()!=WebApp.STATE_INVALID_UNPARSABLE)) {
                SecurityConstraint[] constraints = webXmlDD.getSecurityConstraint();
                for (SecurityConstraint sc : constraints) {
                    WebResourceCollection wrc = sc.getWebResourceCollection(0);
                    if (wrc != null) {
                        String wrcUrlPattern = wrc.getUrlPattern(0);
                        if ((wrcUrlPattern != null) && wrcUrlPattern.contains(urlPattern)) {
                            return sc;
                        }
                    }
                }
            } 
        }
        return null;
    }
                    
    public static void unsetSSLAttributes(final WSDLComponent c) {
        SecurityConstraint sc = getSecurityConstraint(c);
        if (sc != null) {
            try {
                FileObject webXmlFO = getDDFO(c);
                WebApp webXmlDD = DDProvider.getDefault().getDDRoot(webXmlFO);
                if ((webXmlDD != null) && (webXmlDD.getStatus()!=WebApp.STATE_INVALID_UNPARSABLE)) {
                    webXmlDD.removeSecurityConstraint(sc);
                    webXmlDD.write(webXmlFO);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
    public static void setSSLAttributes(final WSDLComponent c) {
        if (getSecurityConstraint(c) == null) {
            FileObject webXmlFO = getDDFO(c);
            try {
                WebApp webXmlDD = DDProvider.getDefault().getDDRoot(webXmlFO);
                if ((webXmlDD != null) && (webXmlDD.getStatus()!=WebApp.STATE_INVALID_UNPARSABLE)) {
                    SecurityConstraint sc = (SecurityConstraint) webXmlDD.createBean("SecurityConstraint");

                    AuthConstraint ac = (AuthConstraint) webXmlDD.createBean("AuthConstraint");
                    ac.addRoleName("EMPLOYEE");
                    sc.setAuthConstraint(ac);

                    UserDataConstraint udc = (UserDataConstraint) webXmlDD.createBean("UserDataConstraint");
                    udc.setTransportGuarantee("CONFIDENTIAL");
                    sc.setUserDataConstraint(udc);

                    String urlPattern = "/";
                    boolean exit = false;
                    if (c instanceof Binding) {
                        Collection<Service> ss = c.getModel().getDefinitions().getServices();
                        for (Service s : ss) {
                            Collection<Port> pp = s.getPorts();
                            for (Port port : pp) {
                                QName qname = port.getBinding().getQName();
                                String bName = ((Binding)c).getName();
                                if (bName.equals(qname.getLocalPart())) {
                                    urlPattern = urlPattern.concat(s.getName() + "/*");
                                    exit = true;
                                    break;
                                }
                            }
                            if (exit) break;
                        }
                    }   
                    WebResourceCollection wrc = (WebResourceCollection) 
                        webXmlDD.createBean("WebResourceCollection");
                    wrc.setHttpMethod(new String[] {"POST"});
                    wrc.setUrlPattern(new String[] {urlPattern});
                    wrc.setWebResourceName("Secure Area");
                    sc.addWebResourceCollection(wrc);

                    webXmlDD.addSecurityConstraint(sc);
                    webXmlDD.write(webXmlFO);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
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

        Binding b = (Binding) o.getParent();
        
        boolean wss11 = SecurityPolicyModelHelper.isWss11(b);                
        boolean rm = RMModelHelper.isRMEnabled(b);
        
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
                return;
            }
            // Profile #3
            if (ComboConstants.PROF_SAMLSSL.equals(profile)) {
                return;
            }
            // Profile #4
            if (ComboConstants.PROF_USERNAME.equals(profile)) {
                SecurityPolicyModelHelper.setDefaultTargets(input, wss11, rm);
                SecurityPolicyModelHelper.setDefaultTargets(output, wss11, rm);
                return;
            }
            // Profile #5
            if (ComboConstants.PROF_MUTUALCERT.equals(profile)) {
                SecurityPolicyModelHelper.setDefaultTargets(input, true, rm);
                SecurityPolicyModelHelper.setDefaultTargets(output, true, rm);
                return;
            }
            // Profile #6
            if (ComboConstants.PROF_ENDORSCERT.equals(profile)) {
                SecurityPolicyModelHelper.setDefaultTargets(input, wss11, rm);
                SecurityPolicyModelHelper.setDefaultTargets(output, wss11, rm);
                return;
            }
            // Profile #7
            if (ComboConstants.PROF_SAMLSENDER.equals(profile)) {
                SecurityPolicyModelHelper.setDefaultTargets(input, wss11, rm);
                SecurityPolicyModelHelper.setDefaultTargets(output, wss11, rm);
                return;
            }
            // Profile #8
            if (ComboConstants.PROF_SAMLHOLDER.equals(profile)) {
                SecurityPolicyModelHelper.setDefaultTargets(input, wss11, rm);
                SecurityPolicyModelHelper.setDefaultTargets(output, wss11, rm);
                return;
            }
            // Profile #9
            if (ComboConstants.PROF_KERBEROS.equals(profile)) {
                SecurityPolicyModelHelper.setDefaultTargets(input, wss11, rm);
                SecurityPolicyModelHelper.setDefaultTargets(output, wss11, rm);
                return;
            }
            // Profile #10
            if (ComboConstants.PROF_STSISSUED.equals(profile)) {
                SecurityPolicyModelHelper.setDefaultTargets(input, wss11, rm);
                SecurityPolicyModelHelper.setDefaultTargets(output, wss11, rm);
                return;
            }
            // Profile #11
            if (ComboConstants.PROF_STSISSUEDCERT.equals(profile)) {
                SecurityPolicyModelHelper.setDefaultTargets(input, wss11, rm);
                SecurityPolicyModelHelper.setDefaultTargets(output, wss11, rm);
                return;
            }
            // Profile #12
            if (ComboConstants.PROF_STSISSUEDENDORSE.equals(profile)) {
                SecurityPolicyModelHelper.setDefaultTargets(input, wss11, rm);
                SecurityPolicyModelHelper.setDefaultTargets(output, wss11, rm);
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

        Binding b = null;
        if (c instanceof BindingOperation) {
            b = (Binding) c.getParent();
        } else {
            b = (Binding) c;
        }
        
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
                
                boolean rm = RMModelHelper.isRMEnabled(b);
                SecurityPolicyModelHelper.setDefaultTargets(p, true, rm);
                
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
