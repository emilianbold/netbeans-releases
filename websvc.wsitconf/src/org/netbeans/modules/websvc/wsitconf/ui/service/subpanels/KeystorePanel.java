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

/**
 *
 * @author Martin Grebac
 */
public class KeystorePanel extends JPanel {

    public static final String PKCS12 = "PKCS12";      //NOI18N
    public static final String JKS = "JKS";            //NOI18N

    public static final String DEFAULT_PASSWORD="changeit";    //NOI18N
    
    private WSDLComponent comp;

    private boolean jsr109 = false;
    private Project project = null;
    
    private boolean client;
    private String keystoreType = JKS;
    
    private boolean inSync = false;
    
    public KeystorePanel(WSDLComponent comp, Project p, boolean jsr109, boolean client) {
        super();
        this.comp = comp;
        this.jsr109 = jsr109;
        this.project = p;
        this.client = client;
        
        initComponents();

        keyAliasCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keyAliasLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keyPasswordLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keyPasswordField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keystoreLocationLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keystoreLocationTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keystorePasswordLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keystorePasswordField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        sync();
    }

    private String getKeystoreAlias() {
        return (String) this.keyAliasCombo.getSelectedItem();
    }

    private void setKeystoreAlias(String alias) {
        this.keyAliasCombo.setSelectedItem(alias);
    }

    private String getKeyPassword() {
        return String.valueOf(this.keyPasswordField.getPassword());
    }

    private void setKeyPassword(String password) {
        this.keyPasswordField.setText(password);
    }
    
    private char[] getCharKeystorePassword() {
        return keystorePasswordField.getPassword();
    }
    
    private String getKeystorePassword() {
        return String.valueOf(this.keystorePasswordField.getPassword());
    }

    private void setKeystorePassword(String password) {
        this.keystorePasswordField.setText(password);
    }

    private void setKeystorePath(String path) {
        this.keystoreLocationTextField.setText(path);
    }
    
    private String getKeystorePath() {
        String path = this.keystoreLocationTextField.getText();
        if ("".equals(path) || (path == null)) {    //NOI18N
            return null;
        }
        return path;
    }

    private void setKeystoreType(String type) {
        this.keystoreType = type;
    }
    
    public void sync() {
        inSync = true;

        String keystoreLocation = ProprietarySecurityPolicyModelHelper.getStoreLocation(comp, false);
        if (keystoreLocation != null) {
            setKeystorePath(keystoreLocation);
        } else if (jsr109) {
            setKeystorePath(Util.getStoreLocation(project, false, client));
        }

        String keystoreType = ProprietarySecurityPolicyModelHelper.getStoreType(comp, false);
        if (keystoreType != null) {
            setKeystoreType(keystoreType);
        }
        
        String keyStorePassword = ProprietarySecurityPolicyModelHelper.getStorePassword(comp, false);
        if (keyStorePassword != null) {
            setKeystorePassword(keyStorePassword);
            reloadAliases();
        } else if (jsr109) {
            setKeystorePassword(DEFAULT_PASSWORD);
            if (!reloadAliases()) {
                String adminPassword = Util.getPassword(project);
                setKeystorePassword(adminPassword);
            }
            if (!reloadAliases()) {
                setKeystorePassword("");
            }
        }

        String keyStoreAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(comp, false);
        setKeystoreAlias(keyStoreAlias);

        String keyPassword = ProprietarySecurityPolicyModelHelper.getKeyPassword(comp);
        if (keyPassword != null) {
            setKeyPassword(keyPassword);
        }
        
        enableDisable();

        inSync = false;
    }

    private void enableDisable() {
        boolean gf = Util.isGlassfish(project);
        keyPasswordField.setEnabled(!gf);
        keyPasswordLabel.setEnabled(!gf);
    }
        
