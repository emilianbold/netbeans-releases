/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform.ui;

import java.io.File;
import java.text.MessageFormat;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.ant.freeform.Util;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class BasicProjectInfoPanel extends javax.swing.JPanel implements HelpCtx.Provider{
    
    private DocumentListener documentListener;
    private ChangeListener listener;
    /** Was antScript property edited by user? */
    private boolean antScriptTouched = false;
    /** Was projectFolder property edited by user? */
    private boolean projectFolderTouched = false;
    /** Was projectName property edited by user? */
    private boolean projectNameTouched = false;
    /** Is choosen Ant script a valid one? */
    private boolean antScriptValidityChecked;
    
    public BasicProjectInfoPanel(String projectLocation, String antScript, String projectName, String projectFolder,
            ChangeListener listener) {
        initComponents();
        this.projectLocation.setText(projectLocation);
        this.antScript.setText(antScript);
        this.projectName.setText(projectName);
        this.projectFolder.setText(projectFolder);
        this.listener = listener;
        documentListener = new DocumentListener() {           
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }

            public void removeUpdate(DocumentEvent e) {
                update(e);
            }

            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
        };
        this.projectLocation.getDocument().addDocumentListener(documentListener);
        this.antScript.getDocument().addDocumentListener(documentListener);
        this.projectName.getDocument().addDocumentListener(documentListener);
        this.projectFolder.getDocument().addDocumentListener(documentListener);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(BasicProjectInfoPanel.class);
    }

    public File getProjectLocation() {
        return getAsFile(projectLocation.getText());
    }

    public File getAntScript() {
        return getAsFile(antScript.getText());
    }

    public String getProjectName() {
        return projectName.getText();
    }

    public File getProjectFolder() {
        return getAsFile(projectFolder.getText());
    }

    public Boolean getMainProject() {
        return Boolean.valueOf(mainProject.isSelected());
    }

    public String getError() {
        if (projectLocation.getText().length() == 0) {
            return org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_1");
        }
        if (!getProjectLocation().exists()) {
            return org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_2");
        }
        if (antScript.getText().length() == 0) {
            return org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_3");
        }
        if (!getAntScript().exists()) {
            return org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_4");
        }
        if (!antScriptValidityChecked) {
            FileObject fo = FileUtil.toFileObject(getAntScript());
            if (fo != null && Util.getAntScriptTargetNames(fo) != null) {
                antScriptValidityChecked = true;
            } else {
                return org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_5");
            }
        }
        if (getProjectName().length() == 0) {
            return org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_6");
        }
        if (projectFolder.getText().length() == 0) {
            return org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_7");
        }
        if (getAsFile(projectFolder.getText() + File.separatorChar + "nbproject").exists()){ // NOI18N
            return org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_8");
        }
        
        Project p;
        File projectFolder = getProjectFolder();
        
        assert projectFolder != null;
        
        if ((p = FileOwnerQuery.getOwner(projectFolder.toURI())) != null && projectFolder.equals(FileUtil.toFile(p.getProjectDirectory()))) {
            ProjectInformation pi = (ProjectInformation) p.getLookup().lookup(ProjectInformation.class);
            String displayName = (pi == null ? "" : pi.getDisplayName());   //NOI18N
            return MessageFormat.format(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_9"),
                new Object[] {displayName});
        }
        
        File projectLocation = getProjectLocation();
        
        assert projectLocation != null;
        
        if ((p = FileOwnerQuery.getOwner(projectLocation.toURI())) != null && projectLocation.equals(FileUtil.toFile(p.getProjectDirectory()))) {
            ProjectInformation pi = (ProjectInformation) p.getLookup().lookup(ProjectInformation.class);
            String displayName = (pi == null ? "" : pi.getDisplayName());   //NOI18N
            return MessageFormat.format(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_Error_10"),
                new Object[] {displayName});
        }
        return null;
    }

    private File getAsFile(String filename) {
        return FileUtil.normalizeFile(new File(filename));
    }

    private boolean ignoreEvent = false;

    private void update(DocumentEvent e) {
        if (ignoreEvent) {
            // side-effect of changes done in this handler
            return;
        }

        // start ignoring events
        ignoreEvent = true;

        if (projectLocation.getDocument() == e.getDocument()) {
            antScriptValidityChecked = false;
            updateAntScriptLocation();
            updateProjectName();
            updateProjectFolder();
        }
        if (antScript.getDocument() == e.getDocument()) {
            antScriptValidityChecked = false;
            updateProjectName();
        }

        // stop ignoring events
        ignoreEvent = false;

        if (projectFolder.getDocument() == e.getDocument()) {
            projectFolderTouched = !"".equals(projectFolder.getText());
        }
        if (antScript.getDocument() == e.getDocument()) {
            antScriptTouched = !"".equals(antScript.getText());
        }
        if (projectName.getDocument() == e.getDocument()) {
            projectNameTouched = !"".equals(projectName.getText());
        }

        listener.stateChanged(null);
    }

    private boolean isValidProjectLocation() {
        return (getProjectLocation().exists() && getProjectLocation().isDirectory() &&
                projectLocation.getText().length() > 0 && (!projectLocation.getText().endsWith(":"))); // NOI18N
    }

    private void updateAntScriptLocation() {
        if (antScriptTouched) {
            return;
        }
        if (isValidProjectLocation()) {
            File as = new File(getProjectLocation().getAbsolutePath() + File.separatorChar + "build.xml"); // NOI18N
            if (as.exists()) {
                antScript.setText(as.getAbsolutePath());
                return;
            }
        }
        antScript.setText(""); // NOI18N
    }

    private void updateProjectName() {
        if (projectNameTouched) {
            return;
        }
        if (getAntScript().exists()) {
            File as = new File(getAntScript().getAbsolutePath());
            if (as.exists()) {
                FileObject fo = FileUtil.toFileObject(as);
                assert fo != null : as;
                String name = Util.getAntScriptName(fo);
                if (name != null) {
                    projectName.setText(name);
                    return;
                }
            }
        }
        projectName.setText(""); // NOI18N
    }

    private void updateProjectFolder() {
        if (projectFolderTouched) {
            return;                                                                
        }
        if (isValidProjectLocation()) {
            projectFolder.setText(getProjectLocation().getAbsolutePath());
        } else {
            projectFolder.setText(""); // NOI18N
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        antScript = new javax.swing.JTextField();
        projectName = new javax.swing.JTextField();
        projectFolder = new javax.swing.JTextField();
        browseAntScript = new javax.swing.JButton();
        browseProjectFolder = new javax.swing.JButton();
        projectLocation = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        browseProjectLocation = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        mainProject = new javax.swing.JCheckBox();

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(323, 223));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_jLabel1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "ACSD_BasicProjectInfoPanel_jLabel1"));

        jLabel2.setLabelFor(antScript);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_jLabel2"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "ACSD_BasicProjectInfoPanel_jLabel2"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_jLabel3"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 12, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "ACSD_BasicProjectInfoPanel_jLabel3"));

        jLabel4.setLabelFor(projectName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_jLabel4"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "ACSD_BasicProjectInfoPanel_jLabel4"));

        jLabel5.setLabelFor(projectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_jLabel5"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "ACSD_BasicProjectInfoPanel_jLabel5"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        add(antScript, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(projectName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        add(projectFolder, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseAntScript, org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "BTN_BasicProjectInfoPanel_browseAntScript"));
        browseAntScript.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(browseAntScript, gridBagConstraints);
        browseAntScript.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "ACSD_BasicProjectInfoPanel_browseAntScript"));

        org.openide.awt.Mnemonics.setLocalizedText(browseProjectFolder, org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "BTN_BasicProjectInfoPanel_browseProjectFolder"));
        browseProjectFolder.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(browseProjectFolder, gridBagConstraints);
        browseProjectFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "ACSD_BasicProjectInfoPanel_browseProjectFolder"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(projectLocation, gridBagConstraints);

        jLabel6.setLabelFor(projectLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_jLabel6"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabel6, gridBagConstraints);
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "ACSD_BasicProjectInfoPanel_jLabel6"));

        org.openide.awt.Mnemonics.setLocalizedText(browseProjectLocation, org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "BTN_BasicProjectInfoPanel_browseProjectLocation"));
        browseProjectLocation.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        add(browseProjectLocation, gridBagConstraints);
        browseProjectLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "ACSD_BasicProjectInfoPanel_browseProjectLocation"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        add(jSeparator1, gridBagConstraints);

        mainProject.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mainProject, org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_BasicProjectInfoPanel_mainProject"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(mainProject, gridBagConstraints);
        mainProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicProjectInfoPanel.class, "ACSD_BasicProjectInfoPanel_mainProject"));

    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == browseAntScript) {
                BasicProjectInfoPanel.this.browseAntScriptActionPerformed(evt);
            }
            else if (evt.getSource() == browseProjectFolder) {
                BasicProjectInfoPanel.this.browseProjectFolderActionPerformed(evt);
            }
            else if (evt.getSource() == browseProjectLocation) {
                BasicProjectInfoPanel.this.browseProjectLocationActionPerformed(evt);
            }
        }
    }//GEN-END:initComponents

    private void browseProjectLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectLocationActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (projectLocation.getText().length() > 0 && getProjectLocation().exists()) {
            chooser.setSelectedFile(getProjectLocation());
        } else {
            chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
        }
        chooser.setDialogTitle(NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_Browse_Location"));
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectLoc = FileUtil.normalizeFile(chooser.getSelectedFile());
            projectLocation.setText(projectLoc.getAbsolutePath());
        }
    }//GEN-LAST:event_browseProjectLocationActionPerformed

    private void browseProjectFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectFolderActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (projectFolder.getText().length() > 0 && getProjectFolder().exists()) {
            chooser.setSelectedFile(getProjectFolder());
        } else if (projectLocation.getText().length() > 0 && getProjectLocation().exists()) {
            chooser.setSelectedFile(getProjectLocation());
        } else {
            chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
        }
        chooser.setDialogTitle(NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_Browse_Project_Folder"));
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            projectFolder.setText(projectDir.getAbsolutePath());
        }                    
    }//GEN-LAST:event_browseProjectFolderActionPerformed

    private void browseAntScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseAntScriptActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        if (antScript.getText().length() > 0 && getAntScript().exists()) {
            chooser.setSelectedFile(getAntScript());
        } else if (projectLocation.getText().length() > 0 && getProjectLocation().exists()) {
            chooser.setSelectedFile(getProjectLocation());
        } else {
            chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
        }
        chooser.setDialogTitle(NbBundle.getMessage(BasicProjectInfoPanel.class, "LBL_Browse_Build_Script"));
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File script = FileUtil.normalizeFile(chooser.getSelectedFile());
            antScript.setText(script.getAbsolutePath());
        }            
    }//GEN-LAST:event_browseAntScriptActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField antScript;
    private javax.swing.JButton browseAntScript;
    private javax.swing.JButton browseProjectFolder;
    private javax.swing.JButton browseProjectLocation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox mainProject;
    private javax.swing.JTextField projectFolder;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JTextField projectName;
    // End of variables declaration//GEN-END:variables
    
}
