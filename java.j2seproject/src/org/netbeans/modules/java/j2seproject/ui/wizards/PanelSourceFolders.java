/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui.wizards;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.java.j2seproject.ui.FoldersListSettings;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.io.File;
import java.text.MessageFormat;

/**
 *
 * @author  tom
 */
public class PanelSourceFolders extends SettingsPanel {

    private PanelConfigureProject firer;
    private WizardDescriptor wizardDescriptor;

    /** Creates new form PanelSourceFolders */
    public PanelSourceFolders (PanelConfigureProject panel) {
        this.firer = panel;
        initComponents();
        DocumentListener pl = new DocumentListener () {
            public void changedUpdate(DocumentEvent e) {
                dataChanged ();
            }

            public void insertUpdate(DocumentEvent e) {
                dataChanged ();
            }

            public void removeUpdate(DocumentEvent e) {
                dataChanged ();
            }
        };
        this.sources.getDocument().addDocumentListener(pl);
        pl = new DocumentListener (){
            public void changedUpdate(DocumentEvent e) {
                calculateProjectFolder ();
                dataChanged ();
            }

            public void insertUpdate(DocumentEvent e) {
                calculateProjectFolder ();
                dataChanged ();
            }

            public void removeUpdate(DocumentEvent e) {
                calculateProjectFolder ();
                dataChanged ();
            }
        };
        this.projectName.getDocument().addDocumentListener (pl);        
        this.projectLocation.getDocument().addDocumentListener(pl);
    }

    private void calculateProjectFolder () {
        File f = new File (this.projectLocation.getText());
        this.projectFolder.setText (f.getAbsolutePath() + File.separator + 
        this.projectName.getText());
    }

    private void dataChanged () {
        this.firer.fireChangeEvent();
    }


    void read (WizardDescriptor settings) {
        this.wizardDescriptor = settings;
        String path = null;
        File srcRoot = (File) settings.getProperty ("sourceRoot");      //NOI18N
        if (srcRoot!=null) {
            path = srcRoot.getAbsolutePath();
        }
        else {
            path = FoldersListSettings.getDefault().getLastExternalSourceRoot();
        }
        if (path!=null) {
            this.sources.setText (path);
        }
        File testRoot = (File) settings.getProperty ("testRoot");       //NOI18N
        if (testRoot != null) {
            path = testRoot.getAbsolutePath();
        }
        else {
            path = FoldersListSettings.getDefault().getLastExternalTestRoot();
        }
        if (path!=null) {
            this.tests.setText (path);
        }
        String projectName = (String) settings.getProperty ("displayName"); //NOI18N
        if (projectName == null) {
            projectName = MessageFormat.format (NbBundle.getMessage(PanelSourceFolders.class,"TXT_JavaProject"), new Object[]{
                new Integer (FoldersListSettings.getDefault().getNewProjectCount()+1)
            });
        }
        this.projectName.setText (projectName);
        
        File projectLocation = (File) settings.getProperty ("projdir");  //NOI18N
        if (projectLocation == null) {
            projectLocation = ProjectChooser.getProjectsFolder();
        }
        else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocation.setText (projectLocation.getAbsolutePath());
    }

    void store (WizardDescriptor settings) {
        File srcRoot = null;
        File testRoot = null;
        String srcPath = this.sources.getText();
        if (srcPath.length() > 0) {
            srcRoot = new File (srcPath);
            FoldersListSettings.getDefault().setLastExternalSourceRoot (srcPath);
        }
        String testPath = this.tests.getText();
        if (testPath.length()>0) {
            testRoot = new File (testPath);
            FoldersListSettings.getDefault().setLastExternalTestRoot (testPath);
        }
        settings.putProperty ("sourceRoot",srcRoot);    //NOI18N
        settings.putProperty("testRoot",testRoot);      //NOI18N
        settings.putProperty ("displayName",this.projectName.getText());
        settings.putProperty ("codename",NewJ2SEProjectWizardIterator.getSystemName(this.projectName.getText()));
        settings.putProperty ("projdir",new File (this.projectFolder.getText()));
        File projectsDir = new File(this.projectLocation.getText());
        if (projectsDir.isDirectory()) {
            ProjectChooser.setProjectsFolder (projectsDir);
        }
    }
    
