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
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel;
import org.netbeans.modules.websvc.rest.codegen.model.State;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.spi.WebRestSupport;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Exceptions;
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
    private FileObject wadlFile;
    private boolean isProjectSelection = true;
    
    /** Creates new form ClientStubVisualPanel1 */
    public ClientStubsSetupPanelVisual(String name) {
        super.setName(name);
        initComponents();
        sourceButtons.add(projectRadioButton);
        sourceButtons.add(wadlRadioButton);
        projectList.addListSelectionListener(new ProjectListSelectionListener());
        projectRadioButton.setSelected(isProjectSelection);
        projectSelection(isProjectSelection);
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
            String folderName = Constants.REST_STUBS_DIR;
            folderTextField.setText(folderName);
            
        }
    }

    public void store(WizardDescriptor wizard) {
        DefaultListModel model = (DefaultListModel) projectList.getModel();
        Project[] projects = new Project[model.size()];
        for (int i=0; i < projects.length; i++) {
            projects[i] = ((ProjectInformation)model.get(i)).getProject();
        }
        wizard.putProperty(WizardProperties.PROJECT_SELECTION, isProjectSelection);
        wizard.putProperty(WizardProperties.PROJECTS_TO_STUB, projects);
        if(wadlFile != null)
            wizard.putProperty(WizardProperties.WADL_TO_STUB, wadlFile);
        wizard.putProperty(WizardProperties.OVERWRITE_EXISTING, overwriteCheckBox.isSelected());
        
        if (stubRootFolder == null && sourceGroups.length > 0) {

            SourceGroup sg = (SourceGroup) locationCB.getSelectedItem();
            stubRootFolder = sg.getRootFolder();
        }
        if (stubRootFolder != null) {
            wizard.putProperty(WizardProperties.STUB_ROOT_FOLDER, stubRootFolder);
        }
        String path = folderTextField.getText();
        if (path != null && path.trim().length() > 0) {
            path = folderTextField.getText();
        } else {
            path = Constants.REST_STUBS_DIR;
        }
        wizard.putProperty(WizardProperties.STUB_FOLDER_NAME, path);
    }

    public boolean valid(WizardDescriptor wizard) {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        if (restSupport == null || restSupport.getAntProjectHelper() == null) {
            AbstractPanel.setErrorMessage(wizard, "MSG_NotSupportedProjectType");
            return false;
        }
        if(isProjectSelection) {
            DefaultListModel model = (DefaultListModel) projectList.getModel();
            if (model.getSize() < 1) {
                AbstractPanel.setInfoMessage(wizard, "MSG_NoProjects");
                return false;
            }
        } else {
            if(wadlTextField.getText() == null || wadlTextField.getText().trim().equals("")) {
                AbstractPanel.setInfoMessage(wizard, "MSG_NoWadlFile");
                return false;
            } else {
                String fileName = wadlTextField.getText().trim();
                State state = validateWadlFile(fileName);
                if(state != State.VALID) {
                    AbstractPanel.setErrorMessage(wizard, "MSG_"+state.value());
                    return false;
                }
            }
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
                boolean reject = true;
                RestSupport restSupport = p.getLookup().lookup(RestSupport.class);
                if (restSupport != null && restSupport instanceof WebRestSupport) {
                    WebRestSupport webRestSupport = (WebRestSupport) restSupport;
                    if (webRestSupport.getApplicationPathFromDD() != null ||
                        webRestSupport.getRestApplications().size() > 0 ) {
                        reject = false;
                    }
                }
                if(reject) {
                    rejecteds.add(p);
                    continue;
                }
                ProjectInformation pInfo = ProjectUtils.getInformation(p);
                if(!listModel.contains(pInfo)) {
                    listModel.addElement(pInfo);
                    changed = true;
                }
            } catch(IOException ioe) {
                Exceptions.printStackTrace(ioe);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourceButtons = new javax.swing.ButtonGroup();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationCB = new javax.swing.JComboBox();
        folderLabel = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        projectList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        overwriteCheckBox = new javax.swing.JCheckBox();
        projectRadioButton = new javax.swing.JRadioButton();
        wadlRadioButton = new javax.swing.JRadioButton();
        wadlBrowseButton = new javax.swing.JButton();
        wadlTextField = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();

        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_Project")); // NOI18N

        projectTextField.setEditable(false);

        locationLabel.setLabelFor(locationCB);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_Location")); // NOI18N

        folderLabel.setLabelFor(folderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(folderLabel, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_Folder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_BrowseFolder")); // NOI18N
        browseButton.setToolTipText(org.openide.util.NbBundle.getBundle(ClientStubsSetupPanelVisual.class).getString("HINT_BrowseFolder")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

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
        projectList.setPreferredSize(new java.awt.Dimension(0, 200));
        jScrollPane1.setViewportView(projectList);
        projectList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "ProjectList")); // NOI18N
        projectList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_ProjectList")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_AddProject")); // NOI18N
        addButton.setToolTipText(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "HINT_CustomizerProjectReferences_AddProject")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonshowProjectDialog(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_RemoveProject")); // NOI18N
        removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "HINT_CustomizerProjectReferences_RemoveProject")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonremoveProject(evt);
            }
        });

        overwriteCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(overwriteCheckBox, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_OverwriteExisting")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(projectRadioButton, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_ProjectsToGenerateStubFor")); // NOI18N
        projectRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(ClientStubsSetupPanelVisual.class).getString("DESC_ProjectList")); // NOI18N
        projectRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(wadlRadioButton, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_WADLToGenerateStubFor")); // NOI18N
        wadlRadioButton.setToolTipText(org.openide.util.NbBundle.getBundle(ClientStubsSetupPanelVisual.class).getString("HINT_WADLToGenerateStubFor")); // NOI18N
        wadlRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wadlRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(wadlBrowseButton, org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_BrowseWadl")); // NOI18N
        wadlBrowseButton.setToolTipText(org.openide.util.NbBundle.getBundle(ClientStubsSetupPanelVisual.class).getString("HINT_BrowseWadl")); // NOI18N
        wadlBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wadlBrowseButtonActionPerformed(evt);
            }
        });

        wadlTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                wadlTextFieldFocusLost(evt);
            }
        });
        wadlTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                wadlTextFieldKeyTyped(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                wadlTextFieldKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(projectRadioButton, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wadlRadioButton, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(wadlTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE))
                        .addGap(4, 4, 4))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(folderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(projectLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(locationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                            .addComponent(locationCB, javax.swing.GroupLayout.Alignment.TRAILING, 0, 374, Short.MAX_VALUE)
                            .addComponent(folderTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE))
                        .addGap(6, 6, 6)))
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(browseButton)
                    .addComponent(addButton)
                    .addComponent(removeButton)
                    .addComponent(wadlBrowseButton)))
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(overwriteCheckBox)
                .addContainerGap(376, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, browseButton, removeButton, wadlBrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectLabel))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationCB, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(browseButton)
                    .addComponent(folderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(folderLabel))
                .addGap(8, 8, 8)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wadlRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(wadlBrowseButton)
                            .addComponent(wadlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(overwriteCheckBox)
                .addContainerGap())
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
        addButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "AddProject")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_AddProject")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "RemoveProject")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_RemoveProject")); // NOI18N
        overwriteCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "OverwriteExistingStubs")); // NOI18N
        overwriteCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "DESC_OverwriteExisting")); // NOI18N
        wadlTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(ClientStubsSetupPanelVisual.class).getString("HINT_WADLToGenerateStubFor")); // NOI18N
        wadlTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ClientStubsSetupPanelVisual.class).getString("HINT_WADLToGenerateStubFor")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(ClientStubsSetupPanelVisual.class).getString("Templates/WebServices/RestClientStubs")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ClientStubsSetupPanelVisual.class).getString("Templates/WebServices/RestClientStubs")); // NOI18N
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


