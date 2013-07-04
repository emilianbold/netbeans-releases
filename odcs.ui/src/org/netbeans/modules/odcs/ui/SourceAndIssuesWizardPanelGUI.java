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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * SourceAndIssuesWizardPanelGUI.java
 *
 * Created on Feb 6, 2009, 11:17:49 AM
 */

package org.netbeans.modules.odcs.ui;

import com.tasktop.c2c.server.scm.domain.ScmType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.queries.VersioningQuery;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import static org.netbeans.modules.odcs.ui.Bundle.*;
import org.netbeans.modules.odcs.ui.NewProjectWizardIterator.SharedItem;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Milan Kubec
 */
public class SourceAndIssuesWizardPanelGUI extends javax.swing.JPanel {

    private WizardDescriptor settings;

    private SourceAndIssuesWizardPanel panel;

    // XXX maybe move to bundle
    @Messages("SourceAndIssuesWizardPanelGUI.GitOnKenai=Git (on {0})")
    private String getGitRepoItem() {
        return SourceAndIssuesWizardPanelGUI_GitOnKenai(panel.getServer().getDisplayName());
    }

    private static final int PANEL_HEIGHT = 110;

    private boolean localFolderPathEdited = false;
    private final SharedItemsListModel itemsToShareModel;

    private final List<SharedItem> itemsToShare = new ArrayList<SharedItem>(1);

