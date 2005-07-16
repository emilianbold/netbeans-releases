/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.io.File;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.earproject.ui.FoldersListSettings;
import org.netbeans.spi.project.support.ant.PropertyUtils;

import org.netbeans.spi.project.ui.support.ProjectChooser;

import org.openide.WizardDescriptor;
//import org.openide.util.NbBundle;
import java.util.ResourceBundle;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;

public class PanelProjectImportVisual extends SettingsPanel implements DocumentListener {
    
    private PanelConfigureProject panel;
    private String propNameIndex;
    private ResourceBundle customBundle;
    boolean importStyle;
    
    /** Creates new form PanelProjectLocationVisual */
    public PanelProjectImportVisual(PanelConfigureProject panel, String propNameIndex, ResourceBundle customBundle, boolean importStyle) {
        this.customBundle = customBundle;
        initComponents();
        this.panel = panel;
    this.propNameIndex = propNameIndex;
        
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
        this.importStyle = importStyle;
        createdFolderTextField.setEditable(importStyle);
        if (importStyle) 
            createdFolderTextField.getDocument().addDocumentListener(this);
        jButton1.setVisible(importStyle);
    }
    
    void addNameListener(DocumentListener listener) {
        projectNameTextField.getDocument().addDocumentListener(listener);        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        Button = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        instructionLabel2 = new javax.swing.JLabel();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(PanelProjectImportVisual.class, "LBL_NWP1_ProjectName_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(projectNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(projectNameTextField, gridBagConstraints);
        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectImportVisual.class, "ACS_LBL_NWP1_ProjectName_A11YDesc"));

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(PanelProjectImportVisual.class, "LBL_ImportLocation_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(projectLocationLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 10, 0);
        add(projectLocationTextField, gridBagConstraints);
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectImportVisual.class, "ACS_LBL_NPW1_ProjectLocation_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(Button, org.openide.util.NbBundle.getMessage(PanelProjectImportVisual.class, "LBL_NWP1_BrowseLocation_Button"));
        Button.setActionCommand("BROWSE");
        Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocationAction(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 10, 0);
        add(Button, gridBagConstraints);
        Button.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectImportVisual.class, "ACS_LBL_NWP1_BrowseLocation_A11YDesc"));

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(PanelProjectImportVisual.class, "LBL_NWP1_CreatedProjectFolder_Lablel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(createdFolderLabel, gridBagConstraints);

        createdFolderTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(createdFolderTextField, gridBagConstraints);
        createdFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectImportVisual.class, "ACS_LBL_NWP1_CreatedProjectFolder_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/earproject/ui/wizards/Bundle").getString("LBL_NWP1_BrowseProjectFolder_Button"));
        jButton1.setActionCommand("BROWSE");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocationAction2(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jButton1, gridBagConstraints);

        instructionLabel2.setBackground(new java.awt.Color(255, 255, 255));
        org.openide.awt.Mnemonics.setLocalizedText(instructionLabel2, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/earproject/ui/wizards/Bundle").getString("LBL_ImportInstructions2"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(instructionLabel2, gridBagConstraints);

        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(2);
        jTextArea1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/earproject/ui/wizards/Bundle").getString("LBL_ImportInstructions1"));
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(jTextArea1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void browseLocationAction2(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocationAction2
        String command = evt.getActionCommand();
        
        if ("BROWSE".equals(command)) { //NOI18N
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(customBundle.getString("LBL_NWP1_SelectProjectLocation")); //NOI18N
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = createdFolderTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists())
                    chooser.setSelectedFile(f);
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                createdFolderTextField.setText(projectDir.getAbsolutePath());
            }            
            panel.fireChangeEvent();
        }
    }//GEN-LAST:event_browseLocationAction2

    private void browseLocationAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocationAction
        String command = evt.getActionCommand();
        
        if ("BROWSE".equals(command)) { //NOI18N
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(customBundle.getString("LBL_NWP1_SelectProjectLocation")); //NOI18N
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists())
                    chooser.setSelectedFile(f);
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(projectDir.getAbsolutePath());
            }            
            panel.fireChangeEvent();
        }
    }//GEN-LAST:event_browseLocationAction
    
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }
    
    private File findExistingParent(String path) {
        File t = new File(path);
        File ret = t.getParentFile();
        while (null != ret && !ret.exists()) {
            int dex = path.lastIndexOf(File.separatorChar);
            if (dex < 0)
                break;
            path = path.substring(0,dex-1);
            t = new File(path);
            ret = t.getParentFile();
        }
        return ret;
    }
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        if (projectNameTextField.getText().length() == 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", //NOI18N
                customBundle.getString("MSG_IllegalProjectName")); //NOI18N
            return false; // Display name not specified
        }
        
        if (importStyle) {
            File appXmlSrc = new File(projectLocationTextField.getText().trim()+
            File.separator+"src"+File.separator+"conf"+File.separator+ //NOI18N
            "application.xml"); //NOI18N
            if (!appXmlSrc.exists()) {
                wizardDescriptor.putProperty("WizardPanel_errorMessage",
                MessageFormat.format(customBundle.getString("MSG_DoesntHaveBPApp"),new Object[] {appXmlSrc.getAbsolutePath()})); //NOI18N
                return false;
            }
            if (createdFolderTextField.getText().trim().length() == 0) {
                wizardDescriptor.putProperty("WizardPanel_errorMessage", //NOI18N
                    customBundle.getString("MSG_EmptyProjectFolder"));//NOI18N
                return false;
                
            }
        }

        File destParent = findExistingParent(createdFolderTextField.getText().trim());
                
        File destDir = new File(createdFolderTextField.getText().trim());
        if (destDir.exists() && !destDir.isDirectory()) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", //NOI18N
            MessageFormat.format(customBundle.getString("MSG_WillOverwrite"),new Object[] {destDir.getAbsolutePath() })); //NOI18N
            return false;
        }
        File buildXml = new File(createdFolderTextField.getText().trim()+
            File.separator+"build.xml"); // NOI18N
        if (buildXml.exists()) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", //NOI18N
            MessageFormat.format(customBundle.getString("MSG_WillOverwrite"),new Object[] {buildXml.getAbsolutePath() })); //NOI18N
            return false;
        }
        File tFile = null; //new File(createdFolderTextField.getText().trim()+
