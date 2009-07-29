/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.project.ui.wizards;

import org.netbeans.modules.java.api.common.project.ui.wizards.FolderList;
import org.netbeans.modules.j2ee.common.FileSearchUtility;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.common.project.ui.ProjectImportLocationWizardPanel;
import org.netbeans.modules.j2ee.common.project.ui.ProjectLocationWizardPanel;
import org.netbeans.modules.j2ee.common.project.ui.ProjectServerWizardPanel;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

//XXX There should be a way how to add nonexistent test dir

//XXX There should be a way how to add nonexistent test dir

/**
 * Sets up name and location for new Java project from existing sources.
 * @author Tomas Zezula et al.
 */
public class PanelSourceFolders extends SettingsPanel implements PropertyChangeListener {

    private final Panel firer;
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
        jTextFieldWebInf.getDocument().addDocumentListener(pl);
    }

    public void initValues(FileObject fo) {        
        ((FolderList) this.sourcePanel).setLastUsedDir(FileUtil.toFile(fo));
        ((FolderList) this.testsPanel).setLastUsedDir(FileUtil.toFile(fo));
        
        FileObject guessFO;
        String webPages = ""; //NOI18N
        String webInf = ""; //NOI18N
        String libraries = ""; //NOI18N
        File javaRoots [] = null;
        
        guessFO = FileSearchUtility.guessDocBase(fo);
        if (guessFO != null)
            webPages = FileUtil.toFile(guessFO).getPath();
        guessFO = FileSearchUtility.guessWebInf(fo);
        if (guessFO != null)
            webInf = FileUtil.toFile(guessFO).getPath();
        guessFO = FileSearchUtility.guessLibrariesFolder(fo);
        if (guessFO != null)
            libraries = FileUtil.toFile(guessFO).getPath();
        javaRoots = FileSearchUtility.guessJavaRootsAsFiles(fo);
        
        //set the locations only if they weren't set before
        if (jTextFieldWebPages.getText().trim().equals(""))
            jTextFieldWebPages.setText(webPages);
        if (jTextFieldWebInf.getText().trim().equals(""))
            jTextFieldWebInf.setText(webInf);
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
        File[] srcRoot = (File[]) settings.getProperty (WizardProperties.JAVA_ROOT);
        if (srcRoot!=null) {
            ((FolderList)this.sourcePanel).setFiles(srcRoot);
        }
        File[] testRoot = (File[]) settings.getProperty (WizardProperties.TEST_ROOT);
        if (testRoot != null) {
            ((FolderList)this.testsPanel).setFiles (testRoot);
        }
        File projectLocation = (File) settings.getProperty(ProjectImportLocationWizardPanel.SOURCE_ROOT);
        ((FolderList)this.sourcePanel).setProjectFolder(projectLocation);
        ((FolderList)this.testsPanel).setProjectFolder(projectLocation);
        
        initValues(FileUtil.toFileObject(projectLocation));
    }

    void store (WizardDescriptor settings) {
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        settings.putProperty (WizardProperties.JAVA_ROOT,sourceRoots);
        settings.putProperty(WizardProperties.TEST_ROOT,testRoots);
        settings.putProperty(WizardProperties.DOC_BASE, jTextFieldWebPages.getText().trim());
        settings.putProperty(WizardProperties.LIB_FOLDER, jTextFieldLibraries.getText().trim());
        settings.putProperty(WizardProperties.WEBINF_FOLDER, jTextFieldWebInf.getText().trim());
    }
    
    boolean valid (WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty (ProjectLocationWizardPanel.PROJECT_DIR);  //NOI18N
	
	if (jTextFieldWebPages.getText().trim().length() == 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelSourceFolders.class, "MSG_WebPagesMandatory")); //NOI18N
	    return false;
	}
        
	if (jTextFieldWebInf.getText().trim().length() == 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelSourceFolders.class, "MSG_WebInfMandatory")); //NOI18N
	    return false;
	}
        
	File webPages = getWebPages();
	File webInf = getWebInfDir();
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        String result = checkValidity (projectLocation, webPages, webInf, sourceRoots, testRoots);
        if (result == null) {
            wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,"");   //NOI18N
            return true;
        }
        else {
            wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,result);       //NOI18N
            return false;
        }
    }

    private String checkValidity (final File projectLocation, final File webPages, final File webInf, final File[] sources, final File[] tests) {
        String ploc = projectLocation.getAbsolutePath ();
        
	if (projectLocation.equals(webPages))
	    return NbBundle.getMessage(PanelSourceFolders.class, "MSG_WebPagesFolderOverlapsProjectFolder"); //NOI18N
	    
	if (!webPages.exists() || !webPages.isDirectory())
	    return NbBundle.getMessage(PanelSourceFolders.class, "MSG_WebPagesFolderDoesNotExist"); //NOI18N
        
	if (!webInf.exists() || !webInf.isDirectory())
	    return NbBundle.getMessage(PanelSourceFolders.class, "MSG_WebInfFolderDoesNotExist"); //NOI18N
	
        FileObject webInfFO = FileUtil.toFileObject(webInf);
        FileObject webXml = webInfFO.getFileObject(ProjectWebModule.FILE_DD);
        //#74837 - filesystem is probably not refreshed and file object for non-existing file is found
        //rather setting to null that refreshing filesystem from a performance reason
        if (webXml != null && !webXml.isValid())
            webXml = null;
        Profile j2eeProfile = (Profile) wizardDescriptor.getProperty(ProjectServerWizardPanel.J2EE_LEVEL);
        if (webXml == null && (j2eeProfile == Profile.J2EE_13 || j2eeProfile == Profile.J2EE_14))
            return NbBundle.getMessage(PanelSourceFolders.class, "MSG_FileNotFound", webInf.getPath()); //NOI18N
        
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
        for (int i = 0; i < folders.length; i++) {
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
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel3 = new javax.swing.JLabel();
        jLabelWebPages = new javax.swing.JLabel();
        jTextFieldWebPages = new javax.swing.JTextField();
        jButtonWebpagesLocation = new javax.swing.JButton();
        jLabelWebInf = new javax.swing.JLabel();
        jTextFieldWebInf = new javax.swing.JTextField();
        jButtonWebInf = new javax.swing.JButton();
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
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName("null");
        jLabel3.getAccessibleContext().setAccessibleDescription("null");

        jLabelWebPages.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_IW_WebPagesLocation_LabelMnemonic").charAt(0));
        jLabelWebPages.setLabelFor(jTextFieldWebPages);
        jLabelWebPages.setText(NbBundle.getMessage(PanelSourceFolders.class, "LBL_IW_WebPagesLocation_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelWebPages, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 11);
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

        jLabelWebInf.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString("MNE_DeploymentDescriptorFolder").charAt(0));
        jLabelWebInf.setLabelFor(jTextFieldWebInf);
        jLabelWebInf.setText(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_DeploymentDescriptorFolder_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelWebInf, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 11);
        add(jTextFieldWebInf, gridBagConstraints);
        jTextFieldWebInf.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_WebInf")); // NOI18N

        jButtonWebInf.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString("MNE_BrowseWebInfLocation").charAt(0));
        jButtonWebInf.setText(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "PanelSourceFolderes.browseWebInf")); // NOI18N
        jButtonWebInf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWebInfActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonWebInf, gridBagConstraints);
        jButtonWebInf.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_BrowseWebInf")); // NOI18N

        jLabelLibraries.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_IW_LibrariesLocation_LabelMnemonic").charAt(0));
        jLabelLibraries.setLabelFor(jTextFieldLibraries);
        jLabelLibraries.setText(NbBundle.getMessage(PanelSourceFolders.class, "LBL_IW_LibrariesLocation_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelLibraries, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 11);
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
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        add(jButtonLibraries, gridBagConstraints);
        jButtonLibraries.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_BrowseLibrariesFolder")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(sourcePanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(testsPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName("null");
        getAccessibleContext().setAccessibleDescription("null");
    }// </editor-fold>//GEN-END:initComponents

private void jButtonWebInfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWebInfActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (jTextFieldWebInf.getText().length() > 0 && getWebInfDir().exists()) {
            chooser.setSelectedFile(getWebInfDir());
        } else {
            chooser.setCurrentDirectory((File) wizardDescriptor.getProperty(ProjectImportLocationWizardPanel.SOURCE_ROOT));
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File webInfDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextFieldWebInf.setText(webInfDir.getAbsolutePath());
        }
}//GEN-LAST:event_jButtonWebInfActionPerformed

    private void jButtonLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLibrariesActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (jTextFieldLibraries.getText().length() > 0 && getLibraries().exists()) {
            chooser.setSelectedFile(getLibraries());
        } else {
            chooser.setCurrentDirectory((File) wizardDescriptor.getProperty(ProjectImportLocationWizardPanel.SOURCE_ROOT));
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
            chooser.setCurrentDirectory((File) wizardDescriptor.getProperty(ProjectImportLocationWizardPanel.SOURCE_ROOT));
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File webPagesDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextFieldWebPages.setText(webPagesDir.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonWebpagesLocationActionPerformed

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonLibraries;
    private javax.swing.JButton jButtonWebInf;
    private javax.swing.JButton jButtonWebpagesLocation;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelLibraries;
    private javax.swing.JLabel jLabelWebInf;
    private javax.swing.JLabel jLabelWebPages;
    private javax.swing.JTextField jTextFieldLibraries;
    private javax.swing.JTextField jTextFieldWebInf;
    private javax.swing.JTextField jTextFieldWebPages;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JPanel testsPanel;
    // End of variables declaration//GEN-END:variables

    
    static class Panel implements WizardDescriptor.ValidatingPanel {
        
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private PanelSourceFolders component;
        private WizardDescriptor settings;
        
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
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
            changeSupport.fireChange();
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
    
    private File getWebInfDir() {
        return getAsFile(jTextFieldWebInf.getText());
    }
    
}
