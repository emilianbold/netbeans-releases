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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.php.project.connections.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.modules.php.project.ui.customizer.RunAsPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class RunAsRemoteWeb extends RunAsPanel.InsidePanel implements RunConfigurationPanelVisual.Changeable {
    private static final long serialVersionUID = -559266988746891271L;
    final ChangeSupport changeSupport = new ChangeSupport(this);
    private static final RemoteConfiguration NO_REMOTE_CONFIGURATION = new RemoteConfiguration(
            NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_NoRemoteConfiguration"), "", null, null, 0, null, false, null, 0); // NOI18N
    private static final UploadFiles DEFAULT_UPLOAD_FILES = UploadFiles.ON_RUN;

    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    private final String displayName;

    public RunAsRemoteWeb(ConfigManager manager) {
        this(manager, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_ConfigRemoteWeb"));
    }

    public RunAsRemoteWeb(ConfigManager manager, String displayName) {
        super(manager);
        this.displayName = displayName;

        initComponents();

        labels = new JLabel[] {
            urlLabel,
            uploadDirectoryLabel,
        };
        textFields = new JTextField[] {
            urlTextField,
            uploadDirectoryTextField,
        };
        propertyNames = new String[] {
            PhpProjectProperties.URL,
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
    }

    @Override
    protected void validateFields() {
        changeSupport.fireChange();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private void populateRemoteConnectionComboBox() {
        if (!Boolean.getBoolean(RemoteConnections.DEBUG_PROPERTY)) {
            remoteConnectionComboBox.addItem(NO_REMOTE_CONFIGURATION);
            return;
        }
        List<RemoteConfiguration> connections = RemoteConnections.get().getRemoteConfigurations();
        if (connections.isEmpty()) {
            // no connections defined
            connections = Arrays.asList(NO_REMOTE_CONFIGURATION);
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(new Vector<RemoteConfiguration>(connections));
        remoteConnectionComboBox.setModel(model);
    }

    private void selectRemoteConnection() {
        String remoteConnection = getValue(PhpProjectProperties.REMOTE_CONNECTION);
        if (remoteConnection == null) {
            remoteConnectionComboBox.setSelectedItem(NO_REMOTE_CONFIGURATION);
            return;
        }
        int size = remoteConnectionComboBox.getModel().getSize();
        for (int i = 0; i < size; ++i) {
            RemoteConfiguration rc = (RemoteConfiguration) remoteConnectionComboBox.getItemAt(i);
            if (remoteConnection.equals(rc.getName())
                    || "".equals(remoteConnection)) { // NOI18N
                // select existing or
                // if no configuration formerly existed and now some were created => so select the first one
                remoteConnectionComboBox.setSelectedItem(rc);
                return;
            }
        }
        assert false : "Should not get here";
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        runAsLabel = new javax.swing.JLabel();
        runAsComboBox = new javax.swing.JComboBox();
        urlLabel = new javax.swing.JLabel();
        urlTextField = new javax.swing.JTextField();
        remoteConnectionLabel = new javax.swing.JLabel();
        remoteConnectionComboBox = new javax.swing.JComboBox();
        manageRemoteConnectionButton = new javax.swing.JButton();
        uploadDirectoryLabel = new javax.swing.JLabel();
        uploadDirectoryTextField = new javax.swing.JTextField();
        uploadFilesLabel = new javax.swing.JLabel();
        uploadFilesComboBox = new javax.swing.JComboBox();
        uploadFilesHintLabel = new javax.swing.JLabel();

        runAsLabel.setLabelFor(runAsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(runAsLabel, org.openide.util.NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_RunAs")); // NOI18N

        urlLabel.setLabelFor(urlTextField);
        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_ProjectUrl")); // NOI18N

        remoteConnectionLabel.setLabelFor(remoteConnectionComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(remoteConnectionLabel, org.openide.util.NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_FtpConnection")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(manageRemoteConnectionButton, org.openide.util.NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_Manage")); // NOI18N
        manageRemoteConnectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageRemoteConnectionButtonActionPerformed(evt);
            }
        });

        uploadDirectoryLabel.setLabelFor(uploadDirectoryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(uploadDirectoryLabel, org.openide.util.NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_UploadDirectory")); // NOI18N

        uploadFilesLabel.setLabelFor(uploadFilesComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(uploadFilesLabel, org.openide.util.NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_UploadFiles")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(uploadFilesHintLabel, "dummy"); // NOI18N
        uploadFilesHintLabel.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(remoteConnectionLabel)
                    .add(uploadDirectoryLabel)
                    .add(uploadFilesLabel)
                    .add(urlLabel)
                    .add(runAsLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(urlTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, uploadFilesHintLabel)
                    .add(layout.createSequentialGroup()
                        .add(remoteConnectionComboBox, 0, 121, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(manageRemoteConnectionButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, uploadDirectoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, uploadFilesComboBox, 0, 222, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, runAsComboBox, 0, 222, Short.MAX_VALUE))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(runAsLabel)
                    .add(runAsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(urlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(remoteConnectionLabel)
                    .add(manageRemoteConnectionButton)
                    .add(remoteConnectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(uploadDirectoryLabel)
                    .add(uploadDirectoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(uploadFilesLabel)
                    .add(uploadFilesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(uploadFilesHintLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void manageRemoteConnectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageRemoteConnectionButtonActionPerformed
        if (!Boolean.getBoolean(RemoteConnections.DEBUG_PROPERTY)) {
            NotifyDescriptor notifyDescriptor = new NotifyDescriptor(
                    "Not implemented yet.", // NOI18N
                    "TODO", // NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.INFORMATION_MESSAGE,
                    new Object[] {NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            return;
        }
        if (RemoteConnections.get().openManager()) {
            populateRemoteConnectionComboBox();
            selectRemoteConnection();
        }
    }//GEN-LAST:event_manageRemoteConnectionButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton manageRemoteConnectionButton;
    private javax.swing.JComboBox remoteConnectionComboBox;
    private javax.swing.JLabel remoteConnectionLabel;
    private javax.swing.JComboBox runAsComboBox;
    private javax.swing.JLabel runAsLabel;
    private javax.swing.JLabel uploadDirectoryLabel;
    private javax.swing.JTextField uploadDirectoryTextField;
    private javax.swing.JComboBox uploadFilesComboBox;
    private javax.swing.JLabel uploadFilesHintLabel;
    private javax.swing.JLabel uploadFilesLabel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables

    private class FieldUpdater extends TextFieldUpdater {

        public FieldUpdater(String propName, JLabel label, JTextField field) {
            super(propName, label, field);
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
