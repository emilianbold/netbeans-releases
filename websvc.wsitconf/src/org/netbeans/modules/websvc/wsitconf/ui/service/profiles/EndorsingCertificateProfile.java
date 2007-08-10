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
import javax.swing.JPanel;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;

/** * Transport Security Profile definition
 *
 * @author Martin Grebac
 */
public class EndorsingCertificateProfile extends SecurityProfile {
    private static final String CERTS_DIR = "certs";
    
    public int getId() {
        return 60;
    }

    public String getDisplayName() {
        return ComboConstants.PROF_ENDORSCERT;
    }

    public String getDescription() {
        return ComboConstants.PROF_ENDORSCERT_INFO;
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

        JPanel profConfigPanel = new EndorsingCertificate(component);
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
    
    @Override
    public void setClientDefaults(WSDLComponent component, WSDLComponent c, Project p) {
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
        return false;
    }

}
