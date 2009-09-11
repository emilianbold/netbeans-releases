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
import javax.swing.JCheckBox;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.connections.ConfigManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * @author  Radek Matous, Tomas Mysik
 */
public class RunAsScript extends RunAsPanel.InsidePanel {
    private static final long serialVersionUID = -559447897914071L;
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
            interpreterLabel,
            argsLabel,
        };
        this.textFields = new JTextField[] {
            indexFileTextField,
            interpreterTextField,
            argsTextField,
        };
        this.propertyNames = new String[] {
            PhpProjectProperties.INDEX_FILE,
            PhpProjectProperties.INTERPRETER,
            PhpProjectProperties.ARGS,
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;
        for (int i = 0; i < textFields.length; i++) {
            DocumentListener dl = new FieldUpdater(propertyNames[i], labels[i], textFields[i]);
            textFields[i].getDocument().addDocumentListener(dl);
        }

        // php cli
        defaultInterpreterCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = defaultInterpreterCheckBox.isSelected();
                interpreterBrowseButton.setEnabled(!selected);
                interpreterTextField.setEditable(!selected);
                String newValue = null;
                if (selected) {
                    newValue = getDefaultPhpInterpreter();
                } else {
                    newValue = interpreterTextField.getText();
                }
                // hack - fire event in _every_ case (need to update run configuration)
                interpreterTextField.setText(newValue + " "); // NOI18N
            }
        });
        phpInterpreterListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (PhpOptions.PROP_PHP_INTERPRETER.equals(evt.getPropertyName())) {
                    if (defaultInterpreterCheckBox.isSelected()) {
                        // #143315
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                interpreterTextField.setText(getDefaultPhpInterpreter());
                                composeHint();
                            }
                        });
                    }
                }
            }
        };
        PhpOptions phpOptions = PhpOptions.getInstance();
        phpOptions.addPropertyChangeListener(WeakListeners.propertyChange(phpInterpreterListener, phpOptions));
        composeHint();
    }

    private String getDefaultPhpInterpreter() {
        String phpInterpreter = PhpOptions.getInstance().getPhpInterpreter();
        return phpInterpreter != null ? phpInterpreter : ""; //NOI18N
    }

    private String initPhpInterpreterFields() {
        String phpInterpreter = getValue(PhpProjectProperties.INTERPRETER);
        boolean def = phpInterpreter == null || phpInterpreter.length() == 0;
        defaultInterpreterCheckBox.setSelected(def);
        interpreterBrowseButton.setEnabled(!def);
        interpreterTextField.setEditable(!def);
        if (def) {
            return getDefaultPhpInterpreter();
        }
        return phpInterpreter;
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
            String val = getValue(propertyNames[i]);
            if (PhpProjectProperties.INTERPRETER.equals(propertyNames[i])) {
                val = initPhpInterpreterFields();
            }
            textFields[i].setText(val);
        }
    }

    protected void validateFields() {
        String phpInterpreter = interpreterTextField.getText().trim();
        String indexFile = indexFileTextField.getText();
        String args = argsTextField.getText().trim();

        String err = RunAsValidator.validateScriptFields(phpInterpreter,
                FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project)), indexFile, args);
        category.setErrorMessage(err);
        // #148957 always allow to save customizer
        category.setValid(true);
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

        @Override
        protected String getPropValue() {
            if (PhpProjectProperties.INTERPRETER.equals(getPropName())
                    && defaultInterpreterCheckBox.isSelected()) {
                return ""; // NOI18N
            }
            return super.getPropValue().trim();
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
        interpreterBrowseButton = new JButton();
        defaultInterpreterCheckBox = new JCheckBox();
        configureButton = new JButton();
        argsLabel = new JLabel();
        argsTextField = new JTextField();
        runAsLabel = new JLabel();
        runAsCombo = new JComboBox();
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();
        hintLabel = new JLabel();

        setFocusTraversalPolicy(new FocusTraversalPolicy() {



            public Component getDefaultComponent(Container focusCycleRoot){
                return argsTextField;
            }//end getDefaultComponent
            public Component getFirstComponent(Container focusCycleRoot){
                return argsTextField;
            }//end getFirstComponent
            public Component getLastComponent(Container focusCycleRoot){
                return argsTextField;
            }//end getLastComponent
            public Component getComponentAfter(Container focusCycleRoot, Component aComponent){
                if(aComponent ==  runAsCombo){
                    return interpreterTextField;
                }
                if(aComponent ==  interpreterBrowseButton){
                    return defaultInterpreterCheckBox;
                }
                if(aComponent ==  defaultInterpreterCheckBox){
                    return configureButton;
                }
                if(aComponent ==  configureButton){
                    return indexFileTextField;
                }
                if(aComponent ==  indexFileTextField){
                    return indexFileBrowseButton;
                }
                if(aComponent ==  interpreterTextField){
                    return interpreterBrowseButton;
                }
                if(aComponent ==  indexFileBrowseButton){
                    return argsTextField;
                }
                return argsTextField;//end getComponentAfter
            }
            public Component getComponentBefore(Container focusCycleRoot, Component aComponent){
                if(aComponent ==  interpreterTextField){
                    return runAsCombo;
                }
                if(aComponent ==  defaultInterpreterCheckBox){
                    return interpreterBrowseButton;
                }
                if(aComponent ==  configureButton){
                    return defaultInterpreterCheckBox;
                }
                if(aComponent ==  indexFileTextField){
                    return configureButton;
                }
                if(aComponent ==  indexFileBrowseButton){
                    return indexFileTextField;
                }
                if(aComponent ==  interpreterBrowseButton){
                    return interpreterTextField;
                }
                if(aComponent ==  argsTextField){
                    return indexFileBrowseButton;
                }
                return argsTextField;//end getComponentBefore

            }}
        );

        interpreterLabel.setLabelFor(interpreterTextField);

        Mnemonics.setLocalizedText(interpreterLabel, NbBundle.getMessage(RunAsScript.class, "LBL_PhpInterpreter")); // NOI18N
        interpreterTextField.setEditable(false);
        Mnemonics.setLocalizedText(interpreterBrowseButton, NbBundle.getMessage(RunAsScript.class, "LBL_BrowseInterpreter"));
        interpreterBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                interpreterBrowseButtonActionPerformed(evt);
            }
        });

        defaultInterpreterCheckBox.setSelected(true);

        Mnemonics.setLocalizedText(defaultInterpreterCheckBox, NbBundle.getMessage(RunAsScript.class, "LBL_UseDefaultInterpreter"));
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


        Mnemonics.setLocalizedText(indexFileLabel,NbBundle.getMessage(RunAsScript.class, "LBL_IndexFile")); // NOI18N
        Mnemonics.setLocalizedText(indexFileBrowseButton, NbBundle.getMessage(RunAsScript.class, "LBL_Browse"));
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
                            .add(GroupLayout.TRAILING, argsTextField, GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(indexFileTextField, GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(indexFileBrowseButton))
                            .add(GroupLayout.TRAILING, runAsCombo, 0, 302, Short.MAX_VALUE)
                            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(interpreterTextField, GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(interpreterBrowseButton))
                            .add(layout.createSequentialGroup()
                                .add(defaultInterpreterCheckBox)
                                .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(configureButton)))
                        .add(0, 0, 0))))
        );

        layout.linkSize(new Component[] {configureButton, indexFileBrowseButton, interpreterBrowseButton}, GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(runAsLabel)
                    .add(runAsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(interpreterLabel)
                    .add(interpreterBrowseButton)
                    .add(interpreterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(defaultInterpreterCheckBox)
                    .add(configureButton))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(indexFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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
        interpreterBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        interpreterBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        defaultInterpreterCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.defaultInterpreterCheckBox.AccessibleContext.accessibleName")); // NOI18N
        defaultInterpreterCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.defaultInterpreterCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
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
        Utils.showGeneralOptionsPanel();
    }//GEN-LAST:event_configureButtonActionPerformed

    private void indexFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        Utils.browseSourceFile(project, indexFileTextField);
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    private void interpreterBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_interpreterBrowseButtonActionPerformed
        Utils.browsePhpInterpreter(this, interpreterTextField);
    }//GEN-LAST:event_interpreterBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel argsLabel;
    private JTextField argsTextField;
    private JButton configureButton;
    private JCheckBox defaultInterpreterCheckBox;
    private JLabel hintLabel;
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JButton interpreterBrowseButton;
    private JLabel interpreterLabel;
    private JTextField interpreterTextField;
    private JComboBox runAsCombo;
    private JLabel runAsLabel;
    // End of variables declaration//GEN-END:variables
}
