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

package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Represents <em>Name and Location</em> panel in J2ME Library Descriptor Wizard.
 *
 * @author ads
 */
final class NameAndLocationVisualPanel extends JPanel {
    
    private static final String MSG_EMPTY_LIB_NAME 
                                              = "MSG_EmptyLibraryName";          // NOI18N 
    private static final String MSG_EMPTY_LIB_DISPLAY_NAME 
                                              = "MSG_EmptyLibraryDisplayName";   // NOI18N 
    private static final String MSG_LIB_EXISTS 
                                              = "MSG_LibraryExists";             // NOI18N 
    
    public  static final String ERROR_MESSAGE = "WizardPanel_errorMessage";      // NOI18N
    public  static final String VALID         = "valid";                    // NOI18N
    

            
            
    /** Creates new NameAndLocationPanel */
    NameAndLocationVisualPanel() {
        initComponents();
        initAccessibility();
        putClientProperty("NewFileWizard_Title",// NOI18N
                NbBundle.getMessage(NameAndLocationVisualPanel.class,"LBL_LibraryWizardTitle")); // NOI18N
        
        DocumentListener dListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                setEnabledForFilesInfo(checkValidity());
                //setFilesInfoIntoTextAreas(_data);
            }
        };
        libraryNameValue.getDocument().addDocumentListener(dListener);
        libraryDisplayNameValue.getDocument().addDocumentListener(dListener);
        
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(NameAndLocationVisualPanel.class, key);
    }
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleDescription(
                getMessage("ACS_NameIconLocationPanel"));
        createdFilesValue.getAccessibleContext().setAccessibleDescription(
                getMessage("ACS_LBL_CreatedFiles"));
        modifiedFilesValue.getAccessibleContext().setAccessibleDescription(
                getMessage("ACS_LBL_ModifiedFiles"));
        libraryDisplayNameValue.getAccessibleContext().setAccessibleDescription(
                getMessage("ACS_LBL_DisplayName"));
        libraryNameValue.getAccessibleContext().setAccessibleDescription(
                getMessage("ACS_LBL_Name"));
        projectNameValue.getAccessibleContext().setAccessibleDescription(
                getMessage("ACS_LBL_ProjectName"));
        
        getAccessibleContext().setAccessibleName(
                getMessage("ACS_NameIconLocationPanel"));
        createdFilesValue.getAccessibleContext().setAccessibleName(
                getMessage("ACS_LBL_CreatedFiles"));
        modifiedFilesValue.getAccessibleContext().setAccessibleName(
                getMessage("ACS_LBL_ModifiedFiles"));
        libraryDisplayNameValue.getAccessibleContext().setAccessibleName(
                getMessage("ACS_LBL_DisplayName"));
        libraryNameValue.getAccessibleContext().setAccessibleName(
                getMessage("ACS_LBL_Name"));
        projectNameValue.getAccessibleContext().setAccessibleName(
                getMessage("ACS_LBL_ProjectName"));
    }
    
    protected void storeData(WizardDescriptor descriptor) {
        /*NewLibraryDescriptor.DataModel _temp = getTemporaryDataModel();        
        data.setLibraryName(_temp.getLibraryName());
        data.setLibraryDisplayName(_temp.getLibraryDisplayName());        
        data.setCreatedModifiedFiles(_temp.getCreatedModifiedFiles());        */
    }
    
    /*private NewLibraryDescriptor.DataModel getTemporaryDataModel() {
        NewLibraryDescriptor.DataModel _temp = data.cloneMe(getSettings());        
        _temp.setLibraryName(libraryNameVale.getText());
        _temp.setLibraryDisplayName(libraryDisplayNameValue.getText());        
        if (_temp.isValidLibraryDisplayName() && _temp.isValidLibraryName()) {
            CreatedModifiedFiles files = CreatedModifiedFilesProvider.createInstance(_temp);
            _temp.setCreatedModifiedFiles(files);
        }                
        return _temp;
    }*/
    
    private void setEnabledForFilesInfo(boolean enabled) {        
        createdFilesValue.setEnabled(enabled);
        modifiedFilesValue.setEnabled(enabled);
    }

    /*private void setFilesInfoIntoTextAreas(final NewLibraryDescriptor.DataModel _temp) {
        if (_temp.getCreatedModifiedFiles() != null) {
            createdFilesValue.setText(UIUtil.generateTextAreaContent(
                    _temp.getCreatedModifiedFiles().getCreatedPaths()));
            modifiedFilesValue.setText(UIUtil.generateTextAreaContent(
                    _temp.getCreatedModifiedFiles().getModifiedPaths()));
        }
    }*/
    
    void readData( WizardDescriptor descriptor) {
        mySettings = descriptor;
        libraryNameValue.setText( getLibName() );
        libraryDisplayNameValue.setText( getDisplayName() );
        projectNameValue.setText( (String)descriptor.getProperty( 
                CustomComponentWizardIterator.PROJECT_NAME));
        checkValidity();
    }

    private String getDisplayName() {
        String displayName = (String)mySettings.getProperty( 
                NewLibraryDescriptor.DISPLAY_NAME );
        if ( displayName == null ){
            Library library = (Library)mySettings.getProperty( 
                    NewLibraryDescriptor.LIBRARY );
            displayName = library.getDisplayName();
        }
        return displayName;
    }

    private String getLibName() {
        String libName = (String)mySettings.getProperty( 
                NewLibraryDescriptor.LIB_NAME );
        if ( libName == null ){
            Library library = (Library)mySettings.getProperty( 
                    NewLibraryDescriptor.LIBRARY );
            libName = library.getName();
        }
        return libName;
    }

    private String getDisplayNameValue() {
        return libraryDisplayNameValue.getText();
    }
    
    private String getLibNameValue() {
        return libraryNameValue.getText();
    }
    
    private String getPanelName() {
        return NbBundle.getMessage(NameAndLocationVisualPanel.class,"LBL_NameAndLocation_Title"); // NOI18N
    }

    private boolean checkValidity(){
        // TODO add library verification ( e.g.: was not added yet )
        if (!isValidLibraryName()){
            setError(MSG_EMPTY_LIB_NAME);
            return false;
        } else if (!isValidLibraryDisplayName()){
            setError(MSG_EMPTY_LIB_DISPLAY_NAME);
            return false;
        } else if (!isLibraryAlreadyExists()){
            setError(MSG_LIB_EXISTS);
            return false;
        }
        return true;
    }
    
    // TODO move to class that will perform library instantiation
    private boolean isValidLibraryName(){
            // XXX may need additional conditions, TBD (would need new message in that case)
            return getLibNameValue() != null && 
                    getLibNameValue().trim().length() != 0;
    }
    
    // TODO move to class that will perform library instantiation
    private boolean isValidLibraryDisplayName(){
            return getDisplayNameValue() != null && 
                    getDisplayNameValue().trim().length() != 0;
    }
    
    // TODO move to class that will perform library instantiation
    private boolean isLibraryAlreadyExists(){
        // TODO perform check
        return true;
    }
    
    
    /*private boolean checkValidity(final NewLibraryDescriptor.DataModel _data) {
        if (!_data.isValidLibraryName()) {
            setError(NbBundle.getMessage(NameAndLocationPanel.class,"ERR_EmptyName")); // NOI18N
            return false;
        } else if (!_data.isValidLibraryDisplayName()) {
            setError(NbBundle.getMessage(NameAndLocationPanel.class,"ERR_EmptyDescName")); // NOI18N
            return false;
        }else if (_data.libraryAlreadyExists()) {
            setError(NbBundle.getMessage(NameAndLocationPanel.class,
                    "ERR_LibraryExists", _data.getLibraryName()));
            return false;
        }
        markValid();
        return true;
    }*/
    
    protected final void setError(String message) {
        assert message != null;
        setMessage(message);
        setValid(false);
    }

    private final void setMessage(String message) {
        mySettings.putProperty(ERROR_MESSAGE, message);
    }

    private final void setValid(boolean valid) {
        firePropertyChange(VALID, null, valid); // NOI18N
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(NameAndLocationVisualPanel.class);
    }
    
    public void addNotify() {
        super.addNotify();
        //checkValidity(getTemporaryDataModel());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        libraryName = new javax.swing.JLabel();
        libraryNameValue = new javax.swing.JTextField();
        libraryDisplayName = new javax.swing.JLabel();
        libraryDisplayNameValue = new javax.swing.JTextField();
        projectName = new javax.swing.JLabel();
        projectNameValue = new javax.swing.JTextField();
        createdFiles = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JLabel();
        createdFilesValueS = new javax.swing.JScrollPane();
        createdFilesValue = new javax.swing.JTextArea();
        modifiedFilesValueS = new javax.swing.JScrollPane();
        modifiedFilesValue = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        libraryName.setLabelFor(libraryNameValue);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/componentssupport/ui/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(libraryName, bundle.getString("LBL_LibraryName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 12);
        add(libraryName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 0);
        add(libraryNameValue, gridBagConstraints);

        libraryDisplayName.setLabelFor(libraryDisplayNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(libraryDisplayName, bundle.getString("LBL_LibraryDisplayName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(libraryDisplayName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(libraryDisplayNameValue, gridBagConstraints);

        projectName.setLabelFor(projectNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(projectName, bundle.getString("LBL_ProjectName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(projectName, gridBagConstraints);

        projectNameValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(projectNameValue, gridBagConstraints);

        createdFiles.setLabelFor(createdFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(createdFiles, bundle.getString("LBL_CreatedFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFiles, gridBagConstraints);

        modifiedFiles.setLabelFor(modifiedFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFiles, bundle.getString("LBL_ModifiedFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFiles, gridBagConstraints);

        createdFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFilesValue.setColumns(20);
        createdFilesValue.setEditable(false);
        createdFilesValue.setRows(5);
        createdFilesValue.setBorder(null);
        createdFilesValueS.setViewportView(createdFilesValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(createdFilesValueS, gridBagConstraints);

        modifiedFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFilesValue.setColumns(20);
        modifiedFilesValue.setEditable(false);
        modifiedFilesValue.setRows(5);
        modifiedFilesValue.setToolTipText("modifiedFilesValue");
        modifiedFilesValue.setBorder(null);
        modifiedFilesValueS.setViewportView(modifiedFilesValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(modifiedFilesValueS, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel createdFiles;
    private javax.swing.JTextArea createdFilesValue;
    private javax.swing.JScrollPane createdFilesValueS;
    private javax.swing.JLabel libraryDisplayName;
    private javax.swing.JTextField libraryDisplayNameValue;
    private javax.swing.JLabel libraryName;
    private javax.swing.JTextField libraryNameValue;
    private javax.swing.JLabel modifiedFiles;
    private javax.swing.JTextArea modifiedFilesValue;
    private javax.swing.JScrollPane modifiedFilesValueS;
    private javax.swing.JLabel projectName;
    private javax.swing.JTextField projectNameValue;
    // End of variables declaration//GEN-END:variables
    
    private WizardDescriptor mySettings;
    
}
