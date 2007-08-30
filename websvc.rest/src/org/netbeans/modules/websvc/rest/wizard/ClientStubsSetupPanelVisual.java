/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;

/**
 *
 * @author  Nam Nguyen
 */
public final class ClientStubsSetupPanelVisual extends JPanel implements AbstractPanel.Settings {
    private Project project;
    private FileObject stubRootFolder;
    private SourceGroup[] sourceGroups;
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    /** Creates new form ClientStubVisualPanel1 */
    public ClientStubsSetupPanelVisual(String name) {
        super.setName(name);
        initComponents();
        projectList.addListSelectionListener(new ProjectListSelectionListener());
    }

    private class ProjectListSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() == projectList) {
                if (projectList.getSelectionModel().isSelectionEmpty()) {
                    removeButton.setEnabled(false);
                } else {
                    removeButton.setEnabled(true);
                }
            }
        }
    }
    
    public void read(WizardDescriptor wizard) {
        if (project == null) {
            project = Templates.getProject(wizard);
            projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
            sourceGroups = Util.getSourceGroups(project);
            SourceGroupUISupport.connect(locationCB, sourceGroups);
            folderTextField.setText(Constants.REST_STUBS);
        }
    }

    public void store(WizardDescriptor wizard) {
        DefaultListModel model = (DefaultListModel) projectList.getModel();
        Project[] projects = new Project[model.size()];
        for (int i=0; i < projects.length; i++) {
            projects[i] = ((ProjectInformation)model.get(i)).getProject();
        }
        wizard.putProperty(WizardProperties.PROJECTS_TO_STUB, projects);
        wizard.putProperty(WizardProperties.OVERWRITE_EXISTING, overwriteCheckBox.isSelected());
        if (stubRootFolder == null && sourceGroups.length > 0) {
            String path = folderTextField.getText();
            if (path == null || path.trim().length() == 0) {
                path = Constants.REST_STUBS;
            }
            SourceGroup sg = (SourceGroup) locationCB.getSelectedItem();
            try {
                stubRootFolder = FileUtil.createFolder(sg.getRootFolder(), path);
            } catch(IOException ioe) {
                AbstractPanel.setErrorMessage(wizard, ioe.getLocalizedMessage());
            }
        }
        if (stubRootFolder != null) {
            wizard.putProperty(WizardProperties.STUB_ROOT_FOLDER, stubRootFolder);
        }
    }

    public boolean valid(WizardDescriptor wizard) {
        DefaultListModel model = (DefaultListModel) projectList.getModel();
        if (model.getSize() < 1) {
            AbstractPanel.setErrorMessage(wizard, "MSG_NoProjects");
            return false;
        }
        
        AbstractPanel.clearErrorMessage(wizard);
        return true;
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }
    private void fireChange() {
        ChangeEvent event =  new ChangeEvent(this);
        
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }
    
    private boolean addProject(File[] projectDirs) {
        boolean changed = false;
        List<Project> rejecteds = new ArrayList<Project>();
        DefaultListModel listModel = (DefaultListModel) projectList.getModel();
        for (File projectDir : projectDirs) {
            try {
                FileObject projectRoot = FileUtil.toFileObject(projectDir);
                Project p = ProjectManager.getDefault().findProject(projectRoot);
                if (! RestUtils.isRestEnabled(p)) {
                    rejecteds.add(p);
                    continue;
                }
                ProjectInformation pInfo = ProjectUtils.getInformation(p);
                listModel.addElement(pInfo);
                changed = true;
            } catch(IOException ioe) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ioe.getLocalizedMessage(), ioe);
            }
        }
        if (rejecteds.size() > 0) {
            String msg = NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "MSG_ProjectsWithoutREST");
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg);
            DialogDisplayer.getDefault().notify(nd);
        }
        return changed;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationCB = new javax.swing.JComboBox();
        folderLabel = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        refLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        projectList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        overwriteCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_Project")); // NOI18N

        projectTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_Location")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(folderLabel, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_Folder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_Browse")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        refLabel.setLabelFor(projectList);
        org.openide.awt.Mnemonics.setLocalizedText(refLabel, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_ProjectsToGenerateStubFor")); // NOI18N

        projectList.setModel(new DefaultListModel());
        projectList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                ProjectInformation pInfo = (ProjectInformation)value;
                super.getListCellRendererComponent( projectList, pInfo, index, isSelected, cellHasFocus );
                setIcon(pInfo.getIcon());
                setText(pInfo.getDisplayName());
                setToolTipText(pInfo.getDisplayName());
                return this;
            }
        });
        jScrollPane1.setViewportView(projectList);
        projectList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "ProjectList")); // NOI18N
        projectList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_ProjectList")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_AddProject")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonshowProjectDialog(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_RemoveProject")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonremoveProject(evt);
            }
        });

        overwriteCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(overwriteCheckBox, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_OverwriteExisting")); // NOI18N
        overwriteCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(locationLabel)
                                    .add(folderLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(locationCB, 0, 327, Short.MAX_VALUE)
                                    .add(projectTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, folderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(projectLabel))
                        .add(7, 7, 7))
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(overwriteCheckBox)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(refLabel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLabel)
                    .add(projectTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(7, 7, 7)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(locationLabel)
                    .add(locationCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(folderLabel)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(folderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(browseButton)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(refLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(overwriteCheckBox))
        );

        projectLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "Project")); // NOI18N
        projectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_Project")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "Project")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_Project")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "Location")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_Location")); // NOI18N
        locationCB.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "Location")); // NOI18N
        locationCB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_Location")); // NOI18N
        folderLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "Folder")); // NOI18N
        folderLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_Folder")); // NOI18N
        folderTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "Folder")); // NOI18N
        folderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_Folder")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "Browser")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_BrowseFolder")); // NOI18N
        refLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "ProjectList")); // NOI18N
        refLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_ProjectList")); // NOI18N
        addButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "AddProject")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_AddProject")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "RemoveProject")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_RemoveProject")); // NOI18N
        overwriteCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "OverwriteExistingStubs")); // NOI18N
        overwriteCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_OverwriteExisting")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        Sources sources = ProjectUtils.getSources(project);
        FileObject fo = BrowseFolders.showDialog( sourceGroups, DataFolder.class,
                folderTextField.getText().replace( File.separatorChar, '/' ) );

        if ( fo != null) {
            stubRootFolder = fo;
            SourceGroup sg = SourceGroupSupport.findSourceGroupForFile(sourceGroups, stubRootFolder);
            locationCB.getModel().setSelectedItem(sg);
            String path = FileUtil.getRelativePath(sg.getRootFolder(), stubRootFolder);
            folderTextField.setText(path);
            fireChange();
        }
}//GEN-LAST:event_browseButtonActionPerformed