    public void storeState() {
        String keystoreAlias = getKeystoreAlias();
        if ((keystoreAlias == null) || (keystoreAlias.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(comp, null, client);
        } else {
            ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(comp, keystoreAlias, client);
        }
        // do not store anything else if GF is target server
        if (!Util.isGlassfish(project)) {
            String keyPasswd = getKeyPassword();
            if ((keyPasswd == null) || (keyPasswd.length() == 0)) {
                ProprietarySecurityPolicyModelHelper.setKeyPassword(comp, null, client);
            } else {
                ProprietarySecurityPolicyModelHelper.setKeyPassword(comp, keyPasswd, client);
            }
            String keyStorePasswd = getKeystorePassword();
            if ((keyStorePasswd == null) || (keyStorePasswd.length() == 0)) {
                ProprietarySecurityPolicyModelHelper.setStorePassword(comp, null, false, client);
            } else {
                ProprietarySecurityPolicyModelHelper.setStorePassword(comp, keyStorePasswd, false, client);
            }
            ProprietarySecurityPolicyModelHelper.setStoreType(comp, keystoreType, false, client);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(comp, getKeystorePath(), false, client);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        keystoreLocationLabel = new javax.swing.JLabel();
        keystorePasswordLabel = new javax.swing.JLabel();
        keystoreLocationTextField = new javax.swing.JTextField();
        keystoreLocationButton = new javax.swing.JButton();
        keyAliasLabel = new javax.swing.JLabel();
        keyPasswordLabel = new javax.swing.JLabel();
        keyAliasCombo = new javax.swing.JComboBox();
        keystorePasswordField = new javax.swing.JPasswordField();
        keyPasswordField = new javax.swing.JPasswordField();
        loadkeysButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(keystoreLocationLabel, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_KeyStorePanel_LocationLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keystorePasswordLabel, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_StorePanel_StorePassword")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keystoreLocationButton, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_KeystorePanel_Browse")); // NOI18N
        keystoreLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keystoreLocationButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(keyAliasLabel, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_KeyStorePanel_KeyAliasLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keyPasswordLabel, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_Keystore_KeyPasswordLabel")); // NOI18N

        keyAliasCombo.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(loadkeysButton, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_LoadKeys")); // NOI18N
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
                    .add(keystoreLocationLabel)
                    .add(keystorePasswordLabel)
                    .add(keyAliasLabel)
                    .add(keyPasswordLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(keystoreLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(keystoreLocationButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, keyAliasCombo, 0, 159, Short.MAX_VALUE)
                            .add(keystorePasswordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, keyPasswordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(loadkeysButton)
                        .add(68, 68, 68)))
                .add(4, 4, 4))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keystoreLocationLabel)
                    .add(keystoreLocationButton)
                    .add(keystoreLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keystorePasswordLabel)
                    .add(keystorePasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyAliasLabel)
                    .add(keyAliasCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(loadkeysButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyPasswordLabel)
                    .add(keyPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {keyAliasCombo, keyPasswordField, keystoreLocationTextField, keystorePasswordField}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

    private void loadkeysButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadkeysButtonActionPerformed
        boolean success = reloadAliases();
        if (!success) {
            DialogDisplayer.getDefault().notifyLater(
                new NotifyDescriptor.Message(NbBundle.getMessage(KeystorePanel.class, "MSG_WrongPassword"   //NOI18N
                )));
        }
    }//GEN-LAST:event_loadkeysButtonActionPerformed
    
    private void keystoreLocationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keystoreLocationButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(NbBundle.getMessage(KeystorePanel.class, "LBL_KeystoreBrowse_Title")); //NOI18N
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new StoreFileFilter());
        File f = new File(keystoreLocationTextField.getText());
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
                setKeystorePath(file.getPath());
                String extension = FileUtil.getExtension(file.getName());
                keystoreType = StoreFileFilter.JKS_EXT.equals(extension) ? JKS : PKCS12;
            }
        }
    }//GEN-LAST:event_keystoreLocationButtonActionPerformed

    private boolean reloadAliases() {
        List<String> aliasList;
        try {
            aliasList = Util.getAliases(getKeystorePath(), getCharKeystorePassword(), keystoreType);
        } catch (IOException ex) {
            return false;
        }
        keyAliasCombo.removeAllItems();
        if (aliasList != null) {
            keyAliasCombo.addItem("");  //NOI18N
            Iterator<String> aliases = aliasList.iterator();
            while (aliases.hasNext()) {
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
    private javax.swing.JPasswordField keyPasswordField;
    private javax.swing.JLabel keyPasswordLabel;
    private javax.swing.JButton keystoreLocationButton;
    private javax.swing.JLabel keystoreLocationLabel;
    private javax.swing.JTextField keystoreLocationTextField;
    private javax.swing.JPasswordField keystorePasswordField;
    private javax.swing.JLabel keystorePasswordLabel;
    private javax.swing.JButton loadkeysButton;
    // End of variables declaration//GEN-END:variables
    
}
