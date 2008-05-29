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

import java.awt.Color;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.projectimport.eclipse.core.EclipseUtils;

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
        Color lblBgr = UIManager.getColor("Label.background"); // NOI18N
        wsDescription.setBackground(lblBgr);
        note.setBackground(lblBgr);
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
    
    /** Returns workspace directory choosed by user. */
    String getWorkspaceDir() {
        return workspaceDir.getText().trim();
    }
    
    private void workspaceChanged() {
        String workspace = getWorkspaceDir().trim();
        if ("".equals(workspace)) {
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ChooseWorkspace")); // NOI18N
            return;
        }
        boolean wsValid = EclipseUtils.isRegularWorkSpace(getWorkspaceDir());
        setErrorMessage(wsValid ? null : ProjectImporterWizard.getMessage(
                "MSG_NotRegularWorkspace", getWorkspaceDir())); // NOI18N
    }
    
    private void projectChanged() {
        // check Eclipse project directory
        String project = getProjectDir();
        if ("".equals(project)) {
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
        String projectDest = getProjectDestinationDir();
        if ("".equals(projectDest)) {
            setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ChooseProjectDestination")); // NOI18N
            return;
        }
        File projectDestFile = new File(projectDest, projectDirFile.getName());
        if (projectDestFile.exists()) {
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
        firePropertyChange("errorMessage", oldMessage, newMessage);
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
        return projectDestDir.getText().trim();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

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
        wsDescription = new javax.swing.JTextArea();
        note = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(workspaceDir, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(worskpaceBrowse, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_BrowseButton_B"));
        worskpaceBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                worskpaceBrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(worskpaceBrowse, gridBagConstraints);

        workSpaceLBL.setLabelFor(workspaceDir);
        org.openide.awt.Mnemonics.setLocalizedText(workSpaceLBL, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_Workspace"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 14, 0, 12);
        add(workSpaceLBL, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(projectDir, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(projectBrowse, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_BrowseButton_R"));
        projectBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectBrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(projectBrowse, gridBagConstraints);

        projectLBL.setLabelFor(projectDir);
        org.openide.awt.Mnemonics.setLocalizedText(projectLBL, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_Project"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 14, 0, 12);
        add(projectLBL, gridBagConstraints);

        buttonGroup.add(projectButton);
        org.openide.awt.Mnemonics.setLocalizedText(projectButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_ProjectButton"));
        projectButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        projectButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        projectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 6, 0);
        add(projectButton, gridBagConstraints);

        buttonGroup.add(workspaceButton);
        workspaceButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(workspaceButton, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_WorkspaceButton"));
        workspaceButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        workspaceButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        workspaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                workspaceButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(workspaceButton, gridBagConstraints);

        projectDestLBL.setLabelFor(projectDestDir);
        org.openide.awt.Mnemonics.setLocalizedText(projectDestLBL, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "LBL_ProjectDestination"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 14, 0, 12);
        add(projectDestLBL, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(projectDestDir, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(projectDestBrowse, org.openide.util.NbBundle.getMessage(SelectionPanel.class, "CTL_BrowseButton_S"));
        projectDestBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectDestBrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(projectDestBrowse, gridBagConstraints);

        wsDescription.setEditable(false);
        wsDescription.setLineWrap(true);
        wsDescription.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/projectimport/eclipse/core/wizard/Bundle").getString("LBL_SpecifyWorkspaceDescription"));
        wsDescription.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        add(wsDescription, gridBagConstraints);

        note.setEditable(false);
        note.setLineWrap(true);
        note.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/projectimport/eclipse/core/wizard/Bundle").getString("LBL_NoteAboutWorkspaceAdvantage"));
        note.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(note, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    private void projectDestBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectDestBrowseActionPerformed
        JFileChooser chooser = new JFileChooser(projectDestDir.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            projectDestDir.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_projectDestBrowseActionPerformed
            
    private void projectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectButtonActionPerformed
        setWorkspaceEnabled(false);
        projectChanged();
        projectDir.requestFocusInWindow();
        firePropertyChange("workspaceChoosen", true, false); // NOI18N
    }//GEN-LAST:event_projectButtonActionPerformed
    
    private void workspaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_workspaceButtonActionPerformed
        setWorkspaceEnabled(true);
        workspaceChanged();
        firePropertyChange("workspaceChoosen", false, true); // NOI18N
    }//GEN-LAST:event_workspaceButtonActionPerformed
    
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
    }
    
    private void projectBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectBrowseActionPerformed
        JFileChooser chooser = new JFileChooser(projectDir.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            projectDir.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_projectBrowseActionPerformed
    
    private void worskpaceBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_worskpaceBrowseActionPerformed
        JFileChooser chooser = new JFileChooser(getWorkspaceDir());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            workspaceDir.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_worskpaceBrowseActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JTextArea note;
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
    private javax.swing.JTextArea wsDescription;
    // End of variables declaration//GEN-END:variables
}
