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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.vmd.componentssupport.ui.UIUtils;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.JavaMELibsConfigurationHelper;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.JavaMELibsPreviewHelper;
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
    
    /** Creates new NameAndLocationPanel */
    NameAndLocationVisualPanel(NameAndLocationWizardPanel panel) {
        myPanel = panel;
        initComponents();
        
        DocumentListener dListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                setEnabledForFilesInfo(checkValidity());
                setFilesInfoIntoTextAreas();
            }
        };
        libraryNameValue.getDocument().addDocumentListener(dListener);
        libraryDisplayNameValue.getDocument().addDocumentListener(dListener);
        
    }
    
    private static String getMessage(String key, Object... args) {
        return NbBundle.getMessage(NameAndLocationVisualPanel.class, key, args);
    }
    
    protected void storeData(WizardDescriptor descriptor) {
        descriptor.putProperty(NewLibraryDescriptor.LIB_NAME, 
                getLibNameValue() );
        descriptor.putProperty(NewLibraryDescriptor.DISPLAY_NAME, 
                getDisplayNameValue() );
    }
    
    void readData( WizardDescriptor descriptor) {
        mySettings = descriptor;
        libraryNameValue.setText( getLibName() );
        libraryDisplayNameValue.setText( getDisplayName() );
        projectNameValue.setText( (String)descriptor.getProperty( 
                CustomComponentWizardIterator.PROJECT_NAME));
        checkValidity();
    }

    private void setEnabledForFilesInfo(boolean enabled) {        
        createdFilesValue.setEnabled(enabled);
        modifiedFilesValue.setEnabled(enabled);
    }

    private void setFilesInfoIntoTextAreas() {
        List<String> created = new ArrayList<String>();
        List<String> modified = new ArrayList<String>();

        addLibJarToList(created, modified);
        addLibXmlToList(created, modified);
        addLayerXmlToList(created, modified);
        addBundleToList(created, modified);

        // publish
            createdFilesValue.setText(UIUtils.generateTextAreaContent(
                    created.toArray(new String[]{})));
            modifiedFilesValue.setText(UIUtils.generateTextAreaContent(
                    modified.toArray(new String[]{})));

        
    }
    
    private void addLibJarToList(List<String> created, List<String> modified){
        List<String> existingArchives = getExistingArchives(getExistingLibraries(), getExistingLibraryNames());

        //libraryErrors.clear();
        
        // add archibes from new lib if are not added yet
        Library library = (Library) mySettings.getProperty(
                NewLibraryDescriptor.LIBRARY);

        //try {
            List<String> archives = JavaMELibsPreviewHelper.extractLibraryJarsPaths(library, getLibNameValue());
            for (String arch : archives) {
                if (!existingArchives.contains(arch)) {
                    created.add(arch);
                }
            }
        //} catch (LibraryParsingException ex) {
        //    libraryErrors.add(ex.getMessage());
        //}
    }

    private static List<String> getExistingArchives(List<Library> existingLibs, 
            List<String> existingLibNames)
    {
        List<String> existingArchives = new ArrayList<String>();
        
        if (existingLibs == null || existingLibNames == null){
            return existingArchives;
        }
        
        Iterator<Library> itLib = existingLibs.iterator();
        Iterator<String> itName = existingLibNames.iterator();
        while (itLib.hasNext()) {
            Library library = itLib.next();
            String name = itName.next();

            //try{
            existingArchives.addAll(
                    JavaMELibsPreviewHelper.extractLibraryJarsPaths(library, name) );
            //} catch (LibraryParsingException ex){
            //    // nothing to do. archives stored in main wizard should correct
            //}
        }
        return existingArchives;
    }
    
    private void addLibXmlToList(List<String> created, List<String> modified){
        String dotCodeNameBase = getCodeNameBase();
        String libName = getLibNameValue();

        String codeNameBase = dotCodeNameBase.replace('.', '/'); // NOI18N
        
        created.add(
                codeNameBase + "/" + libName + 
                JavaMELibsConfigurationHelper.XML_EXTENSION); // NOI18N
    }
    
    private void addLayerXmlToList(List<String> created, List<String> modified){
        String dotCodeNameBase = getCodeNameBase();
        
        String codeNameBase = dotCodeNameBase.replace('.', '/'); // NOI18N
        modified.add(
                codeNameBase + "/" + CustomComponentWizardIterator.LAYER_XML); // NOI18N
    }

    private void addBundleToList(List<String> created, List<String> modified){
        String dotCodeNameBase = getCodeNameBase();
        
        String codeNameBase = dotCodeNameBase.replace('.', '/'); // NOI18N
        modified.add(
                codeNameBase + "/" + CustomComponentWizardIterator.BUNDLE_PROPERTIES); // NOI18N
    }
    
    /**
     * library display name stored in wizard descriptor
     * @return library display name
     */
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

    /**
     * library name stored in wizard descriptor
     * @return library name
     */
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

    /**
     * library display name from UI text field
     * @return String library display name
     */
    private String getDisplayNameValue() {
        return libraryDisplayNameValue.getText();
    }
    
    /**
     * library name from UI text field
     * @return String library name
     */
    private String getLibNameValue() {
        return libraryNameValue.getText();
    }
    
    private boolean checkValidity(){
        
        //if (!libraryErrors.isEmpty()){
        //    setError(libraryErrors.get(0));
        //    return false;
        //} else 
        if (!isValidLibraryName()){
            setError( getMessage(MSG_EMPTY_LIB_NAME) );
            return false;
        } else if (!isValidLibraryDisplayName()){
            setError( getMessage(MSG_EMPTY_LIB_DISPLAY_NAME) );
            return false;
        } else if (isLibraryNameAlreadyExists()){
            setError( getMessage(MSG_LIB_EXISTS, getLibNameValue()) );
            return false;
        }
        markValid();
        return true;
    }
    
    // TODO move to class that will perform library instantiation ?
    private boolean isValidLibraryName() {
        return getLibNameValue() != null &&
                getLibNameValue().trim().length() != 0;
    }
    // TODO move to class that will perform library instantiation ?

    public boolean isValidLibraryDisplayName() {
        return getDisplayNameValue() != null &&
                getDisplayNameValue().trim().length() != 0;
    }

    // TODO move to class that will perform library instantiation ?
    public boolean isLibraryNameAlreadyExists() {
        String libName = getLibNameValue();
        List<String> existingLibNames = getExistingLibraryNames();
        
        if (existingLibNames == null || existingLibNames.size() == 0){
            return false;
        }
        for ( String name : existingLibNames){
            if (name.equals(libName)){
                return true;
            }
        }
        return false;
    }
    
    private List<String> getExistingLibraryNames(){
        return (List<String>)mySettings.getProperty( 
                NewLibraryDescriptor.EXISTING_LIB_NAMES);
    }

    private List<Library> getExistingLibraries(){
        return (List<Library>)mySettings.getProperty( 
                NewLibraryDescriptor.EXISTING_LIBRARIES);
    }

    private String getCodeNameBase(){
        return (String)mySettings.getProperty( 
                CustomComponentWizardIterator.CODE_BASE_NAME);
    }

    /**
     * Set an error message and mark the panel as invalid.
     */
    protected final void setError(String message) {
        assert message != null;
        setMessage(message);
        setValid(false);
    }

    /**
     * Mark the panel as valid and clear any error or warning message.
     */
    protected final void markValid() {
        setMessage(null);
        setValid(true);
    }
    
    private final void setMessage(String message) {
        mySettings.putProperty(
                CustomComponentWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, 
                message);
    }

    private final void setValid(boolean valid) {
        myPanel.setValid(valid);
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(NameAndLocationVisualPanel.class);
    }
    
    public void addNotify() {
        super.addNotify();
        checkValidity();
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
        libraryName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_LBL_Name")); // NOI18N
        libraryName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_LBL_Name")); // NOI18N

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
        libraryDisplayName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_LBL_DisplayName")); // NOI18N
        libraryDisplayName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_LBL_DisplayName")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(libraryDisplayNameValue, gridBagConstraints);

        projectName.setLabelFor(projectNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(projectName, bundle.getString("LBL_LibraryProjectName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(projectName, gridBagConstraints);
        projectName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_LBL_LibraryProjectName")); // NOI18N
        projectName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_LBL_LibraryProjectName")); // NOI18N

        projectNameValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(projectNameValue, gridBagConstraints);
        projectNameValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACSN_ProjectName")); // NOI18N
        projectNameValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACSD_ProjectName")); // NOI18N

        createdFiles.setLabelFor(createdFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(createdFiles, bundle.getString("LBL_CreatedFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFiles, gridBagConstraints);
        createdFiles.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_LBL_CreatedFiles")); // NOI18N
        createdFiles.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_LBL_CreatedFiles")); // NOI18N

        modifiedFiles.setLabelFor(modifiedFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFiles, bundle.getString("LBL_ModifiedFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFiles, gridBagConstraints);
        modifiedFiles.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_LBL_ModifiedFiles")); // NOI18N
        modifiedFiles.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_LBL_ModifiedFiles")); // NOI18N

        createdFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFilesValue.setColumns(20);
        createdFilesValue.setEditable(false);
        createdFilesValue.setRows(5);
        createdFilesValue.setToolTipText(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "LBL_CreatedFilesTip")); // NOI18N
        createdFilesValue.setBorder(null);
        createdFilesValueS.setViewportView(createdFilesValue);
        createdFilesValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACSN_CreatedFiles")); // NOI18N
        createdFilesValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACSD_CreatedFiles")); // NOI18N

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
        modifiedFilesValue.setToolTipText(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "LBL_ModifiedFilesTip")); // NOI18N
        modifiedFilesValue.setBorder(null);
        modifiedFilesValueS.setViewportView(modifiedFilesValue);
        modifiedFilesValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACSN_ModifiedFiles")); // NOI18N
        modifiedFilesValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACSD_ModifiedFiles")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(modifiedFilesValueS, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_NameLocationPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NameAndLocationVisualPanel.class, "ACS_NameLocationPanel")); // NOI18N
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
    private NameAndLocationWizardPanel myPanel;
    //private List<String> libraryErrors = new ArrayList<String>();
    
}