private void removeButtonremoveProject(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonremoveProject
    ListSelectionModel selectionModel =  projectList.getSelectionModel();
    DefaultListModel model = (DefaultListModel) projectList.getModel();
    ArrayList<ProjectInformation> toRemove = new ArrayList<ProjectInformation>();
    if (! selectionModel.isSelectionEmpty()) {
        for (int i=0; i<model.getSize(); i++) {
            if (selectionModel.isSelectedIndex(i)) {
                toRemove.add((ProjectInformation) model.elementAt(i));
            }
        }
        for (ProjectInformation pi : toRemove) {
            model.removeElement(pi);
        }
    }
    if (toRemove.size() > 0) {
        fireChange();
    }
}//GEN-LAST:event_removeButtonremoveProject

private void addButtonshowProjectDialog(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonshowProjectDialog
    // create and show project chooser
    JFileChooser chooser = ProjectChooser.projectChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setMultiSelectionEnabled(true);
    chooser.setDialogTitle(NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_ProjectChooserTitle")); // NOI18N
    chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
    String text = NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_SelectOK");
    chooser.setApproveButtonText(text); // NOI18N
    String mnemonic = NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "MNE_SelectOK");
    chooser.setApproveButtonMnemonic(mnemonic.charAt(0)); // NOI18N
    chooser.setPreferredSize( new Dimension( 650, 380 ) );
    int option = chooser.showOpenDialog(this);
    // Add project if project selected
    if(option == JFileChooser.APPROVE_OPTION) {
        boolean changed = addProject(chooser.getSelectedFiles());
        if (changed) {
            fireChange();
        }
    }
}//GEN-LAST:event_addButtonshowProjectDialog
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox locationCB;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JCheckBox overwriteCheckBox;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JList projectList;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JLabel refLabel;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
}

