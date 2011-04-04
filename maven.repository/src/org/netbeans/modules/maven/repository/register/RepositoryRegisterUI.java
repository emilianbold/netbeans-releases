/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.repository.register;

import java.io.File;
import java.net.URISyntaxException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.openide.util.NbBundle;

/**
 *
 * @author  Anuradha
 */
public class RepositoryRegisterUI extends javax.swing.JPanel {

    private static File lastFolder = new File(System.getProperty("user.home")); //NOI18N
    private boolean modify = false;
    private boolean singleType = false;
    private boolean alreadyFilled = false;

    /** Creates new form RepositoryRegisterUI */
    public RepositoryRegisterUI() {
        initComponents();
        validateInfo();
        String[] types = RepositoryIndexer.getAvailableTypes();
        if (types.length == 1) {
            lblType.setVisible(false);
            comType.setVisible(false);
            singleType = true;
        } 
        comType.setModel(new DefaultComboBoxModel(types));
        comType.setSelectedItem(RepositoryPreferences.TYPE_NEXUS);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        btnOK = new javax.swing.JButton();
        lblHeader = new javax.swing.JLabel();
        lblRepoId = new javax.swing.JLabel();
        txtRepoId = new javax.swing.JTextField();
        lblRepoName = new javax.swing.JLabel();
        txtRepoName = new javax.swing.JTextField();
        lblType = new javax.swing.JLabel();
        comType = new javax.swing.JComboBox();
        lblRepoType = new javax.swing.JLabel();
        jraLocal = new javax.swing.JRadioButton();
        jraRemote = new javax.swing.JRadioButton();
        lblRepoPath = new javax.swing.JLabel();
        txtRepoPath = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();
        lblRepoUrl = new javax.swing.JLabel();
        txtRepoUrl = new javax.swing.JTextField();
        lblValidate = new javax.swing.JLabel();

        btnOK.setText(org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "CMB_Repo_ADD", new Object[] {})); // NOI18N
        btnOK.setEnabled(false);

