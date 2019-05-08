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
package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.makeproject.ui.actions.DefaultProjectOperationsImplementation.InvalidablePanel;
import org.openide.util.ChangeSupport;

/**
 * Copy-pasted from org.netbeans.modules.project.uiapi
 * Intention is to contribute it back as soon as it is adapted to remote environment.
 * Since we are planning to contribute it back,
 * NEVER use any remote or cnd stuff directly, but only via a well defined SPI
 *
 */
public class DefaultProjectRenamePanel extends javax.swing.JPanel implements DocumentListener, InvalidablePanel {
    
    private Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private ProgressHandle handle;
    private JComponent progressComponent;
    private ProgressBar progressBar;
    
    /**
     * Creates new form DefaultProjectRenamePanel
     */
    public DefaultProjectRenamePanel(ProgressHandle handle, Project project, String name) {
        this.project = project;
        this.handle = handle;
        
        if (name == null) {
            name = ProjectUtils.getInformation(project).getDisplayName();
        }
        
        initComponents();
        
        projectName.setText(name);
        projectName.getDocument().addDocumentListener(this);
        updateProjectFolder();
        validateDialog();
        
        if (Boolean.getBoolean("org.netbeans.modules.project.uiapi.DefaultProjectOperations.showProgress")) {
            ((CardLayout) progress.getLayout()).show(progress, "progress"); // NOI18N
        }
    }
    
    private static class ProgressBar extends JPanel {

        private JLabel label;

        private static ProgressBar create(JComponent progress) {
            ProgressBar instance = new ProgressBar();
            instance.setLayout(new BorderLayout());
            instance.label = new JLabel(" "); //NOI18N
            instance.label.setBorder(new EmptyBorder(0, 0, 2, 0));
            instance.add(instance.label, BorderLayout.NORTH);
            instance.add(progress, BorderLayout.CENTER);
            return instance;
        }

        public void setString(String value) {
            label.setText(value);
        }

        private ProgressBar() {
        }
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        projectFolderLabel = new javax.swing.JLabel();
        projectName = new javax.swing.JTextField();
        projectFolder = new javax.swing.JTextField();
        alsoRenameFolder = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        errorMessage = new javax.swing.JLabel();
        progress = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        progressImpl = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(225, 250));
        setPreferredSize(new java.awt.Dimension(542, 250));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(projectName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "LBL_Project_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(jLabel1, gridBagConstraints);

        projectFolderLabel.setLabelFor(projectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(projectFolderLabel, org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "LBL_Project_Folder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 12);
        add(projectFolderLabel, gridBagConstraints);

        projectName.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(projectName, gridBagConstraints);
        projectName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "ACSN_Project_Name", new Object[] {})); // NOI18N
        projectName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "ACSD_Project_Name", new Object[] {})); // NOI18N

        projectFolder.setEditable(false);
        projectFolder.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(projectFolder, gridBagConstraints);
        projectFolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "ACSN_Project_Folder", new Object[] {})); // NOI18N
        projectFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "ACSD_Project_Folder", new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(alsoRenameFolder, org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "LBL_Also_Rename_Project_Folder")); // NOI18N
        alsoRenameFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alsoRenameFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(alsoRenameFolder, gridBagConstraints);
        alsoRenameFolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "ACSN_Also_Rename_Project_Folder", new Object[] {})); // NOI18N
        alsoRenameFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "ACSD_Also_Rename_Project_Folder", new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "LBL_Rename_Dialog_Text", new Object[] {ProjectUtils.getInformation(project).getDisplayName()})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel3, gridBagConstraints);

        errorMessage.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(errorMessage, gridBagConstraints);

        progress.setLayout(new java.awt.CardLayout());
        progress.add(jPanel4, "not-progress"); // NOI18N

        progressImpl.add(progressComponent = ProgressHandleFactory.createProgressComponent(handle));
        progressImpl.setMinimumSize(new java.awt.Dimension(121, 17));
        progressImpl.setPreferredSize(new java.awt.Dimension(121, 17));
        progressImpl.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "LBL_Renaming_Project", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        progressImpl.add(jLabel5, gridBagConstraints);

        progress.add(progressImpl, "progress"); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(progress, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DefaultProjectRenamePanel.class, "ACSD_Project_Rename", new Object[] {})); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void alsoRenameFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alsoRenameFolderActionPerformed
        updateProjectFolder();
        validateDialog();
    }//GEN-LAST:event_alsoRenameFolderActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox alsoRenameFolder;
    private javax.swing.JLabel errorMessage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel progress;
    private javax.swing.JPanel progressImpl;
    private javax.swing.JTextField projectFolder;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JTextField projectName;
    // End of variables declaration//GEN-END:variables
    
    public String getNewName() {
        return projectName.getText();
    }
    
    public boolean getRenameProjectFolder() {
        return alsoRenameFolder.isSelected();
    }
    
    @Override
    public void changedUpdate(DocumentEvent e) {
        //ignored
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        updateProjectFolder();
        validateDialog();
    }
    
    @Override
    public void removeUpdate(DocumentEvent e) {
        updateProjectFolder();
        validateDialog();
    }
    
    private void updateProjectFolder() {
        FileProxy location = FileProxy.create(project.getProjectDirectory().getParent());
        FileProxy projectFolderFile;
        if (alsoRenameFolder.isSelected()) {
            projectFolderFile = location.getChild(projectName.getText());
        } else {
            projectFolderFile = location.getChild(project.getProjectDirectory().getNameExt());
        }

        projectFolder.setText(projectFolderFile.getAbsolutePath());
    }
    
    @Override
    public boolean isPanelValid() {
        return " ".equals(errorMessage.getText()); // NOI18N
    }

    private void validateDialog() {
        String newError = computeError();
        boolean changed = false;
        String currentError = errorMessage.getText();
        
        newError = newError != null ? newError : " "; // NOI18N
        changed = !currentError.equals(newError);
        
        errorMessage.setText(newError);
        
        if (changed) {
            changeSupport.fireChange();
        }
    }
    
    private String computeError() {
        FileProxy location = FileProxy.create(project.getProjectDirectory().getParent());
        return DefaultProjectOperationsImplementation.computeError(location, projectName.getText(), !getRenameProjectFolder());
    }
    
    @Override
    public void showProgress() {
        projectFolder.setEnabled(false);
        projectName.setEnabled(false);
        alsoRenameFolder.setEnabled(false);
        progress.setVisible(true);
        
        ((CardLayout) progress.getLayout()).last(progress);
    }
    
    protected void addProgressBar() {
        progressBar = ProgressBar.create(progressComponent); //NOI18N
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        progressImpl.add(progressBar, gridBagConstraints);
        progressImpl.repaint();
        progressImpl.revalidate();
}
    
    protected void removeProgressBar() {
        progressImpl.remove(progressBar);
    }
}
