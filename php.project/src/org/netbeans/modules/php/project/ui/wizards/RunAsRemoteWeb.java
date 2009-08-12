/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Color;
import java.util.List;
import org.netbeans.modules.php.project.connections.ConfigManager;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.modules.php.project.ui.customizer.RunAsPanel;
import org.netbeans.modules.php.project.ui.customizer.RunAsValidator;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class RunAsRemoteWeb extends RunAsPanel.InsidePanel {
    private static final long serialVersionUID = -5592669886554191271L;
    private static final String NO_CONFIG = "no-config"; // NOI18N
    static final RemoteConfiguration NO_REMOTE_CONFIGURATION =
            new RemoteConfiguration.Empty(NO_CONFIG, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_NoRemoteConfiguration"));
    private static final UploadFiles DEFAULT_UPLOAD_FILES = UploadFiles.ON_RUN;

    final ChangeSupport changeSupport = new ChangeSupport(this);
    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    private final String displayName;
    private final SourcesFolderProvider sourcesFolderProvider;

    public RunAsRemoteWeb(ConfigManager manager, SourcesFolderProvider sourcesFolderProvider) {
        this(manager, sourcesFolderProvider, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_ConfigRemoteWeb"));
    }

    public RunAsRemoteWeb(ConfigManager manager, SourcesFolderProvider sourcesFolderProvider, String displayName) {
        super(manager);
        this.displayName = displayName;
        this.sourcesFolderProvider = sourcesFolderProvider;

        initComponents();

        labels = new JLabel[] {
            urlLabel,
            uploadDirectoryLabel,
            indexFileLabel,
        };
        textFields = new JTextField[] {
            urlTextField,
            uploadDirectoryTextField,
            indexFileTextField,
        };
        propertyNames = new String[] {
            RunConfigurationPanel.URL,
            RunConfigurationPanel.REMOTE_DIRECTORY,
            RunConfigurationPanel.INDEX_FILE,
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;

        populateRemoteConnectionComboBox();
        remoteConnectionComboBox.setRenderer(new RemoteConnectionRenderer());
        for (UploadFiles uploadFiles : UploadFiles.values()) {
            uploadFilesComboBox.addItem(uploadFiles);
        }
        uploadFilesComboBox.setRenderer(new RemoteUploadRenderer());

        // listeners
        for (int i = 0; i < textFields.length; i++) {
            DocumentListener dl = new FieldUpdater(propertyNames[i], labels[i], textFields[i]);
            textFields[i].getDocument().addDocumentListener(dl);
        }
        // remote connection
        ComboBoxSelectedItemConvertor remoteConfigurationConvertor = new ComboBoxSelectedItemConvertor() {
            public String convert(JComboBox comboBox) {
                RemoteConfiguration remoteConfiguration = (RemoteConfiguration) comboBox.getSelectedItem();
                assert remoteConfiguration != null;
                return remoteConfiguration.getName();
            }
        };
        remoteConnectionComboBox.addActionListener(new ComboBoxUpdater(RunConfigurationPanel.REMOTE_CONNECTION, remoteConnectionLabel,
                remoteConnectionComboBox, remoteConfigurationConvertor));
        // remote upload
        ComboBoxSelectedItemConvertor remoteUploadConvertor = new ComboBoxSelectedItemConvertor() {
            public String convert(JComboBox comboBox) {
                UploadFiles uploadFiles = (UploadFiles) comboBox.getSelectedItem();
                assert uploadFiles != null;
                uploadFilesHintLabel.setText(uploadFiles.getDescription());
                return uploadFiles.name();
            }
        };
        uploadFilesComboBox.addActionListener(new ComboBoxUpdater(RunConfigurationPanel.REMOTE_UPLOAD, uploadFilesLabel, uploadFilesComboBox,
                remoteUploadConvertor));
        runAsComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });

        // upload directory hint
        remoteConnectionComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateRemoteConnectionHint();
            }
        });
        uploadDirectoryTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }
            private void processUpdate() {
                updateRemoteConnectionHint();
            }
        });
        updateRemoteConnectionHint();
    }

    @Override
    protected RunAsType getRunAsType() {
        return RunAsType.REMOTE;
    }

    @Override
    protected String getDisplayName() {
        return displayName;
    }

    @Override
    protected JComboBox getRunAsCombo() {
        return runAsComboBox;
    }

    @Override
    protected JLabel getRunAsLabel() {
        return runAsLabel;
    }

    @Override
    protected void loadFields() {
        for (int i = 0; i < textFields.length; i++) {
            textFields[i].setText(getValue(propertyNames[i]));
        }
        // remote connection
        selectRemoteConnection();
        // remote upload
        UploadFiles uploadFiles = null;
        String remoteUpload = getValue(RunConfigurationPanel.REMOTE_UPLOAD);
        if (remoteUpload == null) {
            uploadFiles = DEFAULT_UPLOAD_FILES;
        } else {
            try {
                uploadFiles = UploadFiles.valueOf(remoteUpload);
            } catch (IllegalArgumentException iae) {
                uploadFiles = DEFAULT_UPLOAD_FILES;
            }
        }
        uploadFilesComboBox.setSelectedItem(uploadFiles);
    }

    @Override
    protected void validateFields() {
        changeSupport.fireChange();
    }

    public void addRunAsRemoteWebListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeRunAsRemoteWebListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private void populateRemoteConnectionComboBox() {
        List<RemoteConfiguration> connections = RemoteConnections.get().getRemoteConfigurations();
        if (connections.isEmpty()) {
            // no connections defined
            connections = Arrays.asList(NO_REMOTE_CONFIGURATION);
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(new Vector<RemoteConfiguration>(connections));
        remoteConnectionComboBox.setModel(model);
    }

    private void selectRemoteConnection() {
        String remoteConnection = getValue(RunConfigurationPanel.REMOTE_CONNECTION);
        // #141849 - can be null if one adds remote config for the first time for a project but already has some remote connection
        DefaultComboBoxModel model = (DefaultComboBoxModel) remoteConnectionComboBox.getModel();
        if (remoteConnection == null
                && model.getIndexOf(NO_REMOTE_CONFIGURATION) != -1) {
            remoteConnectionComboBox.setSelectedItem(NO_REMOTE_CONFIGURATION);
            return;
        }
        int size = remoteConnectionComboBox.getModel().getSize();
        for (int i = 0; i < size; ++i) {
            RemoteConfiguration rc = (RemoteConfiguration) remoteConnectionComboBox.getItemAt(i);
            if (remoteConnection == null
                    || NO_CONFIG.equals(remoteConnection)
                    || remoteConnection.equals(rc.getName())) {
                // select existing or
                // if no configuration formerly existed and now some were created => so select the first one
                remoteConnectionComboBox.setSelectedItem(rc);
                return;
            }
        }
        // #165549
        if (model.getIndexOf(NO_REMOTE_CONFIGURATION) == -1) {
            model.addElement(NO_REMOTE_CONFIGURATION);
        }
        remoteConnectionComboBox.setSelectedItem(NO_REMOTE_CONFIGURATION);
    }

    public String getUrl() {
        return urlTextField.getText().trim();
    }

    public void setUrl(String url) {
        urlTextField.setText(url);
    }

    public RemoteConfiguration getRemoteConfiguration() {
        return (RemoteConfiguration) remoteConnectionComboBox.getSelectedItem();
    }

    public void setRemoteConfiguration(RemoteConfiguration remoteConfiguration) {
        remoteConnectionComboBox.setSelectedItem(remoteConfiguration);
    }

    public String getUploadDirectory() {
        return uploadDirectoryTextField.getText().trim();
    }

    public void setUploadDirectory(String uploadDirectory) {
        uploadDirectoryTextField.setText(uploadDirectory);
    }

    public UploadFiles getUploadFiles() {
        return (UploadFiles) uploadFilesComboBox.getSelectedItem();
    }

    public void setUploadFiles(UploadFiles uploadFiles) {
        uploadFilesComboBox.setSelectedItem(uploadFiles);
    }

    public String getIndexFile() {
        return indexFileTextField.getText().trim();
    }

    public void setIndexFile(String indexFile) {
        indexFileTextField.setText(indexFile);
    }

    public void hideIndexFile() {
        indexFileLabel.setVisible(false);
        indexFileTextField.setVisible(false);
        indexFileBrowseButton.setVisible(false);
    }

    public void hideRunAs() {
        runAsLabel.setVisible(false);
        runAsComboBox.setVisible(false);
    }

    public void hideUploadFiles() {
        uploadFilesLabel.setVisible(false);
        uploadFilesComboBox.setVisible(false);
        uploadFilesHintLabel.setVisible(false);
    }

    void updateRemoteConnectionHint() {
        RemoteConfiguration configuration = (RemoteConfiguration) remoteConnectionComboBox.getSelectedItem();
        if (configuration == NO_REMOTE_CONFIGURATION) {
            remoteConnectionHintLabel.setText(" "); // NOI18N
            return;
        }
        remoteConnectionHintLabel.setText(configuration.getUrl(RunAsValidator.sanitizeUploadDirectory(uploadDirectoryTextField.getText(), true)));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        runAsLabel = new JLabel();
        runAsComboBox = new JComboBox();
        urlLabel = new JLabel();
        urlTextField = new JTextField();
        remoteConnectionLabel = new JLabel();
        remoteConnectionComboBox = new JComboBox();
        manageRemoteConnectionButton = new JButton();
        uploadDirectoryLabel = new JLabel();
        uploadDirectoryTextField = new JTextField();
        remoteConnectionHintLabel = new JLabel();
        uploadFilesLabel = new JLabel();
        uploadFilesComboBox = new JComboBox();
        uploadFilesHintLabel = new JLabel();
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();

        setFocusTraversalPolicy(null);

        runAsLabel.setLabelFor(runAsComboBox);

        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_RunAs")); // NOI18N
        urlLabel.setLabelFor(urlTextField);

        Mnemonics.setLocalizedText(urlLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_ProjectUrl")); // NOI18N
        remoteConnectionLabel.setLabelFor(remoteConnectionComboBox);

        Mnemonics.setLocalizedText(remoteConnectionLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_RemoteConnection"));
        Mnemonics.setLocalizedText(manageRemoteConnectionButton, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_Manage"));
        manageRemoteConnectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manageRemoteConnectionButtonActionPerformed(evt);
            }
        });

        uploadDirectoryLabel.setLabelFor(uploadDirectoryTextField);

        Mnemonics.setLocalizedText(uploadDirectoryLabel,NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_UploadDirectory")); // NOI18N
        Mnemonics.setLocalizedText(remoteConnectionHintLabel, "dummy");
        remoteConnectionHintLabel.setEnabled(false);

        uploadFilesLabel.setLabelFor(uploadFilesComboBox);

        Mnemonics.setLocalizedText(uploadFilesLabel,NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_UploadFiles")); // NOI18N
        Mnemonics.setLocalizedText(uploadFilesHintLabel, "dummy");
        uploadFilesHintLabel.setEnabled(false);

        indexFileLabel.setLabelFor(indexFileTextField);

        Mnemonics.setLocalizedText(indexFileLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_IndexFile"));
        Mnemonics.setLocalizedText(indexFileBrowseButton, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_BrowseIndex"));
        indexFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(remoteConnectionLabel)
                    .add(uploadDirectoryLabel)
                    .add(uploadFilesLabel)
                    .add(urlLabel)
                    .add(runAsLabel)
                    .add(indexFileLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(remoteConnectionHintLabel)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(indexFileTextField, GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(indexFileBrowseButton))
                            .add(GroupLayout.TRAILING, urlTextField, GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                            .add(uploadFilesHintLabel)
                            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(remoteConnectionComboBox, 0, 114, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(manageRemoteConnectionButton))
                            .add(uploadDirectoryTextField, GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                            .add(uploadFilesComboBox, 0, 211, Short.MAX_VALUE)
                            .add(runAsComboBox, 0, 211, Short.MAX_VALUE))
                        .add(0, 0, 0))))
        );

        layout.linkSize(new Component[] {indexFileBrowseButton, manageRemoteConnectionButton}, GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(runAsLabel)
                    .add(runAsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(urlTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(indexFileLabel)
                    .add(indexFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(indexFileBrowseButton))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(remoteConnectionLabel)
                    .add(manageRemoteConnectionButton)
                    .add(remoteConnectionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(uploadDirectoryLabel)
                    .add(uploadDirectoryTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(remoteConnectionHintLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(uploadFilesLabel)
                    .add(uploadFilesComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(uploadFilesHintLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        runAsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsLabel.AccessibleContext.accessibleName")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runAsComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsComboBox.AccessibleContext.accessibleName")); // NOI18N
        runAsComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlLabel.AccessibleContext.accessibleName")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsLocalWeb.urlLabel.AccessibleContext.accessibleDescription")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlTextField.AccessibleContext.accessibleName")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlTextField.AccessibleContext.accessibleDescription")); // NOI18N
        remoteConnectionLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionLabel.AccessibleContext.accessibleName")); // NOI18N
        remoteConnectionLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionLabel.AccessibleContext.accessibleDescription")); // NOI18N
        remoteConnectionComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionComboBox.AccessibleContext.accessibleName")); // NOI18N
        remoteConnectionComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        manageRemoteConnectionButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.manageRemoteConnectionButton.AccessibleContext.accessibleName")); // NOI18N
        manageRemoteConnectionButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.manageRemoteConnectionButton.AccessibleContext.accessibleDescription")); // NOI18N
        uploadDirectoryLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectoryLabel.AccessibleContext.accessibleName")); // NOI18N
        uploadDirectoryLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectoryLabel.AccessibleContext.accessibleDescription")); // NOI18N
        uploadDirectoryTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectoryTextField.AccessibleContext.accessibleName")); // NOI18N
        uploadDirectoryTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectoryTextField.AccessibleContext.accessibleDescription")); // NOI18N
        remoteConnectionHintLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionHintLabel.AccessibleContext.accessibleName")); // NOI18N
        remoteConnectionHintLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionHintLabel.AccessibleContext.accessibleDescription")); // NOI18N
        uploadFilesLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesLabel.AccessibleContext.accessibleName")); // NOI18N
        uploadFilesLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesLabel.AccessibleContext.accessibleDescription")); // NOI18N
        uploadFilesComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesComboBox.AccessibleContext.accessibleName")); // NOI18N
        uploadFilesComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        uploadFilesHintLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesHintLabel.AccessibleContext.accessibleName")); // NOI18N
        uploadFilesHintLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesHintLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileLabel.AccessibleContext.accessibleName")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileTextField.AccessibleContext.accessibleName")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void manageRemoteConnectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageRemoteConnectionButtonActionPerformed
        if (RemoteConnections.get().openManager((RemoteConfiguration) remoteConnectionComboBox.getSelectedItem())) {
            populateRemoteConnectionComboBox();
            selectRemoteConnection();
        }
    }//GEN-LAST:event_manageRemoteConnectionButtonActionPerformed

    private void indexFileBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        Utils.browseFolderFile(sourcesFolderProvider.getSourcesFolder(), indexFileTextField);
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JButton manageRemoteConnectionButton;
    private JComboBox remoteConnectionComboBox;
    private JLabel remoteConnectionHintLabel;
    private JLabel remoteConnectionLabel;
    private JComboBox runAsComboBox;
    private JLabel runAsLabel;
    private JLabel uploadDirectoryLabel;
    private JTextField uploadDirectoryTextField;
    private JComboBox uploadFilesComboBox;
    private JLabel uploadFilesHintLabel;
    private JLabel uploadFilesLabel;
    private JLabel urlLabel;
    private JTextField urlTextField;
    // End of variables declaration//GEN-END:variables

    private class FieldUpdater extends TextFieldUpdater {

        public FieldUpdater(String propName, JLabel label, JTextField field) {
            super(propName, label, field);
        }

        @Override
        protected String getPropValue() {
            String value = super.getPropValue();
            if (getPropName().equals(RunConfigurationPanel.REMOTE_DIRECTORY)) {
                value = RunAsValidator.sanitizeUploadDirectory(value, true);
            }
            return value;
        }

        protected final String getDefaultValue() {
            return RunAsRemoteWeb.this.getDefaultValue(getPropName());
        }
    }

    interface ComboBoxSelectedItemConvertor {
        String convert(final JComboBox comboBox);
    }

    private class ComboBoxUpdater implements ActionListener {
        private final JLabel label;
        private final JComboBox field;
        private final String propName;
        private final ComboBoxSelectedItemConvertor comboBoxConvertor;

        public ComboBoxUpdater(String propName, JLabel label, JComboBox field, ComboBoxSelectedItemConvertor comboBoxConvertor) {
            this.propName = propName;
            this.label = label;
            this.field = field;
            this.comboBoxConvertor = comboBoxConvertor;
        }

        public void actionPerformed(ActionEvent e) {
            String value = comboBoxConvertor.convert(field);
            RunAsRemoteWeb.this.putValue(propName, value);
            RunAsRemoteWeb.this.markAsModified(label, propName, value);
            validateFields();
        }
    }

    private static class RemoteConnectionRenderer extends JLabel implements ListCellRenderer, UIResource {
        private static final long serialVersionUID = 93621381917558630L;

        public RemoteConnectionRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            assert value instanceof RemoteConfiguration;
            setName("ComboBox.listRenderer"); // NOI18N
            RemoteConfiguration remoteConfig = (RemoteConfiguration) value;
            setText(remoteConfig.getDisplayName());
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }
            setForeground(getForeground(remoteConfig, list, isSelected));
            return this;
        }

        private Color getForeground(RemoteConfiguration remoteConfig, JList list, boolean isSelected) {
            if (remoteConfig == NO_REMOTE_CONFIGURATION) {
                return UIManager.getColor("nb.errorForeground"); // NOI18N
            }
            return isSelected ? list.getSelectionForeground() : list.getForeground();
        }

        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
    }

    private static class RemoteUploadRenderer extends JLabel implements ListCellRenderer, UIResource {
        private static final long serialVersionUID = 86192358777523629L;

        public RemoteUploadRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            assert value instanceof UploadFiles;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((UploadFiles) value).getLabel());
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
    }
}
