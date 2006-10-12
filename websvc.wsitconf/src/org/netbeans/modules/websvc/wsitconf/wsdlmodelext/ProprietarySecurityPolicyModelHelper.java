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

import org.netbeans.modules.websvc.wsitconf.Utilities;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.ServiceProviderElement;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.All;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.ExactlyOne;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.proprietary.*;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.proprietary.service.CertAlias;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.proprietary.service.Contract;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.proprietary.service.ProprietarySCServiceQName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.proprietary.service.ProprietarySecurityPolicyServiceQName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.proprietary.service.ProprietaryTrustServiceQName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.proprietary.service.SCConfiguration;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.proprietary.service.STSConfiguration;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.proprietary.service.TokenType;
import org.netbeans.modules.xml.wsdl.model.*;
import org.openide.ErrorManager;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.security.proprietary.service.ServiceProvider;

/**
 *
 * @author Martin Grebac
 */
public class ProprietarySecurityPolicyModelHelper {
    
    /**
     * Creates a new instance of ProprietarySecurityPolicyModelHelper
     */
    public ProprietarySecurityPolicyModelHelper() {
    }
   
    public static String getMaxNonceAge(Binding b, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        ValidatorConfiguration vc = getValidatorConfiguration(p);
        if (vc != null) {
            return vc.getMaxNonceAge();
        }
        return null;
    }

    public static String getSTSLifeTime(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        WSDLComponent sc = getSTSConfiguration(p);
        if (sc != null) {
            List<LifeTime> elems = sc.getExtensibilityElements(LifeTime.class);
            if ((elems != null) && (!elems.isEmpty())) {
                return elems.get(0).getLifeTime();
            }
        }
        return null;
    }

    public static STSConfiguration getSTSConfiguration(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        return getSTSConfiguration(p);
    }
    
    public static String getSTSContractClass(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        WSDLComponent sc = getSTSConfiguration(p);
        if (sc != null) {
            List<Contract> elems = sc.getExtensibilityElements(Contract.class);
            if ((elems != null) && (!elems.isEmpty())) {
                return elems.get(0).getContract();
            }
        }
        return null;
    }

    public static boolean getSTSEncryptKey(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        STSConfiguration sc = getSTSConfiguration(p);
        if (sc != null) {
            return sc.getEncryptIssuedKey();
        }
        return false;
    }
    
    public static boolean getSTSEncryptToken(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        STSConfiguration sc = getSTSConfiguration(p);
        if (sc != null) {
            return sc.getEncryptIssuedToken();
        }
        return false;
    }

    public static String getSPCertAlias(ServiceProvider sp) {
        WSDLModel model = sp.getModel();
        if (sp != null) {
            List<CertAlias> elems = sp.getExtensibilityElements(CertAlias.class);
            if ((elems != null) && !(elems.isEmpty())) {
                return elems.get(0).getCertAlias();
            }
        }
        return null;
    }

    public static String getSPTokenType(ServiceProvider sp) {
        WSDLModel model = sp.getModel();
        if (sp != null) {
            List<TokenType> elems = sp.getExtensibilityElements(TokenType.class);
            if ((elems != null) && !(elems.isEmpty())) {
                return elems.get(0).getTokenType();
            }
        }
        return null;
    }
    
    public static List<ServiceProvider> getSTSServiceProviders(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        WSDLComponent sc = getSTSConfiguration(p);
        if (sc != null) {
            List<ServiceProvider> elems = sc.getExtensibilityElements(ServiceProvider.class);
            return elems;
        }
        return null;
    }

    public static List<ServiceProvider> getSTSServiceProviders(STSConfiguration stsConfig) {
        WSDLModel model = stsConfig.getModel();
        if (stsConfig != null) {
            List<ServiceProvider> elems = stsConfig.getExtensibilityElements(ServiceProvider.class);
            return elems;
        }
        return null;
    }

