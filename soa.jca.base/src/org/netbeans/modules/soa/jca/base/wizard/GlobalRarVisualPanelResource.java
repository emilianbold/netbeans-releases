/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.jca.base.wizard;

import org.netbeans.modules.soa.jca.base.GlobalRarRegistry;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import org.netbeans.modules.soa.jca.base.Util;
import org.netbeans.modules.soa.jca.base.generator.api.GeneratorUtil;
import org.netbeans.modules.soa.jca.base.generator.api.JndiBrowser;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * GUI part of wizard panel
 *
 * @author echou
 */
public final class GlobalRarVisualPanelResource extends JPanel {

    private GlobalRarWizardPanelResource wizard;
    private GlobalRarRegistry globalRarRegistry;
    private Project project;
    private String rarName;
    private MethodComboBoxModel methodComboBoxModel;

    /** Creates new form GLOBALRARVisualPanel2 */
    public GlobalRarVisualPanelResource(GlobalRarWizardPanelResource wizard, Project project, String rarName) {
        this.wizard = wizard;
        this.project = project;
        this.rarName = rarName;
        globalRarRegistry = GlobalRarRegistry.getInstance();
        methodComboBoxModel = new MethodComboBoxModel();

        initComponents();

        // set component names for easier testability
        methodComboBox.setName("methodCbo");
        jndiTextField.setName("jndiTxt");
        localVarTextField.setName("localVarTxt");

        methodComboBox.addActionListener(wizard);
        methodComboBox.addActionListener(new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                updateCheckBoxStatus();
            }
        });
        MethodComboBoxEditor methodEditor = new MethodComboBoxEditor();
        methodComboBox.setEditor(methodEditor);
        jndiTextField.getDocument().addDocumentListener(wizard);
        localVarTextField.getDocument().addDocumentListener(wizard);
        returnTypeTextField.getDocument().addDocumentListener(wizard);
        if (!wizard.isContainerManaged()) {
            rollbackCheckBox.setSelected(false);
            rollbackCheckBox.setEnabled(false);
        }
    }

    public String getName() {
        return globalRarRegistry.getRar(rarName).getDisplayName() + " Declaration"; // NOI18N
    }

    public void initFromSettings(WizardDescriptor settings) {
        if (methodComboBox.getItemCount() > 0 && methodComboBox.getSelectedIndex() == -1) {
            methodComboBox.setSelectedIndex(0);
        }

        String localVar = (String) settings.getProperty(GlobalRarWizardAction.LOCAL_VAR_NAME_PROP);
        if (localVar == null || localVar.length() == 0) {
            localVar = wizard.getNextLocalVar(globalRarRegistry.getRar(rarName).getShortName());
        }
        localVarTextField.setText(localVar);
        String jndiName = (String) settings.getProperty(GlobalRarWizardAction.JNDI_NAME_PROP);
        if (jndiName == null || jndiName.length() == 0) {
            //jndiName = "jca/" + localVar; // NOI18N
            // Initialize with CF resource created by default in CAPS installed GF.
            jndiName = "jms/tx/default"; //NOI18N

        }
        jndiTextField.setText(jndiName);
    }

    public void storeToSettings(WizardDescriptor settings) {
        settings.putProperty(GlobalRarWizardAction.BUSINESS_RULE_PROP,
                (String) methodComboBox.getSelectedItem());
        settings.putProperty(GlobalRarWizardAction.JNDI_NAME_PROP,
                jndiTextField.getText().trim());
        settings.putProperty(GlobalRarWizardAction.ROLLBACK_PROP,
                Boolean.valueOf(rollbackCheckBox.isSelected()));
        settings.putProperty(GlobalRarWizardAction.LOG_EX_PROP,
                Boolean.valueOf(logCheckBox.isSelected()));
        settings.putProperty(GlobalRarWizardAction.RETHROW_PROP,
                Boolean.valueOf(rethrowCheckBox.isSelected()));
        settings.putProperty(GlobalRarWizardAction.DESCRIPTION_PROP,
                ""); // NOI18N
        settings.putProperty(GlobalRarWizardAction.AUTHENTICATION_PROP,
                "Container"); // NOI18N
        settings.putProperty(GlobalRarWizardAction.SHAREABLE_PROP,
                "No"); // NOI18N
        settings.putProperty(GlobalRarWizardAction.LOCAL_VAR_NAME_PROP,
                localVarTextField.getText().trim());
        settings.putProperty(GlobalRarWizardAction.RETURN_TYPE_PROP,
                returnTypeTextField.getText().trim());
    }

    public boolean isWizardValid() {
        String selectedBusinessRule = (String) methodComboBox.getSelectedItem();
        if (selectedBusinessRule == null || selectedBusinessRule.trim().length() == 0) {
            errorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/wizard/Bundle").getString("Method_name_cannot_be_empty"));
            return false;
        }
        if (!Util.isJavaIdentifier(selectedBusinessRule)) {
            errorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/wizard/Bundle").getString("Not_a_valid_Java_identifier"));
            return false;
        }
        if (returnTypeTextField.getText() == null || returnTypeTextField.getText().equals("")) { // NOI18N
            errorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/wizard/Bundle").getString("Return_type_cannot_be_empty"));
            return false;
        }
        if (jndiTextField.getText() == null || jndiTextField.getText().equals("")) { // NOI18N
            errorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/wizard/Bundle").getString("JNDI_name_cannot_be_empty"));
            return false;
        }
        if (localVarTextField.getText() == null || localVarTextField.getText().equals("")) { // NOI18N
            errorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/wizard/Bundle").getString("Local_variable_name_cannot_be_empty"));
            return false;
        }
        if (!Util.isJavaIdentifier(localVarTextField.getText())) {
            errorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/wizard/Bundle").getString("Local_variable_name_is_not_valid_Java_identifier"));
            return false;
        }
        if (!wizard.isLocalVarValid(localVarTextField.getText())) {
            errorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/wizard/Bundle").getString("Local_variable_name_is_in_use,_pick_another_one"));
            return false;
        }

        errorLabel.setText(null);
        return true;
    }

    private void updateCheckBoxStatus() {
        String selectedBusinessRule = (String) methodComboBox.getSelectedItem();
        if (selectedBusinessRule == null && selectedBusinessRule.length() == 0) {
            return;
        }
        if (!wizard.getBusinessRules().contains(selectedBusinessRule)) {
            // its a new method to be created
            returnTypeLabel.setEnabled(true);
            returnTypeTextField.setEnabled(true);
            browseButton.setEnabled(true);

            if (wizard.isContainerManaged()) {
                rollbackCheckBox.setSelected(true);
                rollbackCheckBox.setEnabled(true);
            } else {
                rollbackCheckBox.setSelected(false);
                rollbackCheckBox.setEnabled(false);
            }
            logCheckBox.setSelected(true);
            logCheckBox.setEnabled(true);
            rethrowCheckBox.setSelected(false);
            rethrowCheckBox.setEnabled(true);
        } else {
            returnTypeLabel.setEnabled(false);
            returnTypeTextField.setEnabled(false);
            browseButton.setEnabled(false);

            rollbackCheckBox.setSelected(false);
            rollbackCheckBox.setEnabled(false);
            logCheckBox.setSelected(false);
            logCheckBox.setEnabled(false);
            rethrowCheckBox.setSelected(false);
            rethrowCheckBox.setEnabled(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        methodLabel = new javax.swing.JLabel();
        jndiLabel = new javax.swing.JLabel();
        jndiTextField = new javax.swing.JTextField();
        jndiButton = new javax.swing.JButton();
        localVarLabel = new javax.swing.JLabel();
        localVarTextField = new javax.swing.JTextField();
        errorLabel = new javax.swing.JLabel();
        methodComboBox = new javax.swing.JComboBox();
        rollbackCheckBox = new javax.swing.JCheckBox();
        logCheckBox = new javax.swing.JCheckBox();
        rethrowCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        returnTypeLabel = new javax.swing.JLabel();
        returnTypeTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        methodLabel.setLabelFor(methodComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(methodLabel, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelResource.class, "lbl_mthdName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(methodLabel, gridBagConstraints);
        methodLabel.getAccessibleContext().setAccessibleDescription("method name");

        jndiLabel.setDisplayedMnemonic('R');
        jndiLabel.setLabelFor(jndiTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jndiLabel, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelResource.class, "lbl_resource_jndi")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jndiLabel, gridBagConstraints);
        jndiLabel.getAccessibleContext().setAccessibleDescription("jndi name");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jndiTextField, gridBagConstraints);
        jndiTextField.getAccessibleContext().setAccessibleName("resource jndi");
        jndiTextField.getAccessibleContext().setAccessibleDescription("resource jndi");

        org.openide.awt.Mnemonics.setLocalizedText(jndiButton, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelResource.class, "lbl_browse_button")); // NOI18N
        jndiButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseJndiActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jndiButton, gridBagConstraints);
        jndiButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelResource.class, "a11y.name.browsebutton")); // NOI18N
        jndiButton.getAccessibleContext().setAccessibleDescription("browse");

        localVarLabel.setLabelFor(localVarTextField);
        org.openide.awt.Mnemonics.setLocalizedText(localVarLabel, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelResource.class, "lbl_local_var")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(localVarLabel, gridBagConstraints);
        localVarLabel.getAccessibleContext().setAccessibleDescription("variable name");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(localVarTextField, gridBagConstraints);
        localVarTextField.getAccessibleContext().setAccessibleName("local variable name");
        localVarTextField.getAccessibleContext().setAccessibleDescription("local variable name");

        errorLabel.setForeground(new java.awt.Color(255, 0, 51));
        errorLabel.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, "error label");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(errorLabel, gridBagConstraints);
        errorLabel.getAccessibleContext().setAccessibleDescription("error");

        methodComboBox.setEditable(true);
        methodComboBox.setModel(methodComboBoxModel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(methodComboBox, gridBagConstraints);
        methodComboBox.getAccessibleContext().setAccessibleName("method name");
        methodComboBox.getAccessibleContext().setAccessibleDescription("method name");

        rollbackCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rollbackCheckBox, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelResource.class, "lbl_rollback_tx")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(rollbackCheckBox, gridBagConstraints);
        rollbackCheckBox.getAccessibleContext().setAccessibleDescription("rollback");

        logCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(logCheckBox, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelResource.class, "lbl_log_exception")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(logCheckBox, gridBagConstraints);
        logCheckBox.getAccessibleContext().setAccessibleDescription("log");

        org.openide.awt.Mnemonics.setLocalizedText(rethrowCheckBox, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelResource.class, "lbl_rethrow_exception")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(rethrowCheckBox, gridBagConstraints);
        rethrowCheckBox.getAccessibleContext().setAccessibleDescription("rethrow");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "       ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        add(jLabel1, gridBagConstraints);

        returnTypeLabel.setLabelFor(returnTypeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(returnTypeLabel, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelResource.class, "lbl_return_type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(returnTypeLabel, gridBagConstraints);
        returnTypeLabel.getAccessibleContext().setAccessibleDescription("return type");

        returnTypeTextField.setText("void"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(returnTypeTextField, gridBagConstraints);
        returnTypeTextField.getAccessibleContext().setAccessibleName("return type");
        returnTypeTextField.getAccessibleContext().setAccessibleDescription("return type");

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelResource.class, "btn_browse_type")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleDescription("browse");

        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents

    private void browseJndiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseJndiActionPerformed
        String jndiValue = JndiBrowser.popupJndiBrowserDialog(project, JndiBrowser.Category.CONNECTOR_RESOURCE);
        jndiTextField.setText(jndiValue);
    }//GEN-LAST:event_browseJndiActionPerformed

private void browseTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseTypeActionPerformed
    Sources srcs = GeneratorUtil.getSources(project);
    FileObject srgFileObject = null;
    if (srcs != null) {
        SourceGroup[] srg = srcs.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if ((srg != null) && (srg.length > 0)) {
            srgFileObject = srg[0].getRootFolder();
        }
    }

    ClasspathInfo cpInfo = ClasspathInfo.create(
                    ClassPath.getClassPath(srgFileObject, ClassPath.BOOT), // boot classpath
                    ClassPath.getClassPath(srgFileObject, ClassPath.COMPILE), // classpath from dependent projects and libraries
                    ClassPath.getClassPath(srgFileObject, ClassPath.SOURCE)); // source classpath

    final ElementHandle<TypeElement> handle = TypeElementFinder.find(cpInfo, new TypeElementFinder.Customizer() {
            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, NameKind nameKind, Set<SearchScope> searchScopes) {
                return classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
            }

            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                return true;
            }
    });
    if (handle != null) {
        returnTypeTextField.setText(handle.getQualifiedName());
    }
}//GEN-LAST:event_browseTypeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton jndiButton;
    private javax.swing.JLabel jndiLabel;
    private javax.swing.JTextField jndiTextField;
    private javax.swing.JLabel localVarLabel;
    private javax.swing.JTextField localVarTextField;
    private javax.swing.JCheckBox logCheckBox;
    private javax.swing.JComboBox methodComboBox;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JCheckBox rethrowCheckBox;
    private javax.swing.JLabel returnTypeLabel;
    private javax.swing.JTextField returnTypeTextField;
    private javax.swing.JCheckBox rollbackCheckBox;
    // End of variables declaration//GEN-END:variables

    class MethodComboBoxModel extends DefaultComboBoxModel {

        public int getSize() {
            return wizard.getBusinessRules().size();
        }

        public Object getElementAt(int index) {
            return wizard.getBusinessRules().get(index);
        }

    }

    class MethodComboBoxEditor extends BasicComboBoxEditor implements DocumentListener {

        public MethodComboBoxEditor() {
            this.editor.getDocument().addDocumentListener(this);
        }

        public void insertUpdate(DocumentEvent e) {
            changeUpdate();
        }

        public void removeUpdate(DocumentEvent e) {
            changeUpdate();
        }

        public void changedUpdate(DocumentEvent e) {
            changeUpdate();
        }

        private void changeUpdate() {
            methodComboBox.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }
    }

}

