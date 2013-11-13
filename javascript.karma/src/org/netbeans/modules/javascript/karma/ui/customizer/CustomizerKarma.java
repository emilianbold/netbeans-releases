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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferencesValidator;
import org.netbeans.modules.javascript.karma.util.KarmaUtils;
import org.netbeans.modules.javascript.karma.util.ValidationResult;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
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
        karmaTextField.setText(KarmaPreferences.getKarma(project));
        configTextField.setText(KarmaPreferences.getConfig(project));
        autowatchCheckBox.setSelected(KarmaPreferences.isAutowatch(project));
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
        ItemListener defaultItemListener = new DefaultItemListener();
        karmaTextField.getDocument().addDocumentListener(defaultDocumentListener);
        configTextField.getDocument().addDocumentListener(defaultDocumentListener);
        autowatchCheckBox.addItemListener(defaultItemListener);
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
        KarmaPreferences.setKarma(project, karmaTextField.getText());
        KarmaPreferences.setConfig(project, configTextField.getText());
        KarmaPreferences.setAutowatch(project, autowatchCheckBox.isSelected());
    }

    private File getProjectDirectory() {
        return FileUtil.toFile(project.getProjectDirectory());
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
        karmaSearchButton = new JButton();
        configLabel = new JLabel();
        configTextField = new JTextField();
        configBrowseButton = new JButton();
        configSearchButton = new JButton();
        autowatchCheckBox = new JCheckBox();

        karmaLabel.setLabelFor(karmaTextField);
        Mnemonics.setLocalizedText(karmaLabel, NbBundle.getMessage(CustomizerKarma.class, "CustomizerKarma.karmaLabel.text")); // NOI18N

        karmaTextField.setColumns(30);

        Mnemonics.setLocalizedText(karmaBrowseButton, NbBundle.getMessage(CustomizerKarma.class, "CustomizerKarma.karmaBrowseButton.text")); // NOI18N
        karmaBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                karmaBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(karmaSearchButton, NbBundle.getMessage(CustomizerKarma.class, "CustomizerKarma.karmaSearchButton.text")); // NOI18N
        karmaSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                karmaSearchButtonActionPerformed(evt);
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

        Mnemonics.setLocalizedText(configSearchButton, NbBundle.getMessage(CustomizerKarma.class, "CustomizerKarma.configSearchButton.text")); // NOI18N
        configSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(autowatchCheckBox, NbBundle.getMessage(CustomizerKarma.class, "CustomizerKarma.autowatchCheckBox.text")); // NOI18N

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
                        .addComponent(karmaTextField, GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(karmaBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(karmaSearchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(configTextField, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configSearchButton))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(autowatchCheckBox)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {configBrowseButton, karmaBrowseButton});

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {configSearchButton, karmaSearchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(karmaLabel)
                    .addComponent(karmaTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(karmaBrowseButton)
                    .addComponent(karmaSearchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(configLabel)
                    .addComponent(configTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(configBrowseButton)
                    .addComponent(configSearchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autowatchCheckBox))
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
                .setDefaultWorkingDirectory(KarmaUtils.getConfigDir(project))
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            configTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_configBrowseButtonActionPerformed

    @NbBundle.Messages("CustomizerKarma.karma.none=No Karma executable was found.")
    private void karmaSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_karmaSearchButtonActionPerformed
        File karma = KarmaUtils.findKarma(project);
        if (karma != null) {
            karmaTextField.setText(karma.getAbsolutePath());
            return;
        }
        // no karma found
        StatusDisplayer.getDefault().setStatusText(Bundle.CustomizerKarma_karma_none());
    }//GEN-LAST:event_karmaSearchButtonActionPerformed

    @NbBundle.Messages("CustomizerKarma.config.none=No Karma configuration was found.")
    private void configSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configSearchButtonActionPerformed
        File karmaConfig = KarmaUtils.findKarmaConfig(KarmaUtils.getConfigDir(project));
        if (karmaConfig != null) {
            configTextField.setText(karmaConfig.getAbsolutePath());
            return;
        }
        // no config found
        StatusDisplayer.getDefault().setStatusText(Bundle.CustomizerKarma_config_none());
    }//GEN-LAST:event_configSearchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox autowatchCheckBox;
    private JButton configBrowseButton;
    private JLabel configLabel;
    private JButton configSearchButton;
    private JTextField configTextField;
    private JButton karmaBrowseButton;
    private JLabel karmaLabel;
    private JButton karmaSearchButton;
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

    private final class DefaultItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            validateData();
        }

    }

}
