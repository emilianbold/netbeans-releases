/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.cncppunit.editor.filecreation;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.editor.filecreation.BrowseFolders;
import org.netbeans.modules.cnd.editor.filecreation.CndPanelGUI;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * NewCndFileChooserPanelGUI is SimpleTargetChooserPanelGUI extended with extension selector and logic
 * 
 */
final class NewTestCppUnitPanelGUI extends CndPanelGUI implements ActionListener{
  
    private String sourceExt;
    private String headerExt;
    private String runnerExt;
    private final MIMEExtensions sourceExtensions = MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE);
    private final MIMEExtensions headerExtensions = MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE);

    protected static final String NEW_TEST_PREFIX = getMessage("LBL_NewTest_NewTestPrefix"); // NOI18N

    protected static final String DEFAULT_TESTS_FOLDER = "tests"; // NOI18N

    /** Creates new form NewCndFileChooserPanelGUI */
    NewTestCppUnitPanelGUI( Project project, SourceGroup[] folders, Component bottomPanel) {
        super(project, folders);

        initComponents();
        initMnemonics();
        
        locationComboBox.setRenderer( CELL_RENDERER );
        
        if ( bottomPanel != null ) {
            bottomPanelContainer.add( bottomPanel, java.awt.BorderLayout.CENTER );
        }
        initValues( null, null, null );
        
        browseButton.addActionListener( NewTestCppUnitPanelGUI.this );
        locationComboBox.addActionListener( NewTestCppUnitPanelGUI.this );
        classNameTextField.getDocument().addDocumentListener( NewTestCppUnitPanelGUI.this );
        runnerNameTextField.getDocument().addDocumentListener( NewTestCppUnitPanelGUI.this );
        folderTextField.getDocument().addDocumentListener( NewTestCppUnitPanelGUI.this );
        
        setName (NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_SimpleTargetChooserPanel_Name")); // NOI18N
    }
    
    @Override
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
        String relPreselectedFolder = getRelativeNativeName(preselectedGroup.getRootFolder(), preselectedFolder);
        folderTextField.setText( relPreselectedFolder);
        if(folderTextField.getText().isEmpty()) {
            folderTextField.setText(DEFAULT_TESTS_FOLDER);
        }
        
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
        
        
        sourceExt = sourceExtensions.getDefaultExtension();
        cbSourceExtension.setSelectedItem(sourceExt);

        headerExt = headerExtensions.getDefaultExtension();
        cbHeaderExtension.setSelectedItem(headerExt);

        runnerExt = sourceExtensions.getDefaultExtension();
        cbExtension.setSelectedItem(runnerExt);
        
        if (template != null) {
            if (documentName == null) {
                final String baseName = getMessage("NewClassSuggestedName"); // NOI18N
                documentName = baseName;
                FileObject currentFolder = preselectedFolder != null ? preselectedFolder : getTargetGroup().getRootFolder().getFileObject(DEFAULT_TESTS_FOLDER);
                if (currentFolder != null) {
                    documentName += generateUniqueSuffix(
                            currentFolder, getFileName(documentName),
                            sourceExt, headerExt);
                }
                
            }
            classNameTextField.setText (documentName);
        }

        if (template != null) {
            String baseName = getMessage("NewRunnerSuggestedName"); // NOI18N
            String runnerName = baseName;
            FileObject currentFolder = preselectedFolder != null ? preselectedFolder : getTargetGroup().getRootFolder().getFileObject(DEFAULT_TESTS_FOLDER);
            if (currentFolder != null) {
                runnerName += generateUniqueSuffix(
                        currentFolder, getFileName(runnerName),
                        sourceExt, headerExt);
            }
            runnerNameTextField.setText(runnerName);
        }

        if (template != null) {
            String testName;
            final String baseName = NEW_TEST_PREFIX + displayName;
            testName = baseName;
            Folder testsRoot = getTestsRootFolder(project);
            if (testsRoot != null) {
                int index = 0;
                while (true) {
                    boolean exist = false;
                    for (Folder folder : testsRoot.getFolders()) {
                        if(folder.getDisplayName().equals(testName)) {
                            exist = true;
                        }
                    }
                    if (!exist) {
                        break;
                    }
                    testName = baseName + " " + (++index); // NOI18N
                }
            }
            testTextField.setText(testName);
            testTextField.selectAll();
        }

    }

    private static Folder getTestsRootFolder(Project project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor projectDescriptor = cdp.getConfigurationDescriptor();

        Folder root = projectDescriptor.getLogicalFolders();
        Folder testRootFolder = null;
        for (Folder folder : root.getFolders()) {
            if(folder.isTestRootFolder()) {
                testRootFolder = folder;
                break;
            }
        }
        return testRootFolder;
    }
    
    @Override
    public SourceGroup getTargetGroup() {
        Object selectedItem = locationComboBox.getSelectedItem();
        if (selectedItem == null) {
            // workaround for MacOS, see IZ 175457
            selectedItem = locationComboBox.getItemAt(locationComboBox.getSelectedIndex());
            if (selectedItem == null) {
                selectedItem = locationComboBox.getItemAt(0);
            }
        }
        return (SourceGroup) selectedItem;
    }
        
    @Override
    public String getTargetFolder() {
        
        String folderName = folderTextField.getText().trim();
        
        if ( folderName.length() == 0 ) {
            return "";
        }
        else {           
            return folderName.replace( File.separatorChar, '/' ); // NOI18N
        }
    }
    
    @Override
    public String getTargetName() {
        String documentName = getSourceFileName();
        
        if ( documentName.length() == 0 || documentName.charAt(documentName.length() - 1) == '.') {
            return null;
        } else {
            return documentName;
        }
    }

    private String createdFileName(JTextField field){
        FileObject root = getTargetGroup().getRootFolder();
        String folderName = field.getText().trim();
        String createdFileName = FileUtil.getFileDisplayName( root ) +
            ( folderName.startsWith("/") || folderName.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
            folderName +
            ( folderName.endsWith("/") || folderName.endsWith( File.separator ) || folderName.length() == 0 ? "" : "/" );  // NOI18N
        return createdFileName.replace( '/', File.separatorChar );
    }

    @Override
    protected void updateCreatedFile() {
        String sourceFileName = createdFileName(folderTextField) + getSourceFileName();
        String headerFileName = createdFileName(folderTextField) + getHeaderFileName();
        String runnerFileName = createdFileName(folderTextField) + getRunnerFileName();

        if (!sourceFileName.equals(fileTextField.getText()) || 
                !headerFileName.equals(headerTextField.getText()) ||
                !runnerFileName.equals(runnerTextField.getText())) {
            fileTextField.setText( sourceFileName );
            headerTextField.setText( headerFileName );
            runnerTextField.setText( runnerFileName );
            changeSupport.fireChange();
        }
    }

    public String getTestName() {
        String documentName = testTextField.getText().trim();
        if ( documentName.length() == 0){
            return null;
        }
        return documentName;
    }
    
    public String getSourceFileName() {
        return getFileName(getClassName()) + "." + sourceExt; // NOI18N
    }

    private DefaultComboBoxModel getSourceExtensionsModel() {
        return new DefaultComboBoxModel(new Vector<String>(sourceExtensions.getValues()));
    }

    private DefaultComboBoxModel getExtensionsCBModel() {
        return new DefaultComboBoxModel(new Vector<String>(sourceExtensions.getValues()));
    }

    public String getHeaderFileName() {
        return getFileName(getClassName()) + "." + headerExt; // NOI18N
    }

    public String getHeaderFolder() {
        String folderName = folderTextField.getText().trim();
        if ( folderName.length() == 0 ) {
            return "";
        } else {
            return folderName.replace( File.separatorChar, '/' ); // NOI18N
        }
    }

    public String getHeaderName() {
        String documentName = getHeaderFileName();
        if ( documentName.length() == 0 || documentName.charAt(documentName.length() - 1) == '.') {
            return null;
        } else {
            return documentName;
        }
    }

    public String getRunnerFileName() {
        return runnerNameTextField.getText().trim() + "." + runnerExt; // NOI18N
    }

    private DefaultComboBoxModel getHeaderExtensionsModel() {
        return new DefaultComboBoxModel(new Vector<String>(headerExtensions.getValues()));
    }
    
    private static String getFileName(String className) {
        return className;
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

        jLabel8 = new javax.swing.JLabel();
        testTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        classNameLbl = new javax.swing.JLabel();
        classNameTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        cbSourceExtension = new javax.swing.JComboBox();
        fileTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cbHeaderExtension = new javax.swing.JComboBox();
        headerTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        runnerNameTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        cbExtension = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        runnerTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Test_Name_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel8, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(testTextField, gridBagConstraints);

        jLabel1.setLabelFor(projectTextField);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Project_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel1, gridBagConstraints);

        projectTextField.setEditable(false);
        projectTextField.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectTextField, gridBagConstraints);
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestCppUnitPanelGUI.class).getString("AD_projectTextField")); // NOI18N

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Location_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(locationLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(locationComboBox, gridBagConstraints);
        locationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestCppUnitPanelGUI.class).getString("AD_locationComboBox")); // NOI18N

        classNameLbl.setLabelFor(classNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(classNameLbl, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_TestClassName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(classNameLbl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(classNameTextField, gridBagConstraints);

        jLabel2.setLabelFor(folderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Folder_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(folderTextField, gridBagConstraints);
        folderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestCppUnitPanelGUI.class).getString("AD_folderTextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Browse_Button")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName("");
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestCppUnitPanelGUI.class).getString("AD_browseButton")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 392;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(targetSeparator, gridBagConstraints);

        bottomPanelContainer.setFocusable(false);
        bottomPanelContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 12);
        add(bottomPanelContainer, gridBagConstraints);

        jLabel5.setLabelFor(cbSourceExtension);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Extension_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel5, gridBagConstraints);

        cbSourceExtension.setModel(getSourceExtensionsModel());
        cbSourceExtension.setMinimumSize(new java.awt.Dimension(100, 25));
        cbSourceExtension.setPreferredSize(new java.awt.Dimension(100, 25));
        cbSourceExtension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSourceExtensionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(cbSourceExtension, gridBagConstraints);

        fileTextField.setEditable(false);
        fileTextField.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(fileTextField, gridBagConstraints);
        fileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestCppUnitPanelGUI.class).getString("AD_fileTextField")); // NOI18N

        jLabel7.setLabelFor(cbHeaderExtension);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_HeaderChooser_Extension_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel7, gridBagConstraints);

        jLabel6.setLabelFor(headerTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_CreatedFiles_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel6, gridBagConstraints);

        cbHeaderExtension.setModel(getHeaderExtensionsModel());
        cbHeaderExtension.setMinimumSize(new java.awt.Dimension(100, 25));
        cbHeaderExtension.setPreferredSize(new java.awt.Dimension(100, 25));
        cbHeaderExtension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbHeaderExtensionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(cbHeaderExtension, gridBagConstraints);

        headerTextField.setEditable(false);
        headerTextField.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(headerTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Test_Runner_File_Name_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(runnerNameTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_Test_Runner_Extension_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel9, gridBagConstraints);

        cbExtension.setEditable(true);
        cbExtension.setModel(getExtensionsCBModel());
        cbExtension.setMinimumSize(new java.awt.Dimension(100, 25));
        cbExtension.setPreferredSize(new java.awt.Dimension(100, 25));
        cbExtension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbExtensionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(cbExtension, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(NewTestCppUnitPanelGUI.class, "LBL_TargetChooser_CreatedFile_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel10, gridBagConstraints);

        runnerTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(runnerTextField, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewTestCppUnitPanelGUI.class).getString("AD_SimpleTargetChooserPanelGUI_1")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbSourceExtensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSourceExtensionActionPerformed
        sourceExt = (String)cbSourceExtension.getSelectedItem();
        updateCreatedFile();
}//GEN-LAST:event_cbSourceExtensionActionPerformed

    private void cbHeaderExtensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbHeaderExtensionActionPerformed
        headerExt = (String)cbHeaderExtension.getSelectedItem();
        updateCreatedFile();
}//GEN-LAST:event_cbHeaderExtensionActionPerformed

    private void cbExtensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbExtensionActionPerformed
        runnerExt = (String)cbExtension.getSelectedItem();
        updateCreatedFile();
}//GEN-LAST:event_cbExtensionActionPerformed

    private void initMnemonics() {
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanelContainer;
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox cbExtension;
    private javax.swing.JComboBox cbHeaderExtension;
    private javax.swing.JComboBox cbSourceExtension;
    private javax.swing.JLabel classNameLbl;
    private javax.swing.JTextField classNameTextField;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JTextField headerTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JTextField runnerNameTextField;
    private javax.swing.JTextField runnerTextField;
    private javax.swing.JSeparator targetSeparator;
    private javax.swing.JTextField testTextField;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ( browseButton == e.getSource() ) {
            // Show the browse dialog             
            SourceGroup group = getTargetGroup();
            FileObject fo = BrowseFolders.showDialog( new SourceGroup[] { group },
                                           project, 
                                           folderTextField.getText().replace( File.separatorChar, '/' ) ); // NOI18N
                        
            if ( fo != null && fo.isFolder() ) {
                String relPath = FileUtil.getRelativePath( group.getRootFolder(), fo );
                folderTextField.setText( relPath.replace( '/', File.separatorChar ) ); // NOI18N
            }                        
        } else if ( locationComboBox == e.getSource() )  {
            updateCreatedFile();
        } 
    }    

    protected static String getMessage(String name) {
        return NbBundle.getMessage( NewTestCUnitPanelGUI.class, name);
    }

}
