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

package org.netbeans.modules.web.project.ui.wizards;

import java.io.File;
import java.text.MessageFormat;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.netbeans.modules.web.project.ui.FoldersListSettings;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Pavel Buzek, Radko Najman
 */
public class ImportLocationVisual extends SettingsPanel implements HelpCtx.Provider {
    
    private ImportWebProjectWizardIterator.ThePanel panel;
    private Document moduleDocument;
    private Document nameDocument;
    private boolean contextModified = false;
    private boolean locationModified = false;
    private boolean locationComputed = false;
    private WizardDescriptor wizardDescriptor;

    /** Creates new form TestPanel */
    public ImportLocationVisual (ImportWebProjectWizardIterator.ThePanel panel) {
        this.panel = panel;
        initComponents ();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_NWP1_NamePanel_A11YDesc"));  // NOI18N

        setName(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_ImportTitle")); //NOI18N
        
        moduleDocument = moduleLocationTextField.getDocument ();
        nameDocument = projectNameTextField.getDocument();

        DocumentListener pl = new DocumentListener () {
            public void changedUpdate(DocumentEvent e) {
                dataChanged(e);
            }

            public void insertUpdate(DocumentEvent e) {
                dataChanged(e);
            }

            public void removeUpdate(DocumentEvent e) {
                dataChanged(e);
            }
        };
        moduleLocationTextField.getDocument().addDocumentListener(pl);

        projectNameTextField.getDocument().addDocumentListener (new DocumentListener (){
            public void changedUpdate(DocumentEvent e) {
                dataChanged(e);
            }

            public void insertUpdate(DocumentEvent e) {
                dataChanged(e);
            }

            public void removeUpdate(DocumentEvent e) {
                dataChanged(e);
            }
        });
        
        projectLocationTextField.getDocument().addDocumentListener (new DocumentListener (){
            public void changedUpdate(DocumentEvent e) {
                fireChanges();
            }

            public void insertUpdate(DocumentEvent e) {
                fireChanges();
            }

            public void removeUpdate(DocumentEvent e) {
                fireChanges();
            }
        });     

    }
    
    void read (WizardDescriptor settings) {
        this.wizardDescriptor = settings;
        
        String projectName = null;
        File projectLocation = (File) settings.getProperty(WizardProperties.PROJECT_DIR);
        if (projectLocation == null) {
            projectLocation = ProjectChooser.getProjectsFolder();                
            int index = FoldersListSettings.getDefault().getNewProjectCount();
            String formater = NbBundle.getMessage(ImportLocationVisual.class, "LBL_NPW1_DefaultProjectName"); //NOI18N
            File file;
            do {
                index++;                            
                projectName = MessageFormat.format(formater, new Object[] {new Integer (index)});                
                file = new File(projectLocation, projectName);                
            } while (file.exists());                                
            settings.putProperty (NewWebProjectWizardIterator.PROP_NAME_INDEX, new Integer(index));                        
        } else
            projectName = (String) settings.getProperty(WizardProperties.NAME);

        projectNameTextField.setText (projectName);                
        jTextFieldContextPath.setText("/" + projectNameTextField.getText().replace(' ', '_'));
        moduleLocationTextField.selectAll();
    }

    void store (WizardDescriptor settings) {
        File srcRoot = null;
        String srcPath = moduleLocationTextField.getText();
        if (srcPath.length() > 0) {
            srcRoot = FileUtil.normalizeFile(new File(srcPath));           
        }
        settings.putProperty (WizardProperties.SOURCE_ROOT, srcRoot);
        settings.putProperty (WizardProperties.NAME, projectNameTextField.getText());
        File projectsDir = new File(projectLocationTextField.getText());
        
        settings.putProperty (WizardProperties.PROJECT_DIR, projectsDir);
        projectsDir = projectsDir.getParentFile();
        if (projectsDir != null && projectsDir.isDirectory())
            ProjectChooser.setProjectsFolder (projectsDir);
        
        String contextPath = jTextFieldContextPath.getText().trim();
        if (!contextPath.startsWith("/")) //NOI18N
            contextPath = "/" + contextPath; //NOI18N
        settings.putProperty(WizardProperties.CONTEXT_PATH, contextPath);
    }
    
    boolean valid (WizardDescriptor settings) {
        if (projectNameTextField.getText().trim().length() == 0) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", NbBundle.getMessage(ImportLocationVisual.class, "MSG_ProvideProjectName"));
            return false; // Display name not specified
        }
        
        File destFolder = new File(projectLocationTextField.getText());
        File[] kids = destFolder.listFiles();
        if ( destFolder.exists() && kids != null && kids.length > 0) {
            String file = null;
            for (int i=0; i< kids.length; i++) {
                String childName = kids[i].getName();
                if ("nbproject".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_NetBeansProject");
                }
                else if ("build".equals(childName)) {    //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_BuildFolder");                                        
                }
                else if ("dist".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_DistFolder");                    
                }
//                else if ("build.xml".equals(childName)) {   //NOI18N
//                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_BuildXML");                    
//                }
                else if ("manifest.mf".equals(childName)) { //NOI18N
                    file = NbBundle.getMessage (ImportLocationVisual.class,"TXT_Manifest");                    
                }
                if (file != null) {
                    String format = NbBundle.getMessage (ImportLocationVisual.class,"MSG_ProjectFolderInvalid");
                    wizardDescriptor.putProperty( "WizardPanel_errorMessage", MessageFormat.format(format, new Object[] {file}));  //NOI18N
                    return false;
                }
            }
        }
        
        String fileName = moduleLocationTextField.getText();
        if (fileName.length()==0) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", "");  //NOI18N
            return false;
        }
        File f = new File (fileName);        
        if (!f.isDirectory() || !f.canRead()) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", NbBundle.getMessage(ImportLocationVisual.class,"MSG_IllegalSources"));
            return false;
        }
        
        wizardDescriptor.putProperty( "WizardPanel_errorMessage", "");  //NOI18N
        return true;
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelSrcLocationDesc = new javax.swing.JLabel();
        jLabelSrcLocation = new javax.swing.JLabel();
        moduleLocationTextField = new javax.swing.JTextField();
        jButtonSrcLocation = new javax.swing.JButton();
        jLabelPrjLocationDesc = new javax.swing.JLabel();
        jLabelPrjName = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        jLabelPrjLocation = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        jButtonPrjLocation = new javax.swing.JButton();
        jLabelContextPath = new javax.swing.JLabel();
        jTextFieldContextPath = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jLabelSrcLocationDesc.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrcDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelSrcLocationDesc, gridBagConstraints);

        jLabelSrcLocation.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_ImportLocation_LabelMnemonic").charAt(0));
        jLabelSrcLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelSrcLocation.setLabelFor(moduleLocationTextField);
        jLabelSrcLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrc_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelSrcLocation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(moduleLocationTextField, gridBagConstraints);
        moduleLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_IW_ImportLocation_A11YDesc"));

        jButtonSrcLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_BrowseLocation_Button"));
        jButtonSrcLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSrcLocationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonSrcLocation, gridBagConstraints);
        jButtonSrcLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_IW_ImportLocationBrowse_A11YDesc"));

        jLabelPrjLocationDesc.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationPrjDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 11, 0);
        add(jLabelPrjLocationDesc, gridBagConstraints);

        jLabelPrjName.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectName_LabelMnemonic").charAt(0));
        jLabelPrjName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjName.setLabelFor(projectNameTextField);
        jLabelPrjName.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectName_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelPrjName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(projectNameTextField, gridBagConstraints);
        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_ProjectName_A11YDesc"));

        jLabelPrjLocation.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectLocation_LabelMnemonic").charAt(0));
        jLabelPrjLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjLocation.setLabelFor(projectLocationTextField);
        jLabelPrjLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_Lablel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelPrjLocation, gridBagConstraints);

        projectLocationTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                projectLocationTextFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(projectLocationTextField, gridBagConstraints);
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NPW1_ProjectLocation_A11YDesc"));

        jButtonPrjLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_BrowseLocation_Button"));
        jButtonPrjLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrjLocationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonPrjLocation, gridBagConstraints);
        jButtonPrjLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_BrowseLocation_A11YDesc"));

        jLabelContextPath.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ContextPath_CheckBoxMnemonic").charAt(0));
        jLabelContextPath.setLabelFor(jTextFieldContextPath);
        jLabelContextPath.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ContextPath_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        add(jLabelContextPath, gridBagConstraints);

        jTextFieldContextPath.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldContextPathKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        add(jTextFieldContextPath, gridBagConstraints);
        jTextFieldContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_ContextPath_A11YDesc"));

    }//GEN-END:initComponents

    private void projectLocationTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_projectLocationTextFieldKeyReleased
        locationModified = true;
    }//GEN-LAST:event_projectLocationTextFieldKeyReleased

    private void jTextFieldContextPathKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldContextPathKeyReleased
        contextModified = true;
    }//GEN-LAST:event_jTextFieldContextPathKeyReleased

    private void jButtonPrjLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrjLocationActionPerformed
        JFileChooser chooser = createChooser(projectLocationTextField.getText());
        if (chooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText( projectDir.getAbsolutePath());
        }            
    }//GEN-LAST:event_jButtonPrjLocationActionPerformed

    private void jButtonSrcLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSrcLocationActionPerformed
        JFileChooser chooser = createChooser(moduleLocationTextField.getText());    
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            moduleLocationTextField.setText( projectDir.getAbsolutePath());
        }            
    }//GEN-LAST:event_jButtonSrcLocationActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonPrjLocation;
    private javax.swing.JButton jButtonSrcLocation;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelPrjLocation;
    private javax.swing.JLabel jLabelPrjLocationDesc;
    private javax.swing.JLabel jLabelPrjName;
    private javax.swing.JLabel jLabelSrcLocation;
    private javax.swing.JLabel jLabelSrcLocationDesc;
    protected javax.swing.JTextField jTextFieldContextPath;
    public javax.swing.JTextField moduleLocationTextField;
    public javax.swing.JTextField projectLocationTextField;
    public javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables
    
    private static JFileChooser createChooser(String path) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        if (path.length() > 0) {
            File f = new File(path);
            if (f.exists())
                chooser.setSelectedFile(f);
        }

        return chooser;
    }
    
    /** Handles changes in the project name and project directory
     */
    private void dataChanged(DocumentEvent e) {
        if (e.getDocument() == moduleDocument) {
            String moduleFolder = moduleLocationTextField.getText().trim();
            FileObject fo;
            try {
                fo = FileUtil.toFileObject(new File(moduleFolder));
            } catch (IllegalArgumentException exc) {
                return;
            }
            
            if (fo != null && !locationComputed)
                if (!containsWebInf(fo) && !locationModified)
                    projectLocationTextField.setText(moduleFolder);
                else
                    computeLocation();
        } else if (e.getDocument() == nameDocument) {
            if (!contextModified)
                jTextFieldContextPath.setText("/" + projectNameTextField.getText().replace(' ', '_'));
            if (locationComputed)
                computeLocation();
        }

        fireChanges();
    }
    
    private void fireChanges() {
        panel.fireChangeEvent();
    }
    
    private void computeLocation() {
        if (locationModified) //modified by the user, don't compute the location
            return;
        
        File projectLocation = ProjectChooser.getProjectsFolder();        
        StringBuffer folder = new StringBuffer(projectLocation.getAbsolutePath());
        if (!folder.toString().endsWith(File.separator))
            folder.append(File.separatorChar);
        folder.append(projectNameTextField.getText().trim());
        projectLocationTextField.setText(folder.toString());
        locationComputed = true;
    }
    
    private boolean containsWebInf(FileObject dir) {
        FileObject[] ch = dir.getChildren();
        for (int i = 0; i < ch.length; i++)
            if (ch[i].isFolder())
                if (ch[i].getName().equals("WEB-INF")) //NOI18N
                    return true;
    
        return false;
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ImportLocationVisual.class);
    }

}
