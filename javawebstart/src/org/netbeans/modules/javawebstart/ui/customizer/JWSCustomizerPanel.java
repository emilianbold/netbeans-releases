/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.javawebstart.ui.customizer;

import java.awt.Dialog;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import javax.swing.table.TableModel;
import org.netbeans.modules.javawebstart.ui.customizer.JWSProjectProperties.PropertiesTableModel;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.javawebstart.ui.customizer.JWSProjectProperties.CodebaseComboBoxModel;
import org.netbeans.modules.javawebstart.CustomizerRunComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author  Milan Kubec
 */
public class JWSCustomizerPanel extends JPanel implements HelpCtx.Provider {

    private JWSProjectProperties jwsProps;
    private File lastImageFolder = null;

    private static String extResColumnNames[];
    private static String appletParamsColumnNames[];

    public static CustomizerRunComponent runComponent;
    static {
        runComponent = new CustomizerRunComponent();
    }

    /** Creates new form JWSCustomizerPanel */
    public JWSCustomizerPanel(JWSProjectProperties props) {

        this.jwsProps = props;

        initComponents();

        enableCheckBox.setModel(jwsProps.enabledModel);
        enableCheckBox.setMnemonic(NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.enableCheckBox.mnemonic").toCharArray()[0]);
        offlineCheckBox.setModel(jwsProps.allowOfflineModel);
        offlineCheckBox.setMnemonic(NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.offlineCheckBox.mnemonic").toCharArray()[0]);
        signedCheckBox.setModel(jwsProps.signedModel);
        signedCheckBox.setMnemonic(NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.signedCheckBox.mnemonic").toCharArray()[0]);
        iconTextField.setDocument(jwsProps.iconDocument);
        codebaseComboBox.setModel(jwsProps.codebaseModel);
        codebaseTextField.setDocument(jwsProps.codebaseURLDocument);
        appletClassComboBox.setModel(jwsProps.appletClassModel);
        applicationDescRadioButton.setModel(jwsProps.applicationDescButtonModel);
        applicationDescRadioButton.setMnemonic(NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.applicationDescRadioButton.mnemonic").toCharArray()[0]);
        appletDescRadioButton.setModel(jwsProps.appletDescButtonModel);
        appletDescRadioButton.setMnemonic(NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.appletDescRadioButton.mnemonic").toCharArray()[0]);
        compDescRadioButton.setModel(jwsProps.compDescButtonModel);
        compDescRadioButton.setMnemonic(NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.compDescRadioButton.mnemonic").toCharArray()[0]);

        setCodebaseComponents();
        boolean enableSelected = enableCheckBox.getModel().isSelected();
        setEnabledAllComponents(enableSelected);
        setEnabledRunComponent(enableSelected);

        setEnabledAppletControls(appletDescRadioButton.isSelected());

        if (jwsProps.isJnlpImplPreviousVersion) {
            warningArea.setVisible(true);
            extResButton.setEnabled(false);
            appletDescRadioButton.setEnabled(false);
            appletClassLabel.setEnabled(false);
            appletClassComboBox.setEnabled(false);
            appletParamsButton.setEnabled(false);
            compDescRadioButton.setEnabled(false);
        } else {
            warningArea.setVisible(false);
        }

        extResColumnNames = new String[] {
            NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.extResources.href"),
            NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.extResources.name"),
            NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.extResources.version")
        };
        appletParamsColumnNames = new String[] {
            NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.appletParams.name"),
            NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.appletParams.value")
        };

        signedChanged(null);
    }

    private static void setEnabledRunComponent(boolean enable) {
        runComponent.setCheckboxEnabled(enable);
        runComponent.setHintVisible(!enable);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        descButtonGroup = new javax.swing.ButtonGroup();
        enableCheckBox = new javax.swing.JCheckBox();
        iconLabel = new javax.swing.JLabel();
        codebaseLabel = new javax.swing.JLabel();
        iconTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        codebaseComboBox = new javax.swing.JComboBox();
        codebaseValueLabel = new javax.swing.JLabel();
        codebaseTextField = new javax.swing.JTextField();
        offlineCheckBox = new javax.swing.JCheckBox();
        panelDescLabel = new javax.swing.JLabel();
        signedCheckBox = new javax.swing.JCheckBox();
        extResButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        applicationDescRadioButton = new javax.swing.JRadioButton();
        appletClassLabel = new javax.swing.JLabel();
        compDescRadioButton = new javax.swing.JRadioButton();
        appletDescRadioButton = new javax.swing.JRadioButton();
        appletClassComboBox = new javax.swing.JComboBox();
        appletParamsButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        warningArea = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        mixedCodeLabel = new javax.swing.JLabel();
        mixedCodeCombo = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(enableCheckBox, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.enableCheckBox.text")); // NOI18N
        enableCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 2));
        enableCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(enableCheckBox, gridBagConstraints);
        enableCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_EnableWebStart_CheckBox")); // NOI18N
        enableCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_EnableWebStart_Label")); // NOI18N

        iconLabel.setLabelFor(iconTextField);
        org.openide.awt.Mnemonics.setLocalizedText(iconLabel, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.iconLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(iconLabel, gridBagConstraints);
        iconLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Icon_Label")); // NOI18N
        iconLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Icon_Label")); // NOI18N

        codebaseLabel.setLabelFor(codebaseComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(codebaseLabel, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.codebaseLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(codebaseLabel, gridBagConstraints);
        codebaseLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Codebase_Label")); // NOI18N
        codebaseLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Codebase_Label")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(iconTextField, gridBagConstraints);
        iconTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Icon_TextField")); // NOI18N
        iconTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Icon_TextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Browse_Button")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Browse_Button")); // NOI18N

        codebaseComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codebaseComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(codebaseComboBox, gridBagConstraints);
        codebaseComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Codebase_Combobox")); // NOI18N
        codebaseComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Codebase_Combobox")); // NOI18N

        codebaseValueLabel.setLabelFor(codebaseTextField);
        org.openide.awt.Mnemonics.setLocalizedText(codebaseValueLabel, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.codebaseValueLabel.text" )); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(codebaseValueLabel, gridBagConstraints);
        codebaseValueLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Codebase_Result_Label" )); // NOI18N
        codebaseValueLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Codebase_Result_Label" )); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(codebaseTextField, gridBagConstraints);
        codebaseTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Codebase_TextField")); // NOI18N
        codebaseTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Codebase_TextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(offlineCheckBox, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.offlineCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(offlineCheckBox, gridBagConstraints);
        offlineCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_AllowOffline_Checkbox")); // NOI18N
        offlineCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_AllowOffline_Checkbox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(panelDescLabel, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.panelDescLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(panelDescLabel, gridBagConstraints);
        panelDescLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_WebStartTitle_Label")); // NOI18N
        panelDescLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_WebStartTitle_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(signedCheckBox, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.signedCheckBox.text")); // NOI18N
        signedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signedChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(signedCheckBox, gridBagConstraints);
        signedCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_SelfSigned_Checkbox")); // NOI18N
        signedCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_SelfSigned_Checkbox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(extResButton, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.extResButton.text")); // NOI18N
        extResButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extResButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(extResButton, gridBagConstraints);
        extResButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.extResButton.AccessibleContext.accessibleDescription")); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        descButtonGroup.add(applicationDescRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(applicationDescRadioButton, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.applicationDescRadioButton.text")); // NOI18N
        applicationDescRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applicationDescRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(applicationDescRadioButton, gridBagConstraints);
        applicationDescRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.applicationDescRadioButton.AccessibleContext.accessibleDescription")); // NOI18N

        appletClassLabel.setLabelFor(appletClassComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(appletClassLabel, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.appletClassLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 34, 0, 4);
        jPanel1.add(appletClassLabel, gridBagConstraints);
        appletClassLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.appletClassLabel.AccessibleContext.accessibleDescription")); // NOI18N

        descButtonGroup.add(compDescRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(compDescRadioButton, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.compDescRadioButton.text")); // NOI18N
        compDescRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compDescRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(compDescRadioButton, gridBagConstraints);
        compDescRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.compDescRadioButton.AccessibleContext.accessibleDescription")); // NOI18N

        descButtonGroup.add(appletDescRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(appletDescRadioButton, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.appletDescRadioButton.text")); // NOI18N
        appletDescRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appletDescRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(appletDescRadioButton, gridBagConstraints);
        appletDescRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.appletDescRadioButton.AccessibleContext.accessibleDescription")); // NOI18N

        appletClassComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "org.testapplication.TestApplet", "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(appletClassComboBox, gridBagConstraints);
        appletClassComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.appletClassComboBox.AccessibleContext.accessibleName")); // NOI18N
        appletClassComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.appletClassComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(appletParamsButton, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.appletParamsButton.text")); // NOI18N
        appletParamsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appletParamsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        jPanel1.add(appletParamsButton, gridBagConstraints);
        appletParamsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.appletParamsButton.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(jSeparator1, gridBagConstraints);

        warningArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        warningArea.setColumns(20);
        warningArea.setEditable(false);
        warningArea.setLineWrap(true);
        warningArea.setRows(2);
        warningArea.setText(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "Previous_Version_Script_Warning")); // NOI18N
        warningArea.setWrapStyleWord(true);
        warningArea.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 0);
        add(warningArea, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        mixedCodeLabel.setLabelFor(mixedCodeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(mixedCodeLabel, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "TXT_MixedCodeLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(mixedCodeLabel, gridBagConstraints);

        mixedCodeCombo.setModel(jwsProps.mixedCodeModel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(mixedCodeCombo, gridBagConstraints);
        mixedCodeCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "AN_MixedCodeLabel")); // NOI18N
        mixedCodeCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "AD_MixedCodeLabel")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jPanel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        add(jPanel2, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void codebaseComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codebaseComboBoxActionPerformed
        setCodebaseComponents();
    }//GEN-LAST:event_codebaseComboBoxActionPerformed

    private void enableCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableCheckBoxActionPerformed
        boolean isSelected = enableCheckBox.getModel().isSelected();
        setEnabledAllComponents(isSelected);
        setEnabledRunComponent(isSelected);
    }//GEN-LAST:event_enableCheckBoxActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new IconFileFilter());
        if (lastImageFolder != null) {
            chooser.setSelectedFile(lastImageFolder);
        } else { // ???
            // workDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
            // chooser.setSelectedFile(new File(workDir));
        }
        chooser.setDialogTitle(NbBundle.getMessage(JWSCustomizerPanel.class, "LBL_Select_Icon_Image"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            iconTextField.setText(file.getAbsolutePath());
            lastImageFolder = file.getParentFile();
        }
    }//GEN-LAST:event_browseButtonActionPerformed

private void applicationDescRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applicationDescRadioButtonActionPerformed
    setEnabledAppletControls(false);
    jwsProps.updateDescType();
}//GEN-LAST:event_applicationDescRadioButtonActionPerformed

private void extResButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extResButtonActionPerformed

    List<Map<String,String>> origProps = jwsProps.getExtResProperties();
    List<Map<String,String>> props = copyList(origProps);
    JPanel panel = new ExtensionResourcesPanel(new JWSProjectProperties.PropertiesTableModel(props, JWSProjectProperties.extResSuffixes, extResColumnNames));
    DialogDescriptor dialogDesc = new DialogDescriptor(panel, NbBundle.getMessage(JWSCustomizerPanel.class, "TITLE_ExtensionResources"), true, null);
    Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
    dialog.setVisible(true);
    if (dialogDesc.getValue() == DialogDescriptor.OK_OPTION) {
        jwsProps.setExtResProperties(props);
    }
    dialog.dispose();

}//GEN-LAST:event_extResButtonActionPerformed

private void appletDescRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appletDescRadioButtonActionPerformed
    setEnabledAppletControls(true);
    jwsProps.updateDescType();
}//GEN-LAST:event_appletDescRadioButtonActionPerformed

private void compDescRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compDescRadioButtonActionPerformed
    setEnabledAppletControls(false);
    jwsProps.updateDescType();
}//GEN-LAST:event_compDescRadioButtonActionPerformed

private void appletParamsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appletParamsButtonActionPerformed

    List<Map<String,String>> origProps = jwsProps.getAppletParamsProperties();
    List<Map<String,String>> props = copyList(origProps);
    TableModel appletParamsTableModel = new JWSProjectProperties.PropertiesTableModel(props, JWSProjectProperties.appletParamsSuffixes, appletParamsColumnNames);
    JPanel panel = new AppletParametersPanel((PropertiesTableModel) appletParamsTableModel, jwsProps.appletWidthDocument, jwsProps.appletHeightDocument);
    DialogDescriptor dialogDesc = new DialogDescriptor(panel, NbBundle.getMessage(JWSCustomizerPanel.class, "TITLE_AppletParameters"), true, null);
    Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
    dialog.setVisible(true);
    if (dialogDesc.getValue() == DialogDescriptor.OK_OPTION) {
        jwsProps.setAppletParamsProperties(props);
    }
    dialog.dispose();

}//GEN-LAST:event_appletParamsButtonActionPerformed

private void signedChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signedChanged
    final boolean enabled = signedCheckBox.isSelected();
    mixedCodeLabel.setEnabled(enabled);
    mixedCodeCombo.setEnabled(enabled);
}//GEN-LAST:event_signedChanged

    private void setEnabledAppletControls(boolean b) {
        appletClassLabel.setEnabled(b);
        appletClassComboBox.setEnabled(b);
        appletParamsButton.setEnabled(b);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(JWSCustomizerPanel.class);
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
            return NbBundle.getMessage(JWSCustomizerPanel.class, "MSG_IconFileFilter_Description");
        }

    }

    private CodebaseComboBoxModel getCBModel() {
        return (CodebaseComboBoxModel) codebaseComboBox.getModel();
    }

    private void setCodebaseComponents() {
        String value = getCBModel().getSelectedCodebaseItem();
        if (JWSProjectProperties.CB_TYPE_LOCAL.equals(value)) {
            codebaseTextField.setText(jwsProps.getProjectDistDir());
            codebaseTextField.setEditable(false);
        } else if (JWSProjectProperties.CB_TYPE_WEB.equals(value)) {
            codebaseTextField.setText(JWSProjectProperties.CB_URL_WEB);
            codebaseTextField.setEditable(false);
        } else if (JWSProjectProperties.CB_TYPE_USER.equals(value)) {
            codebaseTextField.setText(jwsProps.getCodebaseLocation());
            codebaseTextField.setEditable(true);
        } else if (JWSProjectProperties.CB_NO_CODEBASE.equals(value)) {
            codebaseTextField.setText("");  //NOI18N
            codebaseTextField.setEditable(false);
        }
    }

    private void setEnabledAllComponents(boolean b) {
        iconLabel.setEnabled(b);
        iconTextField.setEnabled(b);
        browseButton.setEnabled(b);
        codebaseLabel.setEnabled(b);
        codebaseComboBox.setEnabled(b);
        codebaseValueLabel.setEnabled(b);
        codebaseTextField.setEnabled(b);
        offlineCheckBox.setEnabled(b);
        signedCheckBox.setEnabled(b);
        extResButton.setEnabled(b);
        applicationDescRadioButton.setEnabled(b);
        appletDescRadioButton.setEnabled(b);
        compDescRadioButton.setEnabled(b);
        if (!b || (b && appletDescRadioButton.isSelected())) {
            setEnabledAppletControls(b);
        }
    }

    private List<Map<String,String>> copyList(List<Map<String,String>> list2Copy) {
        List<Map<String,String>> list2Return = new ArrayList<Map<String,String>>();
        for (Map<String,String> map : list2Copy) {
            Map<String,String> newMap = new HashMap<String,String>();
            for(String key : map.keySet()) {
                String value = map.get(key);
                newMap.put(key, value);
            }
            list2Return.add(newMap);
        }
        return list2Return;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox appletClassComboBox;
    private javax.swing.JLabel appletClassLabel;
    private javax.swing.JRadioButton appletDescRadioButton;
    private javax.swing.JButton appletParamsButton;
    private javax.swing.JRadioButton applicationDescRadioButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox codebaseComboBox;
    private javax.swing.JLabel codebaseLabel;
    private javax.swing.JTextField codebaseTextField;
    private javax.swing.JLabel codebaseValueLabel;
    private javax.swing.JRadioButton compDescRadioButton;
    private javax.swing.ButtonGroup descButtonGroup;
    private javax.swing.JCheckBox enableCheckBox;
    private javax.swing.JButton extResButton;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JTextField iconTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox mixedCodeCombo;
    private javax.swing.JLabel mixedCodeLabel;
    private javax.swing.JCheckBox offlineCheckBox;
    private javax.swing.JLabel panelDescLabel;
    private javax.swing.JCheckBox signedCheckBox;
    private javax.swing.JTextArea warningArea;
    // End of variables declaration//GEN-END:variables

}
