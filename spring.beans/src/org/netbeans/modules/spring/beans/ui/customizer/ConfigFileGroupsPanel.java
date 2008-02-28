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
import org.netbeans.modules.spring.beans.ui.customizer.ConfigFileGroupUIs.FileDisplayName;
import org.netbeans.modules.spring.util.ConfigFiles;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class ConfigFileGroupsPanel extends javax.swing.JPanel {

    private final Project project;
    private final List<ConfigFileGroup> groups;
    private final File basedir;
    private final FileDisplayName fileDisplayName;

    private ConfigFileGroup currentGroup;
    private int currentGroupIndex;

    private List<File> availableFiles;

    public ConfigFileGroupsPanel(Project project, List<ConfigFileGroup> groups) {
        this.project = project;
        this.groups = groups;
        basedir = FileUtil.toFile(project.getProjectDirectory());
        if (basedir == null) {
            throw new IllegalStateException("The directory of project " + project + " is null");
        }
        fileDisplayName = new RelativeDisplayName();
        initComponents();
        ConfigFileGroupUIs.setupGroupsList(groupsList);
        ConfigFileGroupUIs.setupGroupFilesList(groupFilesList, fileDisplayName);
        ConfigFileGroupUIs.connect(groups, groupsList);
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

    public List<ConfigFileGroup> getConfigFileGroups() {
        List<ConfigFileGroup> result = new ArrayList<ConfigFileGroup>(groups.size());
        result.addAll(groups);
        return result;
    }

    private void groupsListSelectionChanged() {
        currentGroupIndex = groupsList.getSelectedIndex();
        if (currentGroupIndex != -1) {
            currentGroup = (ConfigFileGroup)groupsList.getModel().getElementAt(currentGroupIndex);
            ConfigFileGroupUIs.connect(currentGroup, groupFilesList);
            removeGroupButton.setEnabled(true);
            addFileButton.setEnabled(true);
            detectButton.setEnabled(true);
            String currentGroupName = ConfigFileGroupUIs.getGroupName(currentGroup);
            groupFilesLabel.setText(NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_ConfigFilesInGroup", currentGroupName));
            groupFilesList.setSelectedIndices(new int[0]);
        } else {
            currentGroup = null;
            ConfigFileGroupUIs.disconnect(groupFilesList);
            removeGroupButton.setEnabled(false);
            addFileButton.setEnabled(false);
            detectButton.setEnabled(false);
            groupFilesLabel.setText(NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_ConfigFiles"));
        }
    }

    private void groupFilesListSelectionChanged() {
        boolean selected = groupFilesList.getSelectedIndex() != -1;
        removeFileButton.setEnabled(selected);
    }

    private void replaceCurrentGroup(ConfigFileGroup newGroup) {
        groups.set(currentGroupIndex, newGroup);
        int oldGroupIndex = currentGroupIndex;
        ConfigFileGroupUIs.connect(groups, groupsList);
        groupsList.setSelectedIndex(oldGroupIndex);
    }

    private void addFiles(List<File> newFiles) {
        List<File> files = currentGroup.getFiles();
        files.addAll(newFiles);
        ConfigFileGroup newGroup = ConfigFileGroup.create(currentGroup.getName(), files);
        replaceCurrentGroup(newGroup);
        groupFilesList.setSelectedIndex(groupFilesList.getModel().getSize() - 1);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        groupsLabel = new javax.swing.JLabel();
        groupsScrollPane = new javax.swing.JScrollPane();
        groupsList = new javax.swing.JList();
        addGroupButton = new javax.swing.JButton();
        editGroupButton = new javax.swing.JButton();
        removeGroupButton = new javax.swing.JButton();
        groupFilesLabel = new javax.swing.JLabel();
        groupFilesScrollPane = new javax.swing.JScrollPane();
        groupFilesList = new javax.swing.JList();
        addFileButton = new javax.swing.JButton();
        removeFileButton = new javax.swing.JButton();
        detectButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(groupsLabel, org.openide.util.NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_ConfigFileGroups")); // NOI18N
        groupsLabel.setFocusable(false);

        groupsScrollPane.setViewportView(groupsList);

        org.openide.awt.Mnemonics.setLocalizedText(addGroupButton, org.openide.util.NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_AddGroup")); // NOI18N
        addGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGroupButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editGroupButton, org.openide.util.NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_EditGroup")); // NOI18N
        editGroupButton.setEnabled(false);
        editGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editGroupButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeGroupButton, org.openide.util.NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_RemoveGroup")); // NOI18N
        removeGroupButton.setEnabled(false);
        removeGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeGroupButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(groupFilesLabel, org.openide.util.NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_ConfigFiles")); // NOI18N
        groupFilesLabel.setFocusable(false);

        groupFilesScrollPane.setViewportView(groupFilesList);

        org.openide.awt.Mnemonics.setLocalizedText(addFileButton, org.openide.util.NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_AddFile")); // NOI18N
        addFileButton.setEnabled(false);
        addFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeFileButton, org.openide.util.NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_RemoveFile")); // NOI18N
        removeFileButton.setEnabled(false);
        removeFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(detectButton, org.openide.util.NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_Autodetect")); // NOI18N
        detectButton.setEnabled(false);
        detectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detectButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(groupsLabel)
            .add(layout.createSequentialGroup()
                .add(groupFilesLabel)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(groupFilesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, groupsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(detectButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(removeGroupButton, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(editGroupButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, addFileButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, removeFileButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, addGroupButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(groupsLabel)
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addGroupButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editGroupButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeGroupButton))
                    .add(groupsScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 155, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(groupFilesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addFileButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeFileButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(detectButton)
                        .addContainerGap())
                    .add(groupFilesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

private void addGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGroupButtonActionPerformed
        InputLine input = new InputLine(
                NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_GroupName"),
                NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_NewConfigFileGroup"));
        DialogDisplayer.getDefault().notify(input);
        if (input.getValue() == NotifyDescriptor.OK_OPTION) {
            ConfigFileGroup newGroup = ConfigFileGroup.create(input.getInputText(), Collections.<File>emptyList());
            groups.add(newGroup);
            ConfigFileGroupUIs.connect(groups, groupsList);
            groupsList.setSelectedIndex(groupsList.getModel().getSize() - 1);
        }
}//GEN-LAST:event_addGroupButtonActionPerformed

private void addFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_ChooseFile"));
        chooser.setCurrentDirectory(basedir);
        int option = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(groupFilesList));
        if (option == JFileChooser.APPROVE_OPTION) {
            addFiles(Collections.singletonList(chooser.getSelectedFile()));
        }
}//GEN-LAST:event_addFileButtonActionPerformed

private void removeGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeGroupButtonActionPerformed
        groups.remove(currentGroupIndex);
        int oldGroupIndex = currentGroupIndex;
        ConfigFileGroupUIs.connect(groups, groupsList);
        while (oldGroupIndex > groupsList.getModel().getSize() - 1) {
            oldGroupIndex--;
        }
        groupsList.setSelectedIndex(oldGroupIndex);
}//GEN-LAST:event_removeGroupButtonActionPerformed

private void removeFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFileButtonActionPerformed
        int oldFileIndex = groupFilesList.getSelectedIndex();
        List<File> files = currentGroup.getFiles();
        files.remove(oldFileIndex);
        ConfigFileGroup newGroup = ConfigFileGroup.create(currentGroup.getName(), files);
        replaceCurrentGroup(newGroup);
        while (oldFileIndex > groupsList.getModel().getSize() - 1) {
            oldFileIndex--;
        }
        groupFilesList.setSelectedIndex(oldFileIndex);
}//GEN-LAST:event_removeFileButtonActionPerformed

private void editGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editGroupButtonActionPerformed
        InputLine input = new InputLine(
                NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_GroupName"),
                NbBundle.getMessage(ConfigFileGroupsPanel.class, "LBL_EditConfigFileGroup"));
        String currentName = currentGroup.getName();
        if (currentName != null) {
            input.setInputText(currentName);
        }
        DialogDisplayer.getDefault().notify(input);
        if (input.getValue() == NotifyDescriptor.OK_OPTION) {
            ConfigFileGroup newGroup = ConfigFileGroup.create(input.getInputText(), currentGroup.getFiles());
            replaceCurrentGroup(newGroup);
        }
}//GEN-LAST:event_editGroupButtonActionPerformed

private void detectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detectButtonActionPerformed
        Set<File> alreadySelectedFiles = new HashSet<File>(currentGroup.getFiles().size());
        alreadySelectedFiles.addAll(currentGroup.getFiles());
        ConfigFileDetectPanel panel;
        if (availableFiles != null) {
            panel = ConfigFileDetectPanel.create(availableFiles, alreadySelectedFiles, fileDisplayName);
        } else {
            panel = ConfigFileDetectPanel.create(project, alreadySelectedFiles, fileDisplayName);
        }
        if (panel.open()) {
            List<File> files = panel.getAvailableFiles();
            if (files != null) {
                this.availableFiles = files;
            }
            addFiles(panel.getSelectedFiles());
        }
}//GEN-LAST:event_detectButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFileButton;
    private javax.swing.JButton addGroupButton;
    private javax.swing.JButton detectButton;
    private javax.swing.JButton editGroupButton;
    private javax.swing.JLabel groupFilesLabel;
    private javax.swing.JList groupFilesList;
    private javax.swing.JScrollPane groupFilesScrollPane;
    private javax.swing.JLabel groupsLabel;
    private javax.swing.JList groupsList;
    private javax.swing.JScrollPane groupsScrollPane;
    private javax.swing.JButton removeFileButton;
    private javax.swing.JButton removeGroupButton;
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
