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

/*
 * SecurityManagerPanel.java
 *
 * Created on May 31, 2004
 */
package org.netbeans.modules.mobility.project.ui.security;

import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.SoftReference;
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.mobility.project.security.KeyStoreRepository;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam Sotona
 */
public class SecurityManagerPanel extends JPanel implements ListSelectionListener, ActionListener {
    
    private static SoftReference<SecurityManagerPanel> pRef;
    private static SoftReference<Dialog> dRef;
    
    final private DefaultListModel ksModel;    
    final private CardLayout cLayout;
    final private KeyAliasCellRenderer kaRenderer;
    
    private DefaultListModel kaModel;
    
    public static void showSecurityManager(final KeyStoreRepository.KeyStoreBean keystore, final KeyStoreRepository.KeyStoreBean.KeyAliasBean keyAlias) {
        Dialog dialog;
        SecurityManagerPanel panel;
        if (pRef == null || (panel = pRef.get()) == null) {
            panel = new SecurityManagerPanel();
            pRef = new SoftReference<SecurityManagerPanel>(panel);
            dialog = panel.createDialog();///
            dRef = new SoftReference<Dialog>(dialog);
        } else if (dRef == null || (dialog = dRef.get()) == null || !dialog.isShowing()) {
            dialog = panel.createDialog();///
            dRef = new SoftReference<Dialog>(dialog);
        }
        panel.setSelectedItems(keystore, keyAlias);
        dialog.setVisible(true);
    }
    
    /** Creates new form SecurityManagerPanel */
    private SecurityManagerPanel() {
        initComponents();
        initAccessibility();
        cLayout = (CardLayout)pRight.getLayout();
        lKeystores.setCellRenderer(new KeystoreCellRenderer());
        kaRenderer = new KeyAliasCellRenderer();
        lKeys.setCellRenderer(kaRenderer);
        ksModel = new DefaultListModel();
        kaModel = new DefaultListModel();
        lKeys.setModel(kaModel);
        lKeystores.setModel(ksModel);
        lKeystores.addListSelectionListener(this);
        lKeys.addListSelectionListener(this);
        cDetails.addActionListener(this);
        bAddKeystore.addActionListener(this);
        bRemoveKeystore.addActionListener(this);
        bUnlockKeystore.addActionListener(this);
        bCreateKey.addActionListener(this);
        bDeleteKey.addActionListener(this);
        bUnlockKey.addActionListener(this);
        bExportKey.addActionListener(this);
        updateKeystores();
    }
    
    private void updateKeystores() {
        synchronized (ksModel) {
            final Object selected = lKeystores.getSelectedValue();
            boolean exists = false;
            ksModel.clear();
            for  (final Object o : KeyStoreRepository.getDefault().getKeyStores() ) {
                ksModel.addElement(o);
                if (o == selected)
                    exists = true;
            }
            if (exists)
                lKeystores.setSelectedValue(selected, true);
        }
        updateKeys();
    }
    
    private Dialog createDialog() {
        return DialogDisplayer.getDefault().createDialog(new DialogDescriptor(this, NbBundle.getMessage(SecurityManagerPanel.class, "TITLE_SecurityManager"), true, new Object[]{NotifyDescriptor.CLOSED_OPTION}, NotifyDescriptor.CLOSED_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(SecurityManagerPanel.class), null)); // NOI18N
    }
    
    private void setSelectedItems(final KeyStoreRepository.KeyStoreBean keystore, final KeyStoreRepository.KeyStoreBean.KeyAliasBean keyAlias) {
        if (keystore != null)
            lKeystores.setSelectedValue(keystore, true);
        if (keyAlias != null)
            lKeys.setSelectedValue(keyAlias, true);
    }
    
    private void reloadKeys(final KeyStoreRepository.KeyStoreBean keystore) {
        final Object selected = lKeys.getSelectedValue();
        boolean exists = false;
        kaModel = new DefaultListModel();
        lKeys.setModel(kaModel);
        if (keystore == null  ||  ! keystore.isValid()  ||  ! keystore.isOpened())
            return;
        for ( final Object o : keystore.aliasses() ) {
            kaModel.addElement(o);
            if (o == selected)
                exists = true;
        }
        if (exists)
            lKeys.setSelectedValue(selected, true);
    }
    
    private void updateKeys() {
        final KeyStoreRepository.KeyStoreBean keystore = (KeyStoreRepository.KeyStoreBean)lKeystores.getSelectedValue();
        synchronized (kaModel) {
            if (keystore == null || !keystore.isValid()) {
                cLayout.show(pRight, "empty"); //NOI18N
            } else if (keystore.isOpened()) {
                tKeystoreFile.setText(keystore.getKeyStorePath());
                reloadKeys(keystore);
                cLayout.show(pRight, "keys"); //NOI18N
            } else {
                tKeystoreFile2.setText(keystore.getKeyStorePath());
                cLayout.show(pRight, "unlock"); //NOI18N
            }
        }
        bRemoveKeystore.setEnabled(keystore != null && !KeyStoreRepository.isDefaultKeystore(keystore));
        updateButtons();
    }
    
