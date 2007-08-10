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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wizard.SamlCallbackCreator;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.InitiatorToken;
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
public class SAMLHolderOfKeyProfile extends SecurityProfile {
    private static final String CERTS_DIR = "certs";

    private static final String PKGNAME = "samlcb";
    
    public int getId() {
        return 80;
    }

    public String getDisplayName() {
        return ComboConstants.PROF_SAMLHOLDER;
    }

    public String getDescription() {
        return ComboConstants.PROF_SAMLHOLDER_INFO;
    }
    
    /**
     * Called when the profile is selected in the combo box.
     */
    public void profileSelected(WSDLComponent component) {
        ProfilesModelHelper.setSecurityProfile(component, getDisplayName());
        boolean isRM = RMModelHelper.isRMEnabled(component);
        if (isRM) {
            ProfilesModelHelper.enableSecureConversation(component, true, getDisplayName());
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

        JPanel profConfigPanel = new SAMLHolderOfKey(component);
        DialogDescriptor dlgDesc = new DialogDescriptor(profConfigPanel, getDisplayName());
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true); 
        if (dlgDesc.getValue() == dlgDesc.CANCEL_OPTION) {
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
        return !sts;
    }

    @Override
    public void setServiceDefaults(WSDLComponent component, Project p) {
        if (Util.isTomcat(p)) {
            FileObject tomcatLoc = Util.getTomcatLocation(p);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(component, 
                    tomcatLoc.getPath() + File.separator + CERTS_DIR + File.separator + "server-keystore.jks", false, false);
            ProprietarySecurityPolicyModelHelper.setStoreType(component, KeystorePanel.JKS, false, false);
            ProprietarySecurityPolicyModelHelper.setStorePassword(component, KeystorePanel.DEFAULT_PASSWORD, false, false);
        }
        ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(component,ProfilesModelHelper.XWS_SECURITY_SERVER, false);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, false);
    }
    
    private String getSamlVersion(Binding serviceBinding) {
        WSDLComponent secBinding = null;
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(serviceBinding);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);

        boolean secConv = (protToken instanceof SecureConversationToken);

        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(serviceBinding);
        }

        WSDLComponent tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, InitiatorToken.class);
        WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
        return SecurityTokensModelHelper.getTokenProfileVersion(token);
    }
    
    @Override
    public void setClientDefaults(WSDLComponent binding, WSDLComponent serviceBinding, Project p) {

        if (Util.isTomcat(p)) {
            FileObject tomcatLoc = Util.getTomcatLocation(p);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(binding, 
                    tomcatLoc.getPath() + File.separator + CERTS_DIR + File.separator + "client-keystore.jks", false, true);
            ProprietarySecurityPolicyModelHelper.setStoreType(binding, KeystorePanel.JKS, false, true);
            ProprietarySecurityPolicyModelHelper.setStorePassword(binding, KeystorePanel.DEFAULT_PASSWORD, false, true);

            ProprietarySecurityPolicyModelHelper.setStoreLocation(binding, 
                    tomcatLoc.getPath() + File.separator + CERTS_DIR + File.separator + "client-truststore.jks", true, true);
            ProprietarySecurityPolicyModelHelper.setStoreType(binding, KeystorePanel.JKS, true, true);
            ProprietarySecurityPolicyModelHelper.setStorePassword(binding, KeystorePanel.DEFAULT_PASSWORD, true, true);
        }
        ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(binding, 
                ProfilesModelHelper.XWS_SECURITY_CLIENT, true);
        ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(binding, 
                ProfilesModelHelper.XWS_SECURITY_SERVER, true);

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
                cbName = "Saml20HOKCallbackHandler";
                if (targetFolder.getFileObject(cbName, "java") == null) {
                    samlCreator.generateSamlCBHandler(targetFolder, 
                        cbName, SamlCallbackCreator.HOK, SamlCallbackCreator.SAML20);
                }
            } else {
                cbName = "Saml11HOKCallbackHandler";
                if (targetFolder.getFileObject(cbName, "java") == null) {
                    samlCreator.generateSamlCBHandler(targetFolder, 
                        cbName, SamlCallbackCreator.HOK, SamlCallbackCreator.SAML11);
                }
            }
        }
        ProprietarySecurityPolicyModelHelper.setCallbackHandler(
                (Binding)binding, CallbackHandler.SAML_CBHANDLER, PKGNAME + "." + cbName, null, true);
    }
    
    @Override
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

    @Override
    public boolean isClientDefaultSetupUsed(WSDLComponent component, Binding serviceBinding, Project p) {
        String samlVersion = getSamlVersion(serviceBinding);
        String cbName = null;
        
        if (ComboConstants.SAML_V2011.equals(samlVersion)) {
            cbName = "Saml20HOKCallbackHandler";
        } else {
            cbName = "Saml11HOKCallbackHandler";
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
    
}
