/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.websvc.rest.samples.ui;

import java.awt.GridBagConstraints;
import java.io.File;
import java.text.MessageFormat;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Peter Liu
 */
public class SampleWizardPanelVisual extends JPanel implements DocumentListener {
    public static final String PROP_PROJECT_NAME = "projectName"; // NOI18N
    
    private SampleWizardPanel myPanel;
    private int myType;
    
    /** Creates new form PanelProjectLocationVisual */
    public SampleWizardPanelVisual(SampleWizardPanel panel) {
        initComponents();
        myPanel = panel;
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener( this );
        projectLocationTextField.getDocument().addDocumentListener( this );
    }
    
    protected String getDefaultProjectName() {
        return "RestSampleProject"; // NOI18N
    }
    
    public String getProjectName() {
        return this.projectNameTextField.getText();
    }
    
    // <editor-fold defaultstate="collapsed" desc=" UI Code ">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        
        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        
        setLayout(new java.awt.GridBagLayout());
        
        projectNameLabel.setLabelFor(projectNameTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel,NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_ProjectName_Label"));
        add(projectNameLabel, gridBagConstraints);
        
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(projectNameTextField, gridBagConstraints);
        
        projectLocationLabel.setLabelFor(projectLocationTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_ProjectLocation_Label"));
        add(projectLocationLabel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(projectLocationTextField, gridBagConstraints);
        
        browseButton.setActionCommand("BROWSE"); //NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocationAction(evt);
            }
        });
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        org.openide.awt.Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_BrowseLocation_Button"));
        add(browseButton, gridBagConstraints);
        
        createdFolderLabel.setLabelFor(createdFolderTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_CreatedProjectFolder_Lablel"));
        add(createdFolderLabel, gridBagConstraints);
        
        createdFolderTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(createdFolderTextField, gridBagConstraints);
        
        initAdditionalComponents();
    }
    // </editor-fold>
    
    protected void initAdditionalComponents() {
        JLabel specialLabel = new javax.swing.JLabel();
        specialLabel.setFocusable(false);
        
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(specialLabel, gridBagConstraints);
    }
    
    private void browseLocationAction(java.awt.event.ActionEvent evt) {
        String command = evt.getActionCommand();
        if ( "BROWSE".equals( command ) ) { // NOI18N
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogTitle(NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_SelectProjectLocation"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = this.projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText( FileUtil.normalizeFile(projectDir).getAbsolutePath() );
            }
            myPanel.fireChangeEvent();
        }
    }
    
    
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }
    
    protected boolean valid( WizardDescriptor wizardDescriptor ) {
        
        if ( projectNameTextField.getText().length() == 0 ) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", //NOI18N
                    NbBundle.getMessage(SampleWizardPanelVisual.class, "MSG_IllegalProjectName"));
            return false; // Display name not specified
        }
        File f = FileUtil.normalizeFile(new File(projectLocationTextField.getText()).getAbsoluteFile());
        if (!f.isDirectory()) {
            String message = NbBundle.getMessage(SampleWizardPanelVisual.class, "MSG_IllegalProjectLocation");
            wizardDescriptor.putProperty("WizardPanel_errorMessage", message); // NOI18N
            return false;
        }
        final File destFolder = FileUtil.normalizeFile(new File(createdFolderTextField.getText()).getAbsoluteFile());
        
        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", // NOI18N
                    NbBundle.getMessage(SampleWizardPanelVisual.class, "MSG_ProjectFolderReadOnly"));
            return false;
        }
        
        if (FileUtil.toFileObject(projLoc) == null) {
            String message = NbBundle.getMessage(SampleWizardPanelVisual.class, "MSG_IllegalProjectLocation");
            wizardDescriptor.putProperty("WizardPanel_errorMessage", message); // NOI18N
            return false;
        }
        
//        File locFolder = FileUtil.normalizeFile(new File(projectLocationTextField.getText()).getAbsoluteFile());
//        if(locFolder != null && locFolder.listFiles()!=null && locFolder.listFiles().length != 0) {
//            String message = NbBundle.getMessage(SampleWizardPanelVisual.class, "MSG_ProjectLocationNotEmpty");
//            wizardDescriptor.putProperty("WizardPanel_errorMessage", message); // NOI18N
//            return false;
//        }
        
        File[] kids = destFolder.listFiles();
        if ( destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", // NOI18N
                    NbBundle.getMessage(SampleWizardPanelVisual.class, "MSG_ProjectFolderExists"));
            return false;
        }
        wizardDescriptor.putProperty( "WizardPanel_errorMessage", ""); // NOI18N
        return true;
    }
    
    protected void store( WizardDescriptor d ) {
        
        String name = projectNameTextField.getText().trim();
        String location = projectLocationTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();
        
        d.putProperty( SampleWizardIterator.PROJDIR, new File( folder ));
        d.putProperty(  SampleWizardIterator.NAME, name );
    }
    
    protected void read(WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty(SampleWizardIterator.PROJDIR);
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());
        
        String projectName = (String) settings.getProperty(SampleWizardIterator.NAME);
        if (projectName == null) {
            projectName = getDefaultProjectName();
            
            File file = new File(projectLocation, projectName);
            int index1 = 1;
            if(file.exists()) {
                while(file.exists()) {
                    file = new File(projectLocation, projectName + String.valueOf(index1));
                    index1++;
                }
                projectName = projectName + String.valueOf(index1 -1);
            }
            
        }
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
    }
    
    protected void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }
    
    // UI Variables declaration -
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    
//    private javax.swing.JTextArea projectNoteArea;
//    private javax.swing.JTextPane projectNotePane;
    // End of variables declaration
    
    
    // Private methods ---------------------------------------------------------
    
    private static JFileChooser createChooser() {
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        chooser.setAcceptAllFileFilterUsed( false );
        chooser.setName( NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_SelectProjectDirectory") );
        return chooser;
    }
    
    private String validFreeProjectName(final File parentFolder, final String formater, final int index) {
        String name = MessageFormat.format(formater, new Object[]{new Integer(index)});
        File file = new File(parentFolder, name);
        return file.exists() ? null : name;
    }
    // Implementation of DocumentListener --------------------------------------
    
    public void changedUpdate( DocumentEvent e ) {
        updateTexts( e );
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    public void insertUpdate( DocumentEvent e ) {
        updateTexts( e );
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    public void removeUpdate( DocumentEvent e ) {
        updateTexts( e );
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    
    /** Handles changes in the Project name and project directory
     */
    private void updateTexts( DocumentEvent e ) {
        
        Document doc = e.getDocument();
        
        if ( doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument() ) {
            // Change in the project name
            
            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();
            
            //if ( projectFolder.trim().length() == 0 || projectFolder.equals( oldName )  ) {
            createdFolderTextField.setText( projectFolder + File.separatorChar + projectName );
            //}
            
        }
        myPanel.fireChangeEvent(); // Notify that the panel changed
    }
}
