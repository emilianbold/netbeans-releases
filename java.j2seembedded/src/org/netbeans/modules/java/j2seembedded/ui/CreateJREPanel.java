/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seembedded.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Roman Svitanic
 * @author Tomas Zezula
 */
public class CreateJREPanel extends javax.swing.JPanel {

    private boolean valid = false;

    public CreateJREPanel(
            @NullAllowed final String username,
            @NullAllowed final String host) {
        assert username == null ? host == null : host != null;
        initComponents();

        final DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validatePanel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validatePanel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validatePanel();
            }
        };
        jreCreateLocation.getDocument().addDocumentListener(docListener);
        remoteJREPath.getDocument().addDocumentListener(docListener);
        labelRemoteJREInfo.setText(NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelRemoteJREInfo.text", username, host)); //NOI18N
        remoteJREPath.setText(NbBundle.getMessage(CreateJREPanel.class, "LBL_JRE_Path_Default", username)); //NOI18N
        validatePanel();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                remoteJREPath.requestFocusInWindow();
                remoteJREPath.selectAll();
            }
        });
    }

    @CheckForNull
    public static List<String> configure(
            @NonNull File destFolder) {
        Parameters.notNull("destFolder", destFolder);   //NOI18N
        CreateJREPanel panel = new CreateJREPanel(null, null);
        return configureImpl(panel, destFolder);
    }

    @CheckForNull
    public static Pair<List<String>,String> configure(
        @NonNull final String userName,
        @NonNull final String host,
        @NonNull File destFolder) {
        Parameters.notNull("destFolder", destFolder);   //NOI18N
        Parameters.notNull("userName", userName);   //NOI18N
        Parameters.notNull("host", host);   //NOI18N
        final CreateJREPanel panel = new CreateJREPanel(userName, host);
        final List<String> cmdLine = configureImpl(panel, destFolder);
        return cmdLine == null ?
            null :
            Pair.<List<String>,String>of(cmdLine, panel.getRemoteJREPath());
    }

    @CheckForNull
    private static List<String> configureImpl(
        @NonNull final CreateJREPanel panel,
        @NonNull final File destFolder) {
        DialogDescriptor dd = new DialogDescriptor(
            panel,
            NbBundle.getMessage(CreateJREPanel.class, "LBL_CreateJRETitle"));
        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            if (!panel.isPanelValid()) {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(CreateJREPanel.class, "ERROR_Invalid_CreateJREPanel"),
                    NotifyDescriptor.WARNING_MESSAGE));
                return null;
            }
            final List<String> cmdLine = new ArrayList<>();
            final File ejdk = new File(panel.getJRECreateLocation());
            final File bin = new File (ejdk, "bin");   //NOI18N
            final File jrecreate = new File(
                    bin,
                    Utilities.isWindows() ?
                        "jrecreate.bat" :   //NOI18N
                         "jrecreate.sh");   //NOI18N
            cmdLine.add(jrecreate.getAbsolutePath());
            cmdLine.add("--dest");          //NOI18N
            cmdLine.add(destFolder.getAbsolutePath());
            cmdLine.add("--ejdk-home");     //NOI18N
            cmdLine.add(ejdk.getAbsolutePath());
            final String profile = panel.getProfile();
            if (profile != null) {
                cmdLine.add("--profile");     //NOI18N
                cmdLine.add(profile);
            }
            cmdLine.add("--vm");     //NOI18N
            cmdLine.add(panel.getVirtualMachine());
            if (panel.isDebug()) {
                cmdLine.add("--debug");   //NOI18N
            }
            if (panel.isKeepDebugInfo()) {
                cmdLine.add("--keep-debug-info");   //NOI18N
            }
            if (panel.isNoCompression()) {
                cmdLine.add("--no-compression");   //NOI18N
            }
            List<String> extensions = new ArrayList<>();
            if (panel.isFxGraphics()) {
                extensions.add("fx:graphics");  //NOI18N
            }
            if (panel.isFxControls()) {
                extensions.add("fx:controls");  //NOI18N
            }
            if (panel.isSunec()) {
                extensions.add("sunec");        //NOI18N
            }
            if (panel.isSunpkcs11()) {
                extensions.add("sunpkcs11");        //NOI18N
            }
            if (panel.isLocales()) {
                extensions.add("locales");        //NOI18N
            }
            if (panel.isCharsets()) {
                extensions.add("charsets");        //NOI18N
            }
            if (panel.isNashorn()) {
                extensions.add("nashorn");        //NOI18N
            }
            if (!extensions.isEmpty()) {
                cmdLine.add("--extension"); //NOI18N
                cmdLine.addAll(extensions);
            }
            return cmdLine;
        }
        return null;
    }

    private void validatePanel() {
        if (jreCreateLocation.getText().isEmpty()) {
            labelError.setText(NbBundle.getMessage(CreateJREPanel.class, "ERROR_JRE_Create")); //NOI18N
            valid = false;
            return;
        }
        if (remoteJREPath.getText().isEmpty()) {
            labelError.setText(NbBundle.getMessage(CreateJREPanel.class, "ERROR_JRE_Path")); //NOI18N
            valid = false;
            return;
        }
        labelError.setText(null);
        valid = true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelOptions = new javax.swing.JLabel();
        checkBoxDebug = new javax.swing.JCheckBox();
        checkBoxKeepDebugInfo = new javax.swing.JCheckBox();
        checkBoxNoCompression = new javax.swing.JCheckBox();
        labelProfile = new javax.swing.JLabel();
        comboBoxProfile = new javax.swing.JComboBox();
        comboBoxVM = new javax.swing.JComboBox();
        labelVM = new javax.swing.JLabel();
        labelExtensions = new javax.swing.JLabel();
        checkBoxFxGraphics = new javax.swing.JCheckBox();
        checkBoxFxControls = new javax.swing.JCheckBox();
        checkBoxSunec = new javax.swing.JCheckBox();
        checkBoxSunpkcs11 = new javax.swing.JCheckBox();
        checkBoxLocales = new javax.swing.JCheckBox();
        checkBoxCharsets = new javax.swing.JCheckBox();
        checkBoxNashorn = new javax.swing.JCheckBox();
        labelError = new javax.swing.JLabel();
        labelJRECreateLocation = new javax.swing.JLabel();
        jreCreateLocation = new javax.swing.JTextField();
        buttonBrowse = new javax.swing.JButton();
        labelJRECreateInfo = new javax.swing.JLabel();
        labelRemoteJREPath = new javax.swing.JLabel();
        remoteJREPath = new javax.swing.JTextField();
        labelRemoteJREInfo = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        org.openide.awt.Mnemonics.setLocalizedText(labelOptions, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelOptions.text")); // NOI18N

        checkBoxDebug.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(checkBoxDebug, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxDebug.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxKeepDebugInfo, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxKeepDebugInfo.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxNoCompression, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxNoCompression.text")); // NOI18N

        labelProfile.setLabelFor(comboBoxProfile);
        org.openide.awt.Mnemonics.setLocalizedText(labelProfile, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelProfile.text")); // NOI18N

        comboBoxProfile.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Compact1", "Compact2", "Compact3", "Full JRE" }));

        comboBoxVM.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Minimal", "Client", "Server", "All" }));

        labelVM.setLabelFor(comboBoxVM);
        org.openide.awt.Mnemonics.setLocalizedText(labelVM, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelVM.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelExtensions, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelExtensions.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxFxGraphics, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxFxGraphics.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxFxControls, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxFxControls.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxSunec, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxSunec.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxSunpkcs11, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxSunpkcs11.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxLocales, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxLocales.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxCharsets, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxCharsets.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxNashorn, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.checkBoxNashorn.text")); // NOI18N

        labelError.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(labelError, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelError.text")); // NOI18N

        labelJRECreateLocation.setLabelFor(labelJRECreateLocation);
        org.openide.awt.Mnemonics.setLocalizedText(labelJRECreateLocation, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelJRECreateLocation.text")); // NOI18N

        jreCreateLocation.setText(org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.jreCreateLocation.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(buttonBrowse, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.buttonBrowse.text")); // NOI18N
        buttonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBrowseActionPerformed(evt);
            }
        });

        labelJRECreateInfo.setFont(labelJRECreateInfo.getFont().deriveFont((labelJRECreateInfo.getFont().getStyle() | java.awt.Font.ITALIC)));
        org.openide.awt.Mnemonics.setLocalizedText(labelJRECreateInfo, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelJRECreateInfo.text")); // NOI18N

        labelRemoteJREPath.setLabelFor(remoteJREPath);
        org.openide.awt.Mnemonics.setLocalizedText(labelRemoteJREPath, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelRemoteJREPath.text")); // NOI18N

        remoteJREPath.setText(org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.remoteJREPath.text")); // NOI18N

        labelRemoteJREInfo.setFont(labelRemoteJREInfo.getFont().deriveFont((labelRemoteJREInfo.getFont().getStyle() | java.awt.Font.ITALIC)));
        org.openide.awt.Mnemonics.setLocalizedText(labelRemoteJREInfo, org.openide.util.NbBundle.getMessage(CreateJREPanel.class, "CreateJREPanel.labelRemoteJREInfo.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxDebug)
                            .addComponent(checkBoxKeepDebugInfo)
                            .addComponent(checkBoxNoCompression))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelProfile)
                                    .addComponent(labelVM))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(comboBoxVM, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(comboBoxProfile, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelJRECreateLocation)
                                    .addComponent(labelRemoteJREPath))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jreCreateLocation)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buttonBrowse))
                                    .addComponent(remoteJREPath)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelOptions)
                                    .addComponent(labelExtensions)
                                    .addComponent(labelError)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(checkBoxSunpkcs11)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(checkBoxFxGraphics)
                                                    .addComponent(checkBoxFxControls)
                                                    .addComponent(checkBoxSunec))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(checkBoxNashorn)
                                                    .addComponent(checkBoxCharsets)
                                                    .addComponent(checkBoxLocales))))))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelJRECreateInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                                    .addComponent(labelRemoteJREInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelJRECreateLocation)
                    .addComponent(jreCreateLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelJRECreateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelRemoteJREPath)
                    .addComponent(remoteJREPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelRemoteJREInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelOptions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(checkBoxDebug)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxKeepDebugInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxNoCompression)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelProfile)
                    .addComponent(comboBoxProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboBoxVM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelVM))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelExtensions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxFxGraphics)
                    .addComponent(checkBoxLocales))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxFxControls)
                    .addComponent(checkBoxCharsets))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxSunec)
                    .addComponent(checkBoxNashorn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxSunpkcs11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(labelError))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBrowseActionPerformed
        final String oldValue = jreCreateLocation.getText();
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (oldValue != null) {
            chooser.setSelectedFile(new File(oldValue));
        }
        chooser.setDialogTitle(NbBundle.getMessage(CreateJREPanel.class, "Title_Chooser_SelectJRECreate")); //NOI18N
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            jreCreateLocation.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_buttonBrowseActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonBrowse;
    private javax.swing.JCheckBox checkBoxCharsets;
    private javax.swing.JCheckBox checkBoxDebug;
    private javax.swing.JCheckBox checkBoxFxControls;
    private javax.swing.JCheckBox checkBoxFxGraphics;
    private javax.swing.JCheckBox checkBoxKeepDebugInfo;
    private javax.swing.JCheckBox checkBoxLocales;
    private javax.swing.JCheckBox checkBoxNashorn;
    private javax.swing.JCheckBox checkBoxNoCompression;
    private javax.swing.JCheckBox checkBoxSunec;
    private javax.swing.JCheckBox checkBoxSunpkcs11;
    private javax.swing.JComboBox comboBoxProfile;
    private javax.swing.JComboBox comboBoxVM;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jreCreateLocation;
    private javax.swing.JLabel labelError;
    private javax.swing.JLabel labelExtensions;
    private javax.swing.JLabel labelJRECreateInfo;
    private javax.swing.JLabel labelJRECreateLocation;
    private javax.swing.JLabel labelOptions;
    private javax.swing.JLabel labelProfile;
    private javax.swing.JLabel labelRemoteJREInfo;
    private javax.swing.JLabel labelRemoteJREPath;
    private javax.swing.JLabel labelVM;
    private javax.swing.JTextField remoteJREPath;
    // End of variables declaration//GEN-END:variables

    public String getJRECreateLocation() {
        return jreCreateLocation.getText();
    }

    public String getRemoteJREPath() {
        return remoteJREPath.getText();
    }

    public String getProfile() {
        String profile = (String) comboBoxProfile.getSelectedItem();
        if (profile == null ||
            profile.equals(comboBoxProfile.getModel().getElementAt(comboBoxProfile.getModel().getSize()-1))) {
            return null;
        }
        return profile.toLowerCase();
    }

    public String getVirtualMachine() {
        return ((String) comboBoxVM.getSelectedItem()).toLowerCase();
    }

    public boolean isDebug() {
        return checkBoxDebug.isSelected();
    }

    public boolean isKeepDebugInfo() {
        return checkBoxKeepDebugInfo.isSelected();
    }

    public boolean isNoCompression() {
        return checkBoxNoCompression.isSelected();
    }

    public boolean isFxGraphics() {
        return checkBoxFxGraphics.isSelected();
    }

    public boolean isFxControls() {
        return checkBoxFxControls.isSelected();
    }

    public boolean isSunec() {
        return checkBoxSunec.isSelected();
    }

    public boolean isSunpkcs11() {
        return checkBoxSunpkcs11.isSelected();
    }

    public boolean isLocales() {
        return checkBoxLocales.isSelected();
    }

    public boolean isCharsets() {
        return checkBoxCharsets.isSelected();
    }

    public boolean isNashorn() {
        return checkBoxNashorn.isSelected();
    }

    public boolean isPanelValid() {
        return valid;
    }
}
