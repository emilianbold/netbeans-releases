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
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.phpunit.PhpUnit;
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
public final class CustomizerPhpUnit extends JPanel {
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
        bootstrapForCreateTestsCheckBox.setSelected(uiProps.getPhpUnitBootstrapForCreateTests());
        initFile(uiProps.getPhpUnitConfiguration(), configurationCheckBox, configurationTextField);
        initFile(uiProps.getPhpUnitSuite(), suiteCheckBox, suiteTextField);

        enableFile(bootstrapCheckBox.isSelected(), bootstrapLabel, bootstrapTextField, bootstrapGenerateButton, bootstrapBrowseButton, bootstrapForCreateTestsCheckBox);
        enableFile(configurationCheckBox.isSelected(), configurationLabel, configurationTextField, configurationGenerateButton, configurationBrowseButton);
        enableFile(suiteCheckBox.isSelected(), suiteLabel, suiteTextField, suiteBrowseButton, suiteInfoLabel);

        addListeners();
        validateData();
    }

    void enableFile(boolean enabled, JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
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
            configuration = getValidFile(NbBundle.getMessage(CustomizerPhpUnit.class, "LBL_XmlConfiguration"), configurationTextField);
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
        uiProps.setPhpUnitBootstrapForCreateTests(bootstrapForCreateTestsCheckBox.isSelected());
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
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED,
                        bootstrapLabel, bootstrapTextField, bootstrapGenerateButton, bootstrapBrowseButton, bootstrapForCreateTestsCheckBox);
                validateData();
            }
        });
        bootstrapTextField.getDocument().addDocumentListener(defaultDocumentListener);
        bootstrapForCreateTestsCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                validateData();
            }
        });

        configurationCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, configurationLabel, configurationTextField, configurationGenerateButton, configurationBrowseButton);
                validateData();
            }
        });
        configurationTextField.getDocument().addDocumentListener(defaultDocumentListener);

        suiteCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enableFile(e.getStateChange() == ItemEvent.SELECTED, suiteLabel, suiteTextField, suiteBrowseButton, suiteInfoLabel);
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

        phpUnitLabel = new javax.swing.JLabel();
        bootstrapLabel = new javax.swing.JLabel();
        bootstrapTextField = new javax.swing.JTextField();
        bootstrapBrowseButton = new javax.swing.JButton();
        bootstrapGenerateButton = new javax.swing.JButton();
        bootstrapForCreateTestsCheckBox = new javax.swing.JCheckBox();
        configurationCheckBox = new javax.swing.JCheckBox();
        configurationLabel = new javax.swing.JLabel();
        configurationTextField = new javax.swing.JTextField();
        configurationBrowseButton = new javax.swing.JButton();
        bootstrapCheckBox = new javax.swing.JCheckBox();
        configurationGenerateButton = new javax.swing.JButton();
        suiteCheckBox = new javax.swing.JCheckBox();
        suiteLabel = new javax.swing.JLabel();
        suiteTextField = new javax.swing.JTextField();
        suiteBrowseButton = new javax.swing.JButton();
        suiteInfoLabel = new javax.swing.JLabel();

        setFocusTraversalPolicy(null);

        phpUnitLabel.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(phpUnitLabel, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.phpUnitLabel.text")); // NOI18N

        bootstrapLabel.setLabelFor(bootstrapTextField);
        org.openide.awt.Mnemonics.setLocalizedText(bootstrapLabel, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bootstrapBrowseButton, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapBrowseButton.text")); // NOI18N
        bootstrapBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bootstrapBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bootstrapGenerateButton, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapGenerateButton.text")); // NOI18N
        bootstrapGenerateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bootstrapGenerateButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bootstrapForCreateTestsCheckBox, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapForCreateTestsCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(configurationCheckBox, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationCheckBox.text")); // NOI18N

        configurationLabel.setLabelFor(configurationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(configurationLabel, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(configurationBrowseButton, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationBrowseButton.text")); // NOI18N
        configurationBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bootstrapCheckBox, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(configurationGenerateButton, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationGenerateButton.text")); // NOI18N
        configurationGenerateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationGenerateButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(suiteCheckBox, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteCheckBox.text")); // NOI18N

        suiteLabel.setLabelFor(suiteTextField);
        org.openide.awt.Mnemonics.setLocalizedText(suiteLabel, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(suiteBrowseButton, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteBrowseButton.text")); // NOI18N
        suiteBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suiteBrowseButtonActionPerformed(evt);
            }
        });

        suiteInfoLabel.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(suiteInfoLabel, org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteInfoLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(configurationCheckBox)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(configurationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configurationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configurationBrowseButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configurationGenerateButton))
            .addComponent(suiteCheckBox)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(suiteLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(suiteInfoLabel)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(suiteTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(suiteBrowseButton))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(phpUnitLabel)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(bootstrapCheckBox)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bootstrapForCreateTestsCheckBox)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bootstrapLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bootstrapTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bootstrapBrowseButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bootstrapGenerateButton))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {bootstrapBrowseButton, bootstrapGenerateButton, configurationBrowseButton, configurationGenerateButton, suiteBrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(phpUnitLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bootstrapCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bootstrapLabel)
                    .addComponent(bootstrapTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bootstrapGenerateButton)
                    .addComponent(bootstrapBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bootstrapForCreateTestsCheckBox)
                .addGap(18, 18, 18)
                .addComponent(configurationCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configurationLabel)
                    .addComponent(configurationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configurationGenerateButton)
                    .addComponent(configurationBrowseButton))
                .addGap(18, 18, 18)
                .addComponent(suiteCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(suiteLabel)
                    .addComponent(suiteTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(suiteBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(suiteInfoLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        phpUnitLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.phpUnitLabel.AccessibleContext.accessibleName")); // NOI18N
        phpUnitLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.phpUnitLabel.AccessibleContext.accessibleDescription")); // NOI18N
        bootstrapLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapLabel.AccessibleContext.accessibleName")); // NOI18N
        bootstrapLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapLabel.AccessibleContext.accessibleDescription")); // NOI18N
        bootstrapTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapTextField.AccessibleContext.accessibleName")); // NOI18N
        bootstrapTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapTextField.AccessibleContext.accessibleDescription")); // NOI18N
        bootstrapBrowseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        bootstrapBrowseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        bootstrapGenerateButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapGenerateButton.AccessibleContext.accessibleName")); // NOI18N
        bootstrapGenerateButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapGenerateButton.AccessibleContext.accessibleDescription")); // NOI18N
        bootstrapForCreateTestsCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapForCreateTestsCheckBox.AccessibleContext.accessibleName")); // NOI18N
        bootstrapForCreateTestsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapForCreateTestsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        configurationCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationCheckBox.AccessibleContext.accessibleName")); // NOI18N
        configurationCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        configurationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationLabel.AccessibleContext.accessibleName")); // NOI18N
        configurationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationLabel.AccessibleContext.accessibleDescription")); // NOI18N
        configurationTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationTextField.AccessibleContext.accessibleName")); // NOI18N
        configurationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationTextField.AccessibleContext.accessibleDescription")); // NOI18N
        configurationBrowseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        configurationBrowseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        bootstrapCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapCheckBox.AccessibleContext.accessibleName")); // NOI18N
        bootstrapCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.bootstrapCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        configurationGenerateButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationGenerateButton.AccessibleContext.accessibleName")); // NOI18N
        configurationGenerateButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.configurationGenerateButton.AccessibleContext.accessibleDescription")); // NOI18N
        suiteCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteCheckBox.AccessibleContext.accessibleName")); // NOI18N
        suiteCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        suiteLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteLabel.AccessibleContext.accessibleName")); // NOI18N
        suiteLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteLabel.AccessibleContext.accessibleDescription")); // NOI18N
        suiteTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteTextField.AccessibleContext.accessibleName")); // NOI18N
        suiteTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteTextField.AccessibleContext.accessibleDescription")); // NOI18N
        suiteBrowseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        suiteBrowseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        suiteInfoLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        suiteInfoLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.suiteInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPhpUnit.class, "CustomizerPhpUnit.AccessibleContext.accessibleDescription")); // NOI18N
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
    private javax.swing.JButton bootstrapBrowseButton;
    private javax.swing.JCheckBox bootstrapCheckBox;
    private javax.swing.JCheckBox bootstrapForCreateTestsCheckBox;
    private javax.swing.JButton bootstrapGenerateButton;
    private javax.swing.JLabel bootstrapLabel;
    private javax.swing.JTextField bootstrapTextField;
    private javax.swing.JButton configurationBrowseButton;
    private javax.swing.JCheckBox configurationCheckBox;
    private javax.swing.JButton configurationGenerateButton;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JTextField configurationTextField;
    private javax.swing.JLabel phpUnitLabel;
    private javax.swing.JButton suiteBrowseButton;
    private javax.swing.JCheckBox suiteCheckBox;
    private javax.swing.JLabel suiteInfoLabel;
    private javax.swing.JLabel suiteLabel;
    private javax.swing.JTextField suiteTextField;
    // End of variables declaration//GEN-END:variables

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