private void projectRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectRadioButtonActionPerformed
    isProjectSelection = true;
    projectSelection(isProjectSelection);
    fireChange();
}//GEN-LAST:event_projectRadioButtonActionPerformed

private void projectSelection(boolean select) {
    addButton.setEnabled(select);
    wadlBrowseButton.setEnabled(!select);
}

private void wadlRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wadlRadioButtonActionPerformed
    isProjectSelection = false;
    projectSelection(isProjectSelection);
    fireChange();
}//GEN-LAST:event_wadlRadioButtonActionPerformed

private void wadlBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wadlBrowseButtonActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setDialogTitle(NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_WadlChooserTitle")); // NOI18N
    chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
    String text = NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "LBL_SelectOK");
    chooser.setApproveButtonText(text); // NOI18N
    String mnemonic = NbBundle.getMessage(ClientStubsSetupPanelVisual.class, "MNE_SelectOK");
    chooser.setApproveButtonMnemonic(mnemonic.charAt(0)); // NOI18N
    chooser.setPreferredSize( new Dimension( 650, 380 ) );
    int option = chooser.showOpenDialog(this);
    if(option == JFileChooser.APPROVE_OPTION) {
        File f = chooser.getSelectedFile();
        wadlTextField.setText(f.getAbsolutePath());
        fireChange();
    }
}//GEN-LAST:event_wadlBrowseButtonActionPerformed

private void wadlTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wadlTextFieldFocusLost

    fireChange();
}//GEN-LAST:event_wadlTextFieldFocusLost

private void wadlTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_wadlTextFieldKeyTyped
    fireChange();
}//GEN-LAST:event_wadlTextFieldKeyTyped

private void wadlTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_wadlTextFieldKeyReleased
    fireChange();
}//GEN-LAST:event_wadlTextFieldKeyReleased
    
    private State validateWadlFile(final String fileName) {
        File f = new File(fileName);
        State state = State.INVALID;
        try {
            if (f.isFile()) {
                FileObject newWadl = FileUtil.toFileObject(f);
                if (newWadl != null) {
                    state = new ClientStubModel().createModel(newWadl).validate();
                    if (state == State.VALID) {
                        wadlFile = newWadl;
                    }
                }
            }
        } catch (Exception ex) {
        }
        return state;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JComboBox locationCB;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JCheckBox overwriteCheckBox;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JList projectList;
    private javax.swing.JRadioButton projectRadioButton;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.ButtonGroup sourceButtons;
    private javax.swing.JButton wadlBrowseButton;
    private javax.swing.JRadioButton wadlRadioButton;
    private javax.swing.JTextField wadlTextField;
    // End of variables declaration//GEN-END:variables
    
}

