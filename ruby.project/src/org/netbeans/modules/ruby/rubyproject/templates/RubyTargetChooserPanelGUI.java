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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.rubyproject.templates;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingUtilities;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
//import org.netbeans.spi.gsfpath.project.support.ui.PackageView;
import org.netbeans.modules.ruby.RubyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.awt.Mnemonics;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Permits user to select a package to place a Java class (or other resource) into.
 * @author Petr Hrebejk, Jesse Glick
 */
public class RubyTargetChooserPanelGUI extends javax.swing.JPanel implements ActionListener, DocumentListener {
  
    private static final String DEFAULT_NEW_PACKAGE_NAME = 
        NbBundle.getMessage( RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_DefaultNewPackageName" ); // NOI18N
    private static final String NEW_CLASS_PREFIX = 
        NbBundle.getMessage( RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_NewRubyClassPrefix" ); // NOI18N
    
    /** preferred dimension of the panel */
    private static final Dimension PREF_DIM = new Dimension(500, 340);
    
    private Project project;
    /** File set except for when we're manually updating values in some of the
     * dependent text fields */
    private boolean userEdit = true;
    /** Flag used to keep track of whether the user has edited the file field manually */
    private boolean fileEdited;
    /** Flag used to keep track of whether the user has edited the class field manually */
    private boolean classEdited;
    private String expectedExtension;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private int type;
    private SourceGroup groups[];
    //private boolean ignoreRootCombo;
    
    /** Creates new form SimpleTargetChooserGUI */
    public RubyTargetChooserPanelGUI( Project p, SourceGroup[] groups, Component bottomPanel, int type ) {
        this.type = type;
        this.project = p;
        this.groups = groups;
        
        initComponents();      
        
        // BEGIN TOR MODIFICATIONS
        // NOTE - even when adding a -module-, we will use the "class" textfield
        // to represent the name of the module, and the "module" text field to represent
        // modules surrounding the current module
        if (type == NewRubyFileWizardIterator.TYPE_TEST) {
            extendsText.setText("Test::Unit::TestCase"); // NOI18N
            type = this.type = NewRubyFileWizardIterator.TYPE_CLASS;
        }

        if (type == NewRubyFileWizardIterator.TYPE_CLASS || type == NewRubyFileWizardIterator.TYPE_MODULE) {
            if (type == NewRubyFileWizardIterator.TYPE_MODULE) {
                extendsLabel.setVisible(false);
                extendsText.setVisible(false);
                Mnemonics.setLocalizedText(classLabel, NbBundle.getMessage(RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_ModuleName_Label")); // NOI18N
            } else {
                extendsText.getDocument().addDocumentListener(this);
            }
            moduleText.getDocument().addDocumentListener(this);
            classText.getDocument().addDocumentListener(this);
        } else {
            classLabel.setVisible(false);
            classText.setVisible(false);
            moduleLabel.setVisible(false);
            moduleText.setVisible(false);
            extendsLabel.setVisible(false);
            extendsText.setVisible(false);
        }
        packageComboBox.setVisible(false);
        packageLabel.setVisible(false);

        // END TOR MODIFICATIONS
        
        if ( type == NewRubyFileWizardIterator.TYPE_PACKAGE ) {
            packageComboBox.setVisible( false );
            packageLabel.setVisible( false );
            Mnemonics.setLocalizedText (fileLabel, NbBundle.getMessage (RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_CreatedFolder_Label")); // NOI18N
            Mnemonics.setLocalizedText (documentNameLabel, NbBundle.getMessage (RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_PackageName_Label")); // NOI18N
            documentNameTextField.getDocument().addDocumentListener( this );
        }
        else if ( type == NewRubyFileWizardIterator.TYPE_PKG_INFO ) {
            documentNameTextField.setEditable (false);
        }
        else {
            packageComboBox.getEditor().addActionListener( this );
            documentNameTextField.getDocument().addDocumentListener( this );
        }
        
                
        if ( bottomPanel != null ) {
            bottomPanelContainer.add( bottomPanel, java.awt.BorderLayout.CENTER );
        }
                
        //initValues( project, null, null );
        

        // Not very nice
        Component packageEditor = packageComboBox.getEditor().getEditorComponent();
        if ( packageEditor instanceof javax.swing.JTextField ) {
            ((javax.swing.JTextField)packageEditor).getDocument().addDocumentListener( this );
        }
        else {
            packageComboBox.addActionListener( this );
        }
        
        rootComboBox.setRenderer(new GroupListCellRenderer());        
  //      packageComboBox.setRenderer(PackageView.listRenderer());
        rootComboBox.addActionListener( this );
        
        setPreferredSize( PREF_DIM );
        setName( NbBundle.getBundle (RubyTargetChooserPanelGUI.class).getString ("LBL_RubyTargetChooserPanelGUI_Name") ); // NOI18N
    }
            
    public void addNotify () {
        Dimension panel2Size = this.jPanel2.getPreferredSize();
        Dimension bottomPanelSize = this.bottomPanelContainer.getPreferredSize ();
        Dimension splitterSize = this.targetSeparator.getPreferredSize();        
        int vmax = panel2Size.height + bottomPanelSize.height + splitterSize.height + 12;   //Insets=12
        //Update only height, keep the wizard width
        if (vmax > PREF_DIM.height) {
            this.setPreferredSize (new Dimension (PREF_DIM.width,vmax));
        }
        super.addNotify();
    }
    
    public void initValues( FileObject template, FileObject preselectedFolder ) {
        assert project != null : "Project must be specified."; // NOI18N
        // Show name of the project
        projectTextField.setText( ProjectUtils.getInformation(project).getDisplayName() );
        assert template != null;
        
        String displayName = null;
        try {
            DataObject templateDo = DataObject.find (template);
            displayName = templateDo.getNodeDelegate ().getDisplayName ();
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName ();
        }
        
        putClientProperty ("NewFileWizard_Title", displayName);// NOI18N        
        // Setup comboboxes 
        rootComboBox.setModel(new DefaultComboBoxModel(groups));
        SourceGroup preselectedGroup = getPreselectedGroup( preselectedFolder );
        //ignoreRootCombo = true;
        rootComboBox.setSelectedItem( preselectedGroup );                       
        //ignoreRootCombo = false;
        Object preselectedPackage = getPreselectedPackage(preselectedGroup, preselectedFolder, packageComboBox.getModel());
        if ( type == NewRubyFileWizardIterator.TYPE_PACKAGE ) {
            String docName = preselectedPackage == null || preselectedPackage.toString().length() == 0 ? 
                DEFAULT_NEW_PACKAGE_NAME : 
                preselectedPackage.toString() + "." + DEFAULT_NEW_PACKAGE_NAME; // NOI18N

            documentNameTextField.setText( docName );                    
            int docNameLen = docName.length();
            int defPackageNameLen = DEFAULT_NEW_PACKAGE_NAME.length();

            documentNameTextField.setSelectionEnd( docNameLen - 1 );
            documentNameTextField.setSelectionStart( docNameLen - defPackageNameLen );                
        } else {
            if (preselectedPackage != null) {
                // packageComboBox.setSelectedItem( preselectedPackage );
                packageComboBox.getEditor().setItem( preselectedPackage );
            }
            if (template != null) {
            	if ( documentNameTextField.getText().trim().length() == 0 ) { // To preserve the class name on back in the wiazard
                    if (this.type == NewRubyFileWizardIterator.TYPE_PKG_INFO) {
                        documentNameTextField.setText (template.getName ());
                    }
                    else {
                        //Ordinary file
                        String prefix = NEW_CLASS_PREFIX;
                        // See 91580
                        Object customPrefix = template.getAttribute("templateNamePrefix"); // NOI18N
                        if (customPrefix != null) {
                            prefix = customPrefix.toString();
                        }
                        
                        documentNameTextField.setText (prefix + template.getName ());
                        documentNameTextField.selectAll ();
                    }
                }
            }
//            updatePackages( false );
        }
        // Determine the extension
        String ext = template == null ? "" : template.getExt(); // NOI18N
        expectedExtension = ext.length() == 0 ? "" : "." + ext; // NOI18N
        
        updateText();
        fileEdited = false;
        classEdited = false;
        if (type == NewRubyFileWizardIterator.TYPE_CLASS || type == NewRubyFileWizardIterator.TYPE_MODULE) {
            classText.selectAll();
        }
    }
        
    public FileObject getRootFolder() {
        return ((SourceGroup) rootComboBox.getSelectedItem()).getRootFolder();        
    }
    
    public String getPackageFileName() {
        
        if ( type == NewRubyFileWizardIterator.TYPE_PACKAGE ) {
            return ""; // NOI18N
        }
        
        String packageName = packageComboBox.getEditor().getItem().toString();        
        return  packageName.replace( '.', '/' ); // NOI18N        
    }
    
    /**
     * Name of selected package, or "" for default package.
     */
    String getPackageName() {
        if ( type == NewRubyFileWizardIterator.TYPE_PACKAGE ) {
            return ""; // NOI18N
        }
        return packageComboBox.getEditor().getItem().toString();
    }    
    
    public String getTargetName() {
        String text = documentNameTextField.getText().trim();
        
        if ( text.length() == 0 ) {
            return null;
        }
        else {
            return text;
        }

    }

    // BEGIN TOR MODIFICATIONS
    public String getClassName() {
        String text = classText.getText().trim();
        
        if ( text.length() == 0 ) {
            return null;
        }
        else {
            return text;
        }
    }

    public String getModuleName() {
        String text = moduleText.getText().trim();
        
        if ( text.length() == 0 ) {
            return null;
        }
        else {
            return text;
        }
    }

    public String getExtends() {
        String text = extendsText.getText().trim();
        
        if ( text.length() == 0 ) {
            return null;
        }
        else {
            return text;
        }
    }
    // END TOR MODIFICATIONS
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(e);
        }
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        classLabel = new javax.swing.JLabel();
        classText = new javax.swing.JTextField();
        documentNameLabel = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        moduleLabel = new javax.swing.JLabel();
        moduleText = new javax.swing.JTextField();
        extendsLabel = new javax.swing.JLabel();
        extendsText = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        rootComboBox = new javax.swing.JComboBox();
        packageLabel = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(targetSeparator, gridBagConstraints);

        bottomPanelContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(bottomPanelContainer, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        classLabel.setLabelFor(classText);
        org.openide.awt.Mnemonics.setLocalizedText(classLabel, org.openide.util.NbBundle.getMessage(RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_ClassName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel1.add(classLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel1.add(classText, gridBagConstraints);

        documentNameLabel.setLabelFor(documentNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(documentNameLabel, org.openide.util.NbBundle.getMessage(RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_FileName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel1.add(documentNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel1.add(documentNameTextField, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/ruby/rubyproject/templates/Bundle"); // NOI18N
        documentNameTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_documentNameTextField")); // NOI18N

        moduleLabel.setLabelFor(moduleText);
        org.openide.awt.Mnemonics.setLocalizedText(moduleLabel, org.openide.util.NbBundle.getMessage(RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_InModuleName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel1.add(moduleLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel1.add(moduleText, gridBagConstraints);

        extendsLabel.setLabelFor(extendsText);
        org.openide.awt.Mnemonics.setLocalizedText(extendsLabel, org.openide.util.NbBundle.getMessage(RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_Extends_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel1.add(extendsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel1.add(extendsText, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        jPanel2.add(jPanel1, gridBagConstraints);

        jLabel5.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_jLabel5")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel2.add(jLabel5, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel2.add(projectTextField, gridBagConstraints);
        projectTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_projectTextField")); // NOI18N

        jLabel1.setLabelFor(rootComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_jLabel1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel2.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel2.add(rootComboBox, gridBagConstraints);
        rootComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_rootComboBox")); // NOI18N

        packageLabel.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_jLabel2")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel2.add(packageLabel, gridBagConstraints);

        packageComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel2.add(packageComboBox, gridBagConstraints);
        packageComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_packageComboBox")); // NOI18N

        fileLabel.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_CreatedFile_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 12, 0);
        jPanel2.add(fileLabel, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 0);
        jPanel2.add(fileTextField, gridBagConstraints);
        fileTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_fileTextField")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(jPanel2, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(bundle.getString("AD_RubyTargetChooserPanelGUI")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanelContainer;
    private javax.swing.JLabel classLabel;
    private javax.swing.JTextField classText;
    private javax.swing.JLabel documentNameLabel;
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JLabel extendsLabel;
    private javax.swing.JTextField extendsText;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel moduleLabel;
    private javax.swing.JTextField moduleText;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JComboBox rootComboBox;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
        
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ( rootComboBox == e.getSource() ) {            
//            if ( !ignoreRootCombo && type != NewRubyFileWizardIterator.TYPE_PACKAGE ) {
//                updatePackages( true );
//            }
            updateText();
        }
        else if ( packageComboBox == e.getSource() ) {
            updateText();
            fireChange();
        }
        else if ( packageComboBox.getEditor()  == e.getSource() ) {
            updateText();
            fireChange();
        }
    }    
    
    // DocumentListener implementation -----------------------------------------
    
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        trackEdit(e);
        updateText();
        fireChange();        
    }    
    
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    private void syncFields(JTextComponent textComponent, String value) {
        try {
            userEdit = false;
            textComponent.setText(value);
        } finally {
            userEdit = true;
        }
    }
    
    private void capitalizeFirstChar(final JTextComponent field, DocumentEvent e) {
        if (e.getType() == DocumentEvent.EventType.REMOVE) {
            // Don't change the first char when you're deleting it - it would
            // capitalize the second letter which is inconvenient when you're
            // backspacing up to change the word
            return;
        }
        
        String text = field.getText().trim();
        if (text.length() > 0 && Character.isLowerCase(text.charAt(0))) {
            // Force uppercase names to help lazy typists
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String text = field.getText().trim();
                    if (text.length() > 0 && Character.isLowerCase(text.charAt(0))) {
                        boolean wasEditing = userEdit;
                        try {
                            userEdit = false;
                            ((AbstractDocument)field.getDocument()).replace(0, 1, ""+Character.toUpperCase(text.charAt(0)), null);
                        } catch (BadLocationException ble) {
                            Exceptions.printStackTrace(ble);
                        } finally {
                            userEdit = wasEditing;
                        }
                    }
                }
            });
        }
    }
    
    private void trackEdit(DocumentEvent e) {
        if (userEdit) {
            Document doc = e.getDocument();
            if (doc == documentNameTextField.getDocument()) {
                fileEdited = true;
                
                String text = documentNameTextField.getText().trim();
                if (text.length() == 0) {
                    fileEdited = false;
                }
                if ((type == NewRubyFileWizardIterator.TYPE_CLASS || type == NewRubyFileWizardIterator.TYPE_MODULE) && (!classEdited ||
                        classText.getText().length() == 0)) {
                    classEdited = false;
                    syncFields(classText, RubyUtils.underlinedNameToCamel(text));
                }
            } else if (doc == classText.getDocument()) {
                if (e.getType() != DocumentEvent.EventType.REMOVE) {
                    capitalizeFirstChar(classText, e);
                }
                classEdited = true;

                if (!fileEdited || documentNameTextField.getText().trim().length() == 0) {
                    fileEdited = false;
                    String text = classText.getText().trim();
                    syncFields(documentNameTextField, RubyUtils.camelToUnderlinedName(text));
                }
            } else if (doc == extendsText.getDocument()) {
                capitalizeFirstChar(extendsText, e);
            } else if (doc == moduleText.getDocument()) {
                capitalizeFirstChar(moduleText, e);
            }
        }
    }
    
    // Private methods ---------------------------------------------------------
        
    private RequestProcessor.Task updatePackagesTask = null;
    
    private static final ComboBoxModel WAIT_MODEL = new DefaultComboBoxModel( 
        new String[] {
            NbBundle.getMessage( RubyTargetChooserPanelGUI.class, "LBL_RubyTargetChooserPanelGUI_PackageName_PleaseWait" ) // NOI18N
        } 
    ); 
    
//    private void updatePackages( final boolean clean ) {
//        WAIT_MODEL.setSelectedItem( packageComboBox.getEditor().getItem() );
//        packageComboBox.setModel( WAIT_MODEL );
//        
//        if ( updatePackagesTask != null ) {
//            updatePackagesTask.cancel();
//        }
//        
//        updatePackagesTask = new RequestProcessor( "ComboUpdatePackages" ).post(
//            new Runnable() {
//            
//                private ComboBoxModel model;
//            
//                public void run() {
//                    if ( !SwingUtilities.isEventDispatchThread() ) {
//                        model = PackageView.createListView((SourceGroup) rootComboBox.getSelectedItem());                        
//                        SwingUtilities.invokeLater( this );
//                    }
//                    else {
//                        if ( !clean ) {
//                            model.setSelectedItem( packageComboBox.getEditor().getItem() );
//                        }
//                        packageComboBox.setModel( model );
//                    }
//                }
//            }
//        );
//                
//    }
        
    private void updateText() {
        SourceGroup g = (SourceGroup) rootComboBox.getSelectedItem();
        FileObject rootFolder = g.getRootFolder();
        String packageName = getPackageFileName();
        String documentName = documentNameTextField.getText().trim();
        if ( type == NewRubyFileWizardIterator.TYPE_PACKAGE ) {
            documentName = documentName.replace( '.', '/' ); // NOI18N
        }
        else if ( documentName.length() > 0 ) {
            documentName = documentName + expectedExtension;
        }
        String createdFileName = FileUtil.getFileDisplayName( rootFolder ) + 
            ( packageName.startsWith("/") || packageName.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
            packageName + 
            ( packageName.endsWith("/") || packageName.endsWith( File.separator ) || packageName.length() == 0 ? "" : "/" ) + // NOI18N
            documentName;
        
        fileTextField.setText( createdFileName.replace( '/', File.separatorChar ) ); // NOI18N        
    }
    
    private SourceGroup getPreselectedGroup(FileObject folder) {
        for(int i = 0; folder != null && i < groups.length; i++) {
            FileObject root = groups[i].getRootFolder();
            if (root.equals(folder) || FileUtil.isParentOf(root, folder)) {
                return groups[i];
            }
        }
        return groups[0];
    }
    
    /**
     * Get a package combo model item for the package the user selected before opening the wizard.
     * May return null if it cannot find it; or a String instance if there is a well-defined
     * package but it is not listed among the packages shown in the list model.
     */
    private Object getPreselectedPackage(SourceGroup group, FileObject folder, ListModel model) {
        if ( folder == null ) {
            return null;
        }
        FileObject root = group.getRootFolder();
        
        String relPath = FileUtil.getRelativePath( root, folder );
        
        if ( relPath == null ) {
            // Group Root folder is no a parent of the preselected folder
            // No package should be selected
            return null; 
        }        
        else {
            // Find the right item.            
            String name = relPath.replace('/', '.');
            /*
            int max = model.getSize();
            for (int i = 0; i < max; i++) {
                Object item = model.getElementAt(i);
                if (item.toString().equals(name)) {
                    return item;
                }
            }
             */
            // Didn't find it.
            // #49954: should nonetheless show something in the combo box.
            return name;
        }        
    }
    
    // Private innerclasses ----------------------------------------------------

    /**
     * Displays a {@link SourceGroup} in {@link #rootComboBox}.
     */
    private static final class GroupListCellRenderer extends DefaultListCellRenderer/*<SourceGroup>*/ {
        
        public GroupListCellRenderer() {}
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            SourceGroup g = (SourceGroup) value;
            super.getListCellRendererComponent(list, g.getDisplayName(), index, isSelected, cellHasFocus);
            setIcon(g.getIcon(false));
            return this;
        }
        
    }
    
}
