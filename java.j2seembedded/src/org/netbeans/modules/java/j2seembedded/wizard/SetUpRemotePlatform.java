/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seembedded.wizard;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.WizardDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 * @author Roman Svitanic
 */
public class SetUpRemotePlatform extends javax.swing.JPanel {

    private static final String HELP_ID = "java.j2seembedded.setup-remote-platform";    //NOI18N
    private final ChangeSupport cs = new ChangeSupport(this);
    private final Panel panel;

    /**
     * Creates new form SetUpRemotePlatform
     */
    private SetUpRemotePlatform(Panel panel) {
        this.panel = panel;
        initComponents();
        postInitComponents();
    }

    private void postInitComponents() {
        final ChangeListener radioChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                toggleAuthMethod(radioButtonPassword.isSelected());
                cs.fireChange();
            }
        };
        this.radioButtonKey.addChangeListener(radioChangeListener);
        this.radioButtonPassword.addChangeListener(radioChangeListener);
        this.radioButtonPassword.setSelected(true);

        final DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                cs.fireChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                cs.fireChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                cs.fireChange();
            }
        };
        this.displayName.getDocument().addDocumentListener(docListener);
        this.host.getDocument().addDocumentListener(docListener);
        this.username.getDocument().addDocumentListener(docListener);
        this.password.getDocument().addDocumentListener(docListener);
        this.keyFilePath.getDocument().addDocumentListener(docListener);
        this.passphrase.getDocument().addDocumentListener(docListener);
        this.jreLocation.getDocument().addDocumentListener(docListener);
        this.workingDir.getDocument().addDocumentListener(docListener);
    }

    private void toggleAuthMethod(boolean usePasswordAuth) {
        password.setEnabled(usePasswordAuth);
        keyFilePath.setEnabled(!usePasswordAuth);
        passphrase.setEnabled(!usePasswordAuth);
        buttonBrowse.setEnabled(!usePasswordAuth);
    }

    public final synchronized void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    public final synchronized void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupAuth = new javax.swing.ButtonGroup();
        hostLabel = new javax.swing.JLabel();
        host = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        port = new javax.swing.JSpinner();
        usernameLabel = new javax.swing.JLabel();
        username = new javax.swing.JTextField();
        radioButtonPassword = new javax.swing.JRadioButton();
        passwordLabel = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        radioButtonKey = new javax.swing.JRadioButton();
        keyFileLabel = new javax.swing.JLabel();
        keyFilePath = new javax.swing.JTextField();
        buttonBrowse = new javax.swing.JButton();
        passphraseLabel = new javax.swing.JLabel();
        passphrase = new javax.swing.JPasswordField();
        jSeparator1 = new javax.swing.JSeparator();
        jreLocationLabel = new javax.swing.JLabel();
        jreLocation = new javax.swing.JTextField();
        buttonTest = new javax.swing.JButton();
        workingDirLabel = new javax.swing.JLabel();
        workingDir = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        displayNameLabel = new javax.swing.JLabel();
        displayName = new javax.swing.JTextField();

        setName(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "TXT_SetUpRemotePlatform")); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        hostLabel.setLabelFor(host);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.hostLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(hostLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(host, gridBagConstraints);

        portLabel.setLabelFor(port);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.portLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        add(portLabel, gridBagConstraints);

        port.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        port.setEditor(new javax.swing.JSpinner.NumberEditor(port, "#0"));
        port.setPreferredSize(new java.awt.Dimension(54, 20));
        port.setValue(22);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(port, gridBagConstraints);

        usernameLabel.setLabelFor(username);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.usernameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(usernameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(username, gridBagConstraints);

        buttonGroupAuth.add(radioButtonPassword);
        org.openide.awt.Mnemonics.setLocalizedText(radioButtonPassword, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.radioButtonPassword.text")); // NOI18N
        radioButtonPassword.setActionCommand(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.radioButtonPassword.actionCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(radioButtonPassword, gridBagConstraints);

        passwordLabel.setLabelFor(password);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.passwordLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(passwordLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(password, gridBagConstraints);

        buttonGroupAuth.add(radioButtonKey);
        org.openide.awt.Mnemonics.setLocalizedText(radioButtonKey, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.radioButtonKey.text")); // NOI18N
        radioButtonKey.setActionCommand(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.radioButtonKey.actionCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(radioButtonKey, gridBagConstraints);

        keyFileLabel.setLabelFor(keyFilePath);
        org.openide.awt.Mnemonics.setLocalizedText(keyFileLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.keyFileLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(keyFileLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(keyFilePath, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonBrowse, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.buttonBrowse.text")); // NOI18N
        buttonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(buttonBrowse, gridBagConstraints);

        passphraseLabel.setLabelFor(passphrase);
        org.openide.awt.Mnemonics.setLocalizedText(passphraseLabel, NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.passphraseLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(passphraseLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(passphrase, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        add(jSeparator1, gridBagConstraints);

        jreLocationLabel.setLabelFor(jreLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jreLocationLabel, org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.jreLocationLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jreLocationLabel, gridBagConstraints);

        jreLocation.setText(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.jreLocation.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(jreLocation, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonTest, org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.buttonTest.text")); // NOI18N
        buttonTest.setToolTipText(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.buttonTest.toolTipText")); // NOI18N
        buttonTest.setEnabled(false);
        buttonTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        add(buttonTest, gridBagConstraints);

        workingDirLabel.setLabelFor(workingDir);
        org.openide.awt.Mnemonics.setLocalizedText(workingDirLabel, org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.workingDirLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(workingDirLabel, gridBagConstraints);

        workingDir.setText(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.workingDir.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(workingDir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        add(jSeparator2, gridBagConstraints);

        displayNameLabel.setLabelFor(displayName);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameLabel, org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.displayNameLabel.text")); // NOI18N
        displayNameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.displayNameLabel.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(displayNameLabel, gridBagConstraints);

        displayName.setText(org.openide.util.NbBundle.getMessage(SetUpRemotePlatform.class, "SetUpRemotePlatform.displayName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(displayName, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void buttonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBrowseActionPerformed
        final String oldValue = keyFilePath.getText();
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (oldValue != null) {
            chooser.setSelectedFile(new File(oldValue));
        }
        chooser.setDialogTitle(NbBundle.getMessage(SetUpRemotePlatform.class, "Title_Chooser_SelectKeyfile")); //NOI18N
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            keyFilePath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_buttonBrowseActionPerformed

    private void buttonTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTestActionPerformed
        ProgressUtils.showProgressDialogAndRun(new ConnectionValidator(), NbBundle.getMessage(SetUpRemotePlatform.class, "LBL_ConnectingToPlatform")); //NOI18N
    }//GEN-LAST:event_buttonTestActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonBrowse;
    private javax.swing.ButtonGroup buttonGroupAuth;
    private javax.swing.JButton buttonTest;
    private javax.swing.JTextField displayName;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextField host;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jreLocation;
    private javax.swing.JLabel jreLocationLabel;
    private javax.swing.JLabel keyFileLabel;
    private javax.swing.JTextField keyFilePath;
    private javax.swing.JPasswordField passphrase;
    private javax.swing.JLabel passphraseLabel;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JSpinner port;
    private javax.swing.JLabel portLabel;
    private javax.swing.JRadioButton radioButtonKey;
    private javax.swing.JRadioButton radioButtonPassword;
    private javax.swing.JTextField username;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField workingDir;
    private javax.swing.JLabel workingDirLabel;
    // End of variables declaration//GEN-END:variables

    static class Panel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

        private final ChangeSupport changeSupport;
        private SetUpRemotePlatform ui;
        private boolean valid = false;
        private WizardDescriptor wizardDescriptor;

        public Panel() {
            changeSupport = new ChangeSupport(this);
        }

        @Override
        public Component getComponent() {
            if (ui == null) {
                ui = new SetUpRemotePlatform(this);
                ui.addChangeListener(this);
            }
            checkPanelValidity();
            return ui;
        }

        @Override
        public HelpCtx getHelp() {
            return new HelpCtx(HELP_ID);
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            wizardDescriptor = settings;

            if (settings.getProperty(RemotePlatformIt.PROP_DISPLAYNAME) != null) {
                ui.displayName.setText((String) settings.getProperty(RemotePlatformIt.PROP_DISPLAYNAME));
            }
            if (settings.getProperty(RemotePlatformIt.PROP_HOST) != null) {
                ui.host.setText((String) settings.getProperty(RemotePlatformIt.PROP_HOST));
            }
            if (settings.getProperty(RemotePlatformIt.PROP_PORT) != null) {
                ui.port.setValue((Integer) settings.getProperty(RemotePlatformIt.PROP_PORT));
            }
            if (settings.getProperty(RemotePlatformIt.PROP_USERNAME) != null) {
                ui.username.setText((String) settings.getProperty(RemotePlatformIt.PROP_USERNAME));
            }
            if (settings.getProperty(RemotePlatformIt.PROP_PASSWORD) != null) {
                ui.password.setText((String) settings.getProperty(RemotePlatformIt.PROP_PASSWORD));
                ui.radioButtonPassword.setSelected(true);
            }
            if (settings.getProperty(RemotePlatformIt.PROP_KEYFILE) != null) {
                ui.keyFilePath.setText((String) settings.getProperty(RemotePlatformIt.PROP_KEYFILE));
                ui.radioButtonKey.setSelected(true);
            }
            if (settings.getProperty(RemotePlatformIt.PROP_PASSPHRASE) != null) {
                ui.passphrase.setText((String) settings.getProperty(RemotePlatformIt.PROP_PASSPHRASE));
            }
            if (settings.getProperty(RemotePlatformIt.PROP_JREPATH) != null) {
                ui.jreLocation.setText((String) settings.getProperty(RemotePlatformIt.PROP_JREPATH));
            }
            if (settings.getProperty(RemotePlatformIt.PROP_WORKINGDIR) != null) {
                ui.workingDir.setText((String) settings.getProperty(RemotePlatformIt.PROP_WORKINGDIR));
            }
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            settings.putProperty(RemotePlatformIt.PROP_DISPLAYNAME, ui.displayName.getText());
            settings.putProperty(RemotePlatformIt.PROP_HOST, ui.host.getText());
            settings.putProperty(RemotePlatformIt.PROP_PORT, (Integer) ui.port.getValue());
            settings.putProperty(RemotePlatformIt.PROP_USERNAME, ui.username.getText());
            if (ui.radioButtonPassword.isSelected()) {
                settings.putProperty(RemotePlatformIt.PROP_PASSWORD, String.valueOf(ui.password.getPassword()));
                settings.putProperty(RemotePlatformIt.PROP_KEYFILE, null);
                settings.putProperty(RemotePlatformIt.PROP_PASSPHRASE, null);
            } else {
                settings.putProperty(RemotePlatformIt.PROP_KEYFILE, ui.keyFilePath.getText());
                settings.putProperty(RemotePlatformIt.PROP_PASSPHRASE, String.valueOf(ui.passphrase.getPassword()));
                settings.putProperty(RemotePlatformIt.PROP_PASSWORD, null);
            }
            settings.putProperty(RemotePlatformIt.PROP_JREPATH, ui.jreLocation.getText());
            settings.putProperty(RemotePlatformIt.PROP_WORKINGDIR, ui.workingDir.getText());
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            changeSupport.removeChangeListener(listener);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            valid = checkPanelValidity();
            ui.buttonTest.setEnabled(valid);
            changeSupport.fireChange();
        }

        private boolean checkPanelValidity() {
            ui.buttonTest.setEnabled(false);
            if (ui.displayName.getText().length() == 0) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_DisplayName")); // NOI18N
                return false;
            }
            if (ui.host.getText().length() == 0) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_Host")); // NOI18N
                return false;
            }
            if (ui.username.getText().length() == 0) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_Username")); // NOI18N
                return false;
            }
            if (ui.radioButtonPassword.isSelected() && ui.password.getPassword().length == 0) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_Password")); // NOI18N
                return false;
            } else if (ui.radioButtonKey.isSelected() && ui.keyFilePath.getText().length() == 0) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_KeyFile")); // NOI18N
                return false;
            }
            if (ui.jreLocation.getText().length() == 0) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_JRE")); // NOI18N
                return false;
            }
            if (ui.workingDir.getText().length() == 0) {
                displayNotification(NbBundle.getMessage(SetUpRemotePlatform.class, "ERROR_Empty_WorkingDir")); // NOI18N
                return false;
            }
            if (!valid) {
                displayNotification("");
            }
            ui.buttonTest.setEnabled(true);
            return true;
        }

        public void displayNotification(final String message) {
            if (wizardDescriptor != null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, message); // NOI18N
            }
        }
    }

    private class ConnectionValidator implements Runnable {

        @Override
        public void run() {
            String[] antTargets = null;
            final Properties prop = new Properties();
            prop.setProperty("host", host.getText()); //NOI18N
            prop.setProperty("port", String.valueOf(port.getValue())); //NOI18N
            prop.setProperty("username", username.getText()); //NOI18N
            prop.setProperty("jrePath", jreLocation.getText()); //NOI18N
            prop.setProperty("workingDir", workingDir.getText()); //NOI18N
            if (radioButtonPassword.isSelected()) {
                antTargets = new String[]{"connect-ssh-password"}; //NOI18N
                prop.setProperty("password", String.valueOf(password.getPassword())); //NOI18N
            } else if (radioButtonKey.isSelected()) {
                antTargets = new String[]{"connect-ssh-keyfile"}; //NOI18N
                prop.setProperty("keyfile", keyFilePath.getText()); //NOI18N
                prop.setProperty("passphrase", String.valueOf(passphrase.getPassword())); //NOI18N
            }

            final String resourcesPath = "org/netbeans/modules/java/j2seembedded/resources/validateconnection.xml"; //NOI18N
            File tmpFile = null;
            ExecutorTask executorTask  = null;
            try {
                try (InputStream inputStream = SetUpRemotePlatform.class.getClassLoader().getResourceAsStream(resourcesPath)) {
                    tmpFile = File.createTempFile("antScript", ".xml"); //NOI18N
                    try (OutputStream outputStream = new FileOutputStream(tmpFile)) {
                        int read = 0;
                        byte[] bytes = new byte[1024];
                        while ((read = inputStream.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                    }
                }
                final FileObject antScript = FileUtil.createData(FileUtil.normalizeFile(tmpFile));
                executorTask = ActionUtils.runTarget(antScript, antTargets, prop);               
                final int antResult = executorTask.result();
                if (antResult == 0) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (panel.wizardDescriptor != null) {
                                panel.wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                                        NbBundle.getMessage(SetUpRemotePlatform.class, "LBL_ConnectionSuccessful")); //NOI18N
                                panel.changeSupport.fireChange();
                            }
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (panel.wizardDescriptor != null) {
                                panel.wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                        NbBundle.getMessage(SetUpRemotePlatform.class, "LBL_ConnectionError")); //NOI18N
                                panel.changeSupport.fireChange();
                            }
                        }
                    });
                }
            } catch (IllegalArgumentException | IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (executorTask != null) {
                    executorTask.getInputOutput().closeInputOutput();
                }
                if (tmpFile != null) {
                    tmpFile.delete();
                }                
            }
        }
    }
}
