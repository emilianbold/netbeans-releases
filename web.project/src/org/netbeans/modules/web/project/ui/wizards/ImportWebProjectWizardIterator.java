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
import org.openide.NotifyDescriptor;
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

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.modules.web.project.WebProjectGenerator;
import org.netbeans.modules.web.project.Utils;
import org.netbeans.modules.web.project.ui.FoldersListSettings;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;

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
            new ImportWebProjectWizardIterator.SecondPanel()
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
        File dirSrcF = (File) wiz.getProperty (WizardProperties.SOURCE_ROOT);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        String contextPath = (String) wiz.getProperty(WizardProperties.CONTEXT_PATH);
        String docBaseName = (String) wiz.getProperty(WizardProperties.DOC_BASE);
        String javaRootName = (String) wiz.getProperty(WizardProperties.JAVA_ROOT);
        String libName = (String) wiz.getProperty(WizardProperties.LIB_FOLDER);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);

        FileObject wmFO = FileUtil.toFileObject (dirSrcF);
        assert wmFO != null : "No such dir on disk: " + dirSrcF;
        assert wmFO.isFolder() : "Not really a dir: " + dirSrcF;
        
        FileObject javaRoot;
        FileObject docBase;
        FileObject libFolder;
        if (docBaseName == null || docBaseName.equals("")) //NOI18N
            docBase = guessDocBase(wmFO);
        else {
            File f = new File(docBaseName);
            docBase = FileUtil.toFileObject(f);
        }
        if (javaRootName == null || javaRootName.equals("")) //NOI18N
            javaRoot = guessJavaRoot(wmFO);
        else {
            File f = new File(javaRootName);
            javaRoot = FileUtil.toFileObject(f);
        }
        if (libName == null || libName.equals("")) //NOI18N
            libFolder = guessLibrariesFolder(wmFO);
        else {
            File f = new File(libName);
            libFolder = FileUtil.toFileObject(f);
        }

        if(j2eeLevel == null) {
            j2eeLevel = WebModule.J2EE_14_LEVEL;
        }

        String buildfile = getBuildfile();
        
        WebProjectGenerator.importProject (dirF, name, wmFO, javaRoot, docBase, libFolder, j2eeLevel, buildfile);
        FileObject dir = FileUtil.toFileObject (dirF);
        Project p = ProjectManager.getDefault().findProject(dir);
        
        ProjectWebModule wm = (ProjectWebModule) p.getLookup ().lookup (ProjectWebModule.class);
        if (wm != null) //should not be null
            wm.setContextPath(contextPath);

        Integer index = (Integer) wiz.getProperty(NewWebProjectWizardIterator.PROP_NAME_INDEX);
        if (index != null) {
            FoldersListSettings.getDefault().setNewProjectCount(index.intValue());
        }
        wiz.putProperty(WizardProperties.NAME, null); // reset project name

        // Returning set of DataObject of project diretory.
        // Project will be open and set as main
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
    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // uncomment the following and call when needed:
    // fireChangeEvent();
    /*
    private transient Set listeners = new HashSet(1); // Set<ChangeListener>
    public final void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
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
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        listeners = new HashSet(1);
    }
     */

    private FileObject guessDocBase (FileObject dir) {
        Enumeration ch = dir.getChildren (true);
        while (ch.hasMoreElements ()) {
            FileObject f = (FileObject) ch.nextElement ();
            if (f.isFolder () && f.getName ().equals ("WEB-INF")) {
                final FileObject webXmlFO = f.getFileObject ("web.xml");
                if (webXmlFO != null && webXmlFO.isData()) {
                    return f.getParent();
                }
            }
        }
        return null;
    }
    
    private FileObject guessLibrariesFolder (FileObject dir) {
        FileObject docBase = guessDocBase (dir);
        if (docBase != null) {
            FileObject lib = docBase.getFileObject ("WEB-INF/lib"); //NOI18N
            if (lib != null) {
                return lib;
            }
        }
        Enumeration ch = dir.getChildren (true);
        while (ch.hasMoreElements ()) {
            FileObject f = (FileObject) ch.nextElement ();
            if (f.getExt ().equals ("jar")) { //NOI18N
                return f.getParent ();
            }
        }
        return null;
    }
    
    private FileObject guessJavaRoot (FileObject dir) {
        Enumeration ch = dir.getChildren (true);
        try {
            while (ch.hasMoreElements ()) {
                FileObject f = (FileObject) ch.nextElement ();
                if (f.getExt ().equals ("java")) { //NOI18N
                    String pckg = guessPackageName (f);
                    String pkgPath = f.getParent ().getPath (); 
                    if (pckg != null && pkgPath.endsWith (pckg.replace ('.', '/'))) {
                        String rootName = pkgPath.substring (0, pkgPath.length () - pckg.length ());
                        return f.getFileSystem ().findResource (rootName);
                    }
                }
            }
        } catch (FileStateInvalidException fsie) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, fsie);
        }
        FileObject docBase = guessDocBase (dir);
        if (docBase != null) {
            FileObject classes = docBase.getFileObject ("WEB-INF/classes"); //NOI18N
            if (classes != null) {
                return classes;
            }
        }
        return null;
    }
    
    private String guessPackageName (FileObject f) {
        java.io.Reader r = null;
        try {
            r = new BufferedReader (new InputStreamReader (f.getInputStream (), "utf-8")); // NOI18N
            StringBuffer sb = new StringBuffer ();
            final char[] BUFFER = new char[4096];
            int len;

            for (;;) {
                len = r.read (BUFFER);
                if (len == -1) break;
                sb.append (BUFFER, 0, len);
            }
            int idx = sb.indexOf ("package"); // NOI18N
            if (idx >= 0) {
                int idx2 = sb.indexOf (";", idx);  // NOI18N
                if (idx2 >= 0) {
                    return sb.substring (idx + "package".length (), idx2).trim ();
                }
            }
        } catch (java.io.IOException ioe) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ioe);
        } finally {
            try { if (r != null) r.close (); } catch (java.io.IOException ioe) { // ignore this 
            }
        }
        return null;
    }
    
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

        //use it as a project root iff it is not sources or document root
        public boolean isSuitableProjectRoot (FileObject dir) {
            FileObject docRoot = guessDocBase (dir);
            FileObject srcRoot = guessJavaRoot (dir);
            return (docRoot == null || FileUtil.isParentOf (dir, docRoot))
                && (srcRoot == null || FileUtil.isParentOf (dir, srcRoot));
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
    
    public final class SecondPanel implements WizardDescriptor.FinishablePanel, WizardDescriptor.ValidatingPanel {
        private ImportWebLocationsVisual panel;
        private WizardDescriptor wizardDescriptor;

        private SecondPanel () {
        }
        
        public boolean isFinishPanel() {
            return true;
        }
        
        public java.awt.Component getComponent () {
            if (panel == null)
                panel = new ImportWebLocationsVisual(this);
            
            return panel;
        }
        
        public HelpCtx getHelp() {
            return new HelpCtx(SecondPanel.class);
        }
        
        public boolean isValid() {
            final String webPages = panel.jTextFieldWebPages.getText().trim();
            if (webPages.length() == 0) {
                setErrorMessage("MSG_WebPagesMandatory"); //NOI18N
                return false;
            }
            final File webPagesDir = new File(webPages);
            if (!webPagesDir.exists()) {
                setErrorMessage("MSG_WebPagesFolderDoesNotExist"); //NOI18N
                return false;
            }
            File projectDir = (File) wizardDescriptor.getProperty(WizardProperties.PROJECT_DIR);
            if(Utils.isParentOrEqual(webPagesDir, projectDir)) {
                setErrorMessage("MSG_WebPagesFolderOverlapsProjectFolder"); //NOI18N
                return false;
            }
            final String javaSources = panel.jTextFieldJavaSources.getText().trim();
            if (javaSources.length() == 0) {
                setErrorMessage("MSG_JavaSourcesMandatory"); //NOI18N
                return false;
            }
            if(!new File(javaSources).exists()) {
                setErrorMessage("MSG_JavaSourcesFolderDoesNotExist"); //NOI18N
                return false;
            }
            final String libraries = panel.jTextFieldLibraries.getText().trim();
            if (libraries.length() > 0) {
                if(!new File(libraries).exists()) {
                    setErrorMessage("MSG_LibrariesFolderDoesNotExist"); //NOI18N
                    return false;
                }
            }
            setErrorMessage(null);
            return true;
        }

        private void setErrorMessage(final String msg) {
            String s = (msg == null) ? null : NbBundle.getMessage(ImportWebProjectWizardIterator.class, msg);
            wizardDescriptor.putProperty("WizardPanel_errorMessage", s); //NOI18N
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
            presetSecondPanel(moduleLoc);
            
            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewProjectWizard to modify the title
            Object substitute = ((JComponent) panel).getClientProperty("NewProjectWizard_Title"); //NOI18N
            if (substitute != null)
                wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); //NOI18N
        }
        
        public void storeSettings (Object settings) {
            WizardDescriptor d = (WizardDescriptor) settings;
            
            d.putProperty(WizardProperties.DOC_BASE, panel.jTextFieldWebPages.getText().trim());
            d.putProperty(WizardProperties.JAVA_ROOT, panel.jTextFieldJavaSources.getText().trim());
            d.putProperty(WizardProperties.LIB_FOLDER, panel.jTextFieldLibraries.getText().trim());
            d.putProperty(WizardProperties.J2EE_LEVEL, panel.j2eeLevel);

            d.putProperty("NewProjectWizard_Title", null); //NOI18N
        }
        
        private void presetSecondPanel(String sourceRoot) {
            if (sourceRoot.length() > 0) {
                File f = new File(sourceRoot);
                FileObject fo;
                try {
                    fo = FileUtil.toFileObject(f);
                } catch (IllegalArgumentException exc) {
                    return; //invalid file object
                }
                if (fo != null) {
                    FileObject guessFO;
                    String webPages = ""; //NOI18N
                    String javaSources = ""; //NOI18N
                    String libraries = ""; //NOI18N

                    guessFO = guessDocBase(fo);
                    if (guessFO != null)
                        webPages = FileUtil.toFile(guessFO).getPath();
                    guessFO = guessJavaRoot(fo);
                    if (guessFO != null)
                        javaSources = FileUtil.toFile(guessFO).getPath();
                    guessFO = guessLibrariesFolder(fo);
                    if (guessFO != null)
                        libraries = FileUtil.toFile(guessFO).getPath();

                    ((ImportWebLocationsVisual) panels[1].getComponent()).initValues(sourceRoot, webPages, javaSources, libraries);
                }
            }
        }

        public void validate() throws WizardValidationException {
            panel.validateLocations();
            
            //delete existing class files
            searchClassFiles (FileUtil.toFileObject (FileUtil.normalizeFile(new File (panel.jTextFieldJavaSources.getText ()))));
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

                Object DELETE_OPTION = NbBundle.getMessage (ImportWebProjectWizardIterator.class, "TXT_DeleteOption"); // NOI18N
                Object KEEP_OPTION = NbBundle.getMessage (ImportWebProjectWizardIterator.class, "TXT_KeepOption"); // NOI18N
                Object CANCEL_OPTION = NbBundle.getMessage (ImportWebProjectWizardIterator.class, "TXT_CancelOption"); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor (
                        NbBundle.getMessage (ImportWebProjectWizardIterator.class, "MSG_FoundClassFiles"), // NOI18N
                        NbBundle.getMessage (ImportWebProjectWizardIterator.class, "MSG_FoundClassFiles_Title"), // NOI18N
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
                    throw new WizardValidationException (panel.jTextFieldJavaSources, "", ""); // NOI18N
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
                    if ("class".equals(fo.getExt())) //NOI18N
                        fo.delete ();
                } catch (IOException ioe) {
                    ErrorManager.getDefault ().notify (ioe);
                }
            }
        }

    }
}