    private void updateButtons() {
        final KeyStoreRepository.KeyStoreBean bean = (KeyStoreRepository.KeyStoreBean) lKeystores.getSelectedValue();
        final KeyStoreRepository.KeyStoreBean.KeyAliasBean alias = (KeyStoreRepository.KeyStoreBean.KeyAliasBean) lKeys.getSelectedValue();
        final boolean selected = alias != null;
        final boolean notDefault = ! KeyStoreRepository.isDefaultKeystore(bean);
        bUnlockKey.setEnabled(selected  &&  alias.isValid()  &&  ! alias.isOpened());
        bExportKey.setEnabled(selected  &&  alias.isValid()  &&  alias.isOpened());
        bCreateKey.setEnabled(notDefault);
        bDeleteKey.setEnabled(selected  &&  notDefault);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelKestores = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lKeystores = new javax.swing.JList();
        bAddKeystore = new javax.swing.JButton();
        bRemoveKeystore = new javax.swing.JButton();
        pRight = new javax.swing.JPanel();
        pEmpty = new javax.swing.JPanel();
        pUnlock = new javax.swing.JPanel();
        lKeystoreFile2 = new javax.swing.JLabel();
        tKeystoreFile2 = new javax.swing.JTextField();
        bUnlockKeystore = new javax.swing.JButton();
        pKeys = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lKeystoreFile = new javax.swing.JLabel();
        tKeystoreFile = new javax.swing.JTextField();
        lKeys2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lKeys = new javax.swing.JList();
        bCreateKey = new javax.swing.JButton();
        bExportKey = new javax.swing.JButton();
        bDeleteKey = new javax.swing.JButton();
        bUnlockKey = new javax.swing.JButton();
        cDetails = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setMinimumSize(new java.awt.Dimension(700, 400));
        setPreferredSize(new java.awt.Dimension(700, 400));
        setLayout(new java.awt.GridBagLayout());

        jLabelKestores.setLabelFor(lKeystores);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelKestores, NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_Keystores")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelKestores, gridBagConstraints);

        lKeystores.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(lKeystores);
        lKeystores.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_Keystores")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bAddKeystore, org.openide.util.NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_Add")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(bAddKeystore, gridBagConstraints);
        bAddKeystore.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_AddKeystore")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bRemoveKeystore, org.openide.util.NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_Remove")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(bRemoveKeystore, gridBagConstraints);
        bRemoveKeystore.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_RemoveKeystore")); // NOI18N

        pRight.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12));
        pRight.setLayout(new java.awt.CardLayout());
        pRight.add(pEmpty, "empty");

        pUnlock.setLayout(new java.awt.GridBagLayout());

        lKeystoreFile2.setLabelFor(tKeystoreFile);
        org.openide.awt.Mnemonics.setLocalizedText(lKeystoreFile2, org.openide.util.NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_KeystoreFile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        pUnlock.add(lKeystoreFile2, gridBagConstraints);

        tKeystoreFile2.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        pUnlock.add(tKeystoreFile2, gridBagConstraints);
        tKeystoreFile2.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_KeystoreFile")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bUnlockKeystore, org.openide.util.NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_UnlockKeystore")); // NOI18N
        bUnlockKeystore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bUnlockKeystoreActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        pUnlock.add(bUnlockKeystore, gridBagConstraints);
        bUnlockKeystore.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_UnlockKeystore")); // NOI18N

        pRight.add(pUnlock, "unlock");

        pKeys.setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        lKeystoreFile.setLabelFor(tKeystoreFile);
        org.openide.awt.Mnemonics.setLocalizedText(lKeystoreFile, org.openide.util.NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_KeystoreFile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanel2.add(lKeystoreFile, gridBagConstraints);

        tKeystoreFile.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        jPanel2.add(tKeystoreFile, gridBagConstraints);
        tKeystoreFile.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_KeystoreFile2")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        pKeys.add(jPanel2, gridBagConstraints);

        lKeys2.setLabelFor(lKeys);
        org.openide.awt.Mnemonics.setLocalizedText(lKeys2, org.openide.util.NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_Keys")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        pKeys.add(lKeys2, gridBagConstraints);

        lKeys.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(lKeys);
        lKeys.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_Keys")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        pKeys.add(jScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bCreateKey, org.openide.util.NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_NewKey")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 18, 0);
        pKeys.add(bCreateKey, gridBagConstraints);
        bCreateKey.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_New")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bExportKey, org.openide.util.NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_ExportKey")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        pKeys.add(bExportKey, gridBagConstraints);
        bExportKey.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_Export")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bDeleteKey, org.openide.util.NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_DeleteKey")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        pKeys.add(bDeleteKey, gridBagConstraints);
        bDeleteKey.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_Delete")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bUnlockKey, org.openide.util.NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_UnlockKey")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        pKeys.add(bUnlockKey, gridBagConstraints);
        bUnlockKey.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_UnlockKeystore2")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cDetails, org.openide.util.NbBundle.getMessage(SecurityManagerPanel.class, "LBL_Manager_ShowDetails")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        pKeys.add(cDetails, gridBagConstraints);
        cDetails.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager_Details")); // NOI18N

        pRight.add(pKeys, "keys");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pRight, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SecurityManagerPanel.class, "ACSN_Manager"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SecurityManagerPanel.class, "ACSD_Manager"));
    }
    
    private void bUnlockKeystoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bUnlockKeystoreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bUnlockKeystoreActionPerformed
    
    public void valueChanged(final ListSelectionEvent e) {
        if (lKeystores == e.getSource())
            updateKeys();
        else if (lKeys == e.getSource())
            updateButtons();
    }
    
    public void actionPerformed(final ActionEvent e) {
        final Object src = e.getSource();
        if (cDetails == src) {
            kaRenderer.setShowDetails(cDetails.isSelected());
            reloadKeys((KeyStoreRepository.KeyStoreBean) lKeystores.getSelectedValue());
        } else if (bAddKeystore == src) {
            if (AddKeystorePanel.showAddKeystorePanel() != null)
                updateKeystores();
        } else if (bUnlockKeystore == src) {
            EnterPasswordPanel.getKeystorePassword((KeyStoreRepository.KeyStoreBean) lKeystores.getSelectedValue());
            updateKeystores();
        } else if (bRemoveKeystore.equals(src)) {
            KeyStoreRepository.getDefault().removeKeyStore((KeyStoreRepository.KeyStoreBean)lKeystores.getSelectedValue());
            updateKeystores();
        } else if (bCreateKey == src) {
            final KeyStoreRepository.KeyStoreBean.KeyAliasBean alias = CreateKeyAliasPanel.showCreateKeyAliasPanel((KeyStoreRepository.KeyStoreBean) lKeystores.getSelectedValue());
            updateKeys();
            if (alias != null)
                lKeys.setSelectedValue(alias, true);
        } else if (bExportKey == src) {
            final KeyStoreRepository.KeyStoreBean bean = (KeyStoreRepository.KeyStoreBean) lKeystores.getSelectedValue();
            final KeyStoreRepository.KeyStoreBean.KeyAliasBean alias = (KeyStoreRepository.KeyStoreBean.KeyAliasBean) lKeys.getSelectedValue();
            ExportPanel.showExportKeyIntoPlatform(bean, alias, null, null);
        } else if (bDeleteKey == src) {
            final KeyStoreRepository.KeyStoreBean bean = (KeyStoreRepository.KeyStoreBean) lKeystores.getSelectedValue();
            final KeyStoreRepository.KeyStoreBean.KeyAliasBean alias = (KeyStoreRepository.KeyStoreBean.KeyAliasBean) lKeys.getSelectedValue();
            deleteAlias(bean, alias);
        } else if (bUnlockKey == src) {
            EnterPasswordPanel.getAliasPassword((KeyStoreRepository.KeyStoreBean) lKeystores.getSelectedValue(), (KeyStoreRepository.KeyStoreBean.KeyAliasBean) lKeys.getSelectedValue());
            updateKeys();
        }
    }
    
    private void deleteAlias(final KeyStoreRepository.KeyStoreBean bean, final KeyStoreRepository.KeyStoreBean.KeyAliasBean alias) {
        if (bean == null  ||  alias == null)
            return;
        NotifyDescriptor nd;
        DialogDisplayer.getDefault().notify(nd = new NotifyDescriptor.Confirmation(org.openide.util.NbBundle.getMessage(CreateKeyAliasPanel.class, "MSG_PromptDeleteKey", alias), //NOI18N
                org.openide.util.NbBundle.getBundle(CreateKeyAliasPanel.class).getString("MSG_PromptDeleteKeyTitle"), //NOI18N
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE));
        if (nd.getValue() == NotifyDescriptor.YES_OPTION) {
            try {
                bean.removeAliasFromStore(alias);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            updateKeys();
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAddKeystore;
    private javax.swing.JButton bCreateKey;
    private javax.swing.JButton bDeleteKey;
    private javax.swing.JButton bExportKey;
    private javax.swing.JButton bRemoveKeystore;
    private javax.swing.JButton bUnlockKey;
    private javax.swing.JButton bUnlockKeystore;
    private javax.swing.JCheckBox cDetails;
    private javax.swing.JLabel jLabelKestores;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lKeys;
    private javax.swing.JLabel lKeys2;
    private javax.swing.JLabel lKeystoreFile;
    private javax.swing.JLabel lKeystoreFile2;
    private javax.swing.JList lKeystores;
    private javax.swing.JPanel pEmpty;
    private javax.swing.JPanel pKeys;
    private javax.swing.JPanel pRight;
    private javax.swing.JPanel pUnlock;
    private javax.swing.JTextField tKeystoreFile;
    private javax.swing.JTextField tKeystoreFile2;
    // End of variables declaration//GEN-END:variables
    
}
