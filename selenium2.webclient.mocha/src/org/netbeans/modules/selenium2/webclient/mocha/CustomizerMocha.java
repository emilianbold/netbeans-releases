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

package org.netbeans.modules.selenium2.webclient.mocha;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaPreferences;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaPreferencesValidator;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public final class CustomizerMocha extends javax.swing.JPanel {

    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private volatile String mochaInstallFolder;

    // @GuardedBy("EDT")
    private ValidationResult validationResult;


    public CustomizerMocha(Project project) {
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

    public String getMochaInstallFolder() {
        return mochaInstallFolder;
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

    @NbBundle.Messages("CustomizerMocha.mocha.dir.info=Full path of mocha installation dir (typically node_modules/mocha).")
    private void init() {
        assert EventQueue.isDispatchThread();
        // data
        mochaDirTextField.setText(MochaPreferences.getMochaDir(project));
        mochaDirInfoLabel.setText(Bundle.CustomizerMocha_mocha_dir_info());
        // listeners
        addListeners();
        // initial validation
        validateData();
    }

    private void addListeners() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        mochaDirTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    void validateData() {
        assert EventQueue.isDispatchThread();
        mochaInstallFolder = mochaDirTextField.getText();
        validationResult = new MochaPreferencesValidator()
                .validateMochaInstallFolder(mochaInstallFolder)
                .getResult();
        changeSupport.fireChange();
    }


    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mochaDirLabel = new JLabel();
        mochaDirTextField = new JTextField();
        mochaDirBrowseButton = new JButton();
        mochaDirSearchButton = new JButton();
        mochaDirInfoLabel = new JLabel();

        Mnemonics.setLocalizedText(mochaDirLabel, NbBundle.getMessage(CustomizerMocha.class, "CustomizerMocha.mochaDirLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(mochaDirBrowseButton, NbBundle.getMessage(CustomizerMocha.class, "CustomizerMocha.mochaDirBrowseButton.text")); // NOI18N
        mochaDirBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mochaDirBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(mochaDirSearchButton, NbBundle.getMessage(CustomizerMocha.class, "CustomizerMocha.mochaDirSearchButton.text")); // NOI18N
        mochaDirSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mochaDirSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(mochaDirInfoLabel, NbBundle.getMessage(CustomizerMocha.class, "CustomizerMocha.mochaDirInfoLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mochaDirLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mochaDirInfoLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mochaDirTextField, GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mochaDirBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mochaDirSearchButton))))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(mochaDirLabel)
                    .addComponent(mochaDirTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(mochaDirBrowseButton)
                    .addComponent(mochaDirSearchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mochaDirInfoLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CustomizerMocha.chooser.config=Select mocha install location")
    private void mochaDirBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_mochaDirBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(CustomizerMocha.class)
        .setTitle(Bundle.CustomizerMocha_chooser_config())
//        .setFilesOnly(true)
        .setDirectoriesOnly(true)
        .setDefaultWorkingDirectory(FileUtil.toFile(project.getProjectDirectory()))
        .forceUseOfDefaultWorkingDirectory(true)
        .showOpenDialog();
        if (file != null) {
            mochaDirTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_mochaDirBrowseButtonActionPerformed

    @NbBundle.Messages("CustomizerMocha.config.none=No mocha executable was found.")
    private void mochaDirSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_mochaDirSearchButtonActionPerformed
        assert EventQueue.isDispatchThread();
        String installFolder = MochaPreferences.getMochaDir(project);
        if (installFolder != null) {
            File configFile = new File(installFolder);
            mochaDirTextField.setText(configFile.getAbsolutePath());
            return;
        }
        // no mochaInstallFolder found
        StatusDisplayer.getDefault().setStatusText(Bundle.CustomizerMocha_config_none());
    }//GEN-LAST:event_mochaDirSearchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton mochaDirBrowseButton;
    private JLabel mochaDirInfoLabel;
    private JLabel mochaDirLabel;
    private JButton mochaDirSearchButton;
    private JTextField mochaDirTextField;
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
