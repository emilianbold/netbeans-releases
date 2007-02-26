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

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import java.io.IOException;
import java.util.Enumeration;
import org.netbeans.modules.websvc.wsitconf.ui.StoreFileFilter;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import javax.swing.*;
import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

/**
 *
 * @author Martin Grebac
 */
public class TruststorePanel extends JPanel {

    private static final String PKCS12 = "PKCS12";  //NOI18N
    private static final String JKS = "JKS";        //NOI18N

    private static final String DEFAULT_PASSWORD="changeit";    //NOI18N

    private WSDLModel model;
    private WSDLComponent comp;

    private String storeType = JKS;

    private boolean jsr109 = false;
    private Project project = null;
    
    private boolean inSync = false;
    
    public TruststorePanel(WSDLComponent comp, Project p, boolean jsr109) {
        super();
        this.model = comp.getModel();
        this.comp = comp;
        this.jsr109 = jsr109;
        this.project = p;
        
        initComponents();

        keyAliasCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keyAliasLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        storeLocationLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        storeLocationTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        storePasswordLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        storePasswordField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        sync();
    }

    private String getPeerAlias() {
        return (String) this.keyAliasCombo.getSelectedItem();
    }

    private void setPeerAlias(String alias) {
        this.keyAliasCombo.setSelectedItem(alias);
    }

    private char[] getCharStorePassword() {
        return storePasswordField.getPassword();
    }
    
    private String getStorePassword() {
        return String.valueOf(this.storePasswordField.getPassword());
    }

    private void setStorePassword(String password) {
        this.storePasswordField.setText(password);
    }

    private void setStoreLocation(String path) {
        this.storeLocationTextField.setText(path);
    }
    
    private String getStoreLocation() {
        String path = this.storeLocationTextField.getText();
        if ("".equals(path) || (path == null)) {    //NOI18N
            return null;
        }
        return path;
    }

    private void setStoreType(String type) {
        this.storeType = type;
    }
    
    private String getStoreType() {
        String type = this.storeType;
        if ("".equals(type) || (type == null)) {    //NOI18N
            return JKS;
        }
        return type;
    }
    
    public void sync() {
        inSync = true;
        
        String storeLocation = ProprietarySecurityPolicyModelHelper.getStoreLocation(comp, true);
        if (storeLocation != null) {
            setStoreLocation(storeLocation);
        } else if (jsr109) {
            setStoreLocation(getServerStoreLocation());            
        }

        String storeType = ProprietarySecurityPolicyModelHelper.getStoreType(comp, true);
        if (storeType != null) {
            setStoreType(storeType);
        }
       
        String storePassword = ProprietarySecurityPolicyModelHelper.getStorePassword(comp, true);
        if (storePassword != null) {
            setStorePassword(storePassword);
            reloadAliases();
        } else if (jsr109) {
            setStorePassword(DEFAULT_PASSWORD);
        }

        String peerAlias = ProprietarySecurityPolicyModelHelper.getTrustPeerAlias(comp);
        setPeerAlias(peerAlias);

        enableDisable();
        
        inSync = false;
    }

