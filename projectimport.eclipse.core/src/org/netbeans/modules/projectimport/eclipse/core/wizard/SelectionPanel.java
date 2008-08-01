/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.projectimport.eclipse.core.wizard;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.projectimport.eclipse.core.EclipseUtils;
import org.openide.filesystems.FileUtil;

/**
 * Represent "Selection" step(panel) in the Eclipse importer wizard.
 *
 * @author mkrauskopf
 */
final class SelectionPanel extends JPanel {

    private String errorMessage;

    /** Creates new form ProjectSelectionPanel */
    public SelectionPanel() {
        super();
        initComponents();
        workspaceDir.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { workspaceChanged(); }
            public void removeUpdate(DocumentEvent e) { workspaceChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        projectDir.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { projectChanged(); }
            public void removeUpdate(DocumentEvent e) { projectChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        projectDestDir.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { projectChanged(); }
            public void removeUpdate(DocumentEvent e) { projectChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        setWorkspaceEnabled(workspaceButton.isSelected());
    }
    
    /** Returns workspace directory chosen by user, or null. */
    File getWorkspaceDir() {
        String d = workspaceDir.getText();
        if (d != null && d.trim().length() > 0) {
            return FileUtil.normalizeFile(new File(d.trim()));
        } else {
            return null;
        }
    }
    
    private void workspaceChanged() {
        File workspace = getWorkspaceDir();
        if (workspace == null) {
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ChooseWorkspace")); // NOI18N
            return;
        }
        boolean wsValid = EclipseUtils.isRegularWorkSpace(workspace);
        setErrorMessage(wsValid ? null : ProjectImporterWizard.getMessage(
                "MSG_NotRegularWorkspace", workspace)); // NOI18N
    }
    
    private void projectChanged() {
        // check Eclipse project directory
        String project = getProjectDir();
        if ("".equals(project)) { // NOI18N
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ChooseProject")); // NOI18N
            return;
        }
        File projectDirFile = new File(project);
        if (!EclipseUtils.isRegularProject(projectDirFile)) {
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_NotRegularProject", project)); // NOI18N
            return;
        }
        
        // check destination directory
        String projectDest = projectDestDir.getText().trim();
        if ("".equals(projectDest)) { // NOI18N
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ChooseProjectDestination")); // NOI18N
            return;
        }
        File projectDestFile = new File(projectDest, projectDirFile.getName());
        if (!projectDestFile.equals(projectDirFile) && projectDestFile.exists()) {
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ProjectExist", projectDestFile.getName())); // NOI18N
            return;
        }
        
        // valid
        setErrorMessage(null);
    }
    
    void setErrorMessage(String newMessage) {
        String oldMessage = this.errorMessage;
        this.errorMessage = newMessage;
        firePropertyChange("errorMessage", oldMessage, newMessage); // NOI18N
    }
    
    boolean isWorkspaceChosen() {
        return workspaceButton.isSelected();
    }
    
    /** Returns project directory of single-selected project. */
    public String getProjectDir() {
        return projectDir.getText().trim();
    }
    
    /** Returns destination directory for single-selected project. */
    public String getProjectDestinationDir() {
        if (projectDestDir.getText().trim().equals(projectDir.getText().trim())) {
            return null;
        } else {
            return projectDestDir.getText().trim();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        workspaceDir = new javax.swing.JTextField();
        worskpaceBrowse = new javax.swing.JButton();
        workSpaceLBL = new javax.swing.JLabel();
        projectDir = new javax.swing.JTextField();
        projectBrowse = new javax.swing.JButton();
        projectLBL = new javax.swing.JLabel();
        projectButton = new javax.swing.JRadioButton();
        workspaceButton = new javax.swing.JRadioButton();
        projectDestLBL = new javax.swing.JLabel();
        projectDestDir = new javax.swing.JTextField();
        projectDestBrowse = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        note = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(worskpaceBrowse, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_BrowseButton_B")); // NOI18N
        worskpaceBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                worskpaceBrowseActionPerformed(evt);
            }
        });

        workSpaceLBL.setLabelFor(workspaceDir);
        org.openide.awt.Mnemonics.setLocalizedText(workSpaceLBL, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_Workspace")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(projectBrowse, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_BrowseButton_R")); // NOI18N
        projectBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectBrowseActionPerformed(evt);
            }
        });

        projectLBL.setLabelFor(projectDir);
        org.openide.awt.Mnemonics.setLocalizedText(projectLBL, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_Project")); // NOI18N

        buttonGroup.add(projectButton);
        org.openide.awt.Mnemonics.setLocalizedText(projectButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_ProjectButton")); // NOI18N
        projectButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        projectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectButtonActionPerformed(evt);
            }
        });

        buttonGroup.add(workspaceButton);
        workspaceButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(workspaceButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_WorkspaceButton")); // NOI18N
        workspaceButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        workspaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                workspaceButtonActionPerformed(evt);
            }
        });

        projectDestLBL.setLabelFor(projectDestDir);
        org.openide.awt.Mnemonics.setLocalizedText(projectDestLBL, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_ProjectDestination")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(projectDestBrowse, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_BrowseButton_S")); // NOI18N
        projectDestBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectDestBrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_SpecifyWorkspaceDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(note, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_NoteAboutWorkspaceAdvantage")); // NOI18N
        note.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(note, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(workSpaceLBL)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(workspaceDir, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(worskpaceBrowse))
                    .add(workspaceButton)
                    .add(projectButton)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectDestLBL)
                            .add(projectLBL))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(projectDir, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                            .add(projectDestDir, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, projectBrowse)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, projectDestBrowse)))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(workspaceButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(workSpaceLBL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(worskpaceBrowse)
                    .add(workspaceDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(projectButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLBL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(projectBrowse)
                    .add(projectDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectDestLBL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(projectDestBrowse)
                    .add(projectDestDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(note, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, Short.MAX_VALUE))
        );

        workspaceDir.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        worskpaceBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        workSpaceLBL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectDir.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectLBL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        workspaceButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectDestLBL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectDestDir.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        projectDestBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        note.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectionPanel.class, "ACSD_SelectionPanel_NA")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    private void projectDestBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectDestBrowseActionPerformed
        JFileChooser chooser = new JFileChooser(projectDestDir.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileHidingEnabled(false);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            projectDestDir.setText(chooser.getSelectedFile().getAbsolutePath());
        }//GEN-LAST:event_projectDestBrowseActionPerformed
    }                                                 
            
    private void projectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectButtonActionPerformed
        setWorkspaceEnabled(false);
        projectChanged();
        projectDir.requestFocusInWindow();
        firePropertyChange("workspaceChoosen", true, false); // NOI18N//GEN-LAST:event_projectButtonActionPerformed
    }                                             
    
    private void workspaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_workspaceButtonActionPerformed
        setWorkspaceEnabled(true);
        workspaceChanged();
        firePropertyChange("workspaceChoosen", false, true); // NOI18N//GEN-LAST:event_workspaceButtonActionPerformed
    }                                               
    
    private void setWorkspaceEnabled(boolean enabled) {
        workSpaceLBL.setEnabled(enabled);
        worskpaceBrowse.setEnabled(enabled);
        workspaceDir.setEnabled(enabled);
        projectLBL.setEnabled(!enabled);
        projectBrowse.setEnabled(!enabled);
        projectDir.setEnabled(!enabled);
        projectDestBrowse.setEnabled(!enabled);
        projectDestDir.setEnabled(!enabled);
        projectDestLBL.setEnabled(!enabled);
        note.setVisible(!enabled);
    }
    
    private void projectBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectBrowseActionPerformed
        JFileChooser chooser = new JFileChooser(projectDir.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileHidingEnabled(false);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            projectDir.setText(chooser.getSelectedFile().getAbsolutePath());
        }//GEN-LAST:event_projectBrowseActionPerformed
    }                                             
    
    private void worskpaceBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_worskpaceBrowseActionPerformed
        JFileChooser chooser = new JFileChooser(getWorkspaceDir());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileHidingEnabled(false);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            workspaceDir.setText(chooser.getSelectedFile().getAbsolutePath());
        }//GEN-LAST:event_worskpaceBrowseActionPerformed
    }                                               
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel note;
    private javax.swing.JButton projectBrowse;
    private javax.swing.JRadioButton projectButton;
    private javax.swing.JButton projectDestBrowse;
    private javax.swing.JTextField projectDestDir;
    private javax.swing.JLabel projectDestLBL;
    private javax.swing.JTextField projectDir;
    private javax.swing.JLabel projectLBL;
    private javax.swing.JLabel workSpaceLBL;
    private javax.swing.JRadioButton workspaceButton;
    private javax.swing.JTextField workspaceDir;
    private javax.swing.JButton worskpaceBrowse;
    // End of variables declaration//GEN-END:variables
}
