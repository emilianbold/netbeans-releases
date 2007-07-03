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

package org.netbeans.modules.ruby.railsprojects.ui.wizards;

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ruby.railsprojects.ui.FoldersListSettings;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.util.Exceptions;


/**
 * Sets up source and test folders for new Java project from existing sources.
 * @author Tomas Zezula et al.
 */
public class PanelProjectLocationExtSrc extends SettingsPanel {

    private PanelConfigureProject firer;
    private WizardDescriptor wizardDescriptor;
    private boolean calculateName;

    /** Creates new form PanelSourceFolders */
    public PanelProjectLocationExtSrc (PanelConfigureProject panel) {
        this.firer = panel;
        initComponents();

//        this.projectName.getDocument().addDocumentListener (new DocumentListener (){
//            public void changedUpdate(DocumentEvent e) {
//                calculateProjectFolder ();
//                dataChanged ();
//            }
//
//            public void insertUpdate(DocumentEvent e) {
//                calculateProjectFolder ();
//                dataChanged ();
//            }
//
//            public void removeUpdate(DocumentEvent e) {
//                calculateProjectFolder ();
//                dataChanged ();
//            }
//        });        
//        this.projectLocation.getDocument().addDocumentListener(new DocumentListener () {
//            public void changedUpdate(DocumentEvent e) {             
//                setCalculateProjectFolder (false);
//                dataChanged ();
//            }
//
//            public void insertUpdate(DocumentEvent e) {
//                setCalculateProjectFolder (false);
//                dataChanged ();
//            }
//
//            public void removeUpdate(DocumentEvent e) {
//                setCalculateProjectFolder (false);
//                dataChanged ();
//            }
//        });

    
        this.projectName.getDocument().addDocumentListener (new DocumentListener (){
            public void changedUpdate(DocumentEvent e) {             
                setCalculateProjectName (false);
                dataChanged ();
            }

            public void insertUpdate(DocumentEvent e) {
                setCalculateProjectName (false);
                dataChanged ();
            }

            public void removeUpdate(DocumentEvent e) {
                setCalculateProjectName (false);
                dataChanged ();
            }
        });        
        this.projectLocation.getDocument().addDocumentListener(new DocumentListener () {
            public void changedUpdate(DocumentEvent e) {
                calculateProjectName ();
                dataChanged ();
            }

            public void insertUpdate(DocumentEvent e) {
                calculateProjectName ();
                dataChanged ();
            }

            public void removeUpdate(DocumentEvent e) {
                calculateProjectName ();
                dataChanged ();
            }
        });
    
    }

