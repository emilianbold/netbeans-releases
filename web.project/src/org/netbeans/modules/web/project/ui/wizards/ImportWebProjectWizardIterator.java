/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;

import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.FoldersListSettings;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 * Wizard to create a new Web project for an existing web module.
 * @author Pavel Buzek, Radko Najman
 */
public class ImportWebProjectWizardIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = 1L;
    private String buildfileName = GeneratedFilesHelper.BUILD_XML_PATH;

    private String moduleLoc;
    
    /** Create a new wizard iterator. */
    public ImportWebProjectWizardIterator () {}
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ImportWebProjectWizardIterator.ThePanel(),
            new PanelSourceFolders.Panel()
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(ImportWebProjectWizardIterator.class, "LBL_IW_Step1"), //NOI18N
            NbBundle.getMessage(ImportWebProjectWizardIterator.class, "LBL_IW_Step2") //NOI18N
        };
    }
    
    public Set/*<DataObject>*/ instantiate(TemplateWizard wiz) throws IOException/*, IllegalStateException*/ {
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        
        File dirSrcF = (File) wiz.getProperty (WizardProperties.SOURCE_ROOT);
        
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        String contextPath = (String) wiz.getProperty(WizardProperties.CONTEXT_PATH);
        String docBaseName = (String) wiz.getProperty(WizardProperties.DOC_BASE);
        File[] sourceFolders = (File[]) wiz.getProperty(WizardProperties.JAVA_ROOT);
        File[] testFolders = (File[]) wiz.getProperty(WizardProperties.TEST_ROOT);
        String libName = (String) wiz.getProperty(WizardProperties.LIB_FOLDER);
        String serverInstanceID = (String) wiz.getProperty(WizardProperties.SERVER_INSTANCE_ID);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);

        FileObject wmFO = FileUtil.toFileObject (dirSrcF);
        assert wmFO != null : "No such dir on disk: " + dirSrcF;
        assert wmFO.isFolder() : "Not really a dir: " + dirSrcF;
        
        FileObject docBase;
        FileObject libFolder;
        if (docBaseName == null || docBaseName.equals("")) //NOI18N
            docBase = FileSearchUtility.guessDocBase(wmFO);
        else {
            File f = new File(docBaseName);
            docBase = FileUtil.toFileObject(f);
        }