        lblHeader.setText(org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_Register_Header", new Object[] {})); // NOI18N

        lblRepoId.setLabelFor(txtRepoId);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepoId, org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_ID", new Object[] {})); // NOI18N

        txtRepoId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtRepoIdKeyReleased(evt);
            }
        });

        lblRepoName.setLabelFor(txtRepoName);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepoName, org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_Name", new Object[] {})); // NOI18N

        txtRepoName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtRepoNameKeyReleased(evt);
            }
        });

        lblType.setLabelFor(comType);
        org.openide.awt.Mnemonics.setLocalizedText(lblType, org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "RepositoryRegisterUI.lblType.text")); // NOI18N

        comType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblRepoType.setText(org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_Type", new Object[] {})); // NOI18N

        buttonGroup1.add(jraLocal);
        org.openide.awt.Mnemonics.setLocalizedText(jraLocal, org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_Type_Local", new Object[] {})); // NOI18N
        jraLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jraLocalActionPerformed(evt);
            }
        });

        buttonGroup1.add(jraRemote);
        jraRemote.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jraRemote, org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_Type_Remote", new Object[] {})); // NOI18N
        jraRemote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jraRemoteActionPerformed(evt);
            }
        });

        lblRepoPath.setLabelFor(txtRepoPath);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepoPath, org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_Path", new Object[] {})); // NOI18N

        txtRepoPath.setEnabled(false);
        txtRepoPath.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtRepoPathKeyTyped(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnBrowse, org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "CMD_Repo_Path_Browse", new Object[] {})); // NOI18N
        btnBrowse.setEnabled(false);
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        lblRepoUrl.setLabelFor(txtRepoUrl);
        org.openide.awt.Mnemonics.setLocalizedText(lblRepoUrl, org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_URL", new Object[] {})); // NOI18N

        txtRepoUrl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtRepoUrlKeyReleased(evt);
            }
        });

        lblValidate.setForeground(new java.awt.Color(204, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblValidate, javax.swing.GroupLayout.DEFAULT_SIZE, 844, Short.MAX_VALUE)
                            .addComponent(lblHeader, javax.swing.GroupLayout.DEFAULT_SIZE, 844, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblRepoName)
                                    .addComponent(lblRepoId, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                                    .addComponent(lblType))
                                .addGap(14, 14, 14)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtRepoId, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
                                    .addComponent(txtRepoName, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
                                    .addComponent(comType, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblRepoType, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jraLocal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addComponent(lblRepoPath))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jraRemote, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                            .addGap(21, 21, 21)
                                            .addComponent(lblRepoUrl))))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtRepoUrl, javax.swing.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtRepoPath, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBrowse)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblHeader)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRepoId)
                    .addComponent(txtRepoId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRepoName)
                    .addComponent(txtRepoName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblType)
                    .addComponent(comType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblRepoType)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jraLocal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRepoPath)
                    .addComponent(btnBrowse)
                    .addComponent(txtRepoPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jraRemote)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRepoUrl)
                    .addComponent(txtRepoUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addComponent(lblValidate, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        txtRepoId.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "RepositoryRegisterUI.txtRepoId.AccessibleContext.accessibleDescription")); // NOI18N
        txtRepoName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "RepositoryRegisterUI.txtRepoName.AccessibleContext.accessibleDescription")); // NOI18N
        comType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "RepositoryRegisterUI.comType.AccessibleContext.accessibleDescription")); // NOI18N
        jraLocal.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "RepositoryRegisterUI.jraLocal.AccessibleContext.accessibleDescription")); // NOI18N
        jraRemote.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "RepositoryRegisterUI.jraRemote.AccessibleContext.accessibleDescription")); // NOI18N
        txtRepoPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "RepositoryRegisterUI.txtRepoPath.AccessibleContext.accessibleDescription")); // NOI18N
        btnBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "RepositoryRegisterUI.btnBrowse.AccessibleContext.accessibleDescription")); // NOI18N
        txtRepoUrl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RepositoryRegisterUI.class, "RepositoryRegisterUI.txtRepoUrl.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        JFileChooser chooser = new JFileChooser(lastFolder);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_Path_Header", new Object[] {}));

        chooser.setMultiSelectionEnabled(false);
        if (txtRepoPath.getText().trim().length() > 0) {
            File fil = new File(txtRepoPath.getText().trim());
            if (fil.exists()) {
                chooser.setSelectedFile(fil);
            }
        }
        int ret = chooser.showDialog(SwingUtilities.getWindowAncestor(this), NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_SELECT", new Object[] {}));
        if (ret == JFileChooser.APPROVE_OPTION) {
            txtRepoPath.setText(chooser.getSelectedFile().getAbsolutePath());
            txtRepoPath.requestFocusInWindow();
        }
        validateInfo();
}//GEN-LAST:event_btnBrowseActionPerformed

private void jraLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jraLocalActionPerformed
    txtRepoUrl.setEnabled(false);
    txtRepoPath.setEnabled(true);
    btnBrowse.setEnabled(true);
    validateInfo();
}//GEN-LAST:event_jraLocalActionPerformed

private void jraRemoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jraRemoteActionPerformed
    selectRemoteRepo(true);
}//GEN-LAST:event_jraRemoteActionPerformed

private void selectRemoteRepo(boolean checkValidity) {
    txtRepoPath.setEnabled(false);
    btnBrowse.setEnabled(false);
    txtRepoUrl.setEnabled(true);
    if (checkValidity) {
        validateInfo();
    }
}

private void txtRepoIdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRepoIdKeyReleased
    validateInfo();
}//GEN-LAST:event_txtRepoIdKeyReleased

private void txtRepoPathKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRepoPathKeyTyped
    validateInfo();
}//GEN-LAST:event_txtRepoPathKeyTyped

