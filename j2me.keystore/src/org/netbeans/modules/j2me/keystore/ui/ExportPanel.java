/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.j2me.keystore.ui;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2me.keystore.KeyStoreRepository;
import org.netbeans.modules.j2me.keystore.MEKeyTool;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.JavaPlatform;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author David Kaspar
 * @author rsvitanic
 */
public class ExportPanel extends javax.swing.JPanel implements ItemListener, ListSelectionListener {

    private static final String HELP_ID = "org.netbeans.modules.j2me.keystore.ui.ExportPanel"; //NOI18N
    private static final Dimension PREFERRED_SIZE = new Dimension(500, 500);
    private static final RequestProcessor RP = new RequestProcessor(ExportPanel.class);
    public static final String SHARED_CLIENT = "<Shared>"; //NOI18N

    private DialogDescriptor dd;
    final private KeyStoreRepository.KeyStoreBean bean;
    final private KeyStoreRepository.KeyStoreBean.KeyAliasBean alias;
    final private JButton bExport;
    String keyString = NbBundle.getMessage(ExportPanel.class, "LBL_Key"); // NOI18N

    private ErrorPanel panelError = new ErrorPanel();
    private final HashMap<J2MEPlatform.Device, Boolean> mapDevice2ME3Platform = new HashMap<>();

