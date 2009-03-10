/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.api.Pair;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.DebugUrl;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.NotificationLineSupport;
import org.openide.filesystems.FileObject;

/**
 * @author Tomas Mysik
 */
public class RunAsWebAdvanced extends JPanel {
    private static final long serialVersionUID = 78423214132285847L;
    private static final String DEFAULT_LOCAL_PATH = NbBundle.getMessage(RunAsWebAdvanced.class, "LBL_DefaultLocalPath");

    private final PhpProject project;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport notificationLineSupport;

    RunAsWebAdvanced(PhpProject project, String debugUrl, String urlPreview, String remotePath, String localPath) {
        assert project != null;
        assert urlPreview != null;

        this.project = project;

        initComponents();
        setDebugUrl(debugUrl, urlPreview);
        setPathMapping(remotePath, localPath);

        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        remotePathTextField.getDocument().addDocumentListener(defaultDocumentListener);
        localPathTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    public boolean open() {
        descriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(RunAsWebAdvanced.class, "LBL_AdvancedWebConfiguration"),
                true,
                null);
        notificationLineSupport = descriptor.createNotificationLineSupport();
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            validateFields();
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        return descriptor.getValue() == NotifyDescriptor.OK_OPTION;
    }

    public DebugUrl getDebugUrl() {
        DebugUrl debugUrl = null;
        if (defaultUrlRadioButton.isSelected()) {
            debugUrl = DebugUrl.DEFAULT_URL;
        } else if (askUrlRadioButton.isSelected()) {
            debugUrl = DebugUrl.ASK_FOR_URL;
        } else if (doNotOpenBrowserRadioButton.isSelected()) {
            debugUrl = DebugUrl.DO_NOT_OPEN_BROWSER;
        }
        assert debugUrl != null;
        return debugUrl;
    }

    public Pair<String, String> getPathMapping() {
        String remotePath = remotePathTextField.getText();
        String localPath = ""; // NOI18N
        if (PhpProjectUtils.hasText(remotePath)) {
            localPath = localPathTextField.getText();
            if (isDefaultLocalPath(localPath)) {
                localPath = "."; // NOI18N
            }
        }
        return Pair.of(remotePath, localPath);
    }

    void validateFields() {
        assert notificationLineSupport != null;

        if (!isLocalPathValid()) {
            notificationLineSupport.setErrorMessage(NbBundle.getMessage(RunAsWebAdvanced.class, "MSG_LocalPathNotValid"));
            descriptor.setValid(false);
            return;
        }

        notificationLineSupport.clearMessages();
        descriptor.setValid(true);
    }

    private void setDebugUrl(String debugUrl, String urlPreview) {
        if (debugUrl == null) {
            debugUrl = DebugUrl.DEFAULT_URL.name();
        }
        switch (DebugUrl.valueOf(debugUrl)) {
            case DEFAULT_URL:
                defaultUrlRadioButton.setSelected(true);
                break;
            case ASK_FOR_URL:
                askUrlRadioButton.setSelected(true);
                break;
            case DO_NOT_OPEN_BROWSER:
                doNotOpenBrowserRadioButton.setSelected(true);
                break;
            default:
                throw new IllegalArgumentException("Unknown debug url type: " + debugUrl);
        }
        defaultUrlPreviewLabel.setText(urlPreview);
    }

