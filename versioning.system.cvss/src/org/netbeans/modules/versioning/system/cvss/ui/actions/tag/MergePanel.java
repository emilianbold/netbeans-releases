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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.BranchSelector;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.openide.util.NbBundle;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;

/**
 * Merge from Branch settings panel.
 *
 * @author Maros Sandor
 */
class MergePanel extends javax.swing.JPanel implements DocumentListener {
    
    private final File[] roots;
    
    /** Creates new form MergePanel */
    public MergePanel(File [] roots) {
        this.roots = roots;
        initComponents();
        rbFromBranchRoot.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean("MergeBranchSettings.mergeFromBranchRoot", true)); // NOI18N
        rbFromTag.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean("MergeBranchSettings.mergeFromTag", false)); // NOI18N
        rbToHEAD.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean("MergeBranchSettings.mergeToHEAD", true)); // NOI18N
        rbToBranchHead.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean("MergeBranchSettings.mergeToBranchHead", false)); // NOI18N
        rbToTag.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean("MergeBranchSettings.mergeToTag", false)); // NOI18N
        tfEndTag.setText(CvsModuleConfig.getDefault().getPreferences().get("MergeBranchSettings.mergeToTagName", "")); // NOI18N
        
        cbTagAfterMerge.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean("MergeBranchSettings.tagAfterMerge", false)); // NOI18N
        tfAfterMergeTagName.setText(CvsModuleConfig.getDefault().getPreferences().get("MergeBranchSettings.afterMergeTagName", NbBundle.getMessage(MergePanel.class, "BK1003"))); // NOI18N
        
        tfBranch.setText(CvsModuleConfig.getDefault().getPreferences().get("MergeBranchSettings.branchName", NbBundle.getMessage(MergePanel.class, "BK1001"))); // NOI18N
        tfBranch.getDocument().addDocumentListener(this);
        tfStartTag.setText(CvsModuleConfig.getDefault().getPreferences().get("MergeBranchSettings.mergeStartTag", NbBundle.getMessage(MergePanel.class, "BK1002"))); // NOI18N

        tfCurrentBranch.setText(detectCurrentWorkingBranch());
        refreshComponents();
        tfAfterMergeTagName.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                onTagNameChange(tfAfterMergeTagName.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                onTagNameChange(tfAfterMergeTagName.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                onTagNameChange(tfAfterMergeTagName.getText());
            }
        });
        cbTagAfterMerge.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onTagNameChange(tfAfterMergeTagName.getText());
            }
        });
    }
    
    public void saveSettings() {
        CvsModuleConfig.getDefault().getPreferences().putBoolean("MergeBranchSettings.mergeFromBranchRoot", rbFromBranchRoot.isSelected()); // NOI18N
        CvsModuleConfig.getDefault().getPreferences().putBoolean("MergeBranchSettings.mergeFromTag", rbFromTag.isSelected()); // NOI18N
        CvsModuleConfig.getDefault().getPreferences().putBoolean("MergeBranchSettings.mergeToHEAD", rbToHEAD.isSelected()); // NOI18N
        CvsModuleConfig.getDefault().getPreferences().putBoolean("MergeBranchSettings.mergeToBranchHead", rbToBranchHead.isSelected()); // NOI18N
        CvsModuleConfig.getDefault().getPreferences().putBoolean("MergeBranchSettings.mergeToTag", rbToTag.isSelected()); // NOI18N
        CvsModuleConfig.getDefault().getPreferences().put("MergeBranchSettings.mergeToTagName", tfEndTag.getText()); // NOI18N
        
        CvsModuleConfig.getDefault().getPreferences().putBoolean("MergeBranchSettings.tagAfterMerge", cbTagAfterMerge.isSelected()); // NOI18N
        CvsModuleConfig.getDefault().getPreferences().put("MergeBranchSettings.afterMergeTagName", tfAfterMergeTagName.getText()); // NOI18N
        
        CvsModuleConfig.getDefault().getPreferences().put("MergeBranchSettings.branchName", tfBranch.getText()); // NOI18N
        CvsModuleConfig.getDefault().getPreferences().put("MergeBranchSettings.mergeStartTag", tfStartTag.getText()); // NOI18N
    }
    
    private void refreshComponents() {
        tfStartTag.setEnabled(rbFromTag.isSelected());
        browseStartTag.setEnabled(rbFromTag.isSelected());
        tfBranch.setEnabled(rbToBranchHead.isSelected());
        browseBranch.setEnabled(rbToBranchHead.isSelected());
        tfEndTag.setEnabled(rbToTag.isSelected());
        browseEndTag.setEnabled(rbToTag.isSelected());
        cbTagAfterMerge.setEnabled(!rbToTag.isSelected());
        tfAfterMergeTagName.setEnabled(cbTagAfterMerge.isSelected());
        browseAfterMergeTag.setEnabled(cbTagAfterMerge.isSelected());
        String format = isMergingFromTrunk() ?
                java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("CTL_MergeBranchForm_TagAfterMerge_Trunk") :
                java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("CTL_MergeBranchForm_TagAfterMerge_Branch");
        cbTagAfterMerge.setText(MessageFormat.format(format, tfBranch.getText()));
    }
    
    public void changedUpdate(DocumentEvent e) {
        refreshComponents();
    }

    public void insertUpdate(DocumentEvent e) {
        refreshComponents();
    }

    public void removeUpdate(DocumentEvent e) {
        refreshComponents();
    }
    
    public boolean isMergingFromTrunk() {
        return rbToHEAD.isSelected();
    }
    
    public String getBranchName() {
        return tfBranch.getText();
    }
    
    private void onTagNameChange(String text) {
        JButton dd = (JButton) getClientProperty("OKButton");  // NOI18N
        if (dd != null) dd.setEnabled(!cbTagAfterMerge.isSelected() || Utils.isTagValid(text));
    }
    
    private String browseBranches() {
        for (int i = 0; i < roots.length; i++) {
            try {
                CVSRoot.parse(Utils.getCVSRootFor(roots[i]));  // raises exception
                BranchSelector selector = new BranchSelector();
                return selector.selectTag(roots[i]);
            } catch (IOException e) {
                // no root for this file, try next
            }
        }
        return null;
    }
    
    /**
     * Takes the first file and returns its sticky tag.
     * 
     * @return branch of current files
     */ 
    private String detectCurrentWorkingBranch() {
        File root = roots[0];
        if (root.isFile()) {
            Entry entry = null;
            try {
                entry = CvsVersioningSystem.getInstance().getAdminHandler().getEntry(root);
            } catch (IOException e) {
                // no entry, ignore
            }
            if (entry != null) {
                String sticky = entry.getStickyInformation();
                if (sticky != null) {
                    return sticky;
                }
            } else {
                root = root.getParentFile();
            }
        }
        if (root.isDirectory()) {
            String sticky = CvsVersioningSystem.getInstance().getAdminHandler().getStickyTagForDirectory(root);
            if (sticky != null  && sticky.startsWith("T")) {   // NOI18N
                return sticky.substring(1);
            }
        }
        return NbBundle.getBundle(MergePanel.class).getString("MSG_MergeBranchForm_Trunk");
    }
    
    public boolean isTaggingAfterMerge() {
        return cbTagAfterMerge.isSelected() && !rbToTag.isSelected();
    }

    public String getAfterMergeTagName() {
        return tfAfterMergeTagName.getText();
    }
    
    public boolean isMergingFromBranch() {
        return rbToBranchHead.isSelected();
    }
    
    public String getEndTagName() {
        return tfEndTag.getText();
    }    
 
    public boolean isUsingMergeTag() {
        return rbFromTag.isSelected();
    }

    public String getMergeTagName() {
        return tfStartTag.getText();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        tfCurrentBranch = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        rbFromBranchRoot = new javax.swing.JRadioButton();
        rbFromTag = new javax.swing.JRadioButton();
        tfStartTag = new javax.swing.JTextField();
        browseStartTag = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        rbToHEAD = new javax.swing.JRadioButton();
        rbToBranchHead = new javax.swing.JRadioButton();
        rbToTag = new javax.swing.JRadioButton();
        tfEndTag = new javax.swing.JTextField();
        browseEndTag = new javax.swing.JButton();
        browseBranch = new javax.swing.JButton();
        tfBranch = new javax.swing.JTextField();
        cbTagAfterMerge = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        tfAfterMergeTagName = new javax.swing.JTextField();
        browseAfterMergeTag = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.jLabel1.text")); // NOI18N

        tfCurrentBranch.setEditable(false);
        tfCurrentBranch.setText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.tfCurrentBranch.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.jLabel2.text")); // NOI18N

        buttonGroup1.add(rbFromBranchRoot);
        org.openide.awt.Mnemonics.setLocalizedText(rbFromBranchRoot, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.rbFromBranchRoot.text")); // NOI18N
        rbFromBranchRoot.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbFromBranchRoot.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbFromBranchRoot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbFromBranchRootActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbFromTag);
        org.openide.awt.Mnemonics.setLocalizedText(rbFromTag, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.rbFromTag.text")); // NOI18N
        rbFromTag.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbFromTag.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbFromTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbFromTagActionPerformed(evt);
            }
        });

        tfStartTag.setText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.tfStartTag.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseStartTag, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.browseStartTag.text")); // NOI18N
        browseStartTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseStartTagActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.jLabel3.text")); // NOI18N

        buttonGroup2.add(rbToHEAD);
        org.openide.awt.Mnemonics.setLocalizedText(rbToHEAD, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.rbToHEAD.text")); // NOI18N
        rbToHEAD.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbToHEAD.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbToHEAD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbToHEADActionPerformed(evt);
            }
        });

        buttonGroup2.add(rbToBranchHead);
        org.openide.awt.Mnemonics.setLocalizedText(rbToBranchHead, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.rbToBranchHead.text")); // NOI18N
        rbToBranchHead.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbToBranchHead.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbToBranchHead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbToBranchHeadActionPerformed(evt);
            }
        });

        buttonGroup2.add(rbToTag);
        org.openide.awt.Mnemonics.setLocalizedText(rbToTag, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.rbToTag.text")); // NOI18N
        rbToTag.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbToTag.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbToTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbToTagActionPerformed(evt);
            }
        });

        tfEndTag.setText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.tfEndTag.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseEndTag, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.browseEndTag.text")); // NOI18N
        browseEndTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseEndTagActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browseBranch, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.browseBranch.text")); // NOI18N
        browseBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseBranchActionPerformed(evt);
            }
        });

        tfBranch.setText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.tfBranch.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbTagAfterMerge, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.cbTagAfterMerge.text")); // NOI18N
        cbTagAfterMerge.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbTagAfterMerge.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbTagAfterMerge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTagAfterMergeActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.jLabel4.text")); // NOI18N

        tfAfterMergeTagName.setText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.tfAfterMergeTagName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseAfterMergeTag, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.browseAfterMergeTag.text")); // NOI18N
        browseAfterMergeTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseAfterMergeTagActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(tfAfterMergeTagName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseAfterMergeTag)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(rbFromTag)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                              .add(tfStartTag, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
                                    .add(rbFromBranchRoot))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseStartTag)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                  .addContainerGap(536, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rbToHEAD)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(rbToTag)
                                    .add(rbToBranchHead))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                           .add(org.jdesktop.layout.GroupLayout.TRAILING, tfEndTag, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                           .add(org.jdesktop.layout.GroupLayout.TRAILING, tfBranch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(browseBranch)
                                    .add(browseEndTag))
                                .addContainerGap())))
                    .add(layout.createSequentialGroup()
                        .add(cbTagAfterMerge)
                  .addContainerGap(335, Short.MAX_VALUE))
               .add(layout.createSequentialGroup()
                  .add(jLabel1)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(tfCurrentBranch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                  .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(tfCurrentBranch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbFromBranchRoot)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rbFromTag)
                    .add(tfStartTag, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseStartTag))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbToHEAD)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rbToBranchHead)
                    .add(tfBranch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseBranch))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rbToTag)
                    .add(tfEndTag, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseEndTag))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbTagAfterMerge)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(tfAfterMergeTagName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseAfterMergeTag))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbTagAfterMergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTagAfterMergeActionPerformed
        refreshComponents();    
    }//GEN-LAST:event_cbTagAfterMergeActionPerformed

    private void rbToTagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbToTagActionPerformed
        refreshComponents();    
    }//GEN-LAST:event_rbToTagActionPerformed

    private void rbToBranchHeadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbToBranchHeadActionPerformed
        refreshComponents();    
    }//GEN-LAST:event_rbToBranchHeadActionPerformed

    private void rbToHEADActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbToHEADActionPerformed
        refreshComponents();    
    }//GEN-LAST:event_rbToHEADActionPerformed

    private void rbFromBranchRootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFromBranchRootActionPerformed
        refreshComponents();    
    }//GEN-LAST:event_rbFromBranchRootActionPerformed

    private void rbFromTagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFromTagActionPerformed
        refreshComponents();    
    }//GEN-LAST:event_rbFromTagActionPerformed

    private void browseAfterMergeTagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseAfterMergeTagActionPerformed
        String tag = browseBranches();
        if (tag != null) {
            tfAfterMergeTagName.setText(tag);
        }
    }//GEN-LAST:event_browseAfterMergeTagActionPerformed

    private void browseEndTagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseEndTagActionPerformed
        String tag = browseBranches();
        if (tag != null) {
            tfEndTag.setText(tag);
        }
    }//GEN-LAST:event_browseEndTagActionPerformed

    private void browseBranchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseBranchActionPerformed
        String tag = browseBranches();
        if (tag != null) {
            tfBranch.setText(tag);
        }
    }//GEN-LAST:event_browseBranchActionPerformed

    private void browseStartTagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseStartTagActionPerformed
        String tag = browseBranches();
        if (tag != null) {
            tfStartTag.setText(tag);
        }
    }//GEN-LAST:event_browseStartTagActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseAfterMergeTag;
    private javax.swing.JButton browseBranch;
    private javax.swing.JButton browseEndTag;
    private javax.swing.JButton browseStartTag;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JCheckBox cbTagAfterMerge;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JRadioButton rbFromBranchRoot;
    private javax.swing.JRadioButton rbFromTag;
    private javax.swing.JRadioButton rbToBranchHead;
    private javax.swing.JRadioButton rbToHEAD;
    private javax.swing.JRadioButton rbToTag;
    private javax.swing.JTextField tfAfterMergeTagName;
    private javax.swing.JTextField tfBranch;
    private javax.swing.JTextField tfCurrentBranch;
    private javax.swing.JTextField tfEndTag;
    private javax.swing.JTextField tfStartTag;
    // End of variables declaration//GEN-END:variables

}
