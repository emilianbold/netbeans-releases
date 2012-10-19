/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class SourcesPanel extends JPanel implements HelpCtx.Provider {

    private static final long serialVersionUID = -49835154831321L;

    private final ProjectCustomizer.Category category;
    private final ClientSideProjectProperties uiProperties;
    private final ClientSideProject project;


    public SourcesPanel(ProjectCustomizer.Category category, ClientSideProjectProperties uiProperties) {
        assert category != null;
        assert uiProperties != null;

        this.category = category;
        this.uiProperties = uiProperties;
        project = uiProperties.getProject();

        initComponents();
        init();
        initListeners();
        validateData();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.customizer.SourcesPanel"); // NOI18N
    }

    private void init() {
        jProjectFolderTextField.setText(FileUtil.getFileDisplayName(project.getProjectDirectory()));
        jSiteRootFolderTextField.setText(uiProperties.getSiteRootFolder());
        jTestFolderTextField.setText(uiProperties.getTestFolder());
        jEncodingComboBox.setModel(ProjectCustomizer.encodingModel(uiProperties.getEncoding()));
        jEncodingComboBox.setRenderer(ProjectCustomizer.encodingRenderer());
    }

    private void initListeners() {
        DocumentListener documentListener = new DefaultDocumentListener();
        jSiteRootFolderTextField.getDocument().addDocumentListener(documentListener);
        jTestFolderTextField.getDocument().addDocumentListener(documentListener);
        jEncodingComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateAndStore();
            }
        });
    }

    void validateAndStore() {
        validateData();
        storeData();
    }

    private void validateData() {
        // site root
        String error = validateSiteRoot();
        if (error != null) {
            category.setErrorMessage(error);
            category.setValid(false);
            return;
        }
        // test
        error = validateTest();
        if (error != null) {
            category.setErrorMessage(error);
            category.setValid(false);
            return;
        }
        // all ok
        category.setErrorMessage(" "); // NOI18N
        category.setValid(true);
    }

    @NbBundle.Messages("SourcesPanel.error.siteRoot.invalid=Site Root must be a valid directory.")
    private String validateSiteRoot() {
        File siteRootFolder = getSiteRootFolder();
        if (siteRootFolder == null || !siteRootFolder.isDirectory()) {
            return Bundle.SourcesPanel_error_siteRoot_invalid();
        }
        return null;
    }

    @NbBundle.Messages({
        "SourcesPanel.error.test.invalid=Unit Tests must be a valid directory.",
        "SourcesPanel.error.test.notUnderProjectDir=Unit Tests must be underneath project directory."
    })
    private String validateTest() {
        File testFolder = getTestFolder();
        if (testFolder == null) {
            // can be empty
            return null;
        }
        if (!testFolder.isDirectory()) {
            return Bundle.SourcesPanel_error_test_invalid();
        }
        if (!FileUtil.isParentOf(project.getProjectDirectory(), FileUtil.toFileObject(testFolder))) {
            return Bundle.SourcesPanel_error_test_notUnderProjectDir();
        }
        return null;
    }

    private void storeData() {
        File siteRootFolder = getSiteRootFolder();
        uiProperties.setSiteRootFolder(siteRootFolder != null ? siteRootFolder.getAbsolutePath() : ""); // NOI18N
        File testFolder = getTestFolder();
        uiProperties.setTestFolder(testFolder != null ? testFolder.getAbsolutePath() : ""); // NOI18N
        uiProperties.setEncoding(getEncoding().name());
    }

    private File getSiteRootFolder() {
        return resolveFile(jSiteRootFolderTextField.getText());
    }

    private File getTestFolder() {
        return resolveFile(jTestFolderTextField.getText());
    }

    private Charset getEncoding() {
        return (Charset) jEncodingComboBox.getSelectedItem();
    }

    private File resolveFile(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return FileUtil.normalizeFile(project.getProjectHelper().resolveFile(path));
    }

    private String browseFolder(String title, File currentPath) {
        File workDir = null;
        if (currentPath != null) {
            workDir = currentPath.getParentFile();
        }
        if (workDir == null || !workDir.exists()) {
            workDir = FileUtil.toFile(project.getProjectDirectory());
        }
        File folder = new FileChooserBuilder(SourcesPanel.class)
                .setTitle(title)
                .setDirectoriesOnly(true)
                .setDefaultWorkingDirectory(workDir)
                .forceUseOfDefaultWorkingDirectory(true)
                .setFileHiding(true)
                .showOpenDialog();
        if (folder == null) {
            return null;
        }
        String filePath = FileUtil.getRelativePath(project.getProjectDirectory(), FileUtil.toFileObject(folder));
        if (filePath == null) {
            // path cannot be relativized
            filePath = folder.getAbsolutePath();
        }
        return filePath;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jProjectFolderTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jSiteRootFolderTextField = new javax.swing.JTextField();
        jBrowseSiteRootButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jTestFolderTextField = new javax.swing.JTextField();
        jBrowseTestButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jEncodingComboBox = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.jLabel1.text")); // NOI18N

        jProjectFolderTextField.setEditable(false);
        jProjectFolderTextField.setText(org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.jProjectFolderTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.jLabel2.text")); // NOI18N

        jSiteRootFolderTextField.setText(org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.jSiteRootFolderTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jBrowseSiteRootButton, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.jBrowseSiteRootButton.text")); // NOI18N
        jBrowseSiteRootButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBrowseSiteRootButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.jLabel3.text")); // NOI18N

        jTestFolderTextField.setText(org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.jTestFolderTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jBrowseTestButton, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.jBrowseTestButton.text")); // NOI18N
        jBrowseTestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBrowseTestButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProjectFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jEncodingComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 195, Short.MAX_VALUE)
                            .addComponent(jTestFolderTextField)
                            .addComponent(jSiteRootFolderTextField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jBrowseSiteRootButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jBrowseTestButton, javax.swing.GroupLayout.Alignment.TRAILING)))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jProjectFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jSiteRootFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBrowseSiteRootButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTestFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBrowseTestButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jEncodingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 146, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("SourcesPanel.browse.siteRootFolder=Select Site Root")
    private void jBrowseSiteRootButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBrowseSiteRootButtonActionPerformed
        String filePath = browseFolder(Bundle.SourcesPanel_browse_siteRootFolder(), getSiteRootFolder());
        if (filePath != null) {
            jSiteRootFolderTextField.setText(filePath);
        }
    }//GEN-LAST:event_jBrowseSiteRootButtonActionPerformed

    @NbBundle.Messages("SourcesPanel.browse.testFolder=Select Unit Tests")
    private void jBrowseTestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBrowseTestButtonActionPerformed
        String filePath = browseFolder(Bundle.SourcesPanel_browse_testFolder(), getTestFolder());
        if (filePath != null) {
            jTestFolderTextField.setText(filePath);
        }
    }//GEN-LAST:event_jBrowseTestButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBrowseSiteRootButton;
    private javax.swing.JButton jBrowseTestButton;
    private javax.swing.JComboBox jEncodingComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jProjectFolderTextField;
    private javax.swing.JTextField jSiteRootFolderTextField;
    private javax.swing.JTextField jTestFolderTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            validateAndStore();
        }

    }

}