private void txtRepoNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRepoNameKeyReleased
    validateInfo();
}//GEN-LAST:event_txtRepoNameKeyReleased

private void txtRepoUrlKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRepoUrlKeyReleased
    validateInfo();
}//GEN-LAST:event_txtRepoUrlKeyReleased

    public void modify(RepositoryInfo info) {
        modify = true;
        txtRepoId.setEnabled(false);
        txtRepoId.setText(info.getId());
        txtRepoName.setText(info.getName());
        if (singleType && info.getType() != null) {
            comType.setSelectedItem(info.getType());
        }
        if (info.isLocal()) {
            jraLocal.setSelected(true);
            txtRepoPath.setText(info.getRepositoryPath());
            jraLocalActionPerformed(null);
        } else if (info.isRemoteDownloadable()) {
            jraRemote.setSelected(true);
            txtRepoUrl.setText(info.getRepositoryUrl());
            jraRemoteActionPerformed(null);
        }
    }
    
    public RepositoryInfo getRepositoryInfo() throws URISyntaxException {
      return new RepositoryInfo(txtRepoId.getText().trim(),
              (String)comType.getSelectedItem(),
              txtRepoName.getText().trim(),
              jraLocal.isSelected()  ? txtRepoPath.getText().trim() : null,
              jraRemote.isSelected() ? txtRepoUrl.getText().trim() : null);
    }

    private void validateInfo() {
        //check repo id
        if (txtRepoId.getText().trim().length() == 0) {
            btnOK.setEnabled(false);
            lblValidate.setText(NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_id_Error1"));
            return;
        }
        if (!modify) {
            RepositoryInfo info = RepositoryPreferences.getInstance().getRepositoryInfoById(txtRepoId.getText().trim());
            if (info != null && (info.isLocal() || info.isRemoteDownloadable())) {
                btnOK.setEnabled(false);
                lblValidate.setText(NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_id_Error2"));
                return;
            } else if (info != null && !alreadyFilled) {
                txtRepoUrl.setText(info.getRepositoryUrl());
                txtRepoName.setText(info.getName());
                jraRemote.setSelected(true);
                selectRemoteRepo(false);
                alreadyFilled = true;
            }
        }

        //check repo name
        if (txtRepoName.getText().trim().length() == 0) {
            btnOK.setEnabled(false);
            lblValidate.setText(NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_Name_Error1"));
            return;
        }
        if (jraLocal.isSelected()) {
            //check repo url
            if (txtRepoPath.getText().trim().length() == 0 || !new File(txtRepoPath.getText().trim()).exists()) {
                btnOK.setEnabled(false);
                lblValidate.setText(NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_Path_Error"));
                return;
            }
        } else {
            //check repo url
            if (txtRepoUrl.getText().trim().length() == 0) {
                btnOK.setEnabled(false);
                lblValidate.setText(NbBundle.getMessage(RepositoryRegisterUI.class, "LBL_Repo_Url_Error"));
                return;
            }
        }

        lblValidate.setText("");
        btnOK.setEnabled(true);
    }

    public JButton getButton() {
        return btnOK;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnOK;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox comType;
    private javax.swing.JRadioButton jraLocal;
    private javax.swing.JRadioButton jraRemote;
    private javax.swing.JLabel lblHeader;
    private javax.swing.JLabel lblRepoId;
    private javax.swing.JLabel lblRepoName;
    private javax.swing.JLabel lblRepoPath;
    private javax.swing.JLabel lblRepoType;
    private javax.swing.JLabel lblRepoUrl;
    private javax.swing.JLabel lblType;
    private javax.swing.JLabel lblValidate;
    private javax.swing.JTextField txtRepoId;
    private javax.swing.JTextField txtRepoName;
    private javax.swing.JTextField txtRepoPath;
    private javax.swing.JTextField txtRepoUrl;
    // End of variables declaration//GEN-END:variables

}
