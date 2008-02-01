/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.php.project.wizards;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

class PanelOptionsVisual extends JPanel implements PropertyChangeListener{

    private static final long serialVersionUID = -3838819874834494985L;

    public static final String LBL_INDEX_FILE_PATH = "LBL_PathToIndexFile"; // NOI18N
    //private static final String MSG_ILLEGAL_INDEX_FILE_NAME 
    //                                     = "MSG_IllegalIndexFileName";     // NOI18N
    
    PanelOptionsVisual(PhpProjectConfigurePanel panel) {
        myPanel = panel;
        initComponents();
        init();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ExistingSourcesPanel.PROP_SOURCE_ROOT)){
            mySourceRootDir = (File)evt.getNewValue();
            
            updateIndexPreview();
        }
    }

    private void updateIndexPreview(){
        String indexName = indexNameTextField.getText().trim();
        String indexPath = null;
        if (mySourceRootDir != null && indexName.length()!= 0){
            File indexPathFile = new File(mySourceRootDir, indexName);
            indexPath = indexPathFile.getAbsolutePath();
        }
        if (indexPath != null){
            String msg = MessageFormat.format(mySourcePathPreviewMsg, indexPath);
            indexPathPreview.setText(msg);
        }
    }
    
    private void init() {
        //createIndexCheckBox.setVisible(false);
        //indexNameTextField.setVisible(false);
        indexNameTextField.getDocument().addDocumentListener(new NameListener());
    }

    boolean dataIsValid(WizardDescriptor wizardDescriptor) {
        return validate(wizardDescriptor);
    }

    void store(WizardDescriptor descriptor) {
        descriptor.putProperty(NewPhpProjectWizardIterator.SET_AS_MAIN, setAsMainCheckBox.isSelected() ? true : false);

        
        String indexName = indexNameTextField.getText().trim();
        descriptor.putProperty(NewPhpProjectWizardIterator.INDEX_FILE_NAME, indexName);
        
        boolean createIndex = createIndexCheckBox.isVisible() && createIndexCheckBox.isSelected();
        descriptor.putProperty(NewPhpProjectWizardIterator.INDEX_FILE_CREATE, Boolean.valueOf(createIndex) );
        
        
    }

    void read(WizardDescriptor descriptor) {
        String indexName = (String)descriptor.getProperty(NewPhpProjectWizardIterator.INDEX_FILE_NAME);
        if (indexName == null) {
            indexName = getPanel().getDefaultNewFileName();
        }
        indexNameTextField.setText(indexName);
        
        Boolean createIndex = (Boolean)descriptor.getProperty(NewPhpProjectWizardIterator.INDEX_FILE_CREATE);
        if (createIndex != null)
            createIndexCheckBox.setSelected(createIndex.booleanValue());
        
    }

    private boolean validate( WizardDescriptor wizardDescriptor ) {
        
        boolean isIndexNameCorrect = validateIndexName(wizardDescriptor);
        if( !isIndexNameCorrect ) {
            return isIndexNameCorrect;
        }
        
        return true;
    }

    private boolean validateIndexName(WizardDescriptor wizardDescriptor) {
        if (indexNameTextField.isVisible() && indexNameTextField.isEnabled()) {
            String name = indexNameTextField.getText().trim();
            if (name.length() == 0) {
                return true;
            }
            /*
             * check for incorrect path and show message:
                String message = NbBundle.getMessage(
                        PanelOptionsVisual.class, MSG_ILLEGAL_INDEX_FILE_NAME);
                wizardDescriptor.putProperty(
                        NewPhpProjectWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, message);
                return false;
             */
            return true;
        } else {
            return true;
        }
    }

    private class NameListener implements DocumentListener {

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        public void changedUpdate(DocumentEvent arg0) {
            performUpdate();
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
         */
        public void insertUpdate(DocumentEvent arg0) {
            performUpdate();
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        public void removeUpdate(DocumentEvent arg0) {
            performUpdate();
        }
    }


    private void performUpdate() {
        updateIndexPreview();
        getPanel().fireChangeEvent();
    }

    private PhpProjectConfigurePanel getPanel() {
        return myPanel;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setAsMainCheckBox = new javax.swing.JCheckBox();
        indexNameTextField = new javax.swing.JTextField();
        createIndexCheckBox = new javax.swing.JCheckBox();
        fakeVersionLbl = new javax.swing.JLabel();
        fakeVersionValue = new javax.swing.JLabel();
        indexPathPreview = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setAsMainCheckBox.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/php/project/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(setAsMainCheckBox, bundle.getString("LBL_SetAsMain_CheckBox")); // NOI18N
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(setAsMainCheckBox, gridBagConstraints);
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_SetAsMain_A11YDesc")); // NOI18N

        indexNameTextField.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "DefaultNewFileName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(indexNameTextField, gridBagConstraints);
        indexNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "A11_IndexNameText")); // NOI18N
        indexNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_IndexNameText_A11Descr")); // NOI18N

        createIndexCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createIndexCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_createIndexCheckBox")); // NOI18N
        createIndexCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(createIndexCheckBox, gridBagConstraints);
        createIndexCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "A11_createIndexCheckBoxLbl")); // NOI18N
        createIndexCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_createIndexCheckBox_A11Descr")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fakeVersionLbl, "Language Version:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(fakeVersionLbl, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fakeVersionValue, "PHP 5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 12, 0);
        add(fakeVersionValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(indexPathPreview, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "DefaultNewFileName")); // NOI18N
        indexPathPreview.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 5, 0);
        add(indexPathPreview, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox createIndexCheckBox;
    private javax.swing.JLabel fakeVersionLbl;
    private javax.swing.JLabel fakeVersionValue;
    private javax.swing.JTextField indexNameTextField;
    private javax.swing.JLabel indexPathPreview;
    private javax.swing.JCheckBox setAsMainCheckBox;
    // End of variables declaration//GEN-END:variables
    // End of variables declaration
    private PhpProjectConfigurePanel myPanel;

    private File mySourceRootDir;

    private String mySourcePathPreviewMsg = NbBundle.getMessage(
            PanelOptionsVisual.class, LBL_INDEX_FILE_PATH);
}