    private void setPathMapping(String remotePath, String localPath) {
        remotePathTextField.setText(remotePath);
        if (isDefaultLocalPath(localPath)) {
            localPath = DEFAULT_LOCAL_PATH;
        }
        localPathTextField.setText(localPath);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        debugUrlButtonGroup = new ButtonGroup();
        debugUrlLabel = new JLabel();
        defaultUrlRadioButton = new JRadioButton();
        defaultUrlPreviewLabel = new JLabel();
        askUrlRadioButton = new JRadioButton();
        doNotOpenBrowserRadioButton = new JRadioButton();
        pathMappingLabel = new JLabel();
        remotePathLabel = new JLabel();
        remotePathTextField = new JTextField();
        localPathLabel = new JLabel();
        localPathTextField = new JTextField();
        localPathBrowseButton = new JButton();

        debugUrlLabel.setLabelFor(defaultUrlRadioButton);

        Mnemonics.setLocalizedText(debugUrlLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.debugUrlLabel.text")); // NOI18N
        debugUrlButtonGroup.add(defaultUrlRadioButton);
        defaultUrlRadioButton.setSelected(true);

        Mnemonics.setLocalizedText(defaultUrlRadioButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.defaultUrlRadioButton.text")); // NOI18N
        defaultUrlPreviewLabel.setLabelFor(defaultUrlRadioButton);
        Mnemonics.setLocalizedText(defaultUrlPreviewLabel, "dummy"); // NOI18N
        defaultUrlPreviewLabel.setEnabled(false);

        debugUrlButtonGroup.add(askUrlRadioButton);

        Mnemonics.setLocalizedText(askUrlRadioButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.askUrlRadioButton.text")); // NOI18N
        debugUrlButtonGroup.add(doNotOpenBrowserRadioButton);




        Mnemonics.setLocalizedText(doNotOpenBrowserRadioButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.doNotOpenBrowserRadioButton.text")); // NOI18N
        Mnemonics.setLocalizedText(pathMappingLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.pathMappingLabel.text"));
        Mnemonics.setLocalizedText(remotePathLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.remotePathLabel.text"));
        Mnemonics.setLocalizedText(localPathLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.localPathLabel.text"));
        localPathTextField.setEditable(false);
        Mnemonics.setLocalizedText(localPathBrowseButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.localPathBrowseButton.text"));
        localPathBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                localPathBrowseButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(debugUrlLabel)
                    .add(layout.createSequentialGroup()
                        .add(defaultUrlRadioButton)
                        .add(18, 18, 18)
                        .add(defaultUrlPreviewLabel))
                    .add(askUrlRadioButton)
                    .add(doNotOpenBrowserRadioButton)
                    .add(pathMappingLabel)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(remotePathLabel)
                            .add(localPathLabel))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(localPathTextField, GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(localPathBrowseButton))
                            .add(remotePathTextField, GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(debugUrlLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(defaultUrlRadioButton)
                    .add(defaultUrlPreviewLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(askUrlRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(doNotOpenBrowserRadioButton)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(pathMappingLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(remotePathTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(remotePathLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(localPathTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(localPathBrowseButton)
                    .add(localPathLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void localPathBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_localPathBrowseButtonActionPerformed
        String selected = Utils.browseSourceFolder(project, localPathTextField.getText());
        if (isDefaultLocalPath(selected)) {
            selected = DEFAULT_LOCAL_PATH;
        }
        localPathTextField.setText(selected);
    }//GEN-LAST:event_localPathBrowseButtonActionPerformed

    private static boolean isDefaultLocalPath(String path) {
        return path == null || path.trim().length() == 0 || path.equals(".") || DEFAULT_LOCAL_PATH.equals(path); // NOI18N
    }

    private boolean isLocalPathValid() {
        String localPath = localPathTextField.getText();
        if (isDefaultLocalPath(localPath)) {
            return true;
        }
        FileObject directory = ProjectPropertiesSupport.getSourceSubdirectory(project, localPath);
        return directory != null && directory.isValid();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JRadioButton askUrlRadioButton;
    private ButtonGroup debugUrlButtonGroup;
    private JLabel debugUrlLabel;
    private JLabel defaultUrlPreviewLabel;
    private JRadioButton defaultUrlRadioButton;
    private JRadioButton doNotOpenBrowserRadioButton;
    private JButton localPathBrowseButton;
    private JLabel localPathLabel;
    private JTextField localPathTextField;
    private JLabel pathMappingLabel;
    private JLabel remotePathLabel;
    private JTextField remotePathTextField;
    // End of variables declaration//GEN-END:variables

    private final class DefaultDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            validateFields();
        }

        public void removeUpdate(DocumentEvent e) {
            validateFields();
        }

        public void changedUpdate(DocumentEvent e) {
            validateFields();
        }
    }
}
