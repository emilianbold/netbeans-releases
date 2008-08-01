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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

        interpreterLabel = new javax.swing.JLabel();
        interpreterTextField = new javax.swing.JTextField();
        configureButton = new javax.swing.JButton();
        argsLabel = new javax.swing.JLabel();
        argsTextField = new javax.swing.JTextField();
        runAsLabel = new javax.swing.JLabel();
        runAsCombo = new javax.swing.JComboBox();
        indexFileLabel = new javax.swing.JLabel();
        indexFileTextField = new javax.swing.JTextField();
        indexFileBrowseButton = new javax.swing.JButton();
        hintLabel = new javax.swing.JLabel();

        interpreterLabel.setLabelFor(interpreterTextField);
        org.openide.awt.Mnemonics.setLocalizedText(interpreterLabel, org.openide.util.NbBundle.getMessage(RunAsScript.class, "LBL_PhpInterpreter")); // NOI18N

        interpreterTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(configureButton, org.openide.util.NbBundle.getMessage(RunAsScript.class, "LBL_Configure")); // NOI18N
        configureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        argsLabel.setLabelFor(argsTextField);
        org.openide.awt.Mnemonics.setLocalizedText(argsLabel, org.openide.util.NbBundle.getMessage(RunAsScript.class, "LBL_Arguments")); // NOI18N

        runAsLabel.setLabelFor(runAsCombo);
        org.openide.awt.Mnemonics.setLocalizedText(runAsLabel, org.openide.util.NbBundle.getMessage(RunAsScript.class, "LBL_RunAs")); // NOI18N

        indexFileLabel.setLabelFor(indexFileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(indexFileLabel, org.openide.util.NbBundle.getMessage(RunAsScript.class, "LBL_IndexFile")); // NOI18N

        indexFileTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(indexFileBrowseButton, org.openide.util.NbBundle.getMessage(RunAsScript.class, "LBL_Browse")); // NOI18N
        indexFileBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(hintLabel, "dummy"); // NOI18N
        hintLabel.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(runAsLabel)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(interpreterLabel)
                    .add(indexFileLabel)
                    .add(argsLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(hintLabel)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, argsTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(indexFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(indexFileBrowseButton))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, runAsCombo, 0, 222, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(interpreterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(configureButton)))
                        .add(0, 0, 0))))
        );

        layout.linkSize(new java.awt.Component[] {configureButton, indexFileBrowseButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(runAsLabel)
                    .add(runAsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(interpreterLabel)
                    .add(interpreterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(configureButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(indexFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(indexFileLabel)
                    .add(indexFileBrowseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(argsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(argsLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(hintLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        OptionsDisplayer.getDefault().open(PHPOptionsCategory.PATH_IN_LAYER);
    }//GEN-LAST:event_configureButtonActionPerformed

    private void indexFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        Utils.browseSourceFile(project, indexFileTextField);
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel argsLabel;
    private javax.swing.JTextField argsTextField;
    private javax.swing.JButton configureButton;
    private javax.swing.JLabel hintLabel;
    private javax.swing.JButton indexFileBrowseButton;
    private javax.swing.JLabel indexFileLabel;
    private javax.swing.JTextField indexFileTextField;
    private javax.swing.JLabel interpreterLabel;
    private javax.swing.JTextField interpreterTextField;
    private javax.swing.JComboBox runAsCombo;
    private javax.swing.JLabel runAsLabel;
    // End of variables declaration//GEN-END:variables
}
