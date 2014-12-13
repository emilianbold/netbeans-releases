/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.selenium2.webclient.protractor;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.server.api.Selenium2Server;
import org.netbeans.modules.selenium2.webclient.protractor.preferences.ProtractorPreferences;
import org.netbeans.modules.selenium2.webclient.protractor.preferences.ProtractorPreferencesValidator;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
public class CustomizerProtractor extends javax.swing.JPanel {

    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    private volatile String protractor;
    private volatile String seleniumServerJar;

    // @GuardedBy("EDT")
    private ValidationResult validationResult;

    public CustomizerProtractor(Project project) {
        assert EventQueue.isDispatchThread();
        assert project != null;

        this.project = project;

        initComponents();
        init();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getProtractor() {
        return protractor;
    }

    public String getSeleniumServerJar() {
        return seleniumServerJar;
    }

    public String getWarningMessage() {
        assert EventQueue.isDispatchThread();
        for (ValidationResult.Message message : validationResult.getWarnings()) {
            return message.getMessage();
        }
        return null;
    }

    public String getErrorMessage() {
        assert EventQueue.isDispatchThread();
        for (ValidationResult.Message message : validationResult.getErrors()) {
            return message.getMessage();
        }
        return null;
    }

    @NbBundle.Messages({"CustomizerMocha.protractor.dir.info=Full path of protractor (typically node_modules/.bin/protractor).",
    "CustomizerMocha.selenium.server.jar.info=Full path to selenium server standalone jar file."})
    private void init() {
        assert EventQueue.isDispatchThread();
        // get saved protractor executable if previously set from protractor preferences
        String protractorExec = ProtractorPreferences.getProtractor(project);
        if(protractorExec == null) { // protractor executable not set yet, try searching for it in project's local node_modules dir
            String exec = new File(FileUtil.toFile(project.getProjectDirectory()), "node_modules/protractor/bin/protractor").getAbsolutePath();
            ValidationResult result = new ProtractorPreferencesValidator()
                .validateProtractor(exec)
                .getResult();
            if(result.isFaultless()) { // protractor executable is installed in project's local node_modules dir
                protractorExec = exec;
            }
        }
        protractorDirTextField.setText(protractorExec);
        protractorDirInfoLabel.setText(Bundle.CustomizerMocha_protractor_dir_info());
        
        // get saved selenium server jar if previously set from protractor preferences
        String serverJar = ProtractorPreferences.getSeleniumServerJar(project);
        if(serverJar == null) {  
            // selenium server jar not set yet, try searching for it in project's local node_modules dir
            // user hopefully has configured protractor and run "webdriver-manager update"
            File seleniumFolder = new File(FileUtil.toFile(project.getProjectDirectory()), "node_modules/protractor/selenium");
            FileObject seleniumFO = FileUtil.toFileObject(seleniumFolder);
            if (seleniumFO == null) {
                return;
            }
            ArrayList<? extends FileObject> fos = Collections.list(seleniumFO.getData(false));
            for (FileObject fo : fos) {
                if (fo.getName().startsWith("selenium-server-standalone-") && fo.getExt().equals("jar")) {
                    serverJar = FileUtil.toFile(fo).getAbsolutePath();
                    break;
                }
            }
            if(serverJar == null) {
                // last effort - user hopefully has configured selenium server jar in Selenium node under Services Tab
                serverJar = Selenium2Server.getInstance().getServerJarLocation();
            }
        }
        seleniumServerJarTextField.setText(serverJar);
        seleniumServerJarInfoLabel.setText(Bundle.CustomizerMocha_selenium_server_jar_info());
        // listeners
        addListeners();
        // initial validation
        validateData();
    }

    private void addListeners() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        protractorDirTextField.getDocument().addDocumentListener(defaultDocumentListener);
        seleniumServerJarTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    void validateData() {
        assert EventQueue.isDispatchThread();
        protractor = protractorDirTextField.getText();
        seleniumServerJar = seleniumServerJarTextField.getText();
        validationResult = new ProtractorPreferencesValidator()
                .validateProtractor(protractor)
                .validateSeleniumServerJar(seleniumServerJar)
                .getResult();
        changeSupport.fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        protractorDirLabel = new javax.swing.JLabel();
        protractorDirTextField = new javax.swing.JTextField();
        protractorDirBrowseButton = new javax.swing.JButton();
        protractorDirInfoLabel = new javax.swing.JLabel();
        seleniumServerJarLabel = new javax.swing.JLabel();
        seleniumServerJarTextField = new javax.swing.JTextField();
        seleniumServerJarBrowseButton = new javax.swing.JButton();
        seleniumServerJarInfoLabel = new javax.swing.JLabel();

        protractorDirLabel.setLabelFor(protractorDirTextField);
        org.openide.awt.Mnemonics.setLocalizedText(protractorDirLabel, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.protractorDirLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(protractorDirBrowseButton, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.protractorDirBrowseButton.text")); // NOI18N
        protractorDirBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                protractorDirBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(protractorDirInfoLabel, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.protractorDirInfoLabel.text")); // NOI18N

        seleniumServerJarLabel.setLabelFor(protractorDirTextField);
        org.openide.awt.Mnemonics.setLocalizedText(seleniumServerJarLabel, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.seleniumServerJarLabel.text")); // NOI18N

        seleniumServerJarTextField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(seleniumServerJarBrowseButton, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.seleniumServerJarBrowseButton.text")); // NOI18N
        seleniumServerJarBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seleniumServerJarBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(seleniumServerJarInfoLabel, org.openide.util.NbBundle.getMessage(CustomizerProtractor.class, "CustomizerProtractor.seleniumServerJarInfoLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(seleniumServerJarLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(seleniumServerJarInfoLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(seleniumServerJarTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seleniumServerJarBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(protractorDirLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(protractorDirInfoLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(protractorDirTextField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(protractorDirBrowseButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(protractorDirLabel)
                    .addComponent(protractorDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(protractorDirBrowseButton))
                .addGap(6, 6, 6)
                .addComponent(protractorDirInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(seleniumServerJarLabel)
                    .addComponent(seleniumServerJarBrowseButton)
                    .addComponent(seleniumServerJarTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seleniumServerJarInfoLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CustomizerProtractor.chooser.protractor=Select Protractor file")
    private void protractorDirBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_protractorDirBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(CustomizerProtractor.class)
        .setTitle(Bundle.CustomizerProtractor_chooser_protractor())
        .setFilesOnly(true)
        .setDefaultWorkingDirectory(FileUtil.toFile(project.getProjectDirectory()))
        .forceUseOfDefaultWorkingDirectory(true)
        .showOpenDialog();
        if (file != null) {
            protractorDirTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_protractorDirBrowseButtonActionPerformed

    @NbBundle.Messages("CustomizerProtractor.chooser.seleniumServerJar=Select Selenium Server Jar file")
    private void seleniumServerJarBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleniumServerJarBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(CustomizerProtractor.class)
        .setTitle(Bundle.CustomizerProtractor_chooser_seleniumServerJar())
        .setFilesOnly(true)
        .setDefaultWorkingDirectory(FileUtil.toFile(project.getProjectDirectory()))
        .forceUseOfDefaultWorkingDirectory(true)
        .addFileFilter(new FileNameExtensionFilter("Jar File", "jar"))
        .showOpenDialog();
        if (file != null) {
            seleniumServerJarTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_seleniumServerJarBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton protractorDirBrowseButton;
    private javax.swing.JLabel protractorDirInfoLabel;
    private javax.swing.JLabel protractorDirLabel;
    private javax.swing.JTextField protractorDirTextField;
    private javax.swing.JButton seleniumServerJarBrowseButton;
    private javax.swing.JLabel seleniumServerJarInfoLabel;
    private javax.swing.JLabel seleniumServerJarLabel;
    private javax.swing.JTextField seleniumServerJarTextField;
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
            validateData();
        }

    }

}
