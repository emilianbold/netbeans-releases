/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * JFXDeploymentPanel.java
 *
 * Created on 1.8.2011, 15:51:50
 */
package org.netbeans.modules.javafx2.project.ui;

import java.awt.Dialog;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.netbeans.modules.javafx2.project.JFXProjectProperties.BundlingType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Somol
 */
public class JFXDeploymentPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private File lastImageFolder = null;
    private JFXProjectProperties jfxProps;
    
    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N
    
    private volatile boolean comboBoxNativeBundlingActionRunning = false;
    
    /**
     * Creates new form JFXDeploymentPanel
     */
    public JFXDeploymentPanel(JFXProjectProperties props) {
        this.jfxProps = props;
        initComponents();
        if(JFXProjectProperties.isTrue(props.getEvaluator().getProperty(JFXProjectProperties.JAVAFX_SWING))) {
            // disable UI components irrelevant for FX-in-Swing project
            labelInitialRemark.setVisible(false);
            labelInitialRemark.setEnabled(false);
            labelInitialRemarkSwing.setVisible(true);
            labelInitialRemarkSwing.setEnabled(true);
            labelProperties.setVisible(false);
            labelProperties.setEnabled(false);
            labelPropertiesSwing.setVisible(true);
            labelPropertiesSwing.setEnabled(true);
            //checkBoxUpgradeBackground.setVisible(false);
            //checkBoxNoInternet.setVisible(false);
            checkBoxInstallPerm.setVisible(false);
            checkBoxDeskShortcut.setVisible(false);
            checkBoxMenuShortcut.setVisible(false);
            labelCustomJS.setVisible(false);
            labelCustomJSMessage.setVisible(false);
            buttonCustomJSMessage.setVisible(false);
            labelDownloadMode.setVisible(false);
            labelDownloadModeMessage.setVisible(false);
            buttonDownloadMode.setVisible(false);
            //checkBoxUpgradeBackground.setEnabled(false);
            //checkBoxNoInternet.setEnabled(false);
            checkBoxInstallPerm.setEnabled(false);
            checkBoxDeskShortcut.setEnabled(false);
            checkBoxMenuShortcut.setEnabled(false);
            labelCustomJS.setEnabled(false);
            labelCustomJSMessage.setEnabled(false);
            buttonCustomJSMessage.setEnabled(false);
            labelDownloadMode.setEnabled(false);
            labelDownloadModeMessage.setEnabled(false);
            buttonDownloadMode.setEnabled(false);
        } else {
            labelInitialRemark.setVisible(true);
            labelInitialRemark.setEnabled(true);
            labelInitialRemarkSwing.setVisible(false);
            labelInitialRemarkSwing.setEnabled(false);
            labelProperties.setVisible(true);
            labelProperties.setEnabled(true);
            labelPropertiesSwing.setVisible(false);
            labelPropertiesSwing.setEnabled(false);
            checkBoxInstallPerm.setModel(jfxProps.getInstallPermanentlyModel());
            checkBoxDeskShortcut.setModel(jfxProps.getAddDesktopShortcutModel());
            checkBoxMenuShortcut.setModel(jfxProps.getAddStartMenuShortcutModel());
            refreshCustomJSLabel();
            if(jfxProps.getRuntimeCP().isEmpty()) {
                buttonDownloadMode.setEnabled(false);
                labelDownloadMode.setEnabled(false);
                labelDownloadModeMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_DownloadModeNone")); // NOI18N
                labelDownloadModeMessage.setEnabled(false);
            } else {
                refreshDownloadModeControls();
            }
        }
        checkBoxUpgradeBackground.setModel(jfxProps.getBackgroundUpdateCheckModel());
        checkBoxNoInternet.setModel(jfxProps.getAllowOfflineModel());

        textFieldIcon.setDocument(jfxProps.getIconDocumentModel());
        checkBoxUnrestrictedAcc.setSelected(jfxProps.getSigningEnabled());
        labelSigning.setEnabled(jfxProps.getSigningEnabled());
        labelSigningMessage.setEnabled(jfxProps.getSigningEnabled());
        buttonSigning.setEnabled(jfxProps.getSigningEnabled());
        refreshSigningLabel();
        setupNativeBundlingCombo();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panelTop = new javax.swing.JPanel();
        labelInitialRemark = new javax.swing.JLabel();
        labelInitialRemarkSwing = new javax.swing.JLabel();
        labelProperties = new javax.swing.JLabel();
        labelPropertiesSwing = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        checkBoxInstallPerm = new javax.swing.JCheckBox();
        checkBoxDeskShortcut = new javax.swing.JCheckBox();
        checkBoxMenuShortcut = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        checkBoxNoInternet = new javax.swing.JCheckBox();
        checkBoxUpgradeBackground = new javax.swing.JCheckBox();
        labelIcon = new javax.swing.JLabel();
        textFieldIcon = new javax.swing.JTextField();
        buttonIcon = new javax.swing.JButton();
        labelIconRemark = new javax.swing.JLabel();
        panelBottom = new javax.swing.JPanel();
        checkBoxUnrestrictedAcc = new javax.swing.JCheckBox();
        labelSigning = new javax.swing.JLabel();
        labelSigningMessage = new javax.swing.JLabel();
        buttonSigning = new javax.swing.JButton();
        labelCustomJS = new javax.swing.JLabel();
        labelCustomJSMessage = new javax.swing.JLabel();
        buttonCustomJSMessage = new javax.swing.JButton();
        labelDownloadMode = new javax.swing.JLabel();
        labelDownloadModeMessage = new javax.swing.JLabel();
        buttonDownloadMode = new javax.swing.JButton();
        checkBoxBundle = new javax.swing.JCheckBox();
        comboBoxBundle = new javax.swing.JComboBox();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));

        setLayout(new java.awt.GridBagLayout());

        panelTop.setLayout(new java.awt.GridBagLayout());

        labelInitialRemark.setText(org.openide.util.NbBundle.getBundle(JFXDeploymentPanel.class).getString("JFXDeploymentPanel.labelInitialRemark.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        panelTop.add(labelInitialRemark, gridBagConstraints);
        labelInitialRemark.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelInitialRemark.AccessibleContext.accessibleDescription")); // NOI18N

        labelInitialRemarkSwing.setText(org.openide.util.NbBundle.getBundle(JFXDeploymentPanel.class).getString("JFXDeploymentPanel.labelInitialRemarkSwing.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        panelTop.add(labelInitialRemarkSwing, gridBagConstraints);
        labelInitialRemarkSwing.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelInitialRemarkSwing.AccessibleContext.accessibleDescription")); // NOI18N

        labelProperties.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelProperties.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 0);
        panelTop.add(labelProperties, gridBagConstraints);
        labelProperties.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelProperties.AccessibleContext.accessibleDescription")); // NOI18N

        labelPropertiesSwing.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelPropertiesSwing.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 0);
        panelTop.add(labelPropertiesSwing, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxInstallPerm, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.checkBoxInstallPerm.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        jPanel1.add(checkBoxInstallPerm, gridBagConstraints);
        checkBoxInstallPerm.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxInstallPerm.text")); // NOI18N
        checkBoxInstallPerm.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxInstallPerm.text")); // NOI18N
        checkBoxInstallPerm.getAccessibleContext().setAccessibleParent(panelTop);

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxDeskShortcut, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.checkBoxDeskShortcut.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(checkBoxDeskShortcut, gridBagConstraints);
        checkBoxDeskShortcut.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxDeskShortcut.text")); // NOI18N
        checkBoxDeskShortcut.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxDeskShortcut.text")); // NOI18N
        checkBoxDeskShortcut.getAccessibleContext().setAccessibleParent(panelTop);

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxMenuShortcut, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.checkBoxMenuShortcut.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(checkBoxMenuShortcut, gridBagConstraints);
        checkBoxMenuShortcut.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxMenuShortcut.text")); // NOI18N
        checkBoxMenuShortcut.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxMenuShortcut.text")); // NOI18N
        checkBoxMenuShortcut.getAccessibleContext().setAccessibleParent(panelTop);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 10, 0);
        panelTop.add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        checkBoxNoInternet.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(checkBoxNoInternet, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.checkBoxNoInternet.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel2.add(checkBoxNoInternet, gridBagConstraints);
        checkBoxNoInternet.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxNoInternet.text")); // NOI18N
        checkBoxNoInternet.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxNoInternet.text")); // NOI18N

        checkBoxUpgradeBackground.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(checkBoxUpgradeBackground, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.checkBoxUpgradeBackground.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        jPanel2.add(checkBoxUpgradeBackground, gridBagConstraints);
        checkBoxUpgradeBackground.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxUpgradeBackground.text")); // NOI18N
        checkBoxUpgradeBackground.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxUpgradeBackground.text")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 10, 0);
        panelTop.add(jPanel2, gridBagConstraints);

        labelIcon.setLabelFor(textFieldIcon);
        org.openide.awt.Mnemonics.setLocalizedText(labelIcon, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.labelIcon.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        panelTop.add(labelIcon, gridBagConstraints);
        labelIcon.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.labelIcon.text")); // NOI18N
        labelIcon.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.labelIcon.text")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        panelTop.add(textFieldIcon, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonIcon, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.buttonIcon.text")); // NOI18N
        buttonIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonIconActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        panelTop.add(buttonIcon, gridBagConstraints);
        buttonIcon.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.buttonIcon.text")); // NOI18N
        buttonIcon.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.buttonIcon.text")); // NOI18N

        labelIconRemark.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelIconRemark.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 15, 0);
        panelTop.add(labelIconRemark, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(panelTop, gridBagConstraints);

        panelBottom.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxUnrestrictedAcc, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.checkBoxUnrestrictedAcc.text")); // NOI18N
        checkBoxUnrestrictedAcc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxUnrestrictedAccActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        panelBottom.add(checkBoxUnrestrictedAcc, gridBagConstraints);
        checkBoxUnrestrictedAcc.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.checkBoxUnrestrictedAcc.text")); // NOI18N
        checkBoxUnrestrictedAcc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.checkBoxUnrestrictedAcc.text")); // NOI18N

        labelSigning.setLabelFor(labelSigningMessage);
        org.openide.awt.Mnemonics.setLocalizedText(labelSigning, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.labelSigning.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 37, 20, 10);
        panelBottom.add(labelSigning, gridBagConstraints);
        labelSigning.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.labelSigning.text")); // NOI18N
        labelSigning.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.labelSigning.text")); // NOI18N

        labelSigningMessage.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelSigningMessage.text")); // NOI18N
        labelSigningMessage.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        panelBottom.add(labelSigningMessage, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonSigning, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.buttonSigning.text")); // NOI18N
        buttonSigning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSigningActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
        panelBottom.add(buttonSigning, gridBagConstraints);
        buttonSigning.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.buttonSigning.text")); // NOI18N
        buttonSigning.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.buttonSigning.text")); // NOI18N

        labelCustomJS.setLabelFor(labelCustomJSMessage);
        org.openide.awt.Mnemonics.setLocalizedText(labelCustomJS, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.labelCustomJS.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 15, 10);
        panelBottom.add(labelCustomJS, gridBagConstraints);
        labelCustomJS.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.labelCustomJS.text")); // NOI18N
        labelCustomJS.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.labelCustomJS.text")); // NOI18N

        labelCustomJSMessage.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelCustomJSMessage.text")); // NOI18N
        labelCustomJSMessage.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        panelBottom.add(labelCustomJSMessage, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonCustomJSMessage, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.buttonCustomJSMessage.text")); // NOI18N
        buttonCustomJSMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCustomJSMessageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        panelBottom.add(buttonCustomJSMessage, gridBagConstraints);
        buttonCustomJSMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.buttonCustomJSMessage.text")); // NOI18N
        buttonCustomJSMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.buttonCustomJSMessage.text")); // NOI18N

        labelDownloadMode.setLabelFor(labelDownloadModeMessage);
        org.openide.awt.Mnemonics.setLocalizedText(labelDownloadMode, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.labelDownloadMode.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 0, 10);
        panelBottom.add(labelDownloadMode, gridBagConstraints);
        labelDownloadMode.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.labelDownloadMode.text")); // NOI18N
        labelDownloadMode.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.labelDownloadMode.text")); // NOI18N

        labelDownloadModeMessage.setText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.labelDownloadModeMessage.text")); // NOI18N
        labelDownloadModeMessage.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        panelBottom.add(labelDownloadModeMessage, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonDownloadMode, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_JFXDeploymentPanel.buttonDownloadMode.text")); // NOI18N
        buttonDownloadMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDownloadModeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        panelBottom.add(buttonDownloadMode, gridBagConstraints);
        buttonDownloadMode.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AN_JFXDeploymentPanel.buttonDownloadMode.text")); // NOI18N
        buttonDownloadMode.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "AD_JFXDeploymentPanel.buttonDownloadMode.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxBundle, org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "JFXDeploymentPanel.checkBoxBundle.text")); // NOI18N
        checkBoxBundle.setToolTipText(org.openide.util.NbBundle.getMessage(JFXDeploymentPanel.class, "TOOLTIP_labelBundle")); // NOI18N
        checkBoxBundle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxBundleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 10, 0);
        panelBottom.add(checkBoxBundle, gridBagConstraints);

        comboBoxBundle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Image", "Installer", " " }));
        comboBoxBundle.setEnabled(false);
        comboBoxBundle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxBundleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        panelBottom.add(comboBoxBundle, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        add(panelBottom, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.1;
        add(filler1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void buttonIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonIconActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(null);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileFilter(new IconFileFilter());
    if (lastImageFolder != null) {
        chooser.setSelectedFile(lastImageFolder);
    } else { // ???
        // workDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        // chooser.setSelectedFile(new File(workDir));
    }
    chooser.setDialogTitle(NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_Select_Icon_Image")); // NOI18N
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File file = FileUtil.normalizeFile(chooser.getSelectedFile());
        try {
            textFieldIcon.setText(file.toURI().toURL().toString());
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, "File {0} URL could not be retrieved for use as FX icon in JFXDeploymentPanel", file.toString()); // NOI18N
        }
        lastImageFolder = file.getParentFile();
    }
}//GEN-LAST:event_buttonIconActionPerformed

private void buttonSigningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSigningActionPerformed
    JFXSigningPanel panel = new JFXSigningPanel(jfxProps);
    DialogDescriptor dialogDesc = new DialogDescriptor(panel, NbBundle.getMessage(JFXSigningPanel.class, "TITLE_JFXSigningPanel"), true, null); // NOI18N
    Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
    dialog.setVisible(true);
    if (dialogDesc.getValue() == DialogDescriptor.OK_OPTION) {
        panel.store();
        refreshSigningLabel();
    }
}//GEN-LAST:event_buttonSigningActionPerformed

private void checkBoxUnrestrictedAccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxUnrestrictedAccActionPerformed
    boolean sel = checkBoxUnrestrictedAcc.isSelected();
    labelSigning.setEnabled(sel);
    labelSigningMessage.setEnabled(sel);
    buttonSigning.setEnabled(sel);
    jfxProps.setSigningEnabled(sel);
    jfxProps.setPermissionsElevated(sel);
    if(jfxProps.getSigningEnabled() && jfxProps.getSigningType() == JFXProjectProperties.SigningType.NOSIGN) {
        jfxProps.setSigningType(JFXProjectProperties.SigningType.SELF);
    }
    refreshSigningLabel();
}//GEN-LAST:event_checkBoxUnrestrictedAccActionPerformed

private void buttonDownloadModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDownloadModeActionPerformed
    final JFXDownloadModePanel rc = new JFXDownloadModePanel(
            jfxProps.getRuntimeCP(),
            jfxProps.getLazyJars());
    final DialogDescriptor dd = new DialogDescriptor(rc,
            NbBundle.getMessage(JFXDeploymentPanel.class, "TXT_ManageResources"), // NOI18N
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            null);
    if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
        jfxProps.setLazyJars(rc.getResources());
        jfxProps.setLazyJarsChanged(true);
        refreshDownloadModeControls();
    }
}//GEN-LAST:event_buttonDownloadModeActionPerformed

    private void refreshDownloadModeControls() {
        if(jfxProps.getRuntimeCP().size() > jfxProps.getLazyJars().size()) {
            if(jfxProps.getLazyJars().isEmpty()) {
                labelDownloadModeMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_DownloadModeEager")); // NOI18N
            } else {
                labelDownloadModeMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_DownloadModeMixed")); // NOI18N
            }
        } else {
            labelDownloadModeMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_DownloadModeLazy")); // NOI18N
        }
    }

private void buttonCustomJSMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCustomJSMessageActionPerformed
    final JFXJavaScriptCallbacksPanel rc = new JFXJavaScriptCallbacksPanel(jfxProps);
    final DialogDescriptor dd = new DialogDescriptor(rc,
            NbBundle.getMessage(JFXDeploymentPanel.class, "TXT_JSCallbacks"), // NOI18N
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            null);
    if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
        jfxProps.setJSCallbacks(rc.getResources());
        jfxProps.setJSCallbacksChanged(true);
        refreshCustomJSLabel();
    }
}//GEN-LAST:event_buttonCustomJSMessageActionPerformed

    private void checkBoxBundleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxBundleActionPerformed
        boolean sel = checkBoxBundle.isSelected();
        comboBoxBundle.setEnabled(sel);
        jfxProps.setNativeBundlingEnabled(sel);
        if(jfxProps.getNativeBundlingEnabled() && jfxProps.getNativeBundlingType() == JFXProjectProperties.BundlingType.NONE) {
            jfxProps.setNativeBundlingType(JFXProjectProperties.BundlingType.ALL);
            comboBoxBundle.setSelectedItem(JFXProjectProperties.BundlingType.ALL.getString());
        }
    }//GEN-LAST:event_checkBoxBundleActionPerformed

    private void comboBoxBundleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxBundleActionPerformed
        if(!comboBoxNativeBundlingActionRunning) {
            comboBoxNativeBundlingActionRunning = true;
            String sel = (String)comboBoxBundle.getSelectedItem();
            jfxProps.setNativeBundlingType(sel);
            comboBoxNativeBundlingActionRunning = false;
        }
    }//GEN-LAST:event_comboBoxBundleActionPerformed

    private void refreshCustomJSLabel() {
        int jsDefs = 0;
        for (Map.Entry<String,String> entry : jfxProps.getJSCallbacks().entrySet()) {
            if(entry.getValue() != null && !entry.getValue().isEmpty()) {
                jsDefs++;
            }
        }
        if(jsDefs == 0) {
            labelCustomJSMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_CallbacksDefinedNone")); // NOI18N
        } else {
            labelCustomJSMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_CallbacksDefined", jsDefs)); // NOI18N
        }
    }

    private void refreshSigningLabel() {
        if(!jfxProps.getSigningEnabled() || jfxProps.getSigningType() == JFXProjectProperties.SigningType.NOSIGN) {
            labelSigningMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_SigningUnsigned")); // NOI18N
        } else {
            if(jfxProps.getSigningType() == JFXProjectProperties.SigningType.KEY) {
                labelSigningMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_SigningKey", jfxProps.getSigningKeyAlias())); // NOI18N
            } else {
                labelSigningMessage.setText(NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_SigningGenerated")); // NOI18N
            }
        }
    }

    private void setupNativeBundlingCombo() {
        comboBoxNativeBundlingActionRunning = true;
        comboBoxBundle.removeAllItems ();
        for (BundlingType bundleType : BundlingType.values()) {
            if(bundleType != BundlingType.NONE) {
                comboBoxBundle.addItem(bundleType.getString());
            }
        }
        BundlingType bundleType = jfxProps.getNativeBundlingType();
        boolean sel = jfxProps.getNativeBundlingEnabled();
        comboBoxBundle.setSelectedItem(bundleType.getString());
        comboBoxBundle.setEnabled(sel && bundleType != BundlingType.NONE);
        checkBoxBundle.setSelected(sel && bundleType != BundlingType.NONE);
        comboBoxNativeBundlingActionRunning = false;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCustomJSMessage;
    private javax.swing.JButton buttonDownloadMode;
    private javax.swing.JButton buttonIcon;
    private javax.swing.JButton buttonSigning;
    private javax.swing.JCheckBox checkBoxBundle;
    private javax.swing.JCheckBox checkBoxDeskShortcut;
    private javax.swing.JCheckBox checkBoxInstallPerm;
    private javax.swing.JCheckBox checkBoxMenuShortcut;
    private javax.swing.JCheckBox checkBoxNoInternet;
    private javax.swing.JCheckBox checkBoxUnrestrictedAcc;
    private javax.swing.JCheckBox checkBoxUpgradeBackground;
    private javax.swing.JComboBox comboBoxBundle;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel labelCustomJS;
    private javax.swing.JLabel labelCustomJSMessage;
    private javax.swing.JLabel labelDownloadMode;
    private javax.swing.JLabel labelDownloadModeMessage;
    private javax.swing.JLabel labelIcon;
    private javax.swing.JLabel labelIconRemark;
    private javax.swing.JLabel labelInitialRemark;
    private javax.swing.JLabel labelInitialRemarkSwing;
    private javax.swing.JLabel labelProperties;
    private javax.swing.JLabel labelPropertiesSwing;
    private javax.swing.JLabel labelSigning;
    private javax.swing.JLabel labelSigningMessage;
    private javax.swing.JPanel panelBottom;
    private javax.swing.JPanel panelTop;
    private javax.swing.JTextField textFieldIcon;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JFXDeploymentPanel.class.getName());
    }

    private static class IconFileFilter extends FileFilter {

        // XXX should check size of images?
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            int index = name.lastIndexOf('.');
            if (index > 0 && index < name.length() - 1) {
                String ext = name.substring(index+1).toLowerCase();
                if ("gif".equals(ext) || "png".equals(ext) || "jpg".equals(ext)) { // NOI18N
                    return true;
                }
            }
            return false;
        }

        public String getDescription() {
            return NbBundle.getMessage(JFXDeploymentPanel.class, "MSG_IconFileFilter_Description"); // NOI18N
        }

    }
}