    private String getServerStoreLocation() {
        String keystoreLocation = null;
        J2eeModuleProvider mp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        if (mp != null) {
            String sID = mp.getServerInstanceID();
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(sID);
            File[] keyLocs = null;
            keyLocs = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_TRUSTSTORE);
            if ((keyLocs != null) && (keyLocs.length > 0)) {
                keystoreLocation = keyLocs[0].getAbsolutePath();
            }
        }
        return keystoreLocation;
    }
    
    private void enableDisable() {        
        //these depend on jsr109 state
        storeLocationButton.setEnabled(!jsr109);
        storeLocationLabel.setEnabled(!jsr109);
        storeLocationTextField.setEnabled(!jsr109);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        storeLocationLabel = new javax.swing.JLabel();
        storePasswordLabel = new javax.swing.JLabel();
        storeLocationTextField = new javax.swing.JTextField();
        storeLocationButton = new javax.swing.JButton();
        keyAliasLabel = new javax.swing.JLabel();
        keyAliasCombo = new javax.swing.JComboBox();
        storePasswordField = new javax.swing.JPasswordField();
        loadkeysButton = new javax.swing.JButton();

        storeLocationLabel.setText(org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_KeyStorePanel_LocationLabel")); // NOI18N

        storePasswordLabel.setText(org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_TruststorePanel_TruststorePassword")); // NOI18N

        storeLocationButton.setText("Browse...");
        storeLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeLocationButtonActionPerformed(evt);
            }
        });

        keyAliasLabel.setText(org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_KeyStorePanel_KeyAliasLabel")); // NOI18N

        keyAliasCombo.setEditable(true);

        loadkeysButton.setText(org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_LoadKeys")); // NOI18N
        loadkeysButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadkeysButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(storeLocationLabel)
                    .add(storePasswordLabel)
                    .add(keyAliasLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(storeLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 239, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(storeLocationButton))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, keyAliasCombo, 0, 159, Short.MAX_VALUE)
                            .add(storePasswordField))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(loadkeysButton)
                        .add(68, 68, 68)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(storeLocationLabel)
                    .add(storeLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(storeLocationButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(storePasswordLabel)
                    .add(storePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyAliasLabel)
                    .add(keyAliasCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(loadkeysButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {keyAliasCombo, storeLocationTextField, storePasswordField}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

    private void loadkeysButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadkeysButtonActionPerformed
        boolean success = reloadAliases();
        if (!success) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(NbBundle.getMessage(TruststorePanel.class, "MSG_WrongPassword"   //NOI18N
                    )));
        }
    }//GEN-LAST:event_loadkeysButtonActionPerformed
    
    private void storeLocationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storeLocationButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(NbBundle.getMessage(TruststorePanel.class, "LBL_TruststoreBrowse_Title")); //NOI18N
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new StoreFileFilter());
        File f = new File(storeLocationTextField.getText());
        File dir = null;
        if ((f != null) && (f.exists())) {
            if (f.isDirectory()) {
                chooser.setCurrentDirectory(f);
            } else {
                chooser.setCurrentDirectory(f.getParentFile());
            }
        }
        if (chooser.showOpenDialog(this)== JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                setStoreLocation(file.getPath());
                String extension = FileUtil.getExtension(file.getName());
                storeType = StoreFileFilter.JKS_EXT.equals(extension) ? JKS : PKCS12;
            }
        }
    }//GEN-LAST:event_storeLocationButtonActionPerformed

    public void storeState() {
        String peerAlias = getPeerAlias();
        if ((peerAlias != null) && (peerAlias.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(comp, null, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(comp, peerAlias, false);
        }

        String storePasswd = getStorePassword();
        if ((storePasswd != null) && (storePasswd.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setStorePassword(comp, null, true, false);
        } else if (jsr109 && DEFAULT_PASSWORD.equals(storePasswd)) {
            ProprietarySecurityPolicyModelHelper.setStorePassword(comp, null, true, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setStorePassword(comp, storePasswd, true, false);
        }
        
        ProprietarySecurityPolicyModelHelper.setStoreType(comp, storeType, true, false);
        
        if (!jsr109) {
            ProprietarySecurityPolicyModelHelper.setStoreLocation(comp, getStoreLocation(), true, false);
        }
    }
    
    private boolean reloadAliases() {
        Enumeration<String> aliases;
        try {
            aliases = Util.getAliases(getStoreLocation(), getCharStorePassword(), storeType);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        keyAliasCombo.removeAllItems();
        if (aliases != null) {
            keyAliasCombo.addItem("");  //NOI18N
            while (aliases.hasMoreElements()){
                String alias = aliases.nextElement();
                keyAliasCombo.addItem(alias);
            }
            if (keyAliasCombo.getItemCount() > 1) {
                keyAliasCombo.setSelectedIndex(1);
            }
        }
        return true;
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox keyAliasCombo;
    private javax.swing.JLabel keyAliasLabel;
    private javax.swing.JButton loadkeysButton;
    private javax.swing.JButton storeLocationButton;
    private javax.swing.JLabel storeLocationLabel;
    private javax.swing.JTextField storeLocationTextField;
    private javax.swing.JPasswordField storePasswordField;
    private javax.swing.JLabel storePasswordLabel;
    // End of variables declaration//GEN-END:variables
    
}
