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
package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Color;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import org.netbeans.modules.php.project.connections.ConfigManager;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.modules.php.project.ui.customizer.RunAsValidator.InvalidUrlException;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class RunAsRemoteWeb extends RunAsPanel.InsidePanel {
    private static final long serialVersionUID = -5593389531357591271L;
    private static final String NO_CONFIG = "no-config"; // NOI18N
    private static final String MISSING_CONFIG = "missing-config"; // NOI18N
    private static final RemoteConfiguration NO_REMOTE_CONFIGURATION =
            new RemoteConfiguration.Empty(NO_CONFIG, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_NoRemoteConfiguration")); // NOI18N
    private static final RemoteConfiguration MISSING_REMOTE_CONFIGURATION =
            new RemoteConfiguration.Empty(MISSING_CONFIG, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_MissingRemoteConfiguration")); // NOI18N
    private static final UploadFiles DEFAULT_UPLOAD_FILES = UploadFiles.ON_RUN;

    private final PhpProjectProperties properties;
    private final PhpProject project;
    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    private final String displayName;
    final Category category;

    public RunAsRemoteWeb(PhpProjectProperties properties, ConfigManager manager, Category category) {
        super(manager);
        this.properties = properties;
        this.category = category;
        project = properties.getProject();
        displayName = NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_ConfigRemoteWeb");

        initComponents();

        labels = new JLabel[] {
            urlLabel,
            indexFileLabel,
            argsLabel,
            uploadDirectoryLabel,
        };
        textFields = new JTextField[] {
            urlTextField,
            indexFileTextField,
            argsTextField,
            uploadDirectoryTextField,
        };
        propertyNames = new String[] {
            PhpProjectProperties.URL,
            PhpProjectProperties.INDEX_FILE,
            PhpProjectProperties.ARGS,
            PhpProjectProperties.REMOTE_DIRECTORY,
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
        remoteConnectionComboBox.addActionListener(new ComboBoxUpdater(PhpProjectProperties.REMOTE_CONNECTION, remoteConnectionLabel,
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
        uploadFilesComboBox.addActionListener(new ComboBoxUpdater(PhpProjectProperties.REMOTE_UPLOAD, uploadFilesLabel, uploadFilesComboBox,
                remoteUploadConvertor));

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

        preservePermissionsCheckBox.addActionListener(new CheckBoxUpdater(
                PhpProjectProperties.REMOTE_PERMISSIONS, preservePermissionsCheckBox));
        uploadDirectlyCheckBox.addActionListener(new CheckBoxUpdater(
                PhpProjectProperties.REMOTE_UPLOAD_DIRECTLY, uploadDirectlyCheckBox));
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
        String remoteUpload = getValue(PhpProjectProperties.REMOTE_UPLOAD);
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

        boolean preservePermissions = Boolean.parseBoolean(getValue(PhpProjectProperties.REMOTE_PERMISSIONS));
        preservePermissionsCheckBox.setSelected(preservePermissions);

        boolean uploadDirectly = Boolean.parseBoolean(getValue(PhpProjectProperties.REMOTE_UPLOAD_DIRECTLY));
        uploadDirectlyCheckBox.setSelected(uploadDirectly);
    }

    @Override
    protected void validateFields() {
        String url = urlTextField.getText();
        String indexFile = indexFileTextField.getText();
        String args = argsTextField.getText();

        // first validate remote fields because indexFile is "optional" (for run file e.g.)
        //  [not ideal but better than it used to be i hope]
        RemoteConfiguration selected = (RemoteConfiguration) remoteConnectionComboBox.getSelectedItem();
        assert selected != null;
        if (selected == NO_REMOTE_CONFIGURATION) {
            validateCategory(NbBundle.getMessage(RunAsRemoteWeb.class, "MSG_NoConfigurationSelected"));
            return;
        } else if (selected == MISSING_REMOTE_CONFIGURATION) {
            validateCategory(NbBundle.getMessage(RunAsRemoteWeb.class, "MSG_NonExistingConfigurationSelected"));
            return;
        }

        String err = RunAsValidator.validateUploadDirectory(uploadDirectoryTextField.getText(), true);
        if (err != null) {
            validateCategory(err);
            return;
        }

        // #150179 - index file not mandatory
        if (!StringUtils.hasText(indexFile)) {
            indexFile = null;
        }
        err = RunAsValidator.validateWebFields(url, FileUtil.toFile(getWebRoot()), indexFile, args);
        if (err != null) {
            validateCategory(err);
            return;
        }

        validateCategory(null);
    }

    private void validateCategory(String error) {
        category.setErrorMessage(error);
        // #148957 always allow to save customizer
        category.setValid(true);
    }

    private FileObject getWebRoot() {
        return ProjectPropertiesSupport.getSourceSubdirectory(project, properties.getWebRoot());
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
        selectRemoteConnection(null);
    }

    private void selectRemoteConnection(String remoteConnection) {
        if (remoteConnection == null) {
            remoteConnection = getValue(PhpProjectProperties.REMOTE_CONNECTION);
        }
        // #141849 - can be null if one adds remote config for the first time for a project but already has some remote connection
        DefaultComboBoxModel model = (DefaultComboBoxModel) remoteConnectionComboBox.getModel();
        if (remoteConnection == null
                || NO_CONFIG.equals(remoteConnection)) {
            if (model.getIndexOf(NO_REMOTE_CONFIGURATION) < 0) {
                model.insertElementAt(NO_REMOTE_CONFIGURATION, 0);
            }
            remoteConnectionComboBox.setSelectedItem(NO_REMOTE_CONFIGURATION);
            return;
        }

        int size = remoteConnectionComboBox.getModel().getSize();
        for (int i = 0; i < size; ++i) {
            RemoteConfiguration rc = (RemoteConfiguration) remoteConnectionComboBox.getItemAt(i);
            if (remoteConnection.equals(rc.getName())) {
                remoteConnectionComboBox.setSelectedItem(rc);
                return;
            }
        }

        // remote connection is missing (probably removed?)
        remoteConnectionComboBox.addItem(MISSING_REMOTE_CONFIGURATION);
        remoteConnectionComboBox.setSelectedItem(MISSING_REMOTE_CONFIGURATION);
        // # 162230
        model.removeElement(NO_REMOTE_CONFIGURATION);
    }

    void updateRemoteConnectionHint() {
        RemoteConfiguration configuration = (RemoteConfiguration) remoteConnectionComboBox.getSelectedItem();
        if (configuration == NO_REMOTE_CONFIGURATION
                || configuration == MISSING_REMOTE_CONFIGURATION) {
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
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();
        argsLabel = new JLabel();
        argsTextField = new JTextField();
        urlHintLabel = new JTextArea();
        remoteConnectionLabel = new JLabel();
        remoteConnectionComboBox = new JComboBox();
        manageRemoteConnectionButton = new JButton();
        uploadDirectoryLabel = new JLabel();
        uploadDirectoryTextField = new JTextField();
        remoteConnectionHintLabel = new JLabel();
        uploadFilesLabel = new JLabel();
        uploadFilesComboBox = new JComboBox();
        uploadFilesHintLabel = new JLabel();
        preservePermissionsCheckBox = new JCheckBox();
        preservePermissionsLabel = new JLabel();
        uploadDirectlyCheckBox = new JCheckBox();
        uploadDirectlyLabel = new JLabel();
        advancedButton = new JButton();

        setFocusTraversalPolicy(new FocusTraversalPolicy() {



            public Component getDefaultComponent(Container focusCycleRoot){
                return urlHintLabel;
            }//end getDefaultComponent
            public Component getFirstComponent(Container focusCycleRoot){
                return urlHintLabel;
            }//end getFirstComponent
            public Component getLastComponent(Container focusCycleRoot){
                return advancedButton;
            }//end getLastComponent
            public Component getComponentAfter(Container focusCycleRoot, Component aComponent){
                if(aComponent ==  urlHintLabel){
                    return remoteConnectionComboBox;
                }
                if(aComponent ==  runAsComboBox){
                    return urlTextField;
                }
                if(aComponent ==  remoteConnectionComboBox){
                    return manageRemoteConnectionButton;
                }
                if(aComponent ==  manageRemoteConnectionButton){
                    return uploadDirectoryTextField;
                }
                if(aComponent ==  uploadDirectoryTextField){
                    return uploadFilesComboBox;
                }
                if(aComponent ==  indexFileTextField){
                    return indexFileBrowseButton;
                }
                if(aComponent ==  urlTextField){
                    return indexFileTextField;
                }
                if(aComponent ==  uploadDirectlyCheckBox){
                    return advancedButton;
                }
                if(aComponent ==  preservePermissionsCheckBox){
                    return uploadDirectlyCheckBox;
                }
                if(aComponent ==  uploadFilesComboBox){
                    return preservePermissionsCheckBox;
                }
                if(aComponent ==  indexFileBrowseButton){
                    return argsTextField;
                }
                if(aComponent ==  argsTextField){
                    return urlHintLabel;
                }
                return urlHintLabel;//end getComponentAfter
            }
            public Component getComponentBefore(Container focusCycleRoot, Component aComponent){
                if(aComponent ==  remoteConnectionComboBox){
                    return urlHintLabel;
                }
                if(aComponent ==  urlTextField){
                    return runAsComboBox;
                }
                if(aComponent ==  manageRemoteConnectionButton){
                    return remoteConnectionComboBox;
                }
                if(aComponent ==  uploadDirectoryTextField){
                    return manageRemoteConnectionButton;
                }
                if(aComponent ==  uploadFilesComboBox){
                    return uploadDirectoryTextField;
                }
                if(aComponent ==  indexFileBrowseButton){
                    return indexFileTextField;
                }
                if(aComponent ==  indexFileTextField){
                    return urlTextField;
                }
                if(aComponent ==  advancedButton){
                    return uploadDirectlyCheckBox;
                }
                if(aComponent ==  uploadDirectlyCheckBox){
                    return preservePermissionsCheckBox;
                }
                if(aComponent ==  preservePermissionsCheckBox){
                    return uploadFilesComboBox;
                }
                if(aComponent ==  argsTextField){
                    return indexFileBrowseButton;
                }
                if(aComponent ==  urlHintLabel){
                    return argsTextField;
                }
                return advancedButton;//end getComponentBefore

            }}
        );

        runAsLabel.setLabelFor(runAsComboBox);

        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_RunAs")); // NOI18N
        urlLabel.setLabelFor(urlTextField);

        Mnemonics.setLocalizedText(urlLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_ProjectUrl")); // NOI18N
        indexFileLabel.setLabelFor(indexFileTextField);

        Mnemonics.setLocalizedText(indexFileLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_IndexFile"));
        Mnemonics.setLocalizedText(indexFileBrowseButton, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_Browse"));
        indexFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
            }
        });

        argsLabel.setLabelFor(argsTextField);

        Mnemonics.setLocalizedText(argsLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_Arguments")); // NOI18N
        urlHintLabel.setEditable(false);
        urlHintLabel.setLineWrap(true);
        urlHintLabel.setRows(2);
        urlHintLabel.setWrapStyleWord(true);
        urlHintLabel.setBorder(null);
        urlHintLabel.setDisabledTextColor(UIManager.getDefaults().getColor("Label.disabledForeground"));
        urlHintLabel.setEnabled(false);
        urlHintLabel.setOpaque(false);

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


        Mnemonics.setLocalizedText(preservePermissionsCheckBox, NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.preservePermissionsCheckBox.text")); // NOI18N
        preservePermissionsLabel.setLabelFor(preservePermissionsCheckBox);
        Mnemonics.setLocalizedText(preservePermissionsLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.preservePermissionsLabel.text")); // NOI18N
        preservePermissionsLabel.setEnabled(false);


        Mnemonics.setLocalizedText(uploadDirectlyCheckBox, NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectlyCheckBox.text")); // NOI18N
        uploadDirectlyLabel.setLabelFor(uploadDirectlyCheckBox);
        Mnemonics.setLocalizedText(uploadDirectlyLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectlyLabel.text")); // NOI18N
        uploadDirectlyLabel.setEnabled(false);
        Mnemonics.setLocalizedText(advancedButton, NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.advancedButton.text"));
        advancedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                advancedButtonActionPerformed(evt);
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
                    .add(indexFileLabel)
                    .add(argsLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(remoteConnectionHintLabel)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(GroupLayout.TRAILING)
                            .add(urlTextField, GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(indexFileTextField, GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(indexFileBrowseButton))
                            .add(argsTextField, GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                            .add(GroupLayout.LEADING, uploadFilesHintLabel)
                            .add(layout.createSequentialGroup()
                                .add(remoteConnectionComboBox, 0, 225, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(manageRemoteConnectionButton))
                            .add(GroupLayout.LEADING, uploadDirectoryTextField, GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                            .add(GroupLayout.LEADING, uploadFilesComboBox, 0, 322, Short.MAX_VALUE)
                            .add(GroupLayout.LEADING, runAsComboBox, 0, 322, Short.MAX_VALUE)
                            .add(GroupLayout.LEADING, urlHintLabel, GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                            .add(advancedButton))
                        .add(0, 0, 0))))
            .add(layout.createSequentialGroup()
                .add(preservePermissionsCheckBox)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(preservePermissionsLabel)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(uploadDirectlyCheckBox)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(uploadDirectlyLabel)
                .addContainerGap())
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
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(argsLabel)
                    .add(argsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(urlHintLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
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
                .addPreferredGap(LayoutStyle.RELATED)
                .add(preservePermissionsCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(preservePermissionsLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(uploadDirectlyCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(uploadDirectlyLabel)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(advancedButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        runAsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsLabel.AccessibleContext.accessibleName")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runAsComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsComboBox.AccessibleContext.accessibleName")); // NOI18N
        runAsComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlLabel.AccessibleContext.accessibleName")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlLabel.AccessibleContext.accessibleDescription")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlTextField.AccessibleContext.accessibleName")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileLabel.AccessibleContext.accessibleName")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileTextField.AccessibleContext.accessibleName")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.argsLabel.AccessibleContext.accessibleName")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.argsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.argsTextField.AccessibleContext.accessibleName")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.argsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        urlHintLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlHintLabel.AccessibleContext.accessibleName")); // NOI18N
        urlHintLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlHintLabel.AccessibleContext.accessibleDescription")); // NOI18N
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
        preservePermissionsCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.preservePermissionsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        preservePermissionsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.preservePermissionsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        uploadDirectlyCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectlyCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        uploadDirectlyLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectlyLabel.AccessibleContext.accessibleDescription")); // NOI18N
        advancedButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.advancedButton.AccessibleContext.accessibleName")); // NOI18N
        advancedButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.advancedButton.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void manageRemoteConnectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageRemoteConnectionButtonActionPerformed
        if (RemoteConnections.get().openManager((RemoteConfiguration) remoteConnectionComboBox.getSelectedItem())) {
            populateRemoteConnectionComboBox();
            // # 162233
            String selected = null;
            ComboBoxModel model = remoteConnectionComboBox.getModel();
            if (model.getSize() == 1) {
                selected = ((RemoteConfiguration) model.getElementAt(0)).getName();
            }
            selectRemoteConnection(selected);
        }
    }//GEN-LAST:event_manageRemoteConnectionButtonActionPerformed

    private void indexFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        Utils.browseFolderFile(PhpVisibilityQuery.forProject(project), getWebRoot(), indexFileTextField);
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    private void advancedButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_advancedButtonActionPerformed
        RunAsWebAdvanced.Properties props = new RunAsWebAdvanced.Properties(
                getValue(PhpProjectProperties.DEBUG_URL),
                urlHintLabel.getText(),
                getValue(PhpProjectProperties.DEBUG_PATH_MAPPING_REMOTE),
                getValue(PhpProjectProperties.DEBUG_PATH_MAPPING_LOCAL),
                getValue(PhpProjectProperties.DEBUG_PROXY_HOST),
                getValue(PhpProjectProperties.DEBUG_PROXY_PORT));
        RunAsWebAdvanced advanced = new RunAsWebAdvanced(project, props);
        if (advanced.open()) {
            Pair<String, String> pathMapping = advanced.getPathMapping();
            Pair<String, String> debugProxy = advanced.getDebugProxy();
            RunAsRemoteWeb.this.putValue(PhpProjectProperties.DEBUG_URL, advanced.getDebugUrl().name());
            RunAsRemoteWeb.this.putValue(PhpProjectProperties.DEBUG_PATH_MAPPING_REMOTE, pathMapping.first);
            RunAsRemoteWeb.this.putValue(PhpProjectProperties.DEBUG_PATH_MAPPING_LOCAL, pathMapping.second);
            RunAsRemoteWeb.this.putValue(PhpProjectProperties.DEBUG_PROXY_HOST, debugProxy.first);
            RunAsRemoteWeb.this.putValue(PhpProjectProperties.DEBUG_PROXY_PORT, debugProxy.second);
        }
    }//GEN-LAST:event_advancedButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton advancedButton;
    private JLabel argsLabel;
    private JTextField argsTextField;
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JButton manageRemoteConnectionButton;
    private JCheckBox preservePermissionsCheckBox;
    private JLabel preservePermissionsLabel;
    private JComboBox remoteConnectionComboBox;
    private JLabel remoteConnectionHintLabel;
    private JLabel remoteConnectionLabel;
    private JComboBox runAsComboBox;
    private JLabel runAsLabel;
    private JCheckBox uploadDirectlyCheckBox;
    private JLabel uploadDirectlyLabel;
    private JLabel uploadDirectoryLabel;
    private JTextField uploadDirectoryTextField;
    private JComboBox uploadFilesComboBox;
    private JLabel uploadFilesHintLabel;
    private JLabel uploadFilesLabel;
    private JTextArea urlHintLabel;
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
            if (getPropName().equals(PhpProjectProperties.REMOTE_DIRECTORY)) {
                value = RunAsValidator.sanitizeUploadDirectory(value, true);
            }
            return value;
        }

        protected final String getDefaultValue() {
            return RunAsRemoteWeb.this.getDefaultValue(getPropName());
        }

        @Override
        protected void processUpdate() {
            super.processUpdate();
            String hint = ""; // NOI18N
            try {
                hint = RunAsValidator.composeUrlHint(urlTextField.getText(), indexFileTextField.getText(), argsTextField.getText());
            } catch (InvalidUrlException ex) {
                category.setErrorMessage(ex.getMessage());
                category.setValid(false);
            }
            urlHintLabel.setText(hint);
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

    private class CheckBoxUpdater implements ActionListener {
        private final JCheckBox field;
        private final String propName;

        public CheckBoxUpdater(String propName, JCheckBox field) {
            this.field = field;
            this.propName = propName;
        }

        public void actionPerformed(ActionEvent e) {
            String value = Boolean.toString(field.isSelected());
            RunAsRemoteWeb.this.putValue(propName, value);
            RunAsRemoteWeb.this.markAsModified(field, propName, value);
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
            if (remoteConfig == MISSING_REMOTE_CONFIGURATION
                    || remoteConfig == NO_REMOTE_CONFIGURATION) {
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
