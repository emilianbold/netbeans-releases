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
 * ExportPanel.java
 *
 * Created on May 31, 2004
 */
package org.netbeans.modules.mobility.project.ui.security;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.mobility.project.security.KeyStoreRepository;
import org.netbeans.modules.mobility.project.security.MEKeyTool;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * @author  David Kaspar
 */
public class ExportPanel extends javax.swing.JPanel implements ItemListener, ListSelectionListener {
    
    private static final Dimension PREFERRED_SIZE = new Dimension(500, 500);
    
    private DialogDescriptor dd;
    final private KeyStoreRepository.KeyStoreBean bean;
    final private KeyStoreRepository.KeyStoreBean.KeyAliasBean alias;
    final private JButton bExport;
    String keyString = NbBundle.getMessage(ExportPanel.class, "LBL_Key"); // NOI18N
    
    /** Creates new form ExportPanel */
    public ExportPanel(KeyStoreRepository.KeyStoreBean bean, KeyStoreRepository.KeyStoreBean.KeyAliasBean alias, J2MEPlatform preselectedPlatform, String preselectedDomain) {
        this.bean = bean;
        this.alias = alias;
        initComponents();
        initAccessibility();
        bDelete.setEnabled(false);
        bExport = new JButton(NbBundle.getMessage(ExportPanel.class, "LBL_Export")); // NOI18N
        bExport.setDefaultCapable(true);
        bExport.setEnabled(false);
        bExport.addActionListener(new ActionListener() {
            @SuppressWarnings("synthetic-access")
			public void actionPerformed(@SuppressWarnings("unused")
			final ActionEvent evt) {
                export();
                reloadList((J2MEPlatform)cPlatform.getSelectedItem());
            }
        });
        tKeystore.setText(bean.getKeyStorePath());
        tAlias.setText(alias.getAlias());
        lDetails.setText(KeyAliasCellRenderer.getHtmlFormattedText(alias));
        cPlatform.addItemListener(this);
        list.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(final JList list, Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                if (value instanceof MEKeyTool.KeyDetail) {
                    final MEKeyTool.KeyDetail key = (MEKeyTool.KeyDetail) value;
                    final Color color = isSelected ? list.getSelectionForeground() : list.getForeground();
                    final StringBuffer sb = new StringBuffer("<html><font color=\"#"+ Integer.toHexString(color.getRGB() & 0xffffff) +"\"><b>" + keyString + ": " + key.getOrder() + "</b>"); // NOI18N
                    final String[] lines = key.getInfo();
                    if (lines != null)
                        for (int i = 0; i < lines.length; i++)
                            sb.append("<br>").append(lines[i]); // NOI18N
                    value = sb.toString();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        list.addListSelectionListener(this);
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification(J2MEPlatform.SPECIFICATION_NAME, null));
        cPlatform.removeAllItems();
        if (platforms != null) for (int i = 0; i < platforms.length; i++) {
            JavaPlatform platform = platforms[i];
            if (platform instanceof J2MEPlatform)
                cPlatform.addItem(platform);
        }
        if (preselectedPlatform != null) {
            cPlatform.setSelectedItem(preselectedPlatform);
            if (preselectedDomain != null)
                cDomain.setSelectedItem(preselectedDomain);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lKeystore = new javax.swing.JLabel();
        tKeystore = new javax.swing.JTextField();
        lAlias = new javax.swing.JLabel();
        tAlias = new javax.swing.JTextField();
        pDetails = new javax.swing.JPanel();
        lDetails = new javax.swing.JLabel();
        lPlatform = new javax.swing.JLabel();
        cPlatform = new javax.swing.JComboBox();
        lDomain = new javax.swing.JLabel();
        cDomain = new javax.swing.JComboBox();
        lKeys = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        bDelete = new javax.swing.JButton();
        pError = new org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setMinimumSize(new java.awt.Dimension(500, 250));
        setPreferredSize(new java.awt.Dimension(500, 250));
        setLayout(new java.awt.GridBagLayout());

        lKeystore.setLabelFor(tKeystore);
        org.openide.awt.Mnemonics.setLocalizedText(lKeystore, org.openide.util.NbBundle.getMessage(ExportPanel.class, "LBL_Export_File")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(lKeystore, gridBagConstraints);

        tKeystore.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(tKeystore, gridBagConstraints);
        tKeystore.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export_Keystore")); // NOI18N

        lAlias.setLabelFor(tAlias);
        org.openide.awt.Mnemonics.setLocalizedText(lAlias, org.openide.util.NbBundle.getMessage(ExportPanel.class, "LBL_Export_Alias")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(lAlias, gridBagConstraints);

        tAlias.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(tAlias, gridBagConstraints);
        tAlias.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export_Alias")); // NOI18N

        pDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExportPanel.class, "LBL_Export_Details"))); // NOI18N
        pDetails.setPreferredSize(new java.awt.Dimension(300, 100));
        pDetails.setEnabled(false);
        pDetails.setLayout(new java.awt.GridBagLayout());

        lDetails.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        pDetails.add(lDetails, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(pDetails, gridBagConstraints);

        lPlatform.setLabelFor(cPlatform);
        org.openide.awt.Mnemonics.setLocalizedText(lPlatform, org.openide.util.NbBundle.getMessage(ExportPanel.class, "LBL_Export_Platform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(lPlatform, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(cPlatform, gridBagConstraints);
        cPlatform.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export_Platform")); // NOI18N

        lDomain.setLabelFor(cDomain);
        org.openide.awt.Mnemonics.setLocalizedText(lDomain, org.openide.util.NbBundle.getMessage(ExportPanel.class, "LBL_Export_Domain")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(lDomain, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(cDomain, gridBagConstraints);
        cDomain.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export_Domain")); // NOI18N

        lKeys.setLabelFor(list);
        org.openide.awt.Mnemonics.setLocalizedText(lKeys, org.openide.util.NbBundle.getMessage(ExportPanel.class, "LBL_Export_Keys")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(lKeys, gridBagConstraints);

        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(list);
        list.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export_Keys")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 3.0;
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bDelete, org.openide.util.NbBundle.getMessage(ExportPanel.class, "LBL_Export_DeleteKey")); // NOI18N
        bDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bDeleteActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(bDelete, gridBagConstraints);
        bDelete.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export_DeleteKey")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(pError, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportPanel.class, "ACSN_Export"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export"));
    }
    
    public void valueChanged(@SuppressWarnings("unused")
	final ListSelectionEvent e) {
        bDelete.setEnabled(list.getSelectedValue() instanceof MEKeyTool.KeyDetail);
    }
    
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }
    
    protected void setDialogDescriptor(final DialogDescriptor desc) {
        this.dd = desc;
        dd.setHelpCtx(new HelpCtx(ExportPanel.class));
        checkErrors();
    }
    
    public String getErrorMessage() {
        final ListModel model = list.getModel();
        for (int a = 0; a < model.getSize(); a ++) {
            final Object o = model.getElementAt(a);
            if (o instanceof MEKeyTool.KeyDetail) {
                final MEKeyTool.KeyDetail key = (MEKeyTool.KeyDetail) o;
                final String owner = key.getOwner();
                if (owner != null  &&  owner.equals(alias.getSubjectName()))
                    return NbBundle.getMessage(ExportPanel.class, "ERR_KeyIsAlreadyInPlatform", owner, Integer.toString(key.getOrder())); // NOI18N
            }
        }
        return null;
    }
    
    public void checkErrors() {
        final String errorMessage = getErrorMessage();
        pError.setErrorMessage(errorMessage);
        final boolean valid = errorMessage == null;
        bExport.setEnabled(valid  &&  cPlatform.getSelectedItem() != null);
        if (dd != null && valid != dd.isValid())
            dd.setValid(valid);
    }
    
    private void bDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDeleteActionPerformed
        final J2MEPlatform platform = (J2MEPlatform) cPlatform.getSelectedItem();
        final Object value = list.getSelectedValue();
        final String keytool = MEKeyTool.getMEKeyToolPath(platform);
        if (value instanceof MEKeyTool.KeyDetail  &&  keytool != null) {
            final MEKeyTool.KeyDetail key = (MEKeyTool.KeyDetail) value;
            if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportPanel.class, "MSG_DeleteKeyConfirmation", Integer.toString(key.getOrder()), platform.getDisplayName()), NbBundle.getMessage(ExportPanel.class, "TITLE_ConfirmKeyDeletion"), NotifyDescriptor.YES_NO_OPTION)) == NotifyDescriptor.YES_OPTION) { // NOI18N
                try {
                    MEKeyTool.execute(new String[]{keytool, "-delete", "-number", Integer.toString(key.getOrder())}); // NOI18N
                } catch (IOException e) {
                    e.printStackTrace();
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(ExportPanel.class, "ERR_WhileDeletingKey"), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
                }
                reloadList(platform);
            }
        }
    }//GEN-LAST:event_bDeleteActionPerformed
    
    public void itemStateChanged(@SuppressWarnings("unused")
	final ItemEvent e) {
        final J2MEPlatform platform = (J2MEPlatform) cPlatform.getSelectedItem();
        final ArrayList<String> list = new ArrayList<String>();
        if (platform != null) {
            final J2MEPlatform.Device[] devices = platform.getDevices();
            if(devices != null) for (int i = 0; i < devices.length; i++) {
                final J2MEPlatform.Device device = devices[i];
                final String[] domains = device.getSecurityDomains();
                if (domains != null) for (int j = 0; j < domains.length; j++) {
                    final String domain = domains[j];
                    if (!list.contains(domain))
                        list.add(domain);
                }
            }
        }
        cDomain.removeAllItems();
        for (int i = 0; i < list.size(); i++)
            cDomain.addItem(list.get(i));
        reloadList(platform);
    }
    
    protected void reloadList(final J2MEPlatform platform) {
        bDelete.setEnabled(false);
        if (platform != null) {
            setListLoading();
            setList(MEKeyTool.listKeys(platform));
        } else
            setListNotLoaded();
        checkErrors();
    }
    
    private void setListNotLoaded() {
        final DefaultListModel model = new DefaultListModel();
        model.addElement(NbBundle.getMessage(ExportPanel.class, "LBL_NotLoaded")); // NOI18N
        list.setModel(model);
    }
    
    private void setListLoading() {
        final DefaultListModel model = new DefaultListModel();
        model.addElement(NbBundle.getMessage(ExportPanel.class, "LBL_Loading")); // NOI18N
        list.setModel(model);
    }
    
    private void setList(final MEKeyTool.KeyDetail[] keys) {
        final DefaultListModel model = new DefaultListModel();
        if (keys != null) {
            if (keys.length > 0) {
                for (int i = 0; i < keys.length; i++) {
                    final MEKeyTool.KeyDetail key = keys[i];
                    model.addElement(key);
                }
            } else {
                model.addElement(NbBundle.getMessage(ExportPanel.class, "LBL_NoKey")); // NOI18N
            }
        } else {
            model.addElement(NbBundle.getMessage(ExportPanel.class, "LBL_ErrorLoadingKeys")); // NOI18N
        }
        list.setModel(model);
    }
    
    public static void showExportKeyIntoPlatform(final KeyStoreRepository.KeyStoreBean bean, final KeyStoreRepository.KeyStoreBean.KeyAliasBean alias, final J2MEPlatform preselectedPlatform, final String preselectedDomain) {
        if (bean == null  ||  alias == null  ||  ! bean.isOpened()  ||  ! alias.isValid()  ||  ! alias.isOpened())
            return;
        final ExportPanel export = new ExportPanel(bean, alias, preselectedPlatform, preselectedDomain);
        final DialogDescriptor dd = new DialogDescriptor(export, NbBundle.getMessage(ExportPanel.class, "TITLE_ExportKey"), true, null); // NOI18N
        dd.setOptions(new Object[] { export.bExport, NotifyDescriptor.CLOSED_OPTION });
        dd.setClosingOptions(new Object[] { NotifyDescriptor.CLOSED_OPTION });
        export.setDialogDescriptor(dd);
        export.checkErrors();
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
    }
    
    protected void export() {
        final J2MEPlatform platform = (J2MEPlatform) cPlatform.getSelectedItem();
        final String domain = (String) cDomain.getSelectedItem();
        final String keytool = MEKeyTool.getMEKeyToolPath(platform);
        if (platform != null  &&  domain != null  &&  keytool != null) {
            final File target = new File(System.getProperty("user.home", ""), ".keystore"); // NOI18N
            if (target.exists()) {
                if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportPanel.class, "MSG_PromptDeleteFile", bean.getKeyStorePath(), target.getAbsolutePath()), NbBundle.getMessage(ExportPanel.class, "MSG_PromptDeleteFileTitle"), NotifyDescriptor.YES_NO_OPTION)) != NotifyDescriptor.YES_OPTION) // NOI18N
                    return;
                if (! target.delete()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(ExportPanel.class, "ERR_CannotDeleteFile", target.getAbsolutePath()), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
                    return;
                }
            }
            boolean canDeleteTempKeystore = false;
            if (! KeyStoreRepository.KeyStoreBean.equalFiles(bean.getKeyStoreFile(), target)) {
                canDeleteTempKeystore = true;
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    fis = new FileInputStream(bean.getKeyStoreFile());
                    fos = new FileOutputStream(target);
                    FileUtil.copy(fis, fos);
                } catch (IOException e) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(ExportPanel.class, "ERR_IOErrorWhileCopying", bean.getKeyStorePath(), target.getAbsolutePath()), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
                    e.printStackTrace();
                    return;
                } finally {
                    if (fis != null) try { fis.close(); } catch (IOException e) {}
                    if (fos != null) try { fos.close(); } catch (IOException e) {}
                }
            }
            
            try {
                MEKeyTool.execute(new String[] { keytool, "-import", "-storepass", bean.getPassword(), "-alias", alias.getAlias(), "-domain", domain }); // NOI18N
            } catch (IOException e) {
                e.printStackTrace();
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(ExportPanel.class, "MSG_ErrorExportingKey"), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
            }
            
            if (canDeleteTempKeystore)
                target.delete();
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bDelete;
    private javax.swing.JComboBox cDomain;
    private javax.swing.JComboBox cPlatform;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lAlias;
    private javax.swing.JLabel lDetails;
    private javax.swing.JLabel lDomain;
    private javax.swing.JLabel lKeys;
    private javax.swing.JLabel lKeystore;
    private javax.swing.JLabel lPlatform;
    private javax.swing.JList list;
    private javax.swing.JPanel pDetails;
    private org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel pError;
    private javax.swing.JTextField tAlias;
    private javax.swing.JTextField tKeystore;
    // End of variables declaration//GEN-END:variables
    
}