//        if (javaRootName == null || javaRootName.equals("")) //NOI18N
//            javaRoot = guessJavaRoot(wmFO);
//        else {
//            File f = new File(javaRootName);
//            javaRoot = FileUtil.toFileObject(f);
//        }
        if (libName == null || libName.equals("")) //NOI18N
            libFolder = FileSearchUtility.guessLibrariesFolder(wmFO);
        else {
            File f = new File(libName);
            libFolder = FileUtil.toFileObject(f);
        }

        if(j2eeLevel == null) {
            j2eeLevel = WebModule.J2EE_14_LEVEL;
        }

        String buildfile = getBuildfile();
        
        AntProjectHelper h = WebProjectUtilities.importProject (dirF, name, wmFO, sourceFolders, testFolders, docBase, libFolder, j2eeLevel, serverInstanceID, buildfile);
        
        FileObject dir = FileUtil.toFileObject(dirF);
        Project earProject = (Project) wiz.getProperty(WizardProperties.EAR_APPLICATION);
        WebProject createdWebProject = (WebProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdWebProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addWebModule(createdWebProject.getAPIWebModule());
            }
        }

        Project p = ProjectManager.getDefault().findProject(dir);        
        ProjectWebModule wm = (ProjectWebModule) p.getLookup ().lookup (ProjectWebModule.class);
        if (wm != null) //should not be null
            wm.setContextPath(contextPath);

        Integer index = (Integer) wiz.getProperty(NewWebProjectWizardIterator.PROP_NAME_INDEX);
        if (index != null) {
            FoldersListSettings.getDefault().setNewProjectCount(index.intValue());
        }
        wiz.putProperty(WizardProperties.NAME, null); // reset project name

        // downgrade the Java platform or src level to 1.4        
        String platformName = (String)wiz.getProperty(WizardProperties.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(WizardProperties.SOURCE_LEVEL);
        if (platformName != null || sourceLevel != null) {
            WebProjectUtilities.setPlatform(h, platformName, sourceLevel);
        }
        
        return Collections.singleton(DataObject.find(dir));
    }
    
    private String getBuildfile() {
        return buildfileName;
    }
    
    private void setBuildfile(String name) {
        buildfileName = name;
    }

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;

    public void initialize(TemplateWizard wiz) {
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                jc.putClientProperty("NewProjectWizard_Title", NbBundle.getMessage(ImportWebProjectWizardIterator.class, "TXT_WebExtSources")); // NOI18N
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(ImportWebProjectWizardIterator.class, "LBL_WizardStepsCount"), new String[] {(new Integer(index + 1)).toString(), (new Integer(panels.length)).toString()}); //NOI18N
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}

    public final class ThePanel implements WizardDescriptor.ValidatingPanel {

        private ImportLocationVisual panel;
        private WizardDescriptor wizardDescriptor;
        
        private ThePanel () {
        }
        
        public java.awt.Component getComponent () {
            if (panel == null) {
                panel = new ImportLocationVisual (this);
            }
            return panel;
        }
        
        public HelpCtx getHelp() {
            return new HelpCtx(ThePanel.class);
        }

        public boolean isValid () {
            getComponent();
            return panel.valid(wizardDescriptor);
        }
        
        private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
        public final void addChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.add(l);
            }
        }
        public final void removeChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.remove(l);
            }
        }
        protected final void fireChangeEvent() {
            Iterator it;
            synchronized (listeners) {
                it = new HashSet(listeners).iterator();
            }
            ChangeEvent ev = new ChangeEvent(this);
            while (it.hasNext()) {
                ((ChangeListener)it.next()).stateChanged(ev);
            }
        }
        public void readSettings (Object settings) {
            wizardDescriptor = (WizardDescriptor) settings;        
            panel.read(wizardDescriptor);

            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewProjectWizard to modify the title
            Object substitute = ((JComponent) panel).getClientProperty("NewProjectWizard_Title"); //NOI18N
            if (substitute != null)
                wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); //NOI18N
        }
        
        public void storeSettings (Object settings) {
            WizardDescriptor d = (WizardDescriptor) settings;
            panel.store(d);
            ((WizardDescriptor) d).putProperty ("NewProjectWizard_Title", null); //NOI18N
            
            moduleLoc = panel.moduleLocationTextField.getText().trim();
        }

        public void validate() throws WizardValidationException {
            File dirF = new File(panel.projectLocationTextField.getText());
            if (!new File(dirF, GeneratedFilesHelper.BUILD_XML_PATH).exists()) {
                setBuildfile(GeneratedFilesHelper.BUILD_XML_PATH);
            } else {
                File buildFile = new File(dirF, getBuildfile());
                if (buildFile.exists()) {
                    JButton ok = createButton(
                            "LBL_IW_Buildfile_OK", "ACS_IW_BuildFileDialog_OKButton_LabelMnemonic", //NOI18N
                            "LBL_IW_BuildFileDialog_OK_LabelMnemonic"); //NOI18N
                    JButton cancel = createButton(
                            "LBL_IW_Buildfile_Cancel", "ACS_IW_BuildFileDialog_CancelButton_LabelMnemonic", //NOI18N
                            "LBL_IW_BuildFileDialog_Cancel_LabelMnemonic"); //NOI18N
                    final ImportBuildfile ibf = new ImportBuildfile(buildFile, ok);
                    DialogDescriptor descriptor = new DialogDescriptor(ibf,
                            NbBundle.getMessage(ImportWebProjectWizardIterator.class, "LBL_IW_BuildfileTitle"), //NOI18N
                            true, new Object[]{ok, cancel}, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN,
                            null, null);
                    Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                    dialog.setVisible(true);
                    if (descriptor.getValue() != ok) {
                        throw new WizardValidationException(panel.projectLocationTextField, "", "");
                    }
                    setBuildfile(ibf.getBuildName());
                }
            }
        }

        private JButton createButton(String labelId, String labelMnemonicId, String mnemonicId) {
            JButton button = new JButton(NbBundle.getMessage(ImportWebProjectWizardIterator.class, labelId));
            button.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(ImportWebProjectWizardIterator.class, labelMnemonicId));
            button.setMnemonic(NbBundle.getMessage(ImportWebProjectWizardIterator.class, mnemonicId).charAt(0));
            return button;
        }

    }
    
}
