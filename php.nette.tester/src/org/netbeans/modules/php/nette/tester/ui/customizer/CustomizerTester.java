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

package org.netbeans.modules.php.nette.tester.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.nette.tester.preferences.TesterPreferences;
import org.netbeans.modules.php.nette.tester.preferences.TesterPreferencesValidator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class CustomizerTester extends JPanel implements HelpCtx.Provider {

    private final ProjectCustomizer.Category category;
    private final PhpModule phpModule;


    public CustomizerTester(ProjectCustomizer.Category category, PhpModule phpModule) {
        assert category != null;
        assert phpModule != null;

        this.category = category;
        this.phpModule = phpModule;

        initComponents();
        init();
    }

    private void init() {
        initFile(TesterPreferences.isPhpIniEnabled(phpModule),
                TesterPreferences.getPhpIniPath(phpModule),
                phpIniCheckBox, phpIniTextField);
        initFile(TesterPreferences.isTesterEnabled(phpModule),
                TesterPreferences.getTesterPath(phpModule),
                testerCheckBox, testerTextField);

        enableFile(phpIniCheckBox.isSelected(), phpIniLabel, phpIniTextField, phpIniBrowseButton);
        enableFile(testerCheckBox.isSelected(), testerLabel, testerTextField, testerBrowseButton);

        addListeners();
        validateData();
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                storeData();
            }
        });
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.nette.tester.ui.customizer.CustomizerTester"); // NOI18N
    }

    void enableFile(boolean enabled, JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
        }
    }

    void validateData() {
        ValidationResult result = new TesterPreferencesValidator()
                .validatePhpIni(phpIniCheckBox.isSelected(), phpIniTextField.getText())
                .validateTester(testerCheckBox.isSelected(), testerTextField.getText())
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
        TesterPreferences.setPhpIniEnabled(phpModule, phpIniCheckBox.isSelected());
        TesterPreferences.setPhpIniPath(phpModule, phpIniTextField.getText());
        TesterPreferences.setTesterEnabled(phpModule, testerCheckBox.isSelected());
        TesterPreferences.setTesterPath(phpModule, testerTextField.getText());
    }

    private void initFile(boolean enabled, String file, JCheckBox checkBox, JTextField textField) {
        checkBox.setSelected(enabled);
        textField.setText(file);
    }

    private void addListeners() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();

        phpIniCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, phpIniLabel, phpIniTextField, phpIniBrowseButton);
                validateData();
            }
        });
        phpIniTextField.getDocument().addDocumentListener(defaultDocumentListener);

        testerCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, testerLabel, testerTextField, testerBrowseButton);
                validateData();
            }
        });
        testerTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    private File getDefaultDirectory() {
        File defaultDirectory;
        FileObject testDirectory = phpModule.getTestDirectory(null);
        if (testDirectory != null) {
            defaultDirectory = FileUtil.toFile(testDirectory);
        } else {
            FileObject sourcesDirectory = phpModule.getSourceDirectory();
            assert sourcesDirectory != null;
            defaultDirectory = FileUtil.toFile(sourcesDirectory);
        }
        assert defaultDirectory != null;
        return defaultDirectory;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        phpIniCheckBox = new JCheckBox();
        phpIniLabel = new JLabel();
        phpIniTextField = new JTextField();
        phpIniBrowseButton = new JButton();
        testerCheckBox = new JCheckBox();
        testerLabel = new JLabel();
        testerTextField = new JTextField();
        testerBrowseButton = new JButton();

        Mnemonics.setLocalizedText(phpIniCheckBox, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.phpIniCheckBox.text")); // NOI18N

        phpIniLabel.setLabelFor(phpIniTextField);
        Mnemonics.setLocalizedText(phpIniLabel, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.phpIniLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(phpIniBrowseButton, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.phpIniBrowseButton.text")); // NOI18N
        phpIniBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpIniBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(testerCheckBox, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.testerCheckBox.text")); // NOI18N

        testerLabel.setLabelFor(testerTextField);
        Mnemonics.setLocalizedText(testerLabel, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.testerLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(testerBrowseButton, NbBundle.getMessage(CustomizerTester.class, "CustomizerTester.testerBrowseButton.text")); // NOI18N
        testerBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                testerBrowseButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(phpIniCheckBox)
                    .addComponent(testerCheckBox))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(phpIniLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phpIniTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phpIniBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(testerLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testerTextField, GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testerBrowseButton))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {phpIniBrowseButton, testerBrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(phpIniCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(phpIniLabel)
                    .addComponent(phpIniTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(phpIniBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testerCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(testerLabel)
                    .addComponent(testerTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(testerBrowseButton))
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages({
        "CustomizerTester.chooser.php.ini=Select file or folder for php.ini",
        "CustomizerTester.chooser.php.ini.ok=Select",
    })
    private void phpIniBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpIniBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerTester.class)
                .setTitle(Bundle.CustomizerTester_chooser_php_ini())
                .setApproveText(Bundle.CustomizerTester_chooser_php_ini_ok())
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            phpIniTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_phpIniBrowseButtonActionPerformed

    @NbBundle.Messages("CustomizerTester.chooser.tester=Select Tester file")
    private void testerBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_testerBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CustomizerTester.class)
                .setTitle(Bundle.CustomizerTester_chooser_tester())
                .setFilesOnly(true)
                .setDefaultWorkingDirectory(getDefaultDirectory())
                .forceUseOfDefaultWorkingDirectory(true)
                .showOpenDialog();
        if (file != null) {
            testerTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_testerBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton phpIniBrowseButton;
    private JCheckBox phpIniCheckBox;
    private JLabel phpIniLabel;
    private JTextField phpIniTextField;
    private JButton testerBrowseButton;
    private JCheckBox testerCheckBox;
    private JLabel testerLabel;
    private JTextField testerTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

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
            validateData();
        }

    }

}
