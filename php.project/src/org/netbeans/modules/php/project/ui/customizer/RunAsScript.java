/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.connections.ConfigManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * @author  Radek Matous, Tomas Mysik
 */
public class RunAsScript extends RunAsPanel.InsidePanel {
    private static final long serialVersionUID = -5593489197914071L;
    private final PhpProject project;
    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    private final String displayName;
    private final PropertyChangeListener phpInterpreterListener;
    final Category category;

    public RunAsScript(PhpProject project, ConfigManager manager, Category category) {
        this(project, manager, category, NbBundle.getMessage(RunAsScript.class, "LBL_ConfigScript"));
    }

    private RunAsScript(PhpProject project, ConfigManager manager, Category category, String displayName) {
        super(manager);
        this.project = project;
        this.category = category;
        this.displayName = displayName;

        initComponents();
        this.labels = new JLabel[] {
            indexFileLabel,
            argsLabel
        };
        this.textFields = new JTextField[] {
            indexFileTextField,
            argsTextField
        };
        this.propertyNames = new String[] {
            PhpProjectProperties.INDEX_FILE,
            PhpProjectProperties.ARGS
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;
        for (int i = 0; i < textFields.length; i++) {
            DocumentListener dl = new FieldUpdater(propertyNames[i], labels[i], textFields[i]);
            textFields[i].getDocument().addDocumentListener(dl);
        }

        // php cli
        loadPhpInterpreter();
        phpInterpreterListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (PhpOptions.PROP_PHP_INTERPRETER.equals(evt.getPropertyName())) {
                    loadPhpInterpreter();
                    composeHint();
                    validateFields();
                }
            }
        };
        PhpOptions phpOptions = PhpOptions.getInstance();
        phpOptions.addPropertyChangeListener(WeakListeners.propertyChange(phpInterpreterListener, phpOptions));
        composeHint();
    }

    void loadPhpInterpreter() {
        interpreterTextField.setText(PhpOptions.getInstance().getPhpInterpreter());
    }

    @Override
    protected RunAsType getRunAsType() {
        return PhpProjectProperties.RunAsType.SCRIPT;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    protected JLabel getRunAsLabel() {
        return runAsLabel;
    }

    @Override
    public JComboBox getRunAsCombo() {
        return runAsCombo;
    }

    protected void loadFields() {
        for (int i = 0; i < textFields.length; i++) {
            textFields[i].setText(getValue(propertyNames[i]));
        }
    }

    protected void validateFields() {
        String phpInterpreter = interpreterTextField.getText().trim();
        String args = argsTextField.getText().trim();
        String err = RunAsValidator.validateScriptFields(phpInterpreter, null, args);
        category.setErrorMessage(err);
        category.setValid(err == null);
    }

    void composeHint() {
        String php = interpreterTextField.getText();
        String script = "./" + indexFileTextField.getText(); // NOI18N
        String args = argsTextField.getText();
        hintLabel.setText(php + " " + script + " " + args); // NOI18N
    }

    private class FieldUpdater extends TextFieldUpdater {

        public FieldUpdater(String propName, JLabel label, JTextField field) {
            super(propName, label, field);
        }

        protected final String getDefaultValue() {
            return RunAsScript.this.getDefaultValue(getPropName());
        }

        @Override
        protected void processUpdate() {
            super.processUpdate();
            composeHint();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        interpreterLabel = new JLabel();
        interpreterTextField = new JTextField();
        configureButton = new JButton();
        argsLabel = new JLabel();
        argsTextField = new JTextField();
        runAsLabel = new JLabel();
        runAsCombo = new JComboBox();
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();
        hintLabel = new JLabel();

        setFocusTraversalPolicy(null);

        interpreterLabel.setLabelFor(interpreterTextField);

        Mnemonics.setLocalizedText(interpreterLabel, NbBundle.getMessage(RunAsScript.class, "LBL_PhpInterpreter")); // NOI18N
        interpreterTextField.setEditable(false);
        Mnemonics.setLocalizedText(configureButton, NbBundle.getMessage(RunAsScript.class, "LBL_Configure"));
        configureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        argsLabel.setLabelFor(argsTextField);

        Mnemonics.setLocalizedText(argsLabel, NbBundle.getMessage(RunAsScript.class, "LBL_Arguments")); // NOI18N
        runAsLabel.setLabelFor(runAsCombo);

        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsScript.class, "LBL_RunAs")); // NOI18N
        indexFileLabel.setLabelFor(indexFileTextField);

        Mnemonics.setLocalizedText(indexFileLabel, NbBundle.getMessage(RunAsScript.class, "LBL_IndexFile")); // NOI18N
        indexFileTextField.setEditable(false);

        Mnemonics.setLocalizedText(indexFileBrowseButton,NbBundle.getMessage(RunAsScript.class, "LBL_Browse")); // NOI18N
        indexFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(hintLabel, "dummy");
        hintLabel.setEnabled(false);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(runAsLabel)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(interpreterLabel)
                    .add(indexFileLabel)
                    .add(argsLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(hintLabel)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(GroupLayout.TRAILING, argsTextField, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(indexFileTextField, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(indexFileBrowseButton))
                            .add(GroupLayout.TRAILING, runAsCombo, 0, 222, Short.MAX_VALUE)
                            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(interpreterTextField, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(configureButton)))
                        .add(0, 0, 0))))
        
        );

        layout.linkSize(new Component[] {configureButton, indexFileBrowseButton}, GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(runAsLabel)
                    .add(runAsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(interpreterLabel)
                    .add(interpreterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(configureButton))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(indexFileTextField, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                    .add(indexFileLabel)
                    .add(indexFileBrowseButton))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(argsTextField, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                    .add(argsLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(hintLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        
        );

        interpreterLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterLabel.AccessibleContext.accessibleName")); // NOI18N
        interpreterLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterLabel.AccessibleContext.accessibleDescription")); // NOI18N
        interpreterTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterTextField.AccessibleContext.accessibleName")); // NOI18N
        interpreterTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterTextField.AccessibleContext.accessibleDescription")); // NOI18N
        configureButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.configureButton.AccessibleContext.accessibleName")); // NOI18N
        configureButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.configureButton.AccessibleContext.accessibleDescription")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.argsLabel.AccessibleContext.accessibleName")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.argsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.argsTextField.AccessibleContext.accessibleName")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.argsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsLabel.AccessibleContext.accessibleName")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsCombo.AccessibleContext.accessibleName")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsCombo.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileLabel.AccessibleContext.accessibleName")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileTextField.AccessibleContext.accessibleName")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        hintLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.hintLabel.AccessibleContext.accessibleName")); // NOI18N
        hintLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.hintLabel.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        OptionsDisplayer.getDefault().open(PHPOptionsCategory.PATH_IN_LAYER);
    }//GEN-LAST:event_configureButtonActionPerformed

    private void indexFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        Utils.browseSourceFile(project, indexFileTextField);
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel argsLabel;
    private JTextField argsTextField;
    private JButton configureButton;
    private JLabel hintLabel;
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JLabel interpreterLabel;
    private JTextField interpreterTextField;
    private JComboBox runAsCombo;
    private JLabel runAsLabel;
    // End of variables declaration//GEN-END:variables
}