    /** Creates new form SourceAndIssuesWizardPanelGUI */
    public SourceAndIssuesWizardPanelGUI(SourceAndIssuesWizardPanel pnl) {

        panel = pnl;
        initComponents();

        // hack to show the UI still the same way
        spacerPanel.setPreferredSize(localFolderBrowseButton.getPreferredSize());

        DocumentListener firingDocListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                panel.fireChangeEvent();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                panel.fireChangeEvent();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                panel.fireChangeEvent();
            }
        };

        localFolderTextField.getDocument().addDocumentListener(firingDocListener);

        List<SharedItem> initalItems = panel.getInitialItems();

        setupServicesListModels(initalItems.isEmpty());

        itemsToShare.addAll(initalItems);
        itemsToShareModel = new SharedItemsListModel();
        foldersToShareList.setModel(itemsToShareModel);
    }

    @Override
    @Messages("SourceAndIssuesWizardPanelGUI.panelName=Source Code and Issues")
    public String getName() {
        return SourceAndIssuesWizardPanelGUI_panelName(); // NOI18N
    }

    private void setupServicesListModels(final boolean isEmptyKenaiProject) {
        final DefaultComboBoxModel repoModel = new DefaultComboBoxModel();
        repoModel.addElement(getGitRepoItem());
        repoComboBox.setModel(repoModel);
        repoComboBox.setEnabled(false);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        sourceCodeLabel = new JLabel();
        repoComboBox = new JComboBox();
        scSeparator = new JSeparator();
        localRepoFolderLabel = new JLabel();
        localFolderTextField = new JTextField();
        localFolderBrowseButton = new JButton();
        spacerPanel = new JPanel();
        foldersToShareLabel = new JLabel();
        jScrollPane1 = new JScrollPane();
        foldersToShareList = new JList();
        addProjectButton = new JButton();
        addFolderButton = new JButton();
        removeButton = new JButton();

        setLayout(new GridBagLayout());

        sourceCodeLabel.setLabelFor(repoComboBox);
        Mnemonics.setLocalizedText(sourceCodeLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.sourceCodeLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 4, 0);
        add(sourceCodeLabel, gridBagConstraints);
        sourceCodeLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.sourceCodeLabel.AccessibleContext.accessibleDescription")); // NOI18N

        repoComboBox.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 4, 4, 0);
        add(repoComboBox, gridBagConstraints);
        repoComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoComboBox.AccessibleContext.accessibleName")); // NOI18N
        repoComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 4, 0);
        add(scSeparator, gridBagConstraints);

        localRepoFolderLabel.setLabelFor(localFolderTextField);
        Mnemonics.setLocalizedText(localRepoFolderLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localRepoFolderLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        add(localRepoFolderLabel, gridBagConstraints);
        localRepoFolderLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localRepoFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N

        localFolderTextField.setText(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localFolderTextField.text")); // NOI18N
        localFolderTextField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                localFolderTextFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 0, 0);
        add(localFolderTextField, gridBagConstraints);
        localFolderTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localFolderTextField.AccessibleContext.accessibleName")); // NOI18N
        localFolderTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(localFolderBrowseButton, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localFolderBrowseButton.text")); // NOI18N
        localFolderBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                localFolderBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 4, 0, 0);
        add(localFolderBrowseButton, gridBagConstraints);
        localFolderBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localFolderBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(spacerPanel, gridBagConstraints);

        foldersToShareLabel.setLabelFor(foldersToShareList);
        Mnemonics.setLocalizedText(foldersToShareLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.foldersToShareLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        add(foldersToShareLabel, gridBagConstraints);

        jScrollPane1.setViewportView(foldersToShareList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        Mnemonics.setLocalizedText(addProjectButton, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.addProjectButton.text")); // NOI18N
        addProjectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addProjectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(addProjectButton, gridBagConstraints);
        addProjectButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.addProjectButton.AccessibleContext.accessibleName")); // NOI18N
        addProjectButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.addProjectButton.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(addFolderButton, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.addFolderButton.text")); // NOI18N
        addFolderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addFolderButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(4, 4, 0, 0);
        add(addFolderButton, gridBagConstraints);
        addFolderButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.addFolderButton.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.removeButton.text")); // NOI18N
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.removeButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void localFolderBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_localFolderBrowseButtonActionPerformed

        JFileChooser chooser = new JFileChooser();
        File uFile = new File(localFolderTextField.getText());
        if (uFile.exists()) {
            chooser.setCurrentDirectory(FileUtil.normalizeFile(uFile));
        }
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selFile = chooser.getSelectedFile();
            localFolderTextField.setText(selFile.getAbsolutePath());
            localFolderPathEdited = true;
        }

        panel.fireChangeEvent();
        
}//GEN-LAST:event_localFolderBrowseButtonActionPerformed

    private void localFolderTextFieldKeyTyped(KeyEvent evt) {//GEN-FIRST:event_localFolderTextFieldKeyTyped
        localFolderPathEdited = true;
    }//GEN-LAST:event_localFolderTextFieldKeyTyped

    private void addProjectButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addProjectButtonActionPerformed
        ProjectServices projects = Lookup.getDefault().lookup(ProjectServices.class);
        choose(projects.chooseProjects(null));
    }//GEN-LAST:event_addProjectButtonActionPerformed

    private void removeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int [] selection = foldersToShareList.getSelectedIndices();
        for (int i = selection.length - 1; i >=0; i--) {
            itemsToShare.remove(selection[i]);
        }
        foldersToShareList.clearSelection();
        itemsToShareModel.fireContentsChanged();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addFolderButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addFolderButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            choose(chooser.getSelectedFiles());
        }
    }//GEN-LAST:event_addFolderButtonActionPerformed

    private void choose(File[] selFiles) {
        outter: for (File file : selFiles) {
            for (SharedItem item : itemsToShare) {
                if (item.getRoot().equals(file)) {
                    continue outter;
                }
            }
            if (!VersioningQuery.isManaged(org.openide.util.Utilities.toURI(FileUtil.normalizeFile(file)))) { 
                SharedItem item = new SharedItem(file);
                itemsToShare.add(item);
            }
        }
        itemsToShareModel.fireContentsChanged();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton addFolderButton;
    private JButton addProjectButton;
    private JLabel foldersToShareLabel;
    private JList foldersToShareList;
    private JScrollPane jScrollPane1;
    private JButton localFolderBrowseButton;
    private JTextField localFolderTextField;
    private JLabel localRepoFolderLabel;
    private JButton removeButton;
    private JComboBox repoComboBox;
    private JSeparator scSeparator;
    private JLabel sourceCodeLabel;
    private JPanel spacerPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify() {
        super.addNotify();
        panel.fireChangeEvent();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        this.putClientProperty(NewProjectWizardIterator.PROP_EXC_ERR_MSG, null);
    }

    public boolean valid() {

        String message = checkForErrors();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        }

        message = checkForWarnings();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);
        }

        message = checkForInfos();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        }

        return true;

    }

    private String checkForErrors() {
        String excErrMsg = (String) this.getClientProperty(NewProjectWizardIterator.PROP_EXC_ERR_MSG);
        if (excErrMsg != null) {
            return excErrMsg;
        }
        // invalid repo name
        // invalid local repo path
        // invalid ext repo URL
        // invalid ext issues URL
        return null;
    }

    private String checkForWarnings() {
        // No warnings so far
        return null;
    }

    @Messages("SourceAndIssuesWizardPanelGUI.LocalRepoRequired=Local repository folder path is required")
    private String checkForInfos() {
        String localRepoPath = getRepoLocal();
        if (localRepoPath.trim().isEmpty()) {
            return SourceAndIssuesWizardPanelGUI_LocalRepoRequired();
        }
        return null;
    }

    public void validateWizard() throws WizardValidationException {
        // XXX
    }

    public void read(WizardDescriptor settings) {

        this.settings = settings;
        String scmType = (String) this.settings.getProperty(NewProjectWizardIterator.PROP_SCM_TYPE);
        String repoLocal = (String) this.settings.getProperty(NewProjectWizardIterator.PROP_SCM_LOCAL);
        if (repoLocal == null || repoLocal.trim().isEmpty()) {
            File parent = NewProjectWizardIterator.getCommonParent(itemsToShare);
            if (parent != null) {
                setRepoLocal(parent.getAbsolutePath());
            } else {
                setRepoLocal("");
            }
        } else {
            setRepoLocal(repoLocal);
        }
    }

    public void store(WizardDescriptor settings) {
        settings.putProperty(NewProjectWizardIterator.PROP_SCM_TYPE, ScmType.GIT.name());
        settings.putProperty(NewProjectWizardIterator.PROP_SCM_LOCAL, getRepoLocal());
        settings.putProperty(NewProjectWizardIterator.PROP_FOLDERS_TO_SHARE, itemsToShare);
    }

    private void setRepoLocal(String localPath) {
        localFolderTextField.setText(localPath);
    }

    private String getRepoLocal() {
        return localFolderTextField.getText();
    }

    private class SharedItemsListModel extends AbstractListModel {

        @Override
        public Object getElementAt(int arg0) {
            return itemsToShare.get(arg0);
        }

        @Override
        public int getSize() {
            return itemsToShare.size();
        }

        private void fireContentsChanged() {
            fireContentsChanged(this, 0, Integer.MAX_VALUE);
        }
    }
}