//            File.separator+"src"+File.separator+"conf"+File.separator+ //NOI18N
//            "application.xml"); //NOI18N
//        if (tFile.exists()) {
//            wizardDescriptor.putProperty("WizardPanel_errorMessage",
//            MessageFormat.format(customBundle.getString("MSG_WillOverwrite"),new Object[] {tFile.getAbsolutePath()})); //NOI18N
//            return false;
//        }
        
        tFile = new File(createdFolderTextField.getText().trim()+
            File.separator+"nbproject"+File.separator+"project.xml"); //NOI18N
        if (tFile.exists()) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
            MessageFormat.format(customBundle.getString("MSG_AlreadyExixts"),new Object[] {tFile.getAbsolutePath()})); //NOI18N
            return false;
        }
        tFile = new File(createdFolderTextField.getText().trim()+
            File.separator+"nbproject"+File.separator+"project.properties"); //NOI18N
        if (tFile.exists()) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
            MessageFormat.format(customBundle.getString("MSG_AlreadyExixts"),new Object[] {tFile.getAbsolutePath()})); //NOI18N
            return false;
        }
        if (null != destParent && destParent.exists() && !destParent.canWrite()) {
            // Read only project location
            wizardDescriptor.putProperty("WizardPanel_errorMessage", 
                MessageFormat.format(customBundle.getString("MSG_ProjectLocationRO"),new Object[] {destParent.getAbsolutePath()})); //NOI18N
            return false;
        }
                
        wizardDescriptor.putProperty("WizardPanel_errorMessage", ""); //NOI18N
        return true;
    }
    
    void store(WizardDescriptor d) {
        String name = projectNameTextField.getText().trim();
        
        d.putProperty(WizardProperties.PROJECT_DIR, new File(createdFolderTextField.getText().trim()));
        d.putProperty(WizardProperties.NAME, name);
        File srcRoot = null;
        String srcPath = projectLocationTextField.getText();
        if (srcPath.length() > 0) {
            srcRoot = FileUtil.normalizeFile(new File(srcPath));
        }
        d.putProperty(WizardProperties.SOURCE_ROOT, srcRoot);
        
        File projectsDir = new File(this.projectLocationTextField.getText());
        if (projectsDir.isDirectory()) {
            ProjectChooser.setProjectsFolder(projectsDir);
        }
    }
    
    void read(WizardDescriptor settings) {
        //void read (WizardDescriptor settings, String nameIndexProp) {
        File projectLocation = (File) settings.getProperty(WizardProperties.PROJECT_DIR);
        if (projectLocation == null)
            projectLocation = ProjectChooser.getProjectsFolder();
        else
            projectLocation = projectLocation.getParentFile();
        
        if (null != projectLocation) {
            projectLocationTextField.setText(projectLocation.getAbsolutePath());
            String projectName = (String) settings.getProperty(WizardProperties.NAME);
            if (projectName == null) {
                // XXX re-examine this
                int baseCount = 1; // FoldersListSettings.getDefault().getNewProjectCount() + 1;
                String formater = customBundle.getString( "TXT_DefaultProjectName");
                while ((projectName = validFreeProjectName(projectLocation, formater, baseCount)) == null)
                    baseCount++;
                //settings.putProperty(NewEarProjectWizardIterator.PROP_NAME_INDEX, new Integer(baseCount));
                settings.putProperty(propNameIndex, new Integer(baseCount));
            }
            projectNameTextField.setText(projectName);
            if (importStyle)
                createdFolderTextField.setText(System.getProperty("user.home")+
                    File.separator+projectName);
        }
        
        projectNameTextField.selectAll();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Button;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel instructionLabel2;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    protected javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables
        
    private static JFileChooser createChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        return chooser;
    }

    private String validFreeProjectName(final File parentFolder, final String formater, final int index) {
        String name = MessageFormat.format(formater, new Object[] {new Integer (index)});                
        File file = new File(parentFolder, name);
        return file.exists() ? null : name;
    }

    // Implementation of DocumentListener --------------------------------------
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
    }
    
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
    }
    
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
    }
    // End if implementation of DocumentListener -------------------------------
    
    
    /** Handles changes in the project name and project directory
     */
    private void updateTexts(DocumentEvent e) {
        if (!importStyle)
            createdFolderTextField.setText(getCreatedFolderPath());

        panel.fireChangeEvent(); // Notify that the panel changed
    }
    
    private String getCreatedFolderPath() {
        StringBuffer folder = new StringBuffer(projectLocationTextField.getText().trim());
        if (!importStyle) {
            if (!projectLocationTextField.getText().endsWith(File.separator))
                folder.append(File.separatorChar);
            folder.append(projectNameTextField.getText().trim());
        }
        
        return folder.toString();
    }
    
}

//TODO implement check for project folder name and location