    private synchronized void calculateProjectName () {
        if (this.calculateName) {    
            String path = this.projectLocation.getText().trim();
            if (path.length() > 0) {
                File f = new File(path);
                try {
                    File g = f.getCanonicalFile();
                    if (g != null) {
                        f = g;
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
                this.projectName.setText(f.getName());
            }
            File f = new File(this.projectLocation.getText());
            this.calculateName = true;
        }
    }

    private synchronized void setCalculateProjectName (boolean value) {
        this.calculateName = value;
    }
    
    
    private void dataChanged () {
        this.firer.fireChangeEvent();
    }


    void read (WizardDescriptor settings) {
        this.wizardDescriptor = settings;
        String path = null;        
        String projectName = null;
        File projectLocation = (File) settings.getProperty ("projdir");  //NOI18N
        if (projectLocation == null) {
            projectLocation = ProjectChooser.getProjectsFolder();                
//            int index = FoldersListSettings.getDefault().getNewProjectCount();
//            String formater = NbBundle.getMessage(PanelProjectLocationExtSrc.class,"TXT_JavaApplication");
//            File file;
//            do {
//                index++;                            
//                projectName = MessageFormat.format (formater, new Object[]{new Integer (index)});                
//                file = new File (projectLocation, projectName);                
//            } while (file.exists());                                
//            settings.putProperty (NewRailsProjectWizardIterator.PROP_NAME_INDEX, new Integer(index));                        
            this.projectLocation.setText (projectLocation.getAbsolutePath());        
            this.setCalculateProjectName(true);
            this.calculateProjectName();
            this.setCalculateProjectName(true);
            projectName = this.projectName.getText();
        }
        else {
            projectName = (String) settings.getProperty ("name"); //NOI18N
            boolean tmpFlag = this.calculateName;
            this.projectLocation.setText (projectLocation.getAbsolutePath());
            this.setCalculateProjectName(tmpFlag);
        }
//        this.projectName.setText (projectName);                
//        this.projectName.selectAll();
        this.projectLocation.selectAll();
    }

    void store (WizardDescriptor settings) {        
        settings.putProperty ("name",this.projectName.getText()); // NOI18N
        File projectsDir = new File(this.projectLocation.getText());
        settings.putProperty ("projdir", projectsDir); // NOI18N        
    }
    
    boolean valid (WizardDescriptor settings) {
        String result = checkValidity (this.projectName.getText(), this.projectLocation.getText());
        if (result == null) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage","");   //NOI18N
            return true;
        }
        else {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage",result);       //NOI18N
            return false;
        }
    }

    static String checkValidity (final String projectName, final String projectLocation) {
        if ( projectName.length() == 0 
             || projectName.indexOf('/')  > 0           //NOI18N
             || projectName.indexOf('\\') > 0           //NOI18N
             || projectName.indexOf(':')  > 0) {        //NOI18N
            // Display name not specified
            return NbBundle.getMessage(PanelProjectLocationExtSrc.class,"MSG_IllegalProjectName");
        }

        File projLoc = new File (projectLocation).getAbsoluteFile();

        if (PanelProjectLocationVisual.getCanonicalFile(projLoc) == null) {
            return NbBundle.getMessage (PanelProjectLocationVisual.class,"MSG_IllegalProjectLocation");
        }

        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return NbBundle.getMessage(PanelProjectLocationExtSrc.class,"MSG_ProjectFolderReadOnly");
        }

        File destFolder = FileUtil.normalizeFile(new File( projectLocation ));
        File[] kids = destFolder.listFiles();
        boolean foundRails = false;
        if ( destFolder.exists() && kids != null && kids.length > 0) {
            String file = null;
            for (int i=0; i< kids.length; i++) {
                String childName = kids[i].getName();
                if ("nbproject".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (PanelProjectLocationExtSrc.class,"TXT_NetBeansProject");
                }
                
                else if ("config".equals(childName)) {
                    foundRails = true;
                }
                // Only the nbproject file will be rewritten by Rails project - other files can conflict
//                else if ("build".equals(childName)) {    //NOI18N
//                    file = NbBundle.getMessage (PanelProjectLocationExtSrc.class,"TXT_BuildFolder");
//                }
//                else if ("dist".equals(childName)) {   //NOI18N
//                    file = NbBundle.getMessage (PanelProjectLocationExtSrc.class,"TXT_DistFolder");
//                }
//                else if ("build.xml".equals(childName)) {   //NOI18N
//                    file = NbBundle.getMessage (PanelProjectLocationExtSrc.class,"TXT_BuildXML");
//                }
//                else if ("manifest.mf".equals(childName)) { //NOI18N
//                    file = NbBundle.getMessage (PanelProjectLocationExtSrc.class,"TXT_Manifest");
//                }
                
                
                
                if (file != null) {
                    String format = NbBundle.getMessage (PanelProjectLocationExtSrc.class,"MSG_ProjectFolderInvalid");
                    return MessageFormat.format(format, new Object[] {file});
                }
            }
        }
        
        if (!foundRails) {
            return NbBundle.getMessage(PanelProjectLocationExtSrc.class, "TXT_NoRails");
        }

        // For now I'm creating NetBeans Rails application in place
//        // #47611: if there is a live project still residing here, forbid project creation.
//        if (destFolder.isDirectory()) {
//            FileObject destFO = FileUtil.toFileObject(destFolder);
//            assert destFO != null : "No FileObject for " + destFolder;
//            boolean clear = false;
//            try {
//                clear = ProjectManager.getDefault().findProject(destFO) == null;
//            } catch (IOException e) {
//                // need not report here; clear remains false -> error
//            }
//            if (!clear) {
//                return NbBundle.getMessage(PanelProjectLocationExtSrc.class, "MSG_ProjectFolderHasDeletedProject");
//            }
//        }
        return null;
    }        

    void validate(WizardDescriptor settings) throws WizardValidationException {
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        projectName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel4.setLabelFor(jPanel2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "LBL_ProjectNameAndLocationLabel")); // NOI18N

        jLabel5.setLabelFor(projectName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "LBL_NWP1_ProjectName_Label")); // NOI18N

        jLabel6.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(PanelProjectLocationExtSrc.class).getString("LBL_NWP1_CreatedProjectFolder_LablelMnemonic").charAt(0));
        jLabel6.setLabelFor(projectLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "LBL_NWP1_CreatedProjectFolder_Lablel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "LBL_NWP1_BrowseLocation_Button3")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectLocation(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                    .add(projectLocation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton3))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jButton3)
                    .add(projectLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(projectName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_jLabel4")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_jLabel4")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_projectNameLabel")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_projectNameLabel")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_projectLocationLabel")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_projectLocationLabel")); // NOI18N
        jButton3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_browseButton")); // NOI18N
        jButton3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_browseButton")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(jPanel2, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
        jPanel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_jPanel1")); // NOI18N
        jPanel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_jPanel1")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSN_PanelSourceFolders")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationExtSrc.class, "ACSD_PanelSourceFolders")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseProjectLocation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectLocation
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(NbBundle.getMessage(PanelProjectLocationExtSrc.class,"LBL_NWP1_SelectProjectLocation"));
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String path = this.projectLocation.getText();
        if (path.length() > 0) {
            File f = new File (path);
            if (f.exists()) {
                chooser.setSelectedFile (f);
            }
        }
        if (chooser.showOpenDialog(this)== JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                this.projectLocation.setText (FileUtil.normalizeFile(file).getAbsolutePath());
            }
        }
    }//GEN-LAST:event_browseProjectLocation

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JTextField projectName;
    // End of variables declaration//GEN-END:variables


}