    public static String getLifeTime(Binding b, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        WSDLComponent sc = null;
        if (client) { 
            sc = getSCClientConfiguration(p);
        } else {
            sc = getSCConfiguration(p);
        }
        if (sc != null) {
            List<LifeTime> elems = sc.getExtensibilityElements(LifeTime.class);
            if ((elems != null) && (!elems.isEmpty())) {
                return elems.get(0).getLifeTime();
            }
        }
        return null;
    }

    public static String getPreSTSEndpoint(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps != null) {
            return ps.getEndpoint();
        }
        return null;
    }

    public static String getPreSTSNamespace(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps != null) {
            return ps.getNamespace();
        }
        return null;
    }

    public static String getPreSTSPortName(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps != null) {
            return ps.getPortName();
        }
        return null;
    }

    public static String getPreSTSServiceName(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps != null) {
            return ps.getServiceName();
        }
        return null;
    }

    public static String getPreSTSWsdlLocation(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps != null) {
            return ps.getWsdlLocation();
        }
        return null;
    }
    
    public static boolean isRenewExpired(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        SCClientConfiguration sc = getSCClientConfiguration(p);
        if (sc != null) {
            return sc.getRenewExpiredSCT();
        }
        return false;
    }

    public static boolean isRequireCancel(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        SCClientConfiguration sc = getSCClientConfiguration(p);
        if (sc != null) {
            return sc.getRequireCancelSCT();
        }
        return false;
    }
    
    public static String getMaxClockSkew(Binding b, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        ValidatorConfiguration vc = getValidatorConfiguration(p);
        if (vc != null) {
            return vc.getMaxClockSkew();
        }
        return null;
    }
    
    public static String getTimestampFreshness(Binding b, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        ValidatorConfiguration vc = getValidatorConfiguration(p);
        if (vc != null) {
            return vc.getTimestampFreshnessLimit();
        }
        return null;
    }

    public static WSDLComponent getStore(Policy p, boolean trust) {
        if (trust) {
            return getPropConfigElement(p, TrustStore.class);
        } else {
            return getPropConfigElement(p, KeyStore.class);
        }
    }

    public static ValidatorConfiguration getValidatorConfiguration(Policy p) {
        return (ValidatorConfiguration) getPropConfigElement(p, ValidatorConfiguration.class);
    }

    public static CallbackHandlerConfiguration getCallbackHandlerConfiguration(Policy p) {
        return (CallbackHandlerConfiguration) getPropConfigElement(p, CallbackHandlerConfiguration.class);
    }

    public static SCClientConfiguration getSCClientConfiguration(Policy p) {
            return (SCClientConfiguration) getPropConfigElement(p, SCClientConfiguration.class);
    }
    
    public static PreconfiguredSTS getPreconfiguredSTS(Policy p) {
            return (PreconfiguredSTS) getPropConfigElement(p, PreconfiguredSTS.class);
    }

    public static STSConfiguration getSTSConfiguration(Policy p) {
            return (STSConfiguration) getPropConfigElement(p, STSConfiguration.class);
    }

    public static SCConfiguration getSCConfiguration(Policy p) {
            return (SCConfiguration) getPropConfigElement(p, SCConfiguration.class);
    }

    public static WSDLComponent getPropConfigElement(Policy p, Class elementclass) {
        if (p != null) {
            ExactlyOne eo = p.getExactlyOne();
            if (eo != null) {
                All all = eo.getAll();
                if (all != null) {
                    List vcList = all.getExtensibilityElements(elementclass);
                    if ((vcList != null) && (vcList.size() > 0)) {
                        return (WSDLComponent) vcList.get(0);
                    }
                }
            }
        }
        return null;
    }
    
    public static String getStoreLocation(Binding b, boolean trust) {
        if (b == null) return null;
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p == null) return null;
        if (trust) {
            TrustStore ts = (TrustStore) getStore(p, true);
            return ts == null ? null : ts.getLocation();
        } else {
            KeyStore ks = (KeyStore) getStore(p, false);
            return ks == null ? null : ks.getLocation();
        }
    }

    public static String getKeyStoreAlias(Binding b) {
        if (b == null) return null;
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p == null) return null;
        KeyStore ks = (KeyStore) getStore(p, false);
        return (ks != null) ? ks.getAlias() : null;
    }

    public static String getTrustSTSAlias(Binding b) {
        if (b == null) return null;
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p == null) return null;
        TrustStore ks = (TrustStore) getStore(p, true);
        return (ks != null) ? ks.getSTSAlias() : null;
    }

    public static String getTrustPeerAlias(Binding b) {
        if (b == null) return null;
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p == null) return null;
        TrustStore ks = (TrustStore) getStore(p, true);
        return (ks != null) ? ks.getPeerAlias() : null;
    }

    public static String getValidator(Binding b, String validatorType) {
        if (b == null) return null;
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p == null) return null;
        ValidatorConfiguration vc = (ValidatorConfiguration) getValidatorConfiguration(p);
        Validator v = getValidator(validatorType, vc);
        if (v != null) {
            return v.getClassname();
        }
        return null;
    }
    
    private static Validator getValidator(String type, ValidatorConfiguration vc) {
        if (vc == null) return null;
        List<Validator> validators = vc.getExtensibilityElements(Validator.class);
        for (Validator v : validators) {
            if (type.equals(v.getName())) {
                return v;
            }
        }
        return null;
    }

    private static LifeTime getLifeTime(WSDLComponent c) {
        if (c != null) {
            List<LifeTime> attrs = c.getExtensibilityElements(LifeTime.class);
            if ((attrs != null) && !(attrs.isEmpty())) {
                return attrs.get(0);
            }
        }
        return null;
    }

    private static Contract getContract(WSDLComponent c) {
        if (c != null) {
            List<Contract> attrs = c.getExtensibilityElements(Contract.class);
            if ((attrs != null) && !(attrs.isEmpty())) {
                return attrs.get(0);
            }
        }
        return null;
    }
    
    public static String getDefaultUsername(Binding b) {
        if (b == null) return null;
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p == null) return null;
        CallbackHandlerConfiguration chc = (CallbackHandlerConfiguration) getCallbackHandlerConfiguration(p);
        CallbackHandler ch = getCallbackHandler(CallbackHandler.USERNAME_CBHANDLER, chc);
        if (ch != null) {
            return ch.getDefault();
        }
        return null;
    }

    public static String getDefaultPassword(Binding b) {
        if (b == null) return null;
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p == null) return null;
        CallbackHandlerConfiguration chc = (CallbackHandlerConfiguration) getCallbackHandlerConfiguration(p);
        CallbackHandler ch = getCallbackHandler(CallbackHandler.PASSWORD_CBHANDLER, chc);
        if (ch != null) {
            return ch.getDefault();
        }
        return null;
    }
    
    public static String getCallbackHandler(Binding b, String cbhType) {
        if (b == null) return null;
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p == null) return null;
        CallbackHandlerConfiguration chc = (CallbackHandlerConfiguration) getCallbackHandlerConfiguration(p);
        CallbackHandler ch = getCallbackHandler(cbhType, chc);
        if (ch != null) {
            return ch.getClassname();
        }
        return null;
    }
    
    private static CallbackHandler getCallbackHandler(String type, CallbackHandlerConfiguration vc) {
        if (vc == null) return null;
        List<CallbackHandler> handlers = vc.getExtensibilityElements(CallbackHandler.class);
        for (CallbackHandler h : handlers) {
            if (type.equals(h.getName())) {
                return h;
            }
        }
        return null;
    }
    
    public static String getStorePassword(Binding b, boolean trust) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p == null) return null;
        if (trust) {
            TrustStore ts = (TrustStore) getStore(p, true);
            return ts.getStorePassword();
        } else {
            KeyStore ks = (KeyStore) getStore(p, false);
            return ks.getStorePassword();
        }
    }

    public static void setStoreLocation(Binding b, String value, boolean trust, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (trust) {
                TrustStore ks = (TrustStore) getStore(p, trust);
                if ((p == null) || (ks == null)) {
                    ks = (TrustStore) createStore(b, trust, client);
                }
                ks.setLocation(value);
            } else {
                KeyStore ks = (KeyStore) getStore(p, trust);
                if ((p == null) || (ks == null)) {
                    ks = (KeyStore) createStore(b, trust, client);
                }
                ks.setLocation(value);
            }
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setKeyStoreAlias(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        KeyStore ks = (KeyStore) getStore(p, false);
        if ((p == null) || (ks == null)) {
            ks = (KeyStore) createStore(b, false, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ks.setAlias(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setTrustServiceAlias(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        TrustStore ks = (TrustStore) getStore(p, true);
        if ((p == null) || (ks == null)) {
            ks = (TrustStore) createStore(b, true, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ks.setServiceAlias(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setTrustSTSAlias(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        TrustStore ks = (TrustStore) getStore(p, true);
        if ((p == null) || (ks == null)) {
            ks = (TrustStore) createStore(b, true, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ks.setSTSAlias(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setTrustPeerAlias(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        TrustStore ks = (TrustStore) getStore(p, true);
        if ((p == null) || (ks == null)) {
            ks = (TrustStore) createStore(b, true, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ks.setPeerAlias(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setValidator(Binding b, String type, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        ValidatorConfiguration vc = (ValidatorConfiguration) getValidatorConfiguration(p);
        if ((p == null) || (vc == null)) {
            vc = (ValidatorConfiguration) createValidatorConfiguration(b, client);
        }
        Validator v = getValidator(type, vc);
        if (v == null) {
            v = createValidator(vc);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            v.setName(type);
            v.setClassname(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setLifeTime(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        WSDLComponent c = client ? getSCClientConfiguration(p) : getSCConfiguration(p);
        if ((p == null) || (c == null)) {
            c = createSCConfiguration(b, client);
        }
        LifeTime lt = getLifeTime(c);
        if (lt == null) {
            lt = createLifeTime(c, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            lt.setLifeTime(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setSTSLifeTime(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        STSConfiguration c = getSTSConfiguration(p);
        if ((p == null) || (c == null)) {
            c = createSTSConfiguration(b);
        }
        LifeTime lt = getLifeTime(c);
        if (lt == null) {
            lt = createSTSLifeTime(c);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            lt.setLifeTime(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void addSTSServiceProvider(STSConfiguration stsConfig, ServiceProviderElement spe) {
        WSDLModel model = stsConfig.getModel();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            WSDLComponentFactory wcf = model.getFactory();
            ServiceProvider sp = (ServiceProvider)wcf.create(stsConfig, ProprietaryTrustServiceQName.SERVICEPROVIDER.getQName());
            stsConfig.addExtensibilityElement(sp);
            sp.setEndpoint(spe.getEndpoint());
            CertAlias calias = (CertAlias)wcf.create(sp, ProprietaryTrustServiceQName.CERTALIAS.getQName());
            sp.addExtensibilityElement(calias);
            calias.setCertAlias(spe.getCertAlias());
            TokenType ttype = (TokenType)wcf.create(sp, ProprietaryTrustServiceQName.TOKENTYPE.getQName());
            sp.addExtensibilityElement(ttype);
            ttype.setTokenType(spe.getTokenType());
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void removeSTSServiceProvider(STSConfiguration stsConfig, ServiceProviderElement spe) {
        WSDLModel model = stsConfig.getModel();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            WSDLComponentFactory wcf = model.getFactory();
            List<ServiceProvider> spList = stsConfig.getExtensibilityElements(ServiceProvider.class);
            for (ServiceProvider sp : spList) {
                if (spe.getEndpoint().equals(sp.getEndpoint())) {
                    stsConfig.removeExtensibilityElement(sp);
                    break;
                }
            }
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
    
    public static void setSTSContractClass(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        STSConfiguration c = getSTSConfiguration(p);
        if ((p == null) || (c == null)) {
            c = createSTSConfiguration(b);
        }
        Contract contract = getContract(c);
        if (contract == null) {
            contract = createSTSContract(c);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            contract.setContract(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setSTSEncryptKey(Binding b, boolean enable) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        STSConfiguration sc = getSTSConfiguration(p);
        if ((p == null) || (sc == null)) {
            sc = (STSConfiguration) createSTSConfiguration(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            sc.setEncryptIssuedKey(enable);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
    
    public static void setSTSEncryptToken(Binding b, boolean enable) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        STSConfiguration sc = getSTSConfiguration(p);
        if ((p == null) || (sc == null)) {
            sc = (STSConfiguration) createSTSConfiguration(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            sc.setEncryptIssuedToken(enable);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setRequireCancel(Binding b, boolean enable) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        SCClientConfiguration sc = getSCClientConfiguration(p);
        if ((p == null) || (sc == null)) {
            sc = (SCClientConfiguration) createSCConfiguration(b, true);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            sc.setRequireCancelSCT(enable);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setRenewExpired(Binding b, boolean enable) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        SCClientConfiguration sc = getSCClientConfiguration(p);
        if ((p == null) || (sc == null)) {
            sc = (SCClientConfiguration) createSCConfiguration(b, true);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            sc.setRenewExpiredSCT(enable);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
    
    public static void setCallbackHandler(Binding b, String type, String value, String defaultVal, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        CallbackHandlerConfiguration chc = (CallbackHandlerConfiguration) getCallbackHandlerConfiguration(p);
        if ((p == null) || (chc == null)) {
            chc = (CallbackHandlerConfiguration) createCallbackHandlerConfiguration(b, client);
        }
        CallbackHandler h = getCallbackHandler(type, chc);
        if (h == null) {
            h = createCallbackHandler(chc, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            h.setDefault(defaultVal);
            h.setName(type);
            h.setClassname(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setStoreType(Binding b, String value, boolean trust, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (trust) {
                TrustStore ks = (TrustStore) getStore(p, trust);
                if ((p == null) || (ks == null)) {
                    ks = (TrustStore) createStore(b, trust, client);
                }
                ks.setType(value);
            } else {
                KeyStore ks = (KeyStore) getStore(p, trust);
                if ((p == null) || (ks == null)) {
                    ks = (KeyStore) createStore(b, trust, client);
                }
                ks.setType(value);
            }
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setStorePassword(Binding b, String value, boolean trust, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (trust) {
                TrustStore ks = (TrustStore) getStore(p, trust);
                if ((p == null) || (ks == null)) {
                    ks = (TrustStore) createStore(b, trust, client);
                }
                ks.setStorePassword(value);
            } else {
                KeyStore ks = (KeyStore) getStore(p, trust);
                if ((p == null) || (ks == null)) {
                    ks = (KeyStore) createStore(b, trust, client);
                }
                ks.setStorePassword(value);
            }
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setPreSTSEndpoint(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if ((ps == null) || (p == null)) {
            ps = createPreconfiguredSTS(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ps.setEndpoint(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setPreSTSNamespace(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if ((ps == null) || (p == null)) {
            ps = createPreconfiguredSTS(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ps.setNamespace(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
    
    public static void setPreSTSServiceName(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if ((ps == null) || (p == null)) {
            ps = createPreconfiguredSTS(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ps.setServiceName(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setPreSTSPortName(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if ((ps == null) || (p == null)) {
            ps = createPreconfiguredSTS(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ps.setPortName(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
    
    public static void setPreSTSWsdlLocation(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if ((ps == null) || (p == null)) {
            ps = createPreconfiguredSTS(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ps.setWsdlLocation(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
    
    public static void setMaxClockSkew(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        ValidatorConfiguration vc = getValidatorConfiguration(p);        
        if ((vc == null) || (p == null)) {
            vc = createValidatorConfiguration(b, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            vc.setMaxClockSkew(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setTimestampFreshness(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        ValidatorConfiguration vc = getValidatorConfiguration(p);
        if ((vc == null) || (p == null)) {
            vc = createValidatorConfiguration(b, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            vc.setTimestampFreshnessLimit(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }

    public static void setMaxNonceAge(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        ValidatorConfiguration vc = getValidatorConfiguration(p);
        if ((vc == null) || (p == null)) {
            vc = createValidatorConfiguration(b, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            vc.setMaxNonceAge(value);
        } finally {
            if (!isTransaction) {
                    model.endTransaction();
            }
        }
    }
    
    public static WSDLComponent createStore(Binding b, boolean trust, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        if (trust) {
            TrustStore ks = (TrustStore) getStore(p, trust);
            if (ks == null) {
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
                    if (client) {
                        ks = (TrustStore)wcf.create(all, ProprietarySecurityPolicyQName.TRUSTSTORE.getQName());
                    } else {
                        ks = (TrustStore)wcf.create(all, ProprietarySecurityPolicyServiceQName.TRUSTSTORE.getQName());
                    }
                    all.addExtensibilityElement(ks);
//                    ks.setVisibility(ProprietaryPolicyQName.INVISIBLE);
                } finally {
                    if (!isTransaction) {
                            model.endTransaction();
                    }
                }
                return ks;
            }
        } else {
            KeyStore ks = (KeyStore) getStore(p, trust);
            if (ks == null) {
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
                    if (client) {
                        ks = (KeyStore)wcf.create(all, ProprietarySecurityPolicyQName.KEYSTORE.getQName());
                    } else {
                        ks = (KeyStore)wcf.create(all, ProprietarySecurityPolicyServiceQName.KEYSTORE.getQName());
                    }
                    all.addExtensibilityElement(ks);
//                    ks.setVisibility(ProprietaryPolicyQName.INVISIBLE);
                } finally {
                    if (!isTransaction) {
                            model.endTransaction();
                    }
                }
                return ks;
            }
        }
        return null;
    }
    
    public static ValidatorConfiguration createValidatorConfiguration(Binding b, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        ValidatorConfiguration vc = getValidatorConfiguration(p);
        if (vc == null) {
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
                if (client) {
                    vc = (ValidatorConfiguration)wcf.create(all, ProprietarySecurityPolicyQName.VALIDATORCONFIGURATION.getQName());
                } else {
                    vc = (ValidatorConfiguration)wcf.create(all, ProprietarySecurityPolicyServiceQName.VALIDATORCONFIGURATION.getQName());
                }
                all.addExtensibilityElement(vc);
//                vc.setVisibility(ProprietaryPolicyQName.INVISIBLE);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
        return vc;
    }

    public static PreconfiguredSTS createPreconfiguredSTS(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps == null) {
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
                ps = (PreconfiguredSTS)wcf.create(all, ProprietaryTrustClientQName.PRECONFIGUREDSTS.getQName());
                all.addExtensibilityElement(ps);
//                ps.setVisibility(ProprietaryPolicyQName.INVISIBLE);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
        return ps;
    }

    public static STSConfiguration createSTSConfiguration(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        STSConfiguration sc = getSTSConfiguration(p);
        if (sc == null) {
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
                sc = (STSConfiguration)wcf.create(all, ProprietaryTrustServiceQName.STSCONFIGURATION.getQName());
                all.addExtensibilityElement(sc);
//                sc.setVisibility(ProprietaryPolicyQName.INVISIBLE);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
        return sc;
    }
    
    public static WSDLComponent createSCConfiguration(Binding b, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        WSDLComponent c = client ? getSCClientConfiguration(p) : getSCConfiguration(p);
        if (c == null) {
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
                if (client) {
                    c = (SCClientConfiguration)wcf.create(all, ProprietarySCClientQName.SCCLIENTCONFIGURATION.getQName());
                    all.addExtensibilityElement((ExtensibilityElement) c);
//                    ((SCClientConfiguration)c).setVisibility(ProprietaryPolicyQName.INVISIBLE);
                } else {
                    c = (SCConfiguration)wcf.create(all, ProprietarySCServiceQName.SCCONFIGURATION.getQName());
                    all.addExtensibilityElement((ExtensibilityElement) c);
//                    ((SCConfiguration)c).setVisibility(ProprietaryPolicyQName.INVISIBLE);
                }
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
        return c;
    }
    
    public static Validator createValidator(ValidatorConfiguration vc) {
        WSDLModel model = vc.getModel();
        Validator v = null;
        if (vc != null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                v = (Validator)wcf.create(vc, ProprietarySecurityPolicyQName.VALIDATOR.getQName());
                vc.addExtensibilityElement(v);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
        return v;
    }

    public static LifeTime createLifeTime(WSDLComponent c, boolean client) {
        WSDLModel model = c.getModel();
        LifeTime lt = null;
        if (c != null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                if (client) {
                    lt = (LifeTime)wcf.create(c, ProprietarySCClientQName.LIFETIME.getQName());
                } else {
                    lt = (LifeTime)wcf.create(c, ProprietarySCServiceQName.LIFETIME.getQName());
                }
                c.addExtensibilityElement(lt);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
        return lt;
    }

    public static LifeTime createSTSLifeTime(WSDLComponent c) {
        WSDLModel model = c.getModel();
        LifeTime lt = null;
        if (c != null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                lt = (LifeTime)wcf.create(c, ProprietaryTrustServiceQName.LIFETIME.getQName());
                c.addExtensibilityElement(lt);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
        return lt;
    }

    public static Contract createSTSContract(WSDLComponent c) {
        WSDLModel model = c.getModel();
        Contract contract = null;
        if (c != null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                contract = (Contract)wcf.create(c, ProprietaryTrustServiceQName.CONTRACT.getQName());
                c.addExtensibilityElement(contract);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
        return contract;
    }
    
    public static CallbackHandlerConfiguration createCallbackHandlerConfiguration(Binding b, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = Utilities.getPolicyForElement(b, model);
        CallbackHandlerConfiguration chc = getCallbackHandlerConfiguration(p);
        if (chc == null) {
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

                if (client) {
                    chc = (CallbackHandlerConfiguration)wcf.create(all, 
                            ProprietarySecurityPolicyQName.CALLBACKHANDLERCONFIGURATION.getQName());
                } else {
                    chc = (CallbackHandlerConfiguration)wcf.create(all, 
                            ProprietarySecurityPolicyServiceQName.CALLBACKHANDLERCONFIGURATION.getQName());
                }
                all.addExtensibilityElement(chc);
//                chc.setVisibility(ProprietaryPolicyQName.INVISIBLE);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
        return chc;
    }

    public static CallbackHandler createCallbackHandler(CallbackHandlerConfiguration chc, boolean client) {
        WSDLModel model = chc.getModel();
        CallbackHandler h = null;
        if (chc != null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                if (client) {
                    h = (CallbackHandler)wcf.create(chc, ProprietarySecurityPolicyQName.CALLBACKHANDLER.getQName());
                } else {
                    h = (CallbackHandler)wcf.create(chc, ProprietarySecurityPolicyServiceQName.CALLBACKHANDLER.getQName());
                }
                chc.addExtensibilityElement(h);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
        return h;
    }
}
