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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.phpdoc.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class BrowseFolderPanel extends JPanel {
    private static final long serialVersionUID = 1743213547571L;
    private static final String PHPDOC_LAST_FOLDER_SUFFIX = ".phpdoc.dir"; // NOI18N

    private final String info;
    private final FileObject sourceDir;

    private DialogDescriptor dialogDescriptor;
    private NotificationLineSupport notificationLineSupport;

    private BrowseFolderPanel(String info, FileObject sourceDir) {
        assert info != null;
        assert sourceDir != null;

        this.info = info;
        this.sourceDir = sourceDir;

        initComponents();

        docFolderTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }
            private void processUpdate() {
                validateDocFolder();
            }
        });
    }

    public static String open(PhpModule phpModule) {
        String info = NbBundle.getMessage(BrowseFolderPanel.class, "LBL_SelectDocFolder", phpModule.getDisplayName());
        BrowseFolderPanel panel = new BrowseFolderPanel(info, phpModule.getSourceDirectory());
        panel.dialogDescriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(BrowseFolderPanel.class, "LBL_SelectDir"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        panel.notificationLineSupport = panel.dialogDescriptor.createNotificationLineSupport();
        panel.notificationLineSupport.setInformationMessage(info);
        panel.dialogDescriptor.setValid(false);
        if (DialogDisplayer.getDefault().notify(panel.dialogDescriptor) == DialogDescriptor.OK_OPTION) {
            return panel.getDocFolder();
        }
        return null;
    }

    private String getDocFolder() {
        return docFolderTextField.getText().trim();
    }

    void validateDocFolder() {
        assert notificationLineSupport != null;

        String docFolder = getDocFolder();
        String error = FileUtils.validateDirectory(docFolder);
        if (error != null) {
            notificationLineSupport.setErrorMessage(error);
            dialogDescriptor.setValid(false);
            return;
        }
        notificationLineSupport.clearMessages();
        dialogDescriptor.setValid(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        docFolderLabel = new JLabel();
        docFolderTextField = new JTextField();
        browseDocFolderButton = new JButton();

        docFolderLabel.setLabelFor(docFolderTextField);

        Mnemonics.setLocalizedText(docFolderLabel, NbBundle.getMessage(BrowseFolderPanel.class, "BrowseFolderPanel.docFolderLabel.text"));
        Mnemonics.setLocalizedText(browseDocFolderButton, NbBundle.getMessage(BrowseFolderPanel.class, "BrowseFolderPanel.browseDocFolderButton.text"));
        browseDocFolderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseDocFolderButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(docFolderLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(docFolderTextField, GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(browseDocFolderButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(docFolderLabel)
                    .addComponent(docFolderTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseDocFolderButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseDocFolderButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseDocFolderButtonActionPerformed
        File phpDocDir = new FileChooserBuilder(BrowseFolderPanel.class.getName() + PHPDOC_LAST_FOLDER_SUFFIX)
                .setTitle(info)
                .setDirectoriesOnly(true)
                .setFileHiding(true)
                .setDefaultWorkingDirectory(FileUtil.toFile(sourceDir))
                .showOpenDialog();
        if (phpDocDir != null) {
            phpDocDir = FileUtil.normalizeFile(phpDocDir);
            docFolderTextField.setText(phpDocDir.getAbsolutePath());
        }
    }//GEN-LAST:event_browseDocFolderButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton browseDocFolderButton;
    private JLabel docFolderLabel;
    private JTextField docFolderTextField;
    // End of variables declaration//GEN-END:variables

}