    /**
     * Creates new form ExportPanel
     */
    public ExportPanel(KeyStoreRepository.KeyStoreBean bean, KeyStoreRepository.KeyStoreBean.KeyAliasBean alias, Object preselectedTarget, String preselectedDomain) {
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
            @Override
            public void actionPerformed(@SuppressWarnings("unused") final ActionEvent evt) {
                setListCustomMessage(NbBundle.getMessage(ExportPanel.class, "MSG_Exporting")); //NOI18N
                bExport.setEnabled(false);
                cTarget.setEnabled(false);
                cClient.setEnabled(false);
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        export();
                        String client = null;
                        if (cClient.isVisible() && cClient.getSelectedItem() != null && !cClient.getSelectedItem().equals(SHARED_CLIENT)) {
                            client = (String) cClient.getSelectedItem();
                        }
                        reloadList(cTarget.getSelectedItem(), client);
                    }
                });
            }
        });
        tKeystore.setText(bean.getKeyStorePath());
        tAlias.setText(alias.getAlias());
        lDetails.setText(KeyAliasCellRenderer.getHtmlFormattedText(alias));
        cTarget.addItemListener(this);
        cClient.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String client = (String) cClient.getSelectedItem();
                    if (!client.equals(NbBundle.getMessage(ExportPanel.class, "MSG_Loading_Clients"))) { //NOI18N
                        reloadList((J2MEPlatform.Device) cTarget.getSelectedItem(), client.equals(SHARED_CLIENT) ? null : client);
                    }
                }
            }
        });
        list.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(final JList list, Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                if (value instanceof MEKeyTool.KeyDetail) {
                    final MEKeyTool.KeyDetail key = (MEKeyTool.KeyDetail) value;
                    final Color color = isSelected ? list.getSelectionForeground() : list.getForeground();
                    final StringBuffer sb = new StringBuffer("<html><font color=\"#" + Integer.toHexString(color.getRGB() & 0xffffff) + "\"><b>" + keyString + ": " + key.getOrder() + "</b>"); // NOI18N
                    final String[] lines = key.getInfo();
                    if (lines != null) {
                        for (String line : lines) {
                            sb.append("<br>").append(line); // NOI18N
                        }
                    }
                    value = sb.toString();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        list.addListSelectionListener(this);
        cTarget.removeAllItems();
        cTarget.addItem(org.openide.util.NbBundle.getMessage(ExportPanel.class, "MSG_Loading")); // NOI18N
        final Collection<JavaPlatform> javaMEPlatforms = getJavaMEPlatformsWithoutBdj();
        RP.post(new Runnable() {
            @Override
            public void run() {
                boolean removedLoading = false;
                for (final JavaPlatform platform : javaMEPlatforms) {
                    if (platform != null) {
                        if (platform instanceof J2MEPlatform) {
                            J2MEPlatform.Device[] dev = ((J2MEPlatform) platform).getDevices();
                            for (J2MEPlatform.Device device : dev) {
                                if (!((J2MEPlatform) platform).isMe3Platform() || null != MEKeyTool.keystoreForDevice(device)) {
                                    if (!removedLoading) {
                                        cTarget.removeAllItems();
                                        removedLoading = true;
                                    }
                                    synchronized (mapDevice2ME3Platform) {
                                        mapDevice2ME3Platform.put(device, ((J2MEPlatform) platform).isMe3Platform());
                                    }
                                    cTarget.addItem(device);
                                } else {
                                    cTarget.removeAllItems();
                                    cTarget.addItem(platform); //platform only once
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(panelError, gridBagConstraints);
    }

    private boolean equivalent(String owner, String subjectName) {
        final String[] ownerSplit = owner.split("[,;]"); //NOI18N
        final Set<String> ownerSet = new HashSet<>();
        for (final String string : ownerSplit) {
            ownerSet.add(string.trim());
        }

        final String[] subjectSplit = subjectName.split("[,;]"); //NOI18N
        final Set<String> subjectSet = new HashSet<>();
        for (final String string : subjectSplit) {
            subjectSet.add(string.trim());
        }

        return ownerSet.equals(subjectSet);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lKeystore = new javax.swing.JLabel();
        tKeystore = new javax.swing.JTextField();
        lAlias = new javax.swing.JLabel();
        tAlias = new javax.swing.JTextField();
        pDetails = new javax.swing.JPanel();
        lDetails = new javax.swing.JLabel();
        lPlatform = new javax.swing.JLabel();
        cTarget = new javax.swing.JComboBox();
        lDomain = new javax.swing.JLabel();
        cDomain = new javax.swing.JComboBox();
        lKeys = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        bDelete = new javax.swing.JButton();
        lClient = new javax.swing.JLabel();
        cClient = new javax.swing.JComboBox();

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
        pDetails.setEnabled(false);
        pDetails.setPreferredSize(new java.awt.Dimension(300, 100));
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

        lPlatform.setLabelFor(cTarget);
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
        add(cTarget, gridBagConstraints);
        cTarget.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export_Platform")); // NOI18N

        lDomain.setLabelFor(cDomain);
        org.openide.awt.Mnemonics.setLocalizedText(lDomain, org.openide.util.NbBundle.getMessage(ExportPanel.class, "LBL_Export_Domain")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(lDomain, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(cDomain, gridBagConstraints);
        cDomain.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export_Domain")); // NOI18N

        lKeys.setLabelFor(list);
        org.openide.awt.Mnemonics.setLocalizedText(lKeys, org.openide.util.NbBundle.getMessage(ExportPanel.class, "LBL_Export_Keys")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(lKeys, gridBagConstraints);

        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(list);
        list.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export_Keys")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
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
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(bDelete, gridBagConstraints);
        bDelete.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export_DeleteKey")); // NOI18N

        lClient.setLabelFor(cClient);
        org.openide.awt.Mnemonics.setLocalizedText(lClient, org.openide.util.NbBundle.getMessage(ExportPanel.class, "LBL_Export_Client")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(lClient, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(cClient, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(ExportPanel.class, "ACSN_Export")); //NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExportPanel.class, "ACSD_Export")); //NOI18N
    }

    @Override
    public void valueChanged(@SuppressWarnings("unused")
            final ListSelectionEvent e) {
        bDelete.setEnabled(list.getSelectedValue() instanceof MEKeyTool.KeyDetail);
    }

    @Override
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }

    protected void setDialogDescriptor(final DialogDescriptor desc) {
        this.dd = desc;
        dd.setHelpCtx(new HelpCtx(HELP_ID));
        checkErrors();
    }

    public String getErrorMessage() {
        final ListModel model = list.getModel();
        for (int a = 0; a < model.getSize(); a++) {
            final Object o = model.getElementAt(a);
            if (o instanceof MEKeyTool.KeyDetail) {
                final MEKeyTool.KeyDetail key = (MEKeyTool.KeyDetail) o;
                final String owner = key.getOwner();
                if (owner != null && equivalent(owner, alias.getSubjectName())) {
                    return NbBundle.getMessage(ExportPanel.class, "ERR_KeyIsAlreadyInPlatform", owner, Integer.toString(key.getOrder())); // NOI18N
                }
            }
        }
        return null;
    }

    public void checkErrors() {
        final String errorMessage = getErrorMessage();
        panelError.setErrorMessage(errorMessage);
        final boolean valid = errorMessage == null;
        bExport.setEnabled(valid && cTarget.getSelectedItem() != null && !(cTarget.getSelectedItem() instanceof String));
        if (dd != null && valid != dd.isValid()) {
            dd.setValid(valid);
        }
    }

    private void bDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDeleteActionPerformed
        final Object target = cTarget.getSelectedItem();
        final Object value = list.getSelectedValue();
        RP.post(new Runnable() {
            @Override
            public void run() {
                deleteKey(target, value);
            }
        });
    }//GEN-LAST:event_bDeleteActionPerformed

    private void deleteKey(Object target, Object value) {
        final String keytool = MEKeyTool.getMEKeyToolPath(target);
        if (value instanceof MEKeyTool.KeyDetail && keytool != null) {
            final MEKeyTool.KeyDetail key = (MEKeyTool.KeyDetail) value;
            String name = (target instanceof J2MEPlatform.Device) ? ((J2MEPlatform.Device) target).getName() : ((J2MEPlatform) target).getName();
            if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportPanel.class, "MSG_DeleteKeyConfirmation", Integer.toString(key.getOrder()), name), NbBundle.getMessage(ExportPanel.class, "TITLE_ConfirmKeyDeletion"), NotifyDescriptor.YES_NO_OPTION)) == NotifyDescriptor.YES_OPTION) { // NOI18N
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        cTarget.setEnabled(false);
                        cClient.setEnabled(false);
                    }
                });
                setListCustomMessage(NbBundle.getMessage(ExportPanel.class, "MSG_Deleting")); //NOI18N
                String client = null;
                if (cClient.isVisible() && cClient.getSelectedItem() != null && !cClient.getSelectedItem().equals(SHARED_CLIENT)) {
                    client = (String) cClient.getSelectedItem();
                }
                try {
                    if (target instanceof J2MEPlatform.Device) {
                        final String keystore = MEKeyTool.keystoreForDevice((J2MEPlatform.Device) target);
                        if (keystore != null) {
                            MEKeyTool.execute(new String[]{keytool, "-delete", "-MEkeystore", keystore, "-number", Integer.toString(key.getOrder())}); // NOI18N
                        } else if (client != null) {
                            MEKeyTool.execute(new String[]{keytool, "-delete", "-Xdevice:" + ((J2MEPlatform.Device) target).getName(), "-client", client, "-number", Integer.toString(key.getOrder())}); // NOI18N
                        } else {
                            MEKeyTool.execute(new String[]{keytool, "-delete", "-Xdevice:" + ((J2MEPlatform.Device) target).getName(), "-number", Integer.toString(key.getOrder())}); // NOI18N
                        }
                    } else {
                        MEKeyTool.execute(new String[]{keytool, "-delete", "-number", Integer.toString(key.getOrder())}); // NOI18N
                    }
                } catch (IOException e) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(ExportPanel.class, "ERR_WhileDeletingKey") + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
                }
                reloadList(cTarget.getSelectedItem(), client);
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            return;
        }
        final Object target = cTarget.getSelectedItem();
        if (target instanceof String) {
            reloadList(target);
            return;
        }
        synchronized (mapDevice2ME3Platform) {
            if (target != null
                    && ((target instanceof J2MEPlatform.Device
                    && (mapDevice2ME3Platform.get((J2MEPlatform.Device) target) != null && mapDevice2ME3Platform.get((J2MEPlatform.Device) target).booleanValue()))
                    || (target instanceof J2MEPlatform && ((J2MEPlatform) target).isMe3Platform()))) {
                // clients option is not available for ME 3.x
                enableClients(false);
                // security domains are valid only for ME 3.x, not for ME 8.0+
                enableSecurityDomains(true);
                final ArrayList<String> domainList = new ArrayList<>();
                if (target instanceof J2MEPlatform.Device) {
                    final String[] domains = ((J2MEPlatform.Device) target).getSecurityDomains();
                    if (domains != null) {
                        for (String domain : domains) {
                            if (!domainList.contains(domain)) {
                                domainList.add(domain);
                            }
                        }
                    }
                } else {
                    final J2MEPlatform.Device[] devices = ((J2MEPlatform) target).getDevices();
                    if (devices != null) {
                        for (J2MEPlatform.Device device : devices) {
                            final String[] domains = device.getSecurityDomains();
                            if (domains != null) {
                                for (String domain : domains) {
                                    if (!domainList.contains(domain)) {
                                        domainList.add(domain);
                                    }
                                }
                            }
                        }
                    }
                }
                cDomain.removeAllItems();
                for (int i = 0; i < domainList.size(); i++) {
                    cDomain.addItem(domainList.get(i));
                }
                reloadList(target);
            } else {
                enableSecurityDomains(false);
                if (target instanceof J2MEPlatform.Device) {
                    loadClients((J2MEPlatform.Device) target);
                } else {
                    enableClients(false);
                    reloadList(target);
                }
            }
        }
    }

    private void loadClients(final J2MEPlatform.Device device) {
        setListLoading();
        if (SwingUtilities.isEventDispatchThread()) {
            cClient.setEnabled(false);
            cClient.removeAllItems();
            cClient.addItem(NbBundle.getMessage(ExportPanel.class, "MSG_Loading_Clients")); // NOI18N
            RP.post(new Runnable() {
                @Override
                public void run() {
                    final String[] clients = MEKeyTool.listClientsForDevice(device);
                    updateClientCombo(device, clients);
                }
            });
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    cClient.setEnabled(false);
                    cClient.removeAllItems();
                    cClient.addItem(NbBundle.getMessage(ExportPanel.class, "MSG_Loading_Clients")); // NOI18N
                }
            });
            final String[] clients = MEKeyTool.listClientsForDevice(device);
            updateClientCombo(device, clients);
        }
    }

    private void updateClientCombo(final J2MEPlatform.Device device, final String[] clients) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean clientsAvailable = clients != null && clients.length > 0;
                if (clientsAvailable) {
                    cClient.setModel(new DefaultComboBoxModel(clients));
                }
                enableClients(clientsAvailable);
                String client = clientsAvailable ? (String) cClient.getSelectedItem() : null;
                if (client != null && client.equals(SHARED_CLIENT)) {
                    // Do not use specific client
                    client = null;
                }
                reloadList(device, client);
            }
        });
    }

    protected void reloadList(final Object target) {
        reloadList(target, null);
    }

    protected void reloadList(final Object target, final String client) {
        bDelete.setEnabled(false);
        if (target != null && !(target instanceof String)) {
            setListLoading();
            if (SwingUtilities.isEventDispatchThread()) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        reloadListImpl(target, client);
                    }
                });
            } else {
                reloadListImpl(target, client);
            }
        } else {
            setListNotLoaded();
            checkErrors();
        }
    }

    private void reloadListImpl(Object target, String client) {
        final MEKeyTool.KeyDetail[] keyList;
        if (client != null) {
            keyList = MEKeyTool.listKeys((J2MEPlatform.Device) target, client);
        } else {
            keyList = MEKeyTool.listKeys(target);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setList(keyList);
                cTarget.setEnabled(true);
                cClient.setEnabled(cClient.getModel() != null && cClient.getModel().getSize() > 0);
                checkErrors();
            }
        });
    }

    private void setListNotLoaded() {
        setListCustomMessage(NbBundle.getMessage(ExportPanel.class, "LBL_NotLoaded")); //NOI18N
    }

    private void setListLoading() {
        setListCustomMessage(NbBundle.getMessage(ExportPanel.class, "LBL_Loading")); //NOI18N
    }

    private void setListCustomMessage(final String message) {
        final DefaultListModel model = new DefaultListModel();
        model.addElement(message);
        if (SwingUtilities.isEventDispatchThread()) {
            list.setModel(model);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    list.setModel(model);
                }
            });
        }
    }

    private void setList(final MEKeyTool.KeyDetail[] keys) {
        final DefaultListModel model = new DefaultListModel();
        if (keys != null) {
            if (keys.length > 0) {
                for (MEKeyTool.KeyDetail key : keys) {
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

    public static void showExportKeyIntoPlatform(final KeyStoreRepository.KeyStoreBean bean, final KeyStoreRepository.KeyStoreBean.KeyAliasBean alias, final Object preselectedTarget, final String preselectedDomain) {
        if (bean == null || alias == null || !bean.isOpened() || !alias.isValid() || !alias.isOpened()) {
            return;
        }
        final ExportPanel export = new ExportPanel(bean, alias, preselectedTarget, preselectedDomain);
        final DialogDescriptor dd = new DialogDescriptor(export, NbBundle.getMessage(ExportPanel.class, "TITLE_ExportKey"), true, null); // NOI18N
        dd.setOptions(new Object[]{export.bExport, NotifyDescriptor.CLOSED_OPTION});
        dd.setClosingOptions(new Object[]{NotifyDescriptor.CLOSED_OPTION});
        export.setDialogDescriptor(dd);
        export.checkErrors();
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
    }

    protected void export() {
        final Object target = cTarget.getSelectedItem();
        String domain = null;
        if (cDomain.isVisible()) {
            domain = (String) cDomain.getSelectedItem();
        }
        String client = null;
        if (cClient.isVisible()) {
            client = (String) cClient.getSelectedItem();
            if (client.equals(SHARED_CLIENT)) {
                // Do not use client
                client = null;
            }
        }
        final String keytool = MEKeyTool.getMEKeyToolPath(target);
        if (target != null && keytool != null) {
            final File keyStoreFile = new File(System.getProperty("user.home", ""), ".keystore"); // NOI18N
            if (keyStoreFile.exists()) {
                if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportPanel.class, "MSG_PromptDeleteFile", bean.getKeyStorePath(), keyStoreFile.getAbsolutePath()), NbBundle.getMessage(ExportPanel.class, "MSG_PromptDeleteFileTitle"), NotifyDescriptor.YES_NO_OPTION)) != NotifyDescriptor.YES_OPTION) // NOI18N
                {
                    return;
                }
                if (!keyStoreFile.delete()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(ExportPanel.class, "ERR_CannotDeleteFile", keyStoreFile.getAbsolutePath()), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
                    return;
                }
            }
            boolean canDeleteTempKeystore = false;
            if (!KeyStoreRepository.KeyStoreBean.equalFiles(bean.getKeyStoreFile(), keyStoreFile)) {
                canDeleteTempKeystore = true;
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    fis = new FileInputStream(bean.getKeyStoreFile());
                    fos = new FileOutputStream(keyStoreFile);
                    FileUtil.copy(fis, fos);
                } catch (IOException e) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(ExportPanel.class, "ERR_IOErrorWhileCopying", bean.getKeyStorePath(), keyStoreFile.getAbsolutePath()), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
                    return;
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            }

            try {
                if (target instanceof J2MEPlatform.Device) {
                    final String keystore = MEKeyTool.keystoreForDevice((J2MEPlatform.Device) target);
                    if (domain != null && keystore != null) {
                        MEKeyTool.execute(new String[]{keytool, "-import", "-MEkeystore", keystore, "-keystore", bean.getKeyStorePath(), "-storepass", bean.getPassword(), "-alias", alias.getAlias(), "-domain", domain}); // NOI18N
                    } else if (client != null) {
                        MEKeyTool.execute(new String[]{keytool, "-import", "-Xdevice:" + ((J2MEPlatform.Device) target).getName(), "-client", client, "-keystore", bean.getKeyStorePath(), "-storepass", bean.getPassword(), "-alias", alias.getAlias()}); // NOI18N
                    } else {
                        MEKeyTool.execute(new String[]{keytool, "-import", "-Xdevice:" + ((J2MEPlatform.Device) target).getName(), "-keystore", bean.getKeyStorePath(), "-storepass", bean.getPassword(), "-alias", alias.getAlias()}); // NOI18N
                    }
                } else {
                    if (domain != null) {
                        MEKeyTool.execute(new String[]{keytool, "-import", "-storepass", bean.getPassword(), "-alias", alias.getAlias(), "-domain", domain}); // NOI18N
                    }
                }
            } catch (IOException e) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(ExportPanel.class, "MSG_ErrorExportingKey") + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
            }

            if (canDeleteTempKeystore) {
                keyStoreFile.delete();
            }
        }
    }

    public static Collection<JavaPlatform> getJavaMEPlatformsWithoutBdj() {
        final java.util.List<JavaPlatform> res = new ArrayList<>();
        final JavaPlatformManager platformManager = JavaPlatformManager.getDefault();
        JavaPlatform[] platforms = null;
        try {
            platforms = platformManager.getInstalledPlatforms();
        } catch (Exception e) {
            return Collections.emptyList();
        }
        for (JavaPlatform javaPlatform : platforms) {
            if (javaPlatform instanceof J2MEPlatform) {
                res.add(javaPlatform);
            }
        }
        return res;
    }

    private void enableSecurityDomains(boolean enable) {
        lDomain.setVisible(enable);
        cDomain.setVisible(enable);
    }

    private void enableClients(boolean enable) {
        lClient.setVisible(enable);
        cClient.setVisible(enable);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bDelete;
    private javax.swing.JComboBox cClient;
    private javax.swing.JComboBox cDomain;
    private javax.swing.JComboBox cTarget;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lAlias;
    private javax.swing.JLabel lClient;
    private javax.swing.JLabel lDetails;
    private javax.swing.JLabel lDomain;
    private javax.swing.JLabel lKeys;
    private javax.swing.JLabel lKeystore;
    private javax.swing.JLabel lPlatform;
    private javax.swing.JList list;
    private javax.swing.JPanel pDetails;
    private javax.swing.JTextField tAlias;
    private javax.swing.JTextField tKeystore;
    // End of variables declaration//GEN-END:variables

}
