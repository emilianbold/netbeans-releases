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
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class SourcesPanel extends javax.swing.JPanel {

    private ClientSideProject project;
    
    /**
     * Creates new form SourcesPanel
     */
    public SourcesPanel(ProjectCustomizer.Category category, ClientSideProject p) {
        this.project = p;
        initComponents();
        jProjectFolderTextField.setText(FileUtil.getFileDisplayName(project.getProjectDirectory()));
        String s = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER);
        if (s == null) {
            s = ""; //NOI18N
        }
        jSiteRootFolderTextField.setText(s);
        s = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_TEST_FOLDER);
        if (s == null) {
            s = ""; //NOI18N
        }
        jTestFolderTextField.setText(s);
        String originalEncoding = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_ENCODING);
        if (originalEncoding == null) {
            originalEncoding = Charset.defaultCharset().name();
        }
        jEncodingComboBox.setModel(ProjectCustomizer.encodingModel(originalEncoding));
        jEncodingComboBox.setRenderer(ProjectCustomizer.encodingRenderer());
        
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditableProperties ep = project.getProjectProperties();
                ep.setProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER, jSiteRootFolderTextField.getText());
                ep.setProperty(ClientSideProjectConstants.PROJECT_TEST_FOLDER, jTestFolderTextField.getText());
                Charset enc = (Charset)jEncodingComboBox.getSelectedItem();
                if (enc != null && enc.name() != null) {
                    ep.setProperty(ClientSideProjectConstants.PROJECT_ENCODING, enc.name());
                }
                project.getProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            }
        });
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

    private void jBrowseSiteRootButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBrowseSiteRootButtonActionPerformed
        browse(jSiteRootFolderTextField, true, project.getProjectDirectory());
    }//GEN-LAST:event_jBrowseSiteRootButtonActionPerformed

    private void browse(JTextField tf, boolean allowNonProjectFolders, FileObject baseFolder) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File file = PropertyUtils.resolveFile(FileUtil.toFile(baseFolder), tf.getText());
        if (file.exists()) {
            chooser.setSelectedFile(file);
        } else {
            chooser.setCurrentDirectory(FileUtil.toFile(baseFolder));
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File selected = FileUtil.normalizeFile(chooser.getSelectedFile());
            FileObject fo = FileUtil.toFileObject(selected);
            if (fo != null && fo.isFolder()) {
                String rel = FileUtil.getRelativePath(baseFolder, fo);
                if (rel != null) {
                    tf.setText(rel);
                } else {
                    if (allowNonProjectFolders) {
                        tf.setText(FileUtil.getFileDisplayName(fo));
                    } else {
                        DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(
                            org.openide.util.NbBundle.getMessage(SourcesPanel.class, "WRONG_FOLDER")));
                    }
                }
            }
        }
    }
    
    private void jBrowseTestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBrowseTestButtonActionPerformed
        browse(jTestFolderTextField, false, project.getProjectDirectory());
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
}
