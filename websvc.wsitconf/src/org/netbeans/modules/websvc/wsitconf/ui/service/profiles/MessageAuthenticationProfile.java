/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.wsitconf.ui.service.profiles;

import java.awt.Dialog;
import javax.swing.JPanel;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.spi.features.ClientDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.SecureConversationFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ServiceDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.AlgoSuiteModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.TransportBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.TrustElement;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * Transport Security Profile definition
 *
 * @author Martin Grebac
 */
public class MessageAuthenticationProfile extends ProfileBase 
        implements SecureConversationFeature,ClientDefaultsFeature,ServiceDefaultsFeature {

    private static final String DEFAULT_USERNAME = "wsit";
    private static final String DEFAULT_PASSWORD = "wsitPassword";
    
    public int getId() {
        return 40;
    }
    
    public String getDisplayName() {
        return ComboConstants.PROF_MSGAUTHSSL;
    }

    public String getDescription() {
        return ComboConstants.PROF_MSGAUTHSSL_INFO;
    }
    
    /**
     * Should return true if the profile is set on component, false otherwise
     */
    public boolean isCurrentProfile(WSDLComponent component) {
        return getDisplayName().equals(ProfilesModelHelper.getWSITSecurityProfile(component));
    }
    
    @Override()
    public void displayConfig(WSDLComponent component, UndoManager undoManager) {
        UndoCounter undoCounter = new UndoCounter();
        WSDLModel model = component.getModel();
        
        model.addUndoableEditListener(undoCounter);

        JPanel profConfigPanel = new MessageAuthentication(component, this);
        DialogDescriptor dlgDesc = new DialogDescriptor(profConfigPanel, getDisplayName());
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true); 
        if (dlgDesc.getValue() == DialogDescriptor.CANCEL_OPTION) {
            for (int i=0; i<undoCounter.getCounter();i++) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        }
        
        model.removeUndoableEditListener(undoCounter);
    }
    
    public void setServiceDefaults(WSDLComponent component, Project p) {
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, false);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, false);
    }
 
    public void setClientDefaults(WSDLComponent component, WSDLComponent serviceBinding, Project p) {
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, true);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, true);
        ProprietarySecurityPolicyModelHelper.setCallbackHandler(
                (Binding)component, CallbackHandler.USERNAME_CBHANDLER, null, DEFAULT_USERNAME, true);
        ProprietarySecurityPolicyModelHelper.setCallbackHandler(
                (Binding)component, CallbackHandler.PASSWORD_CBHANDLER, null, DEFAULT_PASSWORD, true);
    }
    
    public boolean isServiceDefaultSetupUsed(WSDLComponent component, Project p) {
        String keyAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, false);
        String trustAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, true);
        String trustLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, true);
        String keyLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, false);
        String keyPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, false);
        String trustPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, true);
        if ((keyAlias == null) && (trustAlias == null)  && (trustLoc == null) && (keyLoc == null) && (trustPasswd == null) && (keyPasswd == null)) {
            return true;
        }
        return false;
    }

    public boolean isClientDefaultSetupUsed(WSDLComponent component, Binding serviceBinding, Project p) {
        String trustLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, true);
        String keyLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, false);
        String keyPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, false);
        String trustPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, true);
        String keyAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, false);
        String trustAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, true);
        if ((keyAlias == null) && (trustAlias == null)  && (trustLoc == null) && (keyLoc == null) && (trustPasswd == null) && (keyPasswd == null)) {
            String user = ProprietarySecurityPolicyModelHelper.getDefaultUsername((Binding)component);
            String passwd = ProprietarySecurityPolicyModelHelper.getDefaultPassword((Binding)component);
            if ((DEFAULT_PASSWORD.equals(passwd)) && (DEFAULT_USERNAME.equals(user))) {
                if ((trustLoc == null) && (trustPasswd == null) && (keyPasswd != null) && (keyLoc == null)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSecureConversation(WSDLComponent component) {
        WSDLComponent endToken = SecurityTokensModelHelper.getSupportingToken(component, SecurityTokensModelHelper.ENDORSING);
        return (endToken != null);
    }

    public void enableSecureConversation(WSDLComponent component, boolean enable) {
        if (enable) {
            WSDLModel model = component.getModel();

            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }

            try {
                SecurityPolicyModelHelper.enableTrust10(component);
                SecurityTokensModelHelper.removeSupportingTokens(component);
                
                WSDLComponent suppToken = SecurityTokensModelHelper.setSupportingTokens(component, 
                        ComboConstants.SECURECONVERSATION, SecurityTokensModelHelper.ENDORSING);
                Policy p = PolicyModelHelper.createElement(suppToken, PolicyQName.POLICY.getQName(), Policy.class, false);
                BootstrapPolicy bp = PolicyModelHelper.createElement(p, SecurityPolicyQName.BOOTSTRAPPOLICY.getQName(), BootstrapPolicy.class, false);
                p = PolicyModelHelper.createElement(bp, PolicyQName.POLICY.getQName(), Policy.class, false);
                TransportBinding tb = PolicyModelHelper.createElement(p, SecurityPolicyQName.TRANSPORTBINDING.getQName(), TransportBinding.class, false);

                boolean rm = RMModelHelper.isRMEnabled(component);
                SecurityPolicyModelHelper.setDefaultTargets(p, true, rm);
                
                SecurityTokensModelHelper.setTokenType(tb, ComboConstants.TRANSPORT, ComboConstants.HTTPS);
                SecurityPolicyModelHelper.setLayout(tb, ComboConstants.STRICT);
                SecurityPolicyModelHelper.enableIncludeTimestamp(tb, true);
                AlgoSuiteModelHelper.setAlgorithmSuite(tb, ComboConstants.BASIC128);

                SecurityTokensModelHelper.setSupportingTokens(p, 
                        ComboConstants.USERNAME, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                
                WssElement wss = SecurityPolicyModelHelper.enableWss(bp, true);
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefThumbprint(wss, true);
                SecurityPolicyModelHelper.enableMustSupportRefEncryptedKey(wss, true);

                TrustElement trust = SecurityPolicyModelHelper.enableTrust10(bp);
                SecurityPolicyModelHelper.enableMustSupportIssuedTokens(trust, true);
                SecurityPolicyModelHelper.enableRequireClientEntropy(trust, true);
                SecurityPolicyModelHelper.enableRequireServerEntropy(trust, true);
                
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        } else {
            SecurityTokensModelHelper.removeSupportingTokens(component);
            SecurityTokensModelHelper.setSupportingTokens(component, ComboConstants.USERNAME, SecurityTokensModelHelper.SIGNED_SUPPORTING);            
        }
    }
    
}
