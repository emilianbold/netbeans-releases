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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class CustomizerPhpUnit extends JPanel {
    private static final long serialVersionUID = 2171421712032630826L;

    private final Category category;
    private final PhpProjectProperties uiProps;
    private final PhpProject project;

    public CustomizerPhpUnit(Category category, PhpProjectProperties uiProps) {

        this.category = category;
        this.uiProps = uiProps;
        project = uiProps.getProject();

        initComponents();

        initFile(uiProps.getPhpUnitBootstrap(), bootstrapCheckBox, bootstrapTextField);
        initFile(uiProps.getPhpUnitConfiguration(), configurationCheckBox, configurationTextField);
        initFile(uiProps.getPhpUnitSuite(), suiteCheckBox, suiteTextField);

        enableFile(bootstrapCheckBox.isSelected(), bootstrapLabel, bootstrapTextField, bootstrapGenerateButton, bootstrapBrowseButton);
        enableFile(configurationCheckBox.isSelected(), configurationLabel, configurationTextField, configurationGenerateButton, configurationBrowseButton);
        enableFile(suiteCheckBox.isSelected(), suiteLabel, suiteTextField, suiteBrowseButton, null);

        addListeners();
        validateData();
    }

    void enableFile(boolean enabled, JLabel label, JTextField textField, JButton browseButton, JButton generateButton) {
        label.setEnabled(enabled);
        textField.setEnabled(enabled);
        browseButton.setEnabled(enabled);
        if (generateButton != null) {
            generateButton.setEnabled(enabled);
        }
    }

    void validateData() {
        String bootstrap = ""; // NOI18N
        if (bootstrapCheckBox.isSelected()) {
            bootstrap = getValidFile(NbBundle.getMessage(CustomizerPhpUnit.class, "LBL_Bootstrap"), bootstrapTextField);
            if (bootstrap == null) {
                return;
            }
        }
        String configuration = ""; // NOI18N
        if (configurationCheckBox.isSelected()) {
            configuration = getValidFile(NbBundle.getMessage(CustomizerPhpUnit.class, "LBL_Configuration"), configurationTextField);
            if (configuration == null) {
                return;
            }
        }
        String suite = ""; // NOI18N
        if (suiteCheckBox.isSelected()) {
            suite = getValidFile(NbBundle.getMessage(CustomizerPhpUnit.class, "LBL_TestSuite"), suiteTextField);
            if (suite == null) {
                return;
            }
        }

        uiProps.setPhpUnitBootstrap(bootstrap);
        uiProps.setPhpUnitConfiguration(configuration);
        uiProps.setPhpUnitSuite(suite);

        category.setErrorMessage(null);
        category.setValid(true);
    }

    private void initFile(String file, JCheckBox checkBox, JTextField textField) {
        if (StringUtils.hasText(file)) {
            checkBox.setSelected(true);
            textField.setText(file);
        }
    }

    private void addListeners() {
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        bootstrapCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, bootstrapLabel, bootstrapTextField, bootstrapGenerateButton, bootstrapBrowseButton);
                validateData();
            }
        });
        bootstrapTextField.getDocument().addDocumentListener(defaultDocumentListener);
        configurationCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, configurationLabel, configurationTextField, configurationGenerateButton, configurationBrowseButton);
                validateData();
            }
        });
        configurationTextField.getDocument().addDocumentListener(defaultDocumentListener);
        suiteCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, suiteLabel, suiteTextField, suiteBrowseButton, null);
                validateData();
            }
        });
        suiteTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    private String getValidFile(String name, JTextField textField) {
        String file = textField.getText();
        String error = validateFile(file, name);
        if (error != null) {
            category.setErrorMessage(error);
            category.setValid(false);
            return null;
        }
        return file;
    }

    private String validateFile(String path, String name) {
        if (!StringUtils.hasText(path)) {
            return NbBundle.getMessage(CustomizerPhpUnit.class, "MSG_NoFile", name);
        }
        File file = new File(path);
        if (!file.isFile()) {
            return NbBundle.getMessage(CustomizerPhpUnit.class, "MSG_NotFile", name);
        } else if (!file.isAbsolute()) {
            return NbBundle.getMessage(CustomizerPhpUnit.class, "MSG_NotAbsoluteFile", name);
        } else if (!file.canRead()) {
            return NbBundle.getMessage(CustomizerPhpUnit.class, "MSG_NotReadableFile", name);
        }
        return null;
    }

    private File getDefaultDirectory() {
        File defaultDirectory = null;
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(project, false);
        if (testDirectory != null) {
            defaultDirectory = FileUtil.toFile(testDirectory);
        } else {
            FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
            assert sourcesDirectory != null;
            defaultDirectory = FileUtil.toFile(sourcesDirectory);
        }
        assert defaultDirectory != null;
        return defaultDirectory;
    }

    private boolean checkTestDirectory() {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(project, false);
        if (testDirectory == null) {
            if (askQuestion(NbBundle.getMessage(CustomizerPhpUnit.class, "LBL_TestsNotSet", project.getLookup().lookup(ProjectInformation.class).getDisplayName()))) {
                testDirectory = ProjectPropertiesSupport.getTestDirectory(project, true);
            }
        }
        return testDirectory != null;
    }

    private boolean askQuestion(String question) {
        NotifyDescriptor confirmation = new NotifyDescriptor.Confirmation(
                question,
                NotifyDescriptor.YES_NO_OPTION);
        return DialogDisplayer.getDefault().notify(confirmation) == NotifyDescriptor.YES_OPTION;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {


        phpUnitLabel = new JLabel();
        bootstrapLabel = new JLabel();
        bootstrapTextField = new JTextField();
        bootstrapBrowseButton = new JButton();
        bootstrapGenerateButton = new JButton();
        configurationCheckBox = new JCheckBox();
        configurationLabel = new JLabel();
        configurationTextField = new JTextField();
        configurationBrowseButton = new JButton();
        bootstrapCheckBox = new JCheckBox();
        configurationGenerateButton = new JButton();
        suiteCheckBox = new JCheckBox();
        suiteLabel = new JLabel();
        suiteTextField = new JTextField();
        suiteBrowseButton = new JButton();
        suiteInfoLabel = new JLabel();

        Mnemonics.setLocalizedText(phpUnitLabel, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.phpUnitLabel.text")); // NOI18N
        bootstrapLabel.setLabelFor(bootstrapTextField);



        Mnemonics.setLocalizedText(bootstrapLabel, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(bootstrapBrowseButton, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapBrowseButton.text"));
        bootstrapBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bootstrapBrowseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(bootstrapGenerateButton, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapGenerateButton.text"));
        bootstrapGenerateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bootstrapGenerateButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(configurationCheckBox, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationCheckBox.text"));
        configurationLabel.setLabelFor(configurationTextField);




        Mnemonics.setLocalizedText(configurationLabel, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(configurationBrowseButton, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationBrowseButton.text"));
        configurationBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configurationBrowseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(bootstrapCheckBox, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapCheckBox.text"));
        Mnemonics.setLocalizedText(configurationGenerateButton, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationGenerateButton.text"));
        configurationGenerateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configurationGenerateButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(suiteCheckBox, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteCheckBox.text"));
        suiteLabel.setLabelFor(suiteTextField);


        Mnemonics.setLocalizedText(suiteLabel, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(suiteBrowseButton, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteBrowseButton.text"));
        suiteBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                suiteBrowseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(suiteInfoLabel, NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteInfoLabel.text"));
        suiteInfoLabel.setEnabled(false);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(bootstrapLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(bootstrapTextField, GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(bootstrapBrowseButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(bootstrapGenerateButton))
            .add(configurationCheckBox)
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(configurationLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(configurationTextField, GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(configurationBrowseButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(configurationGenerateButton))
            .add(suiteCheckBox)
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(suiteLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(suiteInfoLabel)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(suiteTextField, GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(suiteBrowseButton))))
            .add(layout.createSequentialGroup()
                .add(phpUnitLabel)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(bootstrapCheckBox)
                .addContainerGap())
        );

        layout.linkSize(new Component[] {bootstrapBrowseButton, bootstrapGenerateButton, configurationBrowseButton, configurationGenerateButton, suiteBrowseButton}, GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(phpUnitLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(bootstrapCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(bootstrapLabel)
                    .add(bootstrapTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(bootstrapGenerateButton)
                    .add(bootstrapBrowseButton))
                .add(18, 18, 18)
                .add(configurationCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(configurationLabel)
                    .add(configurationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(configurationGenerateButton)
                    .add(configurationBrowseButton))
                .add(18, 18, 18)
                .add(suiteCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(suiteLabel)
                    .add(suiteTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(suiteBrowseButton))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(suiteInfoLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bootstrapBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_bootstrapBrowseButtonActionPerformed
        File file = Utils.browseFileAction(this, getDefaultDirectory(), NbBundle.getMessage(CustomizerPhpUnit.class, "LBL_SelectBootstrap"));
        if (file != null) {
            bootstrapTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_bootstrapBrowseButtonActionPerformed

    private void bootstrapGenerateButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_bootstrapGenerateButtonActionPerformed
        if (checkTestDirectory()) {
            File bootstrap = PhpUnit.createBootstrapFile(project);
            if (bootstrap != null) {
                bootstrapTextField.setText(bootstrap.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_bootstrapGenerateButtonActionPerformed

    private void configurationBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configurationBrowseButtonActionPerformed
        File file = Utils.browseFileAction(this, getDefaultDirectory(), NbBundle.getMessage(CustomizerPhpUnit.class, "LBL_SelectConfiguration"));
        if (file != null) {
            configurationTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_configurationBrowseButtonActionPerformed

    private void configurationGenerateButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configurationGenerateButtonActionPerformed
        if (checkTestDirectory()) {
            File configuration = PhpUnit.createConfigurationFile(project);
            if (configuration != null) {
                configurationTextField.setText(configuration.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_configurationGenerateButtonActionPerformed

    private void suiteBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_suiteBrowseButtonActionPerformed
        File file = Utils.browseFileAction(this, getDefaultDirectory(), NbBundle.getMessage(CustomizerPhpUnit.class, "LBL_SelectSuite"));
        if (file != null) {
            suiteTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_suiteBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton bootstrapBrowseButton;
    private JCheckBox bootstrapCheckBox;
    private JButton bootstrapGenerateButton;
    private JLabel bootstrapLabel;
    private JTextField bootstrapTextField;
    private JButton configurationBrowseButton;
    private JCheckBox configurationCheckBox;
    private JButton configurationGenerateButton;
    private JLabel configurationLabel;
    private JTextField configurationTextField;
    private JLabel phpUnitLabel;
    private JButton suiteBrowseButton;
    private JCheckBox suiteCheckBox;
    private JLabel suiteInfoLabel;
    private JLabel suiteLabel;
    private JTextField suiteTextField;
    // End of variables declaration//GEN-END:variables

    private final class DefaultDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }
        private void processUpdate() {
            validateData();
        }
    }
}
