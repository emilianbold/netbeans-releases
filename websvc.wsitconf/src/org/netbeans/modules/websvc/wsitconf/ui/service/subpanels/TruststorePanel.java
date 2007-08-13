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
import org.netbeans.modules.websvc.wsitconf.ui.StoreFileFilter;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import javax.swing.*;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;

/**
 *
 * @author Martin Grebac
 */
public class TruststorePanel extends JPanel {

    private static final String PKCS12 = "PKCS12";  //NOI18N
    private static final String JKS = "JKS";        //NOI18N

    private static final String DEFAULT_PASSWORD="changeit";    //NOI18N

    private WSDLComponent comp;

    private String storeType = JKS;

    private boolean jsr109 = false;
    private Project project = null;
    private String profile = null;
    
    private boolean inSync = false;
    
    private boolean client;
    
    public TruststorePanel(WSDLComponent comp, Project p, boolean jsr109, String profile, boolean client) {
        super();
        this.comp = comp;
        this.jsr109 = jsr109;
        this.project = p;
        this.profile = profile;
        this.client = client;
        
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
        
    public void sync() {
        inSync = true;
        
        String storeLocation = ProprietarySecurityPolicyModelHelper.getStoreLocation(comp, true);
        if (storeLocation != null) {
            setStoreLocation(storeLocation);
        } else if (jsr109) {
            setStoreLocation(Util.getStoreLocation(project, true, client));
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
            if (!reloadAliases()) {
                String adminPassword = Util.getPassword(project);
                setStorePassword(adminPassword);
            }
            if (!reloadAliases()) {
                setStorePassword("");
            }
        }

        String peerAlias = ProprietarySecurityPolicyModelHelper.getTrustPeerAlias(comp);
        setPeerAlias(peerAlias);

        enableDisable();
        
        inSync = false;
    }
    
    private void enableDisable() {
        if (!client) {
            boolean aliasRequired = true;
            if (ComboConstants.PROF_USERNAME.equals(profile) ||
                ComboConstants.PROF_ENDORSCERT.equals(profile) ||
                ComboConstants.PROF_SAMLSENDER.equals(profile) ||
                ComboConstants.PROF_SAMLHOLDER.equals(profile) ||
                ComboConstants.PROF_STSISSUED.equals(profile) ||
                ComboConstants.PROF_STSISSUEDCERT.equals(profile) ||
                ComboConstants.PROF_STSISSUEDENDORSE.equals(profile) ||
                ComboConstants.PROF_MUTUALCERT.equals(profile)) {
                aliasRequired = false;
            }
            keyAliasCombo.setEnabled(aliasRequired);
            keyAliasLabel.setEnabled(aliasRequired);
            loadkeysButton.setEnabled(aliasRequired);
        } else {
            
        }
                
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

        org.openide.awt.Mnemonics.setLocalizedText(storeLocationLabel, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_KeyStorePanel_LocationLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(storePasswordLabel, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_TruststorePanel_TruststorePassword")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(storeLocationButton, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_TruststorePanel_Browse")); // NOI18N
        storeLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeLocationButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(keyAliasLabel, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_KeyStorePanel_KeyAliasLabel")); // NOI18N

        keyAliasCombo.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(loadkeysButton, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_LoadKeys")); // NOI18N
        loadkeysButton.setActionCommand("&Load Aliases");
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, keyAliasCombo, 0, 167, Short.MAX_VALUE)
                            .add(storePasswordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(loadkeysButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 85, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(storeLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(storeLocationButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(storeLocationLabel)
                    .add(storeLocationButton)
                    .add(storeLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
            DialogDisplayer.getDefault().notifyLater(
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
            ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(comp, null, client);
        } else {
            ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(comp, peerAlias, client);
        }
        if (!Util.isGlassfish(project)) {
            String storePasswd = getStorePassword();
            if ((storePasswd != null) && (storePasswd.length() == 0)) {
                ProprietarySecurityPolicyModelHelper.setStorePassword(comp, null, true, client);
            } else {
                ProprietarySecurityPolicyModelHelper.setStorePassword(comp, storePasswd, true, client);
            }

            ProprietarySecurityPolicyModelHelper.setStoreType(comp, storeType, true, client);

            ProprietarySecurityPolicyModelHelper.setStoreLocation(comp, getStoreLocation(), true, client);
        }
    }
    
    private boolean reloadAliases() {
        List<String> aliasList;
        try {
            aliasList = Util.getAliases(getStoreLocation(), getCharStorePassword(), storeType);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        keyAliasCombo.removeAllItems();
        if (aliasList != null) {
            keyAliasCombo.addItem("");  //NOI18N
            Iterator<String> aliases = aliasList.iterator();
            while (aliases.hasNext()){
                String alias = aliases.next();
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
