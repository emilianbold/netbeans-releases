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

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.ui.FoldersListSettings;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Sets up source and test folders for new Java project from existing sources.
 * @author Tomas Zezula et al.
 */
public class PanelSourceFolders extends SettingsPanel {

    private PanelConfigureProject firer;
    private WizardDescriptor wizardDescriptor;
    private boolean calculatePF;

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
        this.tests.getDocument().addDocumentListener(pl);
        this.projectName.getDocument().addDocumentListener (new DocumentListener (){
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
        });        
        this.projectLocation.getDocument().addDocumentListener(new DocumentListener () {
            public void changedUpdate(DocumentEvent e) {             
                setCalculateProjectFolder (false);
                dataChanged ();
            }

            public void insertUpdate(DocumentEvent e) {
                setCalculateProjectFolder (false);
                dataChanged ();
            }

            public void removeUpdate(DocumentEvent e) {
                setCalculateProjectFolder (false);
                dataChanged ();
            }
        });
    }

    private synchronized void calculateProjectFolder () {
        if (this.calculatePF) {                        
            File f = ProjectChooser.getProjectsFolder();
            this.projectLocation.setText (f.getAbsolutePath() + File.separator + this.projectName.getText());
            this.calculatePF = true;
        }
    }
    
    private synchronized void setCalculateProjectFolder (boolean value) {
        this.calculatePF = value;
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
        if (path!=null) {
            this.sources.setText (path);
        }
        File testRoot = (File) settings.getProperty ("testRoot");       //NOI18N
        if (testRoot != null) {
            path = testRoot.getAbsolutePath();
        }
        if (path!=null) {
            this.tests.setText (path);
        }
        String projectName = null;
        File projectLocation = (File) settings.getProperty ("projdir");  //NOI18N
        // bugfix #46387, check wrong default overtaken from other project's types
        if (projectLocation == null || ( !projectLocation.exists () )) {
            projectLocation = ProjectChooser.getProjectsFolder();                
            int index = FoldersListSettings.getDefault().getNewProjectCount();
            String formater = NbBundle.getMessage(PanelSourceFolders.class,"TXT_JavaProject");
            File file;
            do {
                index++;                            
                projectName = MessageFormat.format (formater, new Object[]{new Integer (index)});                
                file = new File (projectLocation, projectName);                
            } while (file.exists());                                
            settings.putProperty (NewJ2SEProjectWizardIterator.PROP_NAME_INDEX, new Integer(index));                        
            this.projectLocation.setText (projectLocation.getAbsolutePath());        
            this.setCalculateProjectFolder(true);
        }
        else {
            projectName = (String) settings.getProperty ("name"); //NOI18N
            boolean tmpFlag = this.calculatePF;
            this.projectLocation.setText (projectLocation.getAbsolutePath());
            this.setCalculateProjectFolder(tmpFlag);
        }
        this.projectName.setText (projectName);                
        this.sources.selectAll ();
    }

    void store (WizardDescriptor settings) {
        File srcRoot = null;
        File testRoot = null;
        String srcPath = this.sources.getText();
        if (srcPath.length() > 0) {
            srcRoot = FileUtil.normalizeFile(new File(srcPath));           
        }
        String testPath = this.tests.getText();
        if (testPath.length()>0) {
            testRoot = FileUtil.normalizeFile(new File(testPath));            
        }
        settings.putProperty ("sourceRoot",srcRoot);    //NOI18N
        settings.putProperty("testRoot",testRoot);      //NOI18N
        settings.putProperty ("name",this.projectName.getText()); // NOI18N
        File projectsDir = new File(this.projectLocation.getText());
        settings.putProperty ("projdir", projectsDir); // NOI18N        
    }
    
    boolean valid (WizardDescriptor settings) {
        String result = checkValidity (this.projectName.getText(), this.projectLocation.getText(),
            this.sources.getText(), this.tests.getText());
        if (result == null) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage","");   //NOI18N
            return true;
        }
        else {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage",result);       //NOI18N
            return false;
        }
    }

    static String checkValidity (final String projectName, final String projectLocation, final String sources, final String tests ) {
        if ( projectName.length() == 0 ) {
            // Display name not specified
            return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectName");
        }

        File projLoc = new File (projectLocation).getAbsoluteFile();

        if (PanelProjectLocationVisual.getCanonicalFile(projLoc) == null) {
            return NbBundle.getMessage (PanelProjectLocationVisual.class,"MSG_IllegalProjectLocation");
        }

        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return NbBundle.getMessage(PanelSourceFolders.class,"MSG_ProjectFolderReadOnly");
        }

        File destFolder = FileUtil.normalizeFile(new File( projectLocation ));
        File[] kids = destFolder.listFiles();
        if ( destFolder.exists() && kids != null && kids.length > 0) {
            String file = null;
            for (int i=0; i< kids.length; i++) {
                String childName = kids[i].getName();
                if ("nbproject".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_NetBeansProject");
                }
                else if ("build".equals(childName)) {    //NOI18N
                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_BuildFolder");
                }
                else if ("dist".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_DistFolder");
                }
                else if ("build.xml".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_BuildXML");
                }
                else if ("manifest.mf".equals(childName)) { //NOI18N
                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_Manifest");
                }
                if (file != null) {
                    String format = NbBundle.getMessage (PanelSourceFolders.class,"MSG_ProjectFolderInvalid");
                    return MessageFormat.format(format, new Object[] {file});
                }
            }
        }

        // #47611: if there is a live project still residing here, forbid project creation.
        if (destFolder.isDirectory()) {
            FileObject destFO = FileUtil.toFileObject(destFolder);
            assert destFO != null : "No FileObject for " + destFolder;
            boolean clear = false;
            try {
                clear = ProjectManager.getDefault().findProject(destFO) == null;
            } catch (IOException e) {
                // need not report here; clear remains false -> error
            }
            if (!clear) {
                return NbBundle.getMessage(PanelSourceFolders.class, "MSG_ProjectFolderHasDeletedProject");
            }
        }


        if (sources.length()==0) {
            return "";  //NOI18N
        }
        File f = FileUtil.normalizeFile(new File (sources));
        if (!f.isDirectory() || !f.canRead()) {
            return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalSources");
        }

        String ploc = destFolder.getAbsolutePath ();
        String sloc = f.getAbsolutePath ();
        if (ploc.equals (sloc) || ploc.startsWith (sloc + File.separatorChar)) {
            return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectFolder");
        }

        if (tests.length()>0) {
            File tf = FileUtil.normalizeFile(new File (tests));
            String tloc = tf.getAbsolutePath();
            if (ploc.equals(tloc) || ploc.startsWith(tloc + File.separatorChar)) {
                return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectFolder");
            }
            if (tloc.equals(sloc) || tloc.startsWith(sloc + File.separatorChar) || sloc.startsWith(tloc + File.separatorChar)) {
                return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalTests");
            }
        }
        return null;
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // sources root
        searchClassFiles (FileUtil.toFileObject (FileUtil.normalizeFile(new File (sources.getText ()))));
        // test root, not asked in issue 48198
        //searchClassFiles (FileUtil.toFileObject (FileUtil.normalizeFile(new File (tests.getText ()))));
    }
    
    private void searchClassFiles (FileObject folder) throws WizardValidationException {
        Enumeration en = folder.getData (true);
        boolean found = false;
        while (!found && en.hasMoreElements ()) {
            Object obj = en.nextElement ();
            assert obj instanceof FileObject : "Instance of FileObject: " + obj; // NOI18N
            FileObject fo = (FileObject) obj;
            found = "class".equals (fo.getExt ()); // NOI18N
        }
        
        if (found) {
            
            Object DELETE_OPTION = NbBundle.getMessage (PanelSourceFolders.class, "TXT_DeleteOption"); // NOI18N
            Object KEEP_OPTION = NbBundle.getMessage (PanelSourceFolders.class, "TXT_KeepOption"); // NOI18N
            Object CANCEL_OPTION = NbBundle.getMessage (PanelSourceFolders.class, "TXT_CancelOption"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor (
                    NbBundle.getMessage (PanelSourceFolders.class, "MSG_FoundClassFiles"), // NOI18N
                    NbBundle.getMessage (PanelSourceFolders.class, "MSG_FoundClassFiles_Title"), // NOI18N
                    NotifyDescriptor.YES_NO_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] {DELETE_OPTION, KEEP_OPTION, CANCEL_OPTION},
                    null
                    );
            
            Object result = DialogDisplayer.getDefault().notify(desc);
            if (DELETE_OPTION.equals (result)) {
                deleteClassFiles (folder);
            } else if (!KEEP_OPTION.equals (result)) {
                // cancel, back to wizard
                throw new WizardValidationException (sources, "", ""); // NOI18N
            }            
        }
    }
    
    private void deleteClassFiles (FileObject folder) {
        Enumeration en = folder.getData (true);
        while (en.hasMoreElements ()) {
            Object obj = en.nextElement ();
            assert obj instanceof FileObject : "Instance of FileObject: " + obj;
            FileObject fo = (FileObject) obj;
            try {
                fo.delete ();
            } catch (IOException ioe) {
                ErrorManager.getDefault ().notify (ioe);
            }
        }
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
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        projectName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_PanelSourceFolders"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_PanelSourceFolders"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_SourceDirectoriesLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelSourceFolders.class).getString("ACSN_jLabel3"));
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelSourceFolders.class).getString("ACSD_jLabel3"));

        jLabel1.setLabelFor(sources);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "CTL_SourceRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_jLabel1"));
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_jLabel1"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(sources, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_BrowseLocation_Button1"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseSourceRoot(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(jButton1, gridBagConstraints);
        jButton1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_browseButton"));
        jButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_browseButton"));

        jLabel2.setLabelFor(tests);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "CTL_TestRoot"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_jLabel2"));
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_jLabel2"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(tests, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_BrowseLocation_Button2"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseTestRoot(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(jButton2, gridBagConstraints);
        jButton2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_browseButton"));
        jButton2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_browseButton"));

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel4.setLabelFor(jPanel2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_ProjectNameAndLocationLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_jLabel4"));
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_jLabel4"));

        jLabel5.setLabelFor(projectName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_ProjectName_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel2.add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_projectNameLabel"));
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_projectNameLabel"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(projectName, gridBagConstraints);

        jLabel6.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(PanelSourceFolders.class).getString("LBL_NWP1_CreatedProjectFolder_LablelMnemonic").charAt(0));
        jLabel6.setLabelFor(projectLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_CreatedProjectFolder_Lablel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel2.add(jLabel6, gridBagConstraints);
        jLabel6.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_projectLocationLabel"));
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_projectLocationLabel"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(projectLocation, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_NWP1_BrowseLocation_Button3"));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectLocation(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(jButton3, gridBagConstraints);
        jButton3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_browseButton"));
        jButton3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_browseButton"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
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
        jPanel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_jPanel1"));
        jPanel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_jPanel1"));

    }//GEN-END:initComponents

    private void browseProjectLocation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectLocation
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
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
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
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
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JTextField projectName;
    private javax.swing.JTextField sources;
    private javax.swing.JTextField tests;
    // End of variables declaration//GEN-END:variables


}
