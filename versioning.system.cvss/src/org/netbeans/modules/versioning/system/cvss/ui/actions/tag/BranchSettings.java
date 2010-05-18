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

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.BranchSelector;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.openide.DialogDescriptor;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.*;
import java.io.IOException;
import java.io.File;
import org.openide.util.*;

/**
 * Settings panel for the Branch command.
 * 
 * @author Maros Sandor
 */
class BranchSettings extends javax.swing.JPanel {
    
    private final File[] roots;
    private boolean autoComputeBaseTagName = true;

    public BranchSettings(File [] roots) {
        this.roots = roots;
        initComponents();        
        cbTagBase.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean("BranchSettings.tagBase", true)); // NOI18N
        tfBaseTagName.setText(CvsModuleConfig.getDefault().getPreferences().get("BranchSettings.tagBaseName", NbBundle.getMessage(BranchSettings.class, "BK0001")));  // NOI18N
        tfBaseTagName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                autoComputeBaseTagName = computeBaseTagName().equals(tfBaseTagName.getText());
                onBranchNameChange(tfBaseTagName.getText());
            }

            public void insertUpdate(DocumentEvent e) {
                autoComputeBaseTagName = computeBaseTagName().equals(tfBaseTagName.getText());
                onBranchNameChange(tfBaseTagName.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                autoComputeBaseTagName = computeBaseTagName().equals(tfBaseTagName.getText());
                onBranchNameChange(tfBaseTagName.getText());
            }
        });        
        
        cbCheckoutBranch.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean("BranchSettings.checkout", true)); // NOI18N
        tfName.setText(CvsModuleConfig.getDefault().getPreferences().get("BranchSettings.branchName", NbBundle.getMessage(BranchSettings.class, "BK0002"))); // NOI18N
        tfName.selectAll();
        tfName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                onBranchNameChange(tfName.getText());
                refreshComponents();
            }

            public void insertUpdate(DocumentEvent e) {
                onBranchNameChange(tfName.getText());
                refreshComponents();
            }

            public void removeUpdate(DocumentEvent e) {
                onBranchNameChange(tfName.getText());
                refreshComponents();
            }
        });
        refreshComponents();
    }

    void onBranchNameChange() {
        onBranchNameChange(tfName.getText());
    }
    
    private void onBranchNameChange(String name) {
        JButton dd = (JButton) getClientProperty("OKButton");
        if (dd != null) dd.setEnabled(Utils.isTagValid(name));
    }
    
    public boolean isCheckingOutBranch() {
        return cbCheckoutBranch.isSelected();
    }

    public boolean isTaggingBase() {
        return cbTagBase.isSelected();
    }

    public String getBranchName() {
        return tfName.getText();
    }

    public String getBaseTagName() {
        return tfBaseTagName.getText();
    }
    
    public void saveSettings() {
        CvsModuleConfig.getDefault().getPreferences().putBoolean("BranchSettings.tagBase", cbTagBase.isSelected());  // NOI18N
        CvsModuleConfig.getDefault().getPreferences().putBoolean("BranchSettings.checkout", cbCheckoutBranch.isSelected()); // NOI18N
        CvsModuleConfig.getDefault().getPreferences().put("BranchSettings.branchName", tfName.getText()); // NOI18N
    }

    private String computeBaseTagName() {
        return NbBundle.getMessage(BranchSettings.class, "BK0003", tfName.getText()); // NOI18N
    }
    
    private void refreshComponents() {
        jLabel1.setEnabled(cbTagBase.isSelected());
        tfBaseTagName.setEnabled(cbTagBase.isSelected());
        if (autoComputeBaseTagName && cbTagBase.isSelected()) {
            tfBaseTagName.setText(computeBaseTagName());
        }
        DialogDescriptor dd = (DialogDescriptor) getClientProperty("org.openide.DialogDescriptor"); // NOI18N
        if (dd != null) {
            dd.setValid(tfName.getText().trim().length() > 0);
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

        cbTagBase = new javax.swing.JCheckBox();
        cbCheckoutBranch = new javax.swing.JCheckBox();
        nameLabel = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        tfBaseTagName = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new java.awt.GridBagLayout());

        cbTagBase.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(cbTagBase, bundle.getString("CTL_BranchForm_TagBase")); // NOI18N
        cbTagBase.setToolTipText(bundle.getString("TT_BranchForm_TagBase")); // NOI18N
        cbTagBase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTagBaseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(cbTagBase, gridBagConstraints);

        cbCheckoutBranch.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbCheckoutBranch, bundle.getString("CTL_BranchForm_UpdateToBranch")); // NOI18N
        cbCheckoutBranch.setToolTipText(bundle.getString("TT_BranchForm_UpdateToBranch")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(cbCheckoutBranch, gridBagConstraints);

        nameLabel.setLabelFor(tfName);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, bundle.getString("CTL_BranchForm_BranchName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(nameLabel, gridBagConstraints);

        tfName.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(tfName, gridBagConstraints);
        tfName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BranchSettings.class, "ACSN_BranchForm_Name")); // NOI18N
        tfName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BranchSettings.class, "ACSD_BranchForm_Name")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, bundle.getString("CTL_BranchForm_BrowseBranch")); // NOI18N
        jButton1.setToolTipText(bundle.getString("TT_BranchForm_Browse")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseBranches(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(jButton1, gridBagConstraints);

        jLabel1.setLabelFor(tfBaseTagName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("CTL_BranchForm_BaseTagName")); // NOI18N
        jLabel1.setToolTipText(bundle.getString("TT_BranchForm_BaseTagName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 21, 0, 5);
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(tfBaseTagName, gridBagConstraints);
        tfBaseTagName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BranchSettings.class, "ACSN_BranchForm_TagName")); // NOI18N
        tfBaseTagName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BranchSettings.class, "ACSD_BranchForm_TagName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseBranches(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseBranches
        for (int i = 0; i < roots.length; i++) {
            try {
                CVSRoot.parse(Utils.getCVSRootFor(roots[i]));  // raises exception
                BranchSelector selector = new BranchSelector();
                String tag = selector.selectTag(roots[i]);
                if (tag != null) {
                    tfName.setText(tag);
                }
                return;
            } catch (IOException e) {
                // no root for this file, try next
            }
        }
    }//GEN-LAST:event_browseBranches

    private void cbTagBaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTagBaseActionPerformed
        refreshComponents();
    }//GEN-LAST:event_cbTagBaseActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbCheckoutBranch;
    private javax.swing.JCheckBox cbTagBase;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField tfBaseTagName;
    private javax.swing.JTextField tfName;
    // End of variables declaration//GEN-END:variables
}
