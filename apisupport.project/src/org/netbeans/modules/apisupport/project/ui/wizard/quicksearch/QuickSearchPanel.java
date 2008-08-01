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

/*
 * QuickSearchPanel.java
 *
 * Created on Jun 24, 2008, 10:23:50 AM
 */
package org.netbeans.modules.apisupport.project.ui.wizard.quicksearch;

import java.awt.Component;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.quicksearch.NewQuickSearchIterator.DataModel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * QuickSearch SPI wizard panel
 *
 * @author Max Sauer
 */
public class QuickSearchPanel extends BasicWizardIterator.Panel {

    private DataModel data;

    QuickSearchPanel(WizardDescriptor settings, DataModel data) {
        super(settings);
        this.data = data;
        initComponents();

        putClientProperty("NewFileWizard_Title", getMessage("LBL_QuickSearchPanel_Title"));

        DocumentListener dListener = new UIUtil.DocumentAdapter() {

            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
        };

        if (data.getPackageName() != null) {
            packageCombo.setSelectedItem(data.getPackageName());
        }

        classNameTextField.getDocument().addDocumentListener(dListener);
        categoryNameTextField.getDocument().addDocumentListener(dListener);
        commandPrefixTextField.getDocument().addDocumentListener(dListener);
        positionTextField.getDocument().addDocumentListener(dListener);
        Component editorComp = packageCombo.getEditor().getEditorComponent();
        if (editorComp instanceof JTextComponent) {
            ((JTextComponent) editorComp).getDocument().addDocumentListener(dListener);
        }
    }

    @Override
    protected String getPanelName() {
        return getMessage("LBL_QuickSearchPanel_Title"); // NOI18N
    }

    @Override
    protected void storeToDataModel() {
        //normalize form fields
        data.setClassName(normalize(classNameTextField.getText().trim()));
        data.setCommandPrefix(commandPrefixTextField.getText().trim());
        data.setCategoryName(categoryNameTextField.getText().trim());
        data.setPosition(Integer.parseInt(positionTextField.getText()));
        data.setPackageName(packageCombo.getEditor().getItem().toString());

        NewQuickSearchIterator.generateFileChanges(data);
    }

    @Override
    protected void readFromDataModel() {
        classNameTextField.setText(data.getClassName());
        commandPrefixTextField.setText(data.getCommandPrefix());
        categoryNameTextField.setText(data.getCategoryName());
        positionTextField.setText(data.getPosition().toString());
        packageCombo.setSelectedItem(data.getPackageName());
    }

    @Override
    protected HelpCtx getHelp() {
        return new HelpCtx(QuickSearchPanel.class);
    }

    private boolean checkValidity() {
        final String fileName = classNameTextField.getText().trim();
        if (fileName.length() == 0) {
            setWarning(getMessage("ERR_FN_EMPTY"), false);
            return false;
        }

        if (!Utilities.isJavaIdentifier(normalize(fileName))) {
            setError(getMessage("ERR_FN_INVALID"));
            return false;
        }

        String packName = packageCombo.getEditor().getItem().toString();
        if (packName.equals("")) {
            setWarning(getMessage("EMPTY_PACKAGE"), false);
            return false;
        }

        if (categoryNameTextField.getText().equals("")) {
            setWarning(getMessage("EMPTY_CATEGORY"), false);
            return false;
        }

        if (commandPrefixTextField.getText().trim().equals("")) {
            setWarning(getMessage("ERR_EMPTY_PREFIX"), false);
        }

        if (!commandPrefixTextField.getText().trim().matches("\\w*")) {//alfanumeric only
            setError(getMessage("ERR_PREFIX_INVALID"));
            return false;
        }

        if (positionTextField.getText().equals("")) {
            setWarning(getMessage("ERR_POSITION_EMPTY"), false);
            return false;
        }

        if (!positionTextField.getText().trim().matches("\\d*")) {
            setError(getMessage("ERR_POSITION_INVALID"));
            return false;
        }

        markValid();
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileNameLabel = new javax.swing.JLabel();
        classNameTextField = new javax.swing.JTextField();
        packageLabel = new javax.swing.JLabel();
        packageCombo = UIUtil.createPackageComboBox(data.getSourceRootGroup());
        categoryNameLabel = new javax.swing.JLabel();
        categoryNameTextField = new javax.swing.JTextField();
        commandLabel = new javax.swing.JLabel();
        commandPrefixTextField = new javax.swing.JTextField();
        positionLabel = new javax.swing.JLabel();
        positionTextField = new javax.swing.JTextField();

        fileNameLabel.setLabelFor(classNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileNameLabel, org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.fileNameLabel.text")); // NOI18N

        classNameTextField.setText(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.classNameTextField.text")); // NOI18N

        packageLabel.setLabelFor(packageCombo);
        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.packageLabel.text")); // NOI18N

        packageCombo.setEditable(true);

        categoryNameLabel.setLabelFor(categoryNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(categoryNameLabel, org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.categoryNameLabel.text")); // NOI18N

        categoryNameTextField.setText(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.categoryNameTextField.text")); // NOI18N

        commandLabel.setLabelFor(commandPrefixTextField);
        org.openide.awt.Mnemonics.setLocalizedText(commandLabel, org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.commandLabel.text")); // NOI18N

        commandPrefixTextField.setText(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.commandPrefixTextField.text")); // NOI18N

        positionLabel.setLabelFor(positionTextField);
        org.openide.awt.Mnemonics.setLocalizedText(positionLabel, org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.positionLabel.text")); // NOI18N

        positionTextField.setText(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.positionTextField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fileNameLabel)
                    .add(packageLabel)
                    .add(categoryNameLabel)
                    .add(commandLabel)
                    .add(positionLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(classNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .add(packageCombo, 0, 231, Short.MAX_VALUE)
                    .add(categoryNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .add(commandPrefixTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .add(positionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileNameLabel)
                    .add(classNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(packageLabel)
                    .add(packageCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(categoryNameLabel)
                    .add(categoryNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(commandLabel)
                    .add(commandPrefixTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(positionLabel)
                    .add(positionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(124, Short.MAX_VALUE))
        );

        fileNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.fileNameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        classNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.classNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        packageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.packageLabel.AccessibleContext.accessibleDescription")); // NOI18N
        packageCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.packageCombo.AccessibleContext.accessibleName")); // NOI18N
        packageCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.packageCombo.AccessibleContext.accessibleDescription")); // NOI18N
        categoryNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.categoryNameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        categoryNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.categoryNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        commandLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.commandLabel.AccessibleContext.accessibleDescription")); // NOI18N
        commandPrefixTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.commandPrefixTextField.AccessibleContext.accessibleName")); // NOI18N
        commandPrefixTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.commandPrefixTextField.AccessibleContext.accessibleDescription")); // NOI18N
        positionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.positionLabel.AccessibleContext.accessibleDescription")); // NOI18N
        positionTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.positionTextField.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchPanel.class, "QuickSearchPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel categoryNameLabel;
    private javax.swing.JTextField categoryNameTextField;
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JLabel commandLabel;
    private javax.swing.JTextField commandPrefixTextField;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JComboBox packageCombo;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel positionLabel;
    private javax.swing.JTextField positionTextField;
    // End of variables declaration//GEN-END:variables

    private String getMessage(String key) {
        return NbBundle.getMessage(QuickSearchPanel.class, key);
    }

    private String normalize(String trim) {
        if (trim.endsWith(".java")) {
            return trim.substring(0, trim.length() - 5);
        } else {
            return trim;
        }
    }
}
