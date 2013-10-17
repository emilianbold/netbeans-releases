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

package org.netbeans.modules.javascript.karma.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.api.Karma;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferencesValidator;
import org.netbeans.modules.javascript.karma.util.ValidationResult;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class CustomizerKarma extends JPanel {

    private final ProjectCustomizer.Category category;
    private final Project project;


    public CustomizerKarma(ProjectCustomizer.Category category, Project project) {
        assert category != null;
        assert project != null;

        this.category = category;
        this.project = project;

        initComponents();
        init();
    }

    private void init() {
        // data
        karmaTextField.setText(KarmaPreferences.getInstance().getKarma(project));
        configTextField.setText(KarmaPreferences.getInstance().getConfig(project));
        // listeners
        addListeners();
        // initial validation
        validateData();
        // store listener
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                storeData();
            }
        });
    }

    private void addListeners() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        karmaTextField.getDocument().addDocumentListener(defaultDocumentListener);
        configTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    void validateData() {
        ValidationResult result = new KarmaPreferencesValidator()
                .validateKarma(karmaTextField.getText())
                .validateConfig(configTextField.getText())
                .getResult();
        for (ValidationResult.Message message : result.getErrors()) {
            category.setErrorMessage(message.getMessage());
            category.setValid(false);
            return;
        }
        for (ValidationResult.Message message : result.getWarnings()) {
            category.setErrorMessage(message.getMessage());
            category.setValid(true);
            return;
        }
        category.setErrorMessage(null);
        category.setValid(true);
    }

    void storeData() {
        KarmaPreferences.getInstance().setKarma(project, karmaTextField.getText());
        KarmaPreferences.getInstance().setConfig(project, configTextField.getText());
    }

    private File getProjectDirectory() {
        return FileUtil.toFile(project.getProjectDirectory());
    }

    private File getConfigDirectory() {
        Karma.ConfigFolderProvider configFolderProvider = project.getLookup().lookup(Karma.ConfigFolderProvider.class);
        if (configFolderProvider != null) {
            File configFolder = configFolderProvider.getConfigFolder();
            if (configFolder != null
                    && configFolder.isDirectory()) {
                return configFolder;
            }
        }
        return getProjectDirectory();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        karmaLabel = new JLabel();
        karmaTextField = new JTextField();
        karmaBrowseButton = new JButton();
        configLabel = new JLabel();
        configTextField = new JTextField();
        configBrowseButton = new JButton();

        karmaLabel.setLabelFor(karmaTextField);
        Mnemonics.setLocalizedText(karmaLabel, NbBundle.getMessage(CustomizerKarma.class, "CustomizerKarma.karmaLabel.text")); // NOI18N

        karmaTextField.setColumns(30);

        Mnemonics.setLocalizedText(karmaBrowseButton, NbBundle.getMessage(CustomizerKarma.class, "CustomizerKarma.karmaBrowseButton.text")); // NOI18N
        karmaBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                karmaBrowseButtonActionPerformed(evt);
            }
        });

        configLabel.setLabelFor(configTextField);
        Mnemonics.setLocalizedText(configLabel, NbBundle.getMessage(CustomizerKarma.class, "CustomizerKarma.configLabel.text")); // NOI18N

        configTextField.setColumns(30);

        Mnemonics.setLocalizedText(configBrowseButton, NbBundle.getMessage(CustomizerKarma.class, "CustomizerKarma.configBrowseButton.text")); // NOI18N
        configBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configBrowseButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(configLabel)
                    .addComponent(karmaLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(karmaTextField, GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(karmaBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(configTextField, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configBrowseButton))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {configBrowseButton, karmaBrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(karmaLabel)
                    .addComponent(karmaTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(karmaBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(configLabel)
                    .addComponent(configTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(configBrowseButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CustomizerKarma.chooser.karma=Select Karma file")
    private void karmaBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_karmaBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerKarma.class)
                .setTitle(Bundle.CustomizerKarma_chooser_karma())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getProjectDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            karmaTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_karmaBrowseButtonActionPerformed

    @NbBundle.Messages("CustomizerKarma.chooser.config=Select Karma configuration file")
    private void configBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerKarma.class)
                .setTitle(Bundle.CustomizerKarma_chooser_config())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getConfigDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            configTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_configBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton configBrowseButton;
    private JLabel configLabel;
    private JTextField configTextField;
    private JButton karmaBrowseButton;
    private JLabel karmaLabel;
    private JTextField karmaTextField;
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
