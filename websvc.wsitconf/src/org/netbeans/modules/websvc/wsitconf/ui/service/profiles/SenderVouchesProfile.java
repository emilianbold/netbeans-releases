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
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.undo.UndoManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.features.ClientDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.SecureConversationFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ServiceDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wizard.SamlCallbackCreator;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;

/**
 * Transport Security Profile definition
 *
 * @author Martin Grebac
 */
public class SenderVouchesProfile extends SecurityProfile 
        implements SecureConversationFeature,ClientDefaultsFeature,ServiceDefaultsFeature {
    private static final String CERTS_DIR = "certs";
    
    private static final String PKGNAME = "samlcb";

    public int getId() {
        return 70;
    }

    public String getDisplayName() {
        return ComboConstants.PROF_SAMLSENDER;
    }

    public String getDescription() {
        return ComboConstants.PROF_SAMLSENDER_INFO;
    }
    
    /**
     * Called when the profile is selected in the combo box.
     */
    public void profileSelected(WSDLComponent component) {
        ProfilesModelHelper.setSecurityProfile(component, getDisplayName());
        boolean isRM = RMModelHelper.isRMEnabled(component);
        if (isRM) {
            ProfilesModelHelper.enableSecureConversation(component, true);
        }
    }

    /**
     * Called when there's another profile selected, or security is disabled at all.
     */ 
    public void profileDeselected(WSDLComponent component) {
        SecurityPolicyModelHelper.disableSecurity(component, false);
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

        JPanel profConfigPanel = new SenderVouches(component, this);
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
    
    @Override
    public boolean isProfileSupported(Project p, WSDLComponent component, boolean sts) {
        return true;
    }

    public void setClientDefaults(WSDLComponent component, WSDLComponent serviceBinding, Project p) {
        if (Util.isTomcat(p)) {
            FileObject tomcatLoc = Util.getTomcatLocation(p);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(component, 
                    tomcatLoc.getPath() + File.separator + CERTS_DIR + File.separator + "client-keystore.jks", false, true);
            ProprietarySecurityPolicyModelHelper.setStoreType(component, KeystorePanel.JKS, false, true);
            ProprietarySecurityPolicyModelHelper.setStorePassword(component, KeystorePanel.DEFAULT_PASSWORD, false, true);

            ProprietarySecurityPolicyModelHelper.setStoreLocation(component, 
                    tomcatLoc.getPath() + File.separator + CERTS_DIR + File.separator + "client-truststore.jks", true, true);
            ProprietarySecurityPolicyModelHelper.setStoreType(component, KeystorePanel.JKS, true, true);
            ProprietarySecurityPolicyModelHelper.setStorePassword(component, KeystorePanel.DEFAULT_PASSWORD, true, true);
        }
        ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(component,ProfilesModelHelper.XWS_SECURITY_CLIENT, true);
        ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(component,ProfilesModelHelper.XWS_SECURITY_SERVER, true);
                
        FileObject targetFolder = null;
        
        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if ((sourceGroups != null) && (sourceGroups.length > 0)) {
            targetFolder = sourceGroups[0].getRootFolder();
        }
        
        SamlCallbackCreator samlCreator = new SamlCallbackCreator();
        String samlVersion = getSamlVersion((Binding)serviceBinding);
        String cbName = "SamlCallbackHandler";
        
        if (targetFolder != null) {
            if (targetFolder.getFileObject(PKGNAME) == null) {
                try {
                    targetFolder = targetFolder.createFolder(PKGNAME);
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, null, ex);
                }
            } else {
                targetFolder = targetFolder.getFileObject(PKGNAME);
            }
            if (ComboConstants.SAML_V2011.equals(samlVersion)) {
                cbName = "Saml20SVCallbackHandler";
                if (targetFolder.getFileObject(cbName, "java") == null) {
                    samlCreator.generateSamlCBHandler(targetFolder, 
                        cbName, SamlCallbackCreator.SV, SamlCallbackCreator.SAML20);
                }
            } else {
                cbName = "Saml11SVCallbackHandler";
                if (targetFolder.getFileObject(cbName, "java") == null) {
                    samlCreator.generateSamlCBHandler(targetFolder, 
                        cbName, SamlCallbackCreator.SV, SamlCallbackCreator.SAML11);
                }
            }
        }
        ProprietarySecurityPolicyModelHelper.setCallbackHandler(
                (Binding)component, CallbackHandler.SAML_CBHANDLER, PKGNAME + "." + cbName, null, true);
    }

    public void setServiceDefaults(WSDLComponent component, Project p) {
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, false);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, false);
        if (Util.isTomcat(p)) {
            FileObject tomcatLoc = Util.getTomcatLocation(p);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(component, 
                    tomcatLoc.getPath() + File.separator + CERTS_DIR + File.separator + "server-keystore.jks", false, false);
            ProprietarySecurityPolicyModelHelper.setStoreType(component, KeystorePanel.JKS, false, false);
            ProprietarySecurityPolicyModelHelper.setStorePassword(component, KeystorePanel.DEFAULT_PASSWORD, false, false);
        }
        ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(component,ProfilesModelHelper.XWS_SECURITY_SERVER, false);
    }
    
    public boolean isServiceDefaultSetupUsed(WSDLComponent component, Project p) {
        if (ProfilesModelHelper.XWS_SECURITY_SERVER.equals(ProprietarySecurityPolicyModelHelper.getStoreAlias(component, false))) {
            if (Util.isTomcat(p)) {
                FileObject tomcatLoc = Util.getTomcatLocation(p);
                String loc = tomcatLoc.getPath() + File.separator + CERTS_DIR + File.separator + "server-keystore.jks";
                if (loc.equals(ProprietarySecurityPolicyModelHelper.getStoreLocation(component, false))) {
                    if (KeystorePanel.DEFAULT_PASSWORD.equals(ProprietarySecurityPolicyModelHelper.getStorePassword(component, false))) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean isClientDefaultSetupUsed(WSDLComponent component, Binding serviceBinding, Project p) {
        
        String samlVersion = getSamlVersion(serviceBinding);
        String cbName = null;
        
        if (ComboConstants.SAML_V2011.equals(samlVersion)) {
            cbName = "Saml20SVCallbackHandler";
        } else {
            cbName = "Saml11SVCallbackHandler";
        }
        
        String cbHandler = ProprietarySecurityPolicyModelHelper.getCallbackHandler((Binding)component, CallbackHandler.SAML_CBHANDLER);
        if ((PKGNAME + "." + cbName).equals(cbHandler)) {
            if (ProfilesModelHelper.XWS_SECURITY_CLIENT.equals(ProprietarySecurityPolicyModelHelper.getStoreAlias(component, false)) && 
                ProfilesModelHelper.XWS_SECURITY_SERVER.equals(ProprietarySecurityPolicyModelHelper.getStoreAlias(component, true))) {
                    if (Util.isTomcat(p)) {
                        FileObject tomcatLoc = Util.getTomcatLocation(p);
                        String loc = tomcatLoc.getPath() + File.separator + CERTS_DIR + File.separator + "client-truststore.jks";
                        if (loc.equals(ProprietarySecurityPolicyModelHelper.getStoreLocation(component, true))) {
                            if (KeystorePanel.DEFAULT_PASSWORD.equals(ProprietarySecurityPolicyModelHelper.getStorePassword(component, true))) {
                                loc = tomcatLoc.getPath() + File.separator + CERTS_DIR + File.separator + "client-keystore.jks";
                                if (loc.equals(ProprietarySecurityPolicyModelHelper.getStoreLocation(component, false))) {
                                    if (KeystorePanel.DEFAULT_PASSWORD.equals(ProprietarySecurityPolicyModelHelper.getStorePassword(component, false))) {
                                        return true;
                                    }
                                }
                            }
                        }
                    } else {
                        return true;
                    }
            }
        }
        return false;
    }
    
    private String getSamlVersion(Binding serviceBinding) {
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(serviceBinding);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
        
        WSDLComponent tokenKind = null;
        boolean secConv = (protToken instanceof SecureConversationToken);
        
        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
            Policy pp = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class);
            tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, SecurityTokensModelHelper.SIGNED_SUPPORTING);
        } else {
            tokenKind = SecurityTokensModelHelper.getSupportingToken(serviceBinding, SecurityTokensModelHelper.SIGNED_SUPPORTING);
        }

        WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
        return SecurityTokensModelHelper.getTokenProfileVersion(token);
    }
    
    public boolean isSecureConversation(WSDLComponent component) {
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(component);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);        
        return (protToken instanceof SecureConversationToken);
    }

    public void enableSecureConversation(WSDLComponent component, boolean enable) {
        ProfilesModelHelper.enableSecureConversation(component, enable);
    }
    
}