    boolean valid (WizardDescriptor settings) {
        if ( projectName.getText().length() == 0 ) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectName"));
            return false; // Display name not specified
        }
        
        File destFolder = new File( projectFolder.getText() );
        File[] kids = destFolder.listFiles();
        if ( destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", NbBundle.getMessage(PanelSourceFolders.class,"MSG_ProjectFolderExists"));
            return false;
        }                        
        String fileName = sources.getText();
        if (fileName.length()==0) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", "");  //NOI18N
            return false;
        }
        File f = new File (fileName);        
        if (!f.isDirectory() || !f.canRead()) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalSources"));
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

        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        sources = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        tests = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        projectName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        projectFolder = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("LBL_SourceDirectoriesLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel3, gridBagConstraints);

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("CTL_SourceRootMnemonic").charAt(0));
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("CTL_SourceRoot"));
        jLabel1.setLabelFor(sources);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        add(sources, gridBagConstraints);

        jButton1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("LBL_NWP1_BrowseLocation_Button"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseSourceRoot(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(6, 3, 6, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButton1, gridBagConstraints);

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("CTL_TestRootMnemonic").charAt(0));
        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("CTL_TestRoot"));
        jLabel2.setLabelFor(tests);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        add(tests, gridBagConstraints);

        jButton2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("LBL_NWP1_BrowseLocation_Button"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseTestRoot(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(6, 3, 6, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButton2, gridBagConstraints);

        jLabel4.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("LBL_ProjectNameAndLocationLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel4, gridBagConstraints);

        jLabel5.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("LBL_NWP1_ProjectName_LabelMnemonic").charAt(0));
        jLabel5.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("LBL_NWP1_ProjectName_Label"));
        jLabel5.setLabelFor(projectName);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(projectName, gridBagConstraints);

        jLabel6.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("LBL_NWP1_ProjectLocation_LabelMnemonic").charAt(0));
        jLabel6.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("LBL_NWP1_ProjectLocation_Label"));
        jLabel6.setLabelFor(projectLocation);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel6, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(projectLocation, gridBagConstraints);

        jButton3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("LBL_NWP1_BrowseLocation_Button"));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectLocation(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButton3, gridBagConstraints);

        jLabel7.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("LBL_NWP1_CreatedProjectFolder_LablelMnemonic").charAt(0));
        jLabel7.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/wizards/Bundle").getString("LBL_NWP1_CreatedProjectFolder_Lablel"));
        jLabel7.setLabelFor(projectFolder);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel7, gridBagConstraints);

        projectFolder.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(projectFolder, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents

    private void browseProjectLocation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectLocation
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(PanelSourceFolders.class,"LBL_NWP1_SelectProjectLocation"));
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
                this.projectLocation.setText (file.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_browseProjectLocation

    private void browseTestRoot(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseTestRoot
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(PanelSourceFolders.class,"CTL_SelectTestsFolder"));
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String path = this.tests.getText();
        if (path.length() > 0) {
            File f = new File (path);        
            if (f.exists()) {
                chooser.setSelectedFile (f);
            }
        }
        if (chooser.showOpenDialog(this)== JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                this.tests.setText (file.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_browseTestRoot


    private void browseSourceRoot(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseSourceRoot
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(PanelSourceFolders.class,"CTL_SelectSourceFolder"));
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String path = this.sources.getText();
        if (path.length() > 0) {
            File f = new File (path);
            if (f.exists()) {
                chooser.setSelectedFile (f);
            }
        }
        if (chooser.showOpenDialog(this)== JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                this.sources.setText (file.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_browseSourceRoot
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField projectFolder;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JTextField projectName;
    private javax.swing.JTextField sources;
    private javax.swing.JTextField tests;
    // End of variables declaration//GEN-END:variables


}
