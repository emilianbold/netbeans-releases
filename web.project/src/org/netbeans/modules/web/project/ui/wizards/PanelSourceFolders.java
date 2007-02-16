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

package org.netbeans.modules.web.project.ui.wizards;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

//XXX There should be a way how to add nonexistent test dir

/**
 * Sets up name and location for new Java project from existing sources.
 * @author Tomas Zezula et al.
 */
public class PanelSourceFolders extends SettingsPanel implements PropertyChangeListener {

    private Panel firer;
    private WizardDescriptor wizardDescriptor;

    /** Creates new form PanelSourceFolders */
    public PanelSourceFolders (Panel panel) {
        this.firer = panel;
        initComponents();
        this.setName(NbBundle.getMessage(PanelSourceFolders.class,"LAB_ConfigureSourceRoots"));
        this.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelSourceFolders.class,"TXT_WebExtSources")); // NOI18N
        this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PanelSourceFolders.class,"AN_PanelSourceFolders"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelSourceFolders.class,"AD_PanelSourceFolders"));
        this.sourcePanel.addPropertyChangeListener (this);
        this.testsPanel.addPropertyChangeListener(this);
        ((FolderList)this.sourcePanel).setRelatedFolderList((FolderList)this.testsPanel);
        ((FolderList)this.testsPanel).setRelatedFolderList((FolderList)this.sourcePanel);        
	
        DocumentListener pl = new DocumentListener () {
            public void changedUpdate(DocumentEvent e) {
		firer.fireChangeEvent();
            }

            public void insertUpdate(DocumentEvent e) {
		firer.fireChangeEvent();
            }

            public void removeUpdate(DocumentEvent e) {
		firer.fireChangeEvent();
            }
        };
        jTextFieldWebPages.getDocument().addDocumentListener(pl);

    }

    public void initValues(FileObject fo) {        
        ((FolderList) this.sourcePanel).setLastUsedDir(FileUtil.toFile(fo));
        ((FolderList) this.testsPanel).setLastUsedDir(FileUtil.toFile(fo));
        
        FileObject guessFO;
        String webPages = ""; //NOI18N
        String libraries = ""; //NOI18N
        File javaRoots [] = null;
        
        guessFO = FileSearchUtility.guessDocBase(fo);
        if (guessFO != null)
            webPages = FileUtil.toFile(guessFO).getPath();
        guessFO = FileSearchUtility.guessLibrariesFolder(fo);
        if (guessFO != null)
            libraries = FileUtil.toFile(guessFO).getPath();
        javaRoots = FileSearchUtility.guessJavaRootsAsFiles(fo);
        
        //set the locations only if they weren't set before
        if (jTextFieldWebPages.getText().trim().equals(""))
            jTextFieldWebPages.setText(webPages);
        if (jTextFieldLibraries.getText().trim().equals(""))
            jTextFieldLibraries.setText(libraries);
        
        if (((FolderList) this.sourcePanel).getFiles().length == 0 && javaRoots.length > 0)
            ((FolderList) this.sourcePanel).setFiles(javaRoots);
    }
        
    public void propertyChange(PropertyChangeEvent evt) {
        if (FolderList.PROP_FILES.equals(evt.getPropertyName())) {
            this.dataChanged();
        } else if (FolderList.PROP_LAST_USED_DIR.equals (evt.getPropertyName())) {
            if (evt.getSource() == this.sourcePanel) {                
                ((FolderList)this.testsPanel).setLastUsedDir 
                        ((File)evt.getNewValue());
            }
            else if (evt.getSource() == this.testsPanel) {
                ((FolderList)this.sourcePanel).setLastUsedDir 
                        ((File)evt.getNewValue());
            }
        }
    }

    private void dataChanged () {
        this.firer.fireChangeEvent();
    }

    void read (WizardDescriptor settings) {
        this.wizardDescriptor = settings;
        File[] srcRoot = (File[]) settings.getProperty (WizardProperties.JAVA_ROOT);      //NOI18N
        if (srcRoot!=null) {
            ((FolderList)this.sourcePanel).setFiles(srcRoot);
        }
        File[] testRoot = (File[]) settings.getProperty (WizardProperties.TEST_ROOT);       //NOI18N
        if (testRoot != null) {
            ((FolderList)this.testsPanel).setFiles (testRoot);
        }
        File projectLocation = (File) settings.getProperty(WizardProperties.SOURCE_ROOT);
        ((FolderList)this.sourcePanel).setProjectFolder(projectLocation);
        ((FolderList)this.testsPanel).setProjectFolder(projectLocation);
        
        initValues(FileUtil.toFileObject(projectLocation));
    }

    void store (WizardDescriptor settings) {
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        settings.putProperty (WizardProperties.JAVA_ROOT,sourceRoots);    //NOI18N
        settings.putProperty(WizardProperties.TEST_ROOT,testRoots);      //NOI18N
        settings.putProperty(WizardProperties.DOC_BASE, jTextFieldWebPages.getText().trim());
        settings.putProperty(WizardProperties.LIB_FOLDER, jTextFieldLibraries.getText().trim());
    }
    
    boolean valid (WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty (WizardProperties.PROJECT_DIR);  //NOI18N
	
	if (jTextFieldWebPages.getText().trim().length() == 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(PanelSourceFolders.class, "MSG_WebPagesMandatory")); //NOI18N
	    return false;
	}
	
	File webPages = getWebPages();
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        String result = checkValidity (projectLocation, webPages, sourceRoots, testRoots);
        if (result == null) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage","");   //NOI18N
            return true;
        }
        else {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage",result);       //NOI18N
            return false;
        }
    }

    private String checkValidity (final File projectLocation, final File webPages, final File[] sources, final File[] tests) {
        String ploc = projectLocation.getAbsolutePath ();
        
	if (projectLocation.equals(webPages))
	    return NbBundle.getMessage(PanelSourceFolders.class, "MSG_WebPagesFolderOverlapsProjectFolder"); //NOI18N
	    
	if (!webPages.exists() || !webPages.isDirectory())
	    return NbBundle.getMessage(PanelSourceFolders.class, "MSG_WebPagesFolderDoesNotExist"); //NOI18N
	
        FileObject webInf = FileUtil.toFileObject(webPages).getFileObject(ProjectWebModule.FOLDER_WEB_INF);
	if (webInf == null)
	    return NbBundle.getMessage(PanelSourceFolders.class, "MSG_WebInfCorrupted", webPages.getPath()); //NOI18N
        else {
            FileObject webXml = webInf.getFileObject(ProjectWebModule.FILE_DD);
            
            //#74837 - filesystem is probably not refreshed and file object for non-existing file is found
            //rather setting to null that refreshing filesystem from a performance reason
            if (webXml != null && !webXml.isValid())
                webXml = null;
            
            String j2eeLevel = (String) wizardDescriptor.getProperty(WizardProperties.J2EE_LEVEL);
            if (webXml == null && (j2eeLevel.equals(J2eeModule.J2EE_13) || j2eeLevel.equals(J2eeModule.J2EE_14)))
                return NbBundle.getMessage(PanelSourceFolders.class, "MSG_FileNotFound", webPages.getPath()); //NOI18N
        }        
        
        for (int i=0; i<sources.length;i++) {
            if (!sources[i].isDirectory() || !sources[i].canRead()) {
                return MessageFormat.format(NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalSources"), //NOI18N
                        new Object[] {sources[i].getAbsolutePath()});
            }
            String sloc = sources[i].getAbsolutePath ();
            if (ploc.equals (sloc) || ploc.startsWith (sloc + File.separatorChar)) {
                return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectFolder"); //NOI18N
            }
        }
        for (int i=0; i<tests.length; i++) {
            if (!tests[i].isDirectory() || !tests[i].canRead()) {
                return MessageFormat.format(NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalTests"), //NOI18N
                        new Object[] {sources[i].getAbsolutePath()});
            }            
            String tloc = tests[i].getAbsolutePath();
            if (ploc.equals(tloc) || ploc.startsWith(tloc + File.separatorChar)) {
                return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectFolder"); //NOI18N
            }
        }
        return null;
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // sources root
        searchClassFiles (((FolderList)this.sourcePanel).getFiles());
        // test root, not asked in issue 48198
        //searchClassFiles (FileUtil.toFileObject (FileUtil.normalizeFile(new File (tests.getText ()))));
    }
    
    private void searchClassFiles (File[] folders) throws WizardValidationException {
        boolean found = false;
        for (int i=0; i<folders.length; i++) {
            FileObject folder = FileUtil.toFileObject(folders[i]);
            if (folder != null) {
                Enumeration en = folder.getData (true);
                while (!found && en.hasMoreElements ()) {
                    Object obj = en.nextElement ();
                    assert obj instanceof FileObject : "Instance of FileObject: " + obj; // NOI18N
                    FileObject fo = (FileObject) obj;
                    found = "class".equals (fo.getExt ()); // NOI18N
                }
            }
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
                deleteClassFiles (folders);
            } else if (!KEEP_OPTION.equals (result)) {
                // cancel, back to wizard
                throw new WizardValidationException (this.sourcePanel, "", ""); // NOI18N
            }
        }
    }
    
    private void deleteClassFiles (File[] folders) {
        for (int i=0; i<folders.length; i++) {
            FileObject folder = FileUtil.toFileObject(folders[i]);
            Enumeration en = folder.getData (true);
            while (en.hasMoreElements ()) {
                Object obj = en.nextElement ();
                assert obj instanceof FileObject : "Instance of FileObject: " + obj;
                FileObject fo = (FileObject) obj;
                try {
                    if ("class".equals (fo.getExt ())) { // NOI18N
                        fo.delete ();
                    }
                } catch (IOException ioe) {
                    ErrorManager.getDefault ().notify (ioe);
                }
            }
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

        jLabel3 = new javax.swing.JLabel();
        jLabelWebPages = new javax.swing.JLabel();
        jTextFieldWebPages = new javax.swing.JTextField();
        jButtonWebpagesLocation = new javax.swing.JButton();
        jLabelLibraries = new javax.swing.JLabel();
        jTextFieldLibraries = new javax.swing.JTextField();
        jButtonLibraries = new javax.swing.JButton();
        sourcePanel = new FolderList (NbBundle.getMessage(PanelSourceFolders.class,"CTL_SourceRoots"), NbBundle.getMessage(PanelSourceFolders.class,"MNE_SourceRoots").charAt(0),NbBundle.getMessage(PanelSourceFolders.class,"AD_SourceRoots"), NbBundle.getMessage(PanelSourceFolders.class,"CTL_AddSourceRoot"),
            NbBundle.getMessage(PanelSourceFolders.class,"MNE_AddSourceFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_AddSourceFolder"),NbBundle.getMessage(PanelSourceFolders.class,"MNE_RemoveSourceFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_RemoveSourceFolder"));
        testsPanel = new FolderList (NbBundle.getMessage(PanelSourceFolders.class,"CTL_TestRoots"), NbBundle.getMessage(PanelSourceFolders.class,"MNE_TestRoots").charAt(0),NbBundle.getMessage(PanelSourceFolders.class,"AD_TestRoots"), NbBundle.getMessage(PanelSourceFolders.class,"CTL_AddTestRoot"),
            NbBundle.getMessage(PanelSourceFolders.class,"MNE_AddTestFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_AddTestFolder"),NbBundle.getMessage(PanelSourceFolders.class,"MNE_RemoveTestFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_RemoveTestFolder"));

        setPreferredSize(new java.awt.Dimension(500, 340));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_IW_LocationDesc_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(null);
        jLabel3.getAccessibleContext().setAccessibleDescription(null);

        jLabelWebPages.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_IW_WebPagesLocation_LabelMnemonic").charAt(0));
        jLabelWebPages.setLabelFor(jTextFieldWebPages);
        jLabelWebPages.setText(NbBundle.getMessage(PanelSourceFolders.class, "LBL_IW_WebPagesLocation_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        add(jLabelWebPages, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        add(jTextFieldWebPages, gridBagConstraints);
        jTextFieldWebPages.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_WebPagesFolder")); // NOI18N

        jButtonWebpagesLocation.setMnemonic(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_WebPagesFolder_MNE").charAt(0));
        jButtonWebpagesLocation.setText(NbBundle.getMessage(PanelSourceFolders.class, "LBL_BrowseWebPagesLocation_Button")); // NOI18N
        jButtonWebpagesLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWebpagesLocationActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonWebpagesLocation, gridBagConstraints);
        jButtonWebpagesLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_BrowseWebPageFolder")); // NOI18N

        jLabelLibraries.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_IW_LibrariesLocation_LabelMnemonic").charAt(0));
        jLabelLibraries.setLabelFor(jTextFieldLibraries);
        jLabelLibraries.setText(NbBundle.getMessage(PanelSourceFolders.class, "LBL_IW_LibrariesLocation_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelLibraries, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jTextFieldLibraries, gridBagConstraints);
        jTextFieldLibraries.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_LibrariesFolder")); // NOI18N

        jButtonLibraries.setMnemonic(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "MNE_BrowseLibrariesLocation").charAt(0));
        jButtonLibraries.setText(NbBundle.getMessage(PanelSourceFolders.class, "LBL_BrowseLibrariesLocation_Button")); // NOI18N
        jButtonLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLibrariesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonLibraries, gridBagConstraints);
        jButtonLibraries.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_BrowseLibrariesFolder")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(sourcePanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(testsPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(null);
        getAccessibleContext().setAccessibleDescription(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLibrariesActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (jTextFieldLibraries.getText().length() > 0 && getLibraries().exists()) {
            chooser.setSelectedFile(getLibraries());
        } else {
            chooser.setCurrentDirectory((File) wizardDescriptor.getProperty(WizardProperties.SOURCE_ROOT));
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File configFilesDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextFieldLibraries.setText(configFilesDir.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonLibrariesActionPerformed

    private void jButtonWebpagesLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWebpagesLocationActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (jTextFieldWebPages.getText().length() > 0 && getWebPages().exists()) {
            chooser.setSelectedFile(getWebPages());
        } else {
            chooser.setCurrentDirectory((File) wizardDescriptor.getProperty(WizardProperties.SOURCE_ROOT));
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File webPagesDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextFieldWebPages.setText(webPagesDir.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonWebpagesLocationActionPerformed

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonLibraries;
    private javax.swing.JButton jButtonWebpagesLocation;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelLibraries;
    private javax.swing.JLabel jLabelWebPages;
    private javax.swing.JTextField jTextFieldLibraries;
    private javax.swing.JTextField jTextFieldWebPages;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JPanel testsPanel;
    // End of variables declaration//GEN-END:variables

    
    static class Panel implements WizardDescriptor.ValidatingPanel {
        
        private ArrayList listeners;        
        private PanelSourceFolders component;
        private WizardDescriptor settings;
        
        public synchronized void removeChangeListener(ChangeListener l) {
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove(l);
        }

        public void addChangeListener(ChangeListener l) {
            if (this.listeners == null) {
                this.listeners = new ArrayList ();
            }
            this.listeners.add (l);
        }

        public void readSettings(Object settings) {
            this.settings = (WizardDescriptor) settings;
            this.component.read (this.settings);
            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewProjectWizard to modify the title
            Object substitute = component.getClientProperty ("NewProjectWizard_Title"); // NOI18N
            if (substitute != null) {
                this.settings.putProperty ("NewProjectWizard_Title", substitute); // NOI18N
            }
        }

        public void storeSettings(Object settings) {
            this.component.store (this.settings);
        }
        
        public void validate() throws WizardValidationException {
            this.component.validate(this.settings);
        }
                
        public boolean isValid() {
            return this.component.valid (this.settings);
        }

        public synchronized java.awt.Component getComponent() {
            if (this.component == null) {
                this.component = new PanelSourceFolders (this);
            }
            return this.component;
        }

        public HelpCtx getHelp() {
            return new HelpCtx (PanelSourceFolders.class);
        }        
        
        private void fireChangeEvent () {
           Iterator it = null;
           synchronized (this) {
               if (this.listeners == null) {
                   return;
               }
               it = ((ArrayList)this.listeners.clone()).iterator();
           }
           ChangeEvent event = new ChangeEvent (this);
           while (it.hasNext()) {
               ((ChangeListener)it.next()).stateChanged(event);
           }
        }
                
    }

    private File getAsFile(String filename) {
        return FileUtil.normalizeFile(new File(filename));
    }

    public File getWebPages() {
        return getAsFile(jTextFieldWebPages.getText());
    }

    public File getLibraries() {
        return getAsFile(jTextFieldLibraries.getText());
    }
}
