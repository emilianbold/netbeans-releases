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

package org.netbeans.modules.spring.beans.ui.customizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.beans.ui.customizer.ConfigFilesUIs.FileDisplayName;
import org.netbeans.modules.spring.util.ConfigFiles;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SpringCustomizerPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private final Project project;
    private final List<File> files;
    private final List<ConfigFileGroup> groups;
    private final File basedir;
    private final FileDisplayName fileDisplayName;

    private ConfigFileGroup currentGroup;
    private int currentGroupIndex;

    private List<File> detectedFiles;

    public SpringCustomizerPanel(Project project, List<File> files, List<ConfigFileGroup> groups) {
        this.project = project;
        this.files = files;
        this.groups = groups;
        basedir = FileUtil.toFile(project.getProjectDirectory());
        if (basedir == null) {
            throw new IllegalStateException("The directory of project " + project + " is null");
        }
        fileDisplayName = new RelativeDisplayName();
        initComponents();
        ConfigFilesUIs.setupFilesList(filesList, fileDisplayName);
        ConfigFilesUIs.setupGroupsList(groupsList);
        ConfigFilesUIs.setupFilesList(groupFilesList, fileDisplayName);
        ConfigFilesUIs.connectFilesList(files, filesList);
        ConfigFilesUIs.connectGroupsList(groups, groupsList);
        filesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                filesListSelectionChanged();
            }
        });
        groupsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                groupsListSelectionChanged();
            }
        });
        groupFilesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                groupFilesListSelectionChanged();
            }
        });
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(SpringCustomizerPanel.class);
    }

    public List<File> getConfigFiles() {
        return files;
    }

    public List<ConfigFileGroup> getConfigFileGroups() {
        return groups;
    }

    private void filesListSelectionChanged() {
        boolean selected = filesList.getSelectedIndex() != -1;
        removeFileButton.setEnabled(selected);
    }

    private void groupsListSelectionChanged() {
        currentGroupIndex = groupsList.getSelectedIndex();
        if (currentGroupIndex != -1) {
            currentGroup = (ConfigFileGroup)groupsList.getModel().getElementAt(currentGroupIndex);
            ConfigFilesUIs.connectFilesList(currentGroup.getFiles(), groupFilesList);
            editGroupButton.setEnabled(true);
            removeGroupButton.setEnabled(true);
            addGroupFilesButton.setEnabled(true);
            detectFilesButton.setEnabled(true);
            String currentGroupName = ConfigFilesUIs.getGroupName(currentGroup);
            groupFilesLabel.setText(NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFilesInGroup", currentGroupName));
            groupFilesList.setSelectedIndices(new int[0]);
        } else {
            currentGroup = null;
            ConfigFilesUIs.disconnect(groupFilesList);
            editGroupButton.setEnabled(false);
            removeGroupButton.setEnabled(false);
            addGroupFilesButton.setEnabled(false);
            detectFilesButton.setEnabled(false);
            groupFilesLabel.setText(NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFiles"));
        }
    }

    private void groupFilesListSelectionChanged() {
        boolean selected = groupFilesList.getSelectedIndex() != -1;
        removeGroupFileButton.setEnabled(selected);
    }

    private void replaceCurrentGroup(ConfigFileGroup newGroup) {
        groups.set(currentGroupIndex, newGroup);
        int selIndex = currentGroupIndex;
        ConfigFilesUIs.connectGroupsList(groups, groupsList);
        groupsList.setSelectedIndex(selIndex);
    }

    private void addFiles(List<File> newFiles) {
        files.addAll(newFiles);
        ConfigFilesUIs.connectFilesList(files, filesList);
        filesList.setSelectedIndex(filesList.getModel().getSize() - 1);
    }

    private void removeFiles() {
        List<File> removedFiles = new ArrayList<File>();
        int[] indices = filesList.getSelectedIndices();
        for (int i = 0; i < indices.length; i++) {
            removedFiles.add(files.remove(indices[i] - i));
        }
        for (int i = 0; i < groups.size(); i++) {
            ConfigFileGroup group = groups.get(i);
            List<File> groupFiles = group.getFiles();
            if (groupFiles.removeAll(removedFiles)) {
                ConfigFileGroup newGroup = ConfigFileGroup.create(group.getName(), groupFiles);
                if (currentGroup == group) {
                    replaceCurrentGroup(newGroup);
                } else {
                    groups.set(i, newGroup);
                }
            }
        }
        ConfigFilesUIs.connectFilesList(files, filesList);
        int selIndex = Math.min(indices[0], filesList.getModel().getSize() - 1);
        filesList.setSelectedIndex(selIndex);
    }

    private void addGroup(ConfigFileGroup group) {
        groups.add(group);
        ConfigFilesUIs.connectGroupsList(groups, groupsList);
        groupsList.setSelectedIndex(groupsList.getModel().getSize() - 1);
    }

    private void removeGroups() {
        int[] indices = groupsList.getSelectedIndices();
        for (int i = 0; i < indices.length; i++) {
            groups.remove(indices[i] - i);
        }
        ConfigFilesUIs.connectGroupsList(groups, groupsList);
        int selIndex = Math.min(indices[0], groupsList.getModel().getSize() - 1);
        groupsList.setSelectedIndex(selIndex);
    }

    private void addFilesToCurrentGroup(List<File> newFiles) {
        List<File> groupFiles = currentGroup.getFiles();
        groupFiles.addAll(newFiles);
        ConfigFileGroup newGroup = ConfigFileGroup.create(currentGroup.getName(), groupFiles);
        replaceCurrentGroup(newGroup);
        groupFilesList.setSelectedIndex(groupFilesList.getModel().getSize() - 1);
    }

    private void removeFilesFromCurrentGroup() {
        List<File> groupFiles = currentGroup.getFiles();
        int[] indices = groupFilesList.getSelectedIndices();
        for (int i = 0; i < indices.length; i++) {
            groupFiles.remove(indices[i] - i);
        }
        replaceCurrentGroup(ConfigFileGroup.create(currentGroup.getName(), groupFiles));
        int selIndex = Math.min(indices[0], groupFilesList.getModel().getSize() - 1);
        groupFilesList.setSelectedIndex(selIndex);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        springConfigPane = new javax.swing.JTabbedPane();
        configFilesPanel = new javax.swing.JPanel();
        filesLabel = new javax.swing.JLabel();
        filesScrollPane = new javax.swing.JScrollPane();
        filesList = new javax.swing.JList();
        addFileButton = new javax.swing.JButton();
        removeFileButton = new javax.swing.JButton();
        detectFilesButton = new javax.swing.JButton();
        configFileGroupsPanel = new javax.swing.JPanel();
        groupsLabel = new javax.swing.JLabel();
        groupsScrollPane = new javax.swing.JScrollPane();
        groupsList = new javax.swing.JList();
        addGroupButton = new javax.swing.JButton();
        editGroupButton = new javax.swing.JButton();
        removeGroupButton = new javax.swing.JButton();
        groupFilesLabel = new javax.swing.JLabel();
        groupFilesScrollPane = new javax.swing.JScrollPane();
        groupFilesList = new javax.swing.JList();
        addGroupFilesButton = new javax.swing.JButton();
        removeGroupFileButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(filesLabel, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFiles")); // NOI18N

        filesScrollPane.setViewportView(filesList);

        org.openide.awt.Mnemonics.setLocalizedText(addFileButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_AddFile")); // NOI18N
        addFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeFileButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_RemoveFile")); // NOI18N
        removeFileButton.setEnabled(false);
        removeFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(detectFilesButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_DetectFiles")); // NOI18N
        detectFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detectFilesButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout configFilesPanelLayout = new org.jdesktop.layout.GroupLayout(configFilesPanel);
        configFilesPanel.setLayout(configFilesPanelLayout);
        configFilesPanelLayout.setHorizontalGroup(
            configFilesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(configFilesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(configFilesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filesLabel)
                    .add(filesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configFilesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(removeFileButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(detectFilesButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(addFileButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        configFilesPanelLayout.linkSize(new java.awt.Component[] {addFileButton, detectFilesButton, removeFileButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        configFilesPanelLayout.setVerticalGroup(
            configFilesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(configFilesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(filesLabel)
                .add(6, 6, 6)
                .add(configFilesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(configFilesPanelLayout.createSequentialGroup()
                        .add(addFileButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(detectFilesButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeFileButton))
                    .add(filesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE))
                .addContainerGap())
        );

        springConfigPane.addTab(org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFilesTitle"), configFilesPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(groupsLabel, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFileGroups")); // NOI18N
        groupsLabel.setFocusable(false);

        groupsScrollPane.setViewportView(groupsList);

        org.openide.awt.Mnemonics.setLocalizedText(addGroupButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_AddGroup")); // NOI18N
        addGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGroupButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editGroupButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_EditGroup")); // NOI18N
        editGroupButton.setEnabled(false);
        editGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editGroupButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeGroupButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_RemoveGroup")); // NOI18N
        removeGroupButton.setEnabled(false);
        removeGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeGroupButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(groupFilesLabel, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFiles")); // NOI18N
        groupFilesLabel.setFocusable(false);

        groupFilesScrollPane.setViewportView(groupFilesList);

        org.openide.awt.Mnemonics.setLocalizedText(addGroupFilesButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_AddFiles")); // NOI18N
        addGroupFilesButton.setEnabled(false);
        addGroupFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGroupFilesButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeGroupFileButton, org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_RemoveFile")); // NOI18N
        removeGroupFileButton.setEnabled(false);
        removeGroupFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeGroupFileButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout configFileGroupsPanelLayout = new org.jdesktop.layout.GroupLayout(configFileGroupsPanel);
        configFileGroupsPanel.setLayout(configFileGroupsPanelLayout);
        configFileGroupsPanelLayout.setHorizontalGroup(
            configFileGroupsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(configFileGroupsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(configFileGroupsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(groupFilesLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, configFileGroupsPanelLayout.createSequentialGroup()
                        .add(configFileGroupsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, groupFilesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, groupsLabel)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, groupsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configFileGroupsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(removeGroupButton, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(editGroupButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, addGroupButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, addGroupFilesButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(removeGroupFileButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        configFileGroupsPanelLayout.linkSize(new java.awt.Component[] {addGroupButton, addGroupFilesButton, editGroupButton, removeGroupButton, removeGroupFileButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        configFileGroupsPanelLayout.setVerticalGroup(
            configFileGroupsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(configFileGroupsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(groupsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configFileGroupsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(configFileGroupsPanelLayout.createSequentialGroup()
                        .add(addGroupButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editGroupButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeGroupButton))
                    .add(configFileGroupsPanelLayout.createSequentialGroup()
                        .add(groupsScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(groupFilesLabel)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configFileGroupsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(groupFilesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                    .add(configFileGroupsPanelLayout.createSequentialGroup()
                        .add(addGroupFilesButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeGroupFileButton)))
                .addContainerGap())
        );

        springConfigPane.addTab(org.openide.util.NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ConfigFileGroupsTitle"), configFileGroupsPanel); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(springConfigPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(springConfigPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

private void addFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_ChooseFile"));
        chooser.setCurrentDirectory(basedir);
        int option = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(groupFilesList));
        if (option == JFileChooser.APPROVE_OPTION) {
            File newFile = chooser.getSelectedFile();
            if (files.contains(newFile)) {
                String message = NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_FileAlreadyAdded");
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            } else {
                addFiles(Collections.singletonList(newFile));
            }
        }
}//GEN-LAST:event_addFileButtonActionPerformed

private void removeFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFileButtonActionPerformed
        removeFiles();
}//GEN-LAST:event_removeFileButtonActionPerformed

private void detectFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detectFilesButtonActionPerformed
        Set<File> alreadySelectedFiles = new HashSet<File>(files);
        SelectConfigFilesPanel panel;
        if (detectedFiles != null) {
            panel = SelectConfigFilesPanel.create(detectedFiles, alreadySelectedFiles, fileDisplayName);
        } else {
            panel = SelectConfigFilesPanel.create(project, alreadySelectedFiles, fileDisplayName);
        }
        if (panel.open()) {
            List<File> availableFiles = panel.getAvailableFiles();
            if (availableFiles != null) {
                this.detectedFiles = availableFiles;
            }
            addFiles(panel.getSelectedFiles());
        }
}//GEN-LAST:event_detectFilesButtonActionPerformed

private void addGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGroupButtonActionPerformed
        InputLine input = new InputLine(
                NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_GroupName"),
                NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_NewConfigFileGroup"));
        DialogDisplayer.getDefault().notify(input);
        if (input.getValue() == NotifyDescriptor.OK_OPTION) {
            addGroup(ConfigFileGroup.create(input.getInputText(), Collections.<File>emptyList()));
        }
}//GEN-LAST:event_addGroupButtonActionPerformed

private void editGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editGroupButtonActionPerformed
        InputLine input = new InputLine(
                NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_GroupName"),
                NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_EditConfigFileGroup"));
        String currentName = currentGroup.getName();
        if (currentName != null) {
            input.setInputText(currentName);
        }
        DialogDisplayer.getDefault().notify(input);
        if (input.getValue() == NotifyDescriptor.OK_OPTION) {
            replaceCurrentGroup(ConfigFileGroup.create(input.getInputText(), currentGroup.getFiles()));
        }
}//GEN-LAST:event_editGroupButtonActionPerformed

private void removeGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeGroupButtonActionPerformed
        removeGroups();
}//GEN-LAST:event_removeGroupButtonActionPerformed

private void addGroupFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGroupFilesButtonActionPerformed
        if (files.size() == 0) {
            String message = NbBundle.getMessage(SpringCustomizerPanel.class, "LBL_NoFilesAdded");
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            return;
        }
        Set<File> alreadySelectedFiles = new HashSet<File>(groups.get(currentGroupIndex).getFiles());
        SelectConfigFilesPanel panel = SelectConfigFilesPanel.create(files, alreadySelectedFiles, fileDisplayName);
        if (panel.open()) {
            addFilesToCurrentGroup(panel.getSelectedFiles());
        }
}//GEN-LAST:event_addGroupFilesButtonActionPerformed

private void removeGroupFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeGroupFileButtonActionPerformed
        removeFilesFromCurrentGroup();
}//GEN-LAST:event_removeGroupFileButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFileButton;
    private javax.swing.JButton addGroupButton;
    private javax.swing.JButton addGroupFilesButton;
    private javax.swing.JPanel configFileGroupsPanel;
    private javax.swing.JPanel configFilesPanel;
    private javax.swing.JButton detectFilesButton;
    private javax.swing.JButton editGroupButton;
    private javax.swing.JLabel filesLabel;
    private javax.swing.JList filesList;
    private javax.swing.JScrollPane filesScrollPane;
    private javax.swing.JLabel groupFilesLabel;
    private javax.swing.JList groupFilesList;
    private javax.swing.JScrollPane groupFilesScrollPane;
    private javax.swing.JLabel groupsLabel;
    private javax.swing.JList groupsList;
    private javax.swing.JScrollPane groupsScrollPane;
    private javax.swing.JButton removeFileButton;
    private javax.swing.JButton removeGroupButton;
    private javax.swing.JButton removeGroupFileButton;
    private javax.swing.JTabbedPane springConfigPane;
    // End of variables declaration//GEN-END:variables

    private final class RelativeDisplayName implements FileDisplayName {

        private Map<File, String> abs2Rel = new HashMap<File, String>();

        public String getDisplayName(File absolute) {
            String relative = abs2Rel.get(absolute);
            if (relative == null) {
                relative = ConfigFiles.getRelativePath(basedir, absolute);
                abs2Rel.put(absolute, relative);
            }
            return relative;
        }
    }
}
