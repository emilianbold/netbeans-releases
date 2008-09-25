/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.cnd.editor.filecreation;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.loaders.CCDataLoader;
import org.netbeans.modules.cnd.loaders.HDataLoader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * NewCndFileChooserPanelGUI is SimpleTargetChooserPanelGUI extended with extension selector and logic
 * 
 */
class NewCndClassPanelGUI extends CndPanelGUI implements ActionListener{
  
    private String sourceExt;
    private String headerExt;
    /** Creates new form NewCndFileChooserPanelGUI */
    NewCndClassPanelGUI( Project project, SourceGroup[] folders, Component bottomPanel) {
        super(project, folders);

        initComponents();
        initMnemonics();
        
        locationComboBox.setRenderer( CELL_RENDERER );
        
        if ( bottomPanel != null ) {
            bottomPanelContainer.add( bottomPanel, java.awt.BorderLayout.CENTER );
        }
        initValues( null, null, null );
        
        browseButton.addActionListener( this );
        locationComboBox.addActionListener( this );
        classNameTextField.getDocument().addDocumentListener( this );
        folderTextField.getDocument().addDocumentListener( this );
        
        setName (NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_SimpleTargetChooserPanel_Name")); // NOI18N
    }
    
    public void initValues( FileObject template, FileObject preselectedFolder, String documentName ) {
        assert project != null;
        
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
        
        Sources sources = ProjectUtils.getSources( project );
                        
        folders = sources.getSourceGroups( Sources.TYPE_GENERIC );
        
        if ( folders.length < 2 ) {
            // one source group i.e. hide Location
            locationLabel.setVisible( false );
            locationComboBox.setVisible( false );
        }
        else {
            // more source groups user needs to select location
            locationLabel.setVisible( true );
            locationComboBox.setVisible( true );
            
        }
        
        locationComboBox.setModel( new DefaultComboBoxModel( folders ) );
        // Guess the group we want to create the file in
        SourceGroup preselectedGroup = getPreselectedGroup( folders, preselectedFolder );        
        locationComboBox.setSelectedItem( preselectedGroup );               
        // Create OS dependent relative name
        folderTextField.setText( getRelativeNativeName( preselectedGroup.getRootFolder(), preselectedFolder ) );
        
        String displayName = null;
        try {
            if (template != null) {
                DataObject templateDo = DataObject.find (template);
                displayName = templateDo.getNodeDelegate ().getDisplayName ();
            }
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName ();
        }
        putClientProperty ("NewFileWizard_Title", displayName);// NOI18N        
        
        
        sourceExt = ExtensionsSettings.getInstance(CCDataLoader.getInstance()).getDefaultExtension();
        headerExt = ExtensionsSettings.getInstance(HDataLoader.getInstance()).getDefaultExtension();
        
        if (template != null) {
            if (documentName == null) {
                final String baseName = getMessage("NewClassSuggestedName");
                documentName = baseName;
                FileObject currentFolder = preselectedFolder != null ? preselectedFolder : ((SourceGroup)locationComboBox.getSelectedItem()).getRootFolder();
                if (currentFolder != null) {
                    int index = 0;
                    while (true) {
                        FileObject _tmpS = currentFolder.getFileObject(getFileName(documentName), sourceExt);
                        FileObject _tmpH = currentFolder.getFileObject(getFileName(documentName), headerExt);
                        if (_tmpS == null && _tmpH == null) {
                            break;
                        }
                        documentName = baseName + ++index;
                    }
                }
                
            }
            classNameTextField.setText (documentName);
            classNameTextField.selectAll ();
        }

    }
    
    public SourceGroup getTargetGroup() {
        return (SourceGroup)locationComboBox.getSelectedItem();
    }
        
    public String getTargetFolder() {
        
        String folderName = folderTextField.getText().trim();
        
        if ( folderName.length() == 0 ) {
            return "";
        }
        else {           
            return folderName.replace( File.separatorChar, '/' ); // NOI18N
        }
    }
    
    public String getTargetName() {
        String documentName = getSourceFileName();
        
        if ( documentName.length() == 0 || documentName.charAt(documentName.length() - 1) == '.') {
            return null;
        } else {
            return documentName;
        }
    }
        
    protected void updateCreatedFile() {
        FileObject root = ((SourceGroup)locationComboBox.getSelectedItem()).getRootFolder();
        String folderName = folderTextField.getText().trim();
        String createdFileName = FileUtil.getFileDisplayName( root ) + 
            ( folderName.startsWith("/") || folderName.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
            folderName + 
            ( folderName.endsWith("/") || folderName.endsWith( File.separator ) || folderName.length() == 0 ? "" : "/" );  // NOI18N
        createdFileName = createdFileName.replace( '/', File.separatorChar );

        String sourceFileName = createdFileName + getSourceFileName(); // NOI18N

        if (!sourceFileName.equals(fileTextField.getText())) {
            fileTextField.setText( sourceFileName );
    
            String headerFileName = createdFileName + getHeaderFileName(); // NOI18N
            headerTextField.setText( headerFileName );

            changeSupport.fireChange();
        }
    }
    
    public String getSourceFileName() {
        return getFileName(getClassName()) + "." + sourceExt; // NOI18N
    }

    public String getHeaderFileName() {
        return getFileName(getClassName()) + "." + headerExt; // NOI18N
    }
    
    private static String getFileName(String className) {
        return className.toLowerCase();
    }

    public String getClassName() {
        return classNameTextField.getText().trim();
    }

    public String getHeaderExt() {
        return headerExt;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        classNameLbl = new javax.swing.JLabel();
        classNameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        headerTextField = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        classNameLbl.setLabelFor(classNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(classNameLbl, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_FileName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(classNameLbl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(classNameTextField, gridBagConstraints);
        classNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_documentNameTextField")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        add(jPanel1, gridBagConstraints);

        jLabel1.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_Project_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel1, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectTextField, gridBagConstraints);
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_projectTextField")); // NOI18N

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_Location_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(locationLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 4, 0);
        add(locationComboBox, gridBagConstraints);
        locationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_locationComboBox")); // NOI18N

        jLabel2.setLabelFor(folderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_Folder_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(folderTextField, gridBagConstraints);
        folderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_folderTextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_Browse_Button")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName("");
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_browseButton")); // NOI18N

        jLabel4.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/editor/filecreation/Bundle").getString("LBL_TargetChooser_CreatedFile_Label_Mnemonic").charAt(0));
        jLabel4.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_CreatedFile_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel4, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(fileTextField, gridBagConstraints);
        fileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_fileTextField")); // NOI18N

        jLabel6.setLabelFor(headerTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(NewCndClassPanelGUI.class, "LBL_TargetChooser_HeaderFile_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel6, gridBagConstraints);

        headerTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(headerTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(targetSeparator, gridBagConstraints);

        bottomPanelContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(bottomPanelContainer, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewCndClassPanelGUI.class).getString("AD_SimpleTargetChooserPanelGUI_1")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void initMnemonics() {
        // kind Matiss put those above setText() in autogeneration, which is not what JLabel expects, so let's do it manually
        classNameLbl.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/editor/filecreation/Bundle").getString("LBL_TargetChooser_FileName_Label_Mnemonic").charAt(0));
        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/editor/filecreation/Bundle").getString("LBL_TargetChooser_Project_Label_Mnemonic").charAt(0));
        locationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/editor/filecreation/Bundle").getString("LBL_TargetChooser_Location_Label_Mnemonic").charAt(0));
        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/editor/filecreation/Bundle").getString("LBL_TargetChooser_Folder_Label_Mnemonic").charAt(0));
        browseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/editor/filecreation/Bundle").getString("LBL_TargetChooser_Browse_Button_Mnemonic").charAt(0));
        jLabel4.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/editor/filecreation/Bundle").getString("LBL_TargetChooser_CreatedFile_Label_Mnemonic").charAt(0));        
        jLabel6.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/editor/filecreation/Bundle").getString("LBL_TargetChooser_CreatedHeader_Label_Mnemonic").charAt(0));        
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanelContainer;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel classNameLbl;
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JTextField headerTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ( browseButton == e.getSource() ) {
            FileObject fo=null;
            // Show the browse dialog             
            
            SourceGroup group = (SourceGroup)locationComboBox.getSelectedItem();
            
            fo = BrowseFolders.showDialog( new SourceGroup[] { group }, 
                                           project, 
                                           folderTextField.getText().replace( File.separatorChar, '/' ) ); // NOI18N
                        
            if ( fo != null && fo.isFolder() ) {
                String relPath = FileUtil.getRelativePath( group.getRootFolder(), fo );
                folderTextField.setText( relPath.replace( '/', File.separatorChar ) ); // NOI18N
            }                        
        }
        else if ( locationComboBox == e.getSource() )  {
            updateCreatedFile();
        } 
    }    
    
}
