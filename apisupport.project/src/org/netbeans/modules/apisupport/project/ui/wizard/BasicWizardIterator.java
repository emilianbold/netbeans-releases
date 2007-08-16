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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.indent.Reformat;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Convenient class for implementing {@link org.openide.WizardDescriptor.InstantiatingIterator}.
 *
 * @author Radek Matous
 */
public abstract class BasicWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator {
    
    private int position = 0;
    private BasicWizardIterator.PrivateWizardPanel[] wizardPanels;
    
    /** Create a new wizard iterator. */
    protected BasicWizardIterator() {}
    
    /** @return all panels provided by this {@link org.openide.WizardDescriptor.InstantiatingIterator} */
    protected abstract BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz);
    
    /** Basic visual panel.*/
    public abstract static class Panel extends BasicVisualPanel {
        
        protected Panel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        /**
         * Returned name is used by a wizard. e.g. on its left side in the
         * <em>step list</em>.
         * @return name of panel
         */
        protected abstract String getPanelName();
        
        /**
         * Gives a chance to store an instance of {@link
         * BasicWizardIterator.BasicDataModel}. It is called when a panel is
         * going to be <em>hidden</em> (e.g. when switching to next/previous
         * panel).
         */
        protected abstract void storeToDataModel();
        
        /**
         * Gives a chance to refresh a panel (usually by reading a state of an
         * instance of {@link BasicWizardIterator.BasicDataModel}. It is called
         * when a panel is going to be <em>displayed</em> (e.g. when switching
         * from next/previous panel).
         */
        protected abstract void readFromDataModel();
        
        protected abstract HelpCtx getHelp();
        
    }
    
    /** DataModel that is passed through individual panels.*/
    public static class BasicDataModel {
        
        private Project project;
        private NbModuleProvider module;
        private SourceGroup sourceRootGroup;
        private String packageName;
        
        /** Creates a new instance of NewFileDescriptorData */
        public BasicDataModel(WizardDescriptor wiz) {
            Project tmpProject = Templates.getProject(wiz);
            
            if (tmpProject == null) {
                throw new IllegalArgumentException();
            }
            module = tmpProject.getLookup().lookup(NbModuleProvider.class);
            if (module == null) {
                throw new IllegalArgumentException(tmpProject.getClass().toString());
            }
            
            project = tmpProject;
            // #66339 need to prefetch the packagename and populate data with it..
            FileObject fo = Templates.getTargetFolder(wiz);
            if (fo != null) {
                Sources srcs = ProjectUtils.getSources(project); // #63247: don't use lookup directly
                SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (int i = 0; i < grps.length; i++) {
                    if (FileUtil.isParentOf(grps[i].getRootFolder(), fo)) {
                        String relPath = FileUtil.getRelativePath(grps[i].getRootFolder(), fo);
                        relPath = relPath.replace('/', '.');
                        setPackageName(relPath);
                        break;
                    }
                }
            }
        }
        
        public Project getProject() {
            return project;
        }
        
        public NbModuleProvider getModuleInfo() {
            return module;
        }
        
        public String getPackageName() {
            return packageName;
        }
        
        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
        
        public SourceGroup getSourceRootGroup() {
            if (sourceRootGroup == null) {
                FileObject tempSrcRoot = getModuleInfo().getSourceDirectory();
                assert tempSrcRoot != null;
                
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (int i = 0; sourceRootGroup == null && i < groups.length; i++) {
                    if (groups[i].getRootFolder().equals(tempSrcRoot)) {
                        sourceRootGroup = groups[i];
                    }
                }
            }
            return sourceRootGroup;
        }
        
        public String getDefaultPackagePath(String fileName, boolean resource) {
            return (resource ? getModuleInfo().getResourceDirectoryPath(false) : getModuleInfo().getSourceDirectoryPath()) + '/' +
                    getPackageName().replace('.','/') + '/' + fileName;
        }
        
        /**
         * Conditionally adds an operation to the given {@link
         * CreatedModifiedFiles}. Result of the operation, after given
         * CreatedModifiedFiles are run, is copied (into the package) icon
         * representing given <code>origIconPath</code>. If the origIconPath is
         * already inside the project's source directory nothing happens.
         *
         * @return path of the icon relative to the project's source directory
         */
        public String addCreateIconOperation(CreatedModifiedFiles cmf, String origIconPath) {
            FileObject origIconFO = FileUtil.toFileObject(new File(origIconPath));
            String relativeIconPath = null;
            if (!FileUtil.isParentOf(Util.getResourceDirectory(getProject()), origIconFO)) {
                String iconPath = getDefaultPackagePath(origIconFO.getNameExt(), true);
                try {
                    cmf.add(cmf.createFile(iconPath, origIconFO.getURL()));
                    relativeIconPath = getPackageName().replace('.', '/') + '/' + origIconFO.getNameExt();
                } catch (FileStateInvalidException exc) {
                    Util.err.notify(exc);
                }
            } else {
                relativeIconPath = FileUtil.getRelativePath(Util.getResourceDirectory(getProject()), origIconFO);
            }
            return relativeIconPath;
        }
        
    }
    
    public void initialize(WizardDescriptor wiz) {
        // mkleint: copied from the NewJavaFileWizardIterator.. there must be something painfully wrong..
        String[] beforeSteps = null;
        Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        position = 0;
        BasicWizardIterator.Panel[] panels = createPanels(wiz);
        String[] steps = BasicWizardIterator.createSteps(beforeSteps, panels);
        wizardPanels = new BasicWizardIterator.PrivateWizardPanel[panels.length];
        
        for (int i = 0; i < panels.length; i++) {
            wizardPanels[i] = new BasicWizardIterator.PrivateWizardPanel(panels[i], steps, i);
        }
    }
    
    // mkleint: copied from the NewJavaFileWizardIterator.. there must be something painfully wrong..
    private static String[] createSteps(String[] before, BasicWizardIterator.Panel[] panels) {
        assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getPanelName();
            }
        }
        return res;
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        wizardPanels = null;
    }
    
    public String name() {
        return ((BasicWizardIterator.PrivateWizardPanel)
        current()).getPanel().getPanelName();
    }
    
    public boolean hasNext() {
        return position < (wizardPanels.length - 1);
    }
    
    public boolean hasPrevious() {
        return position > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        position++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        position--;
    }
    
    public WizardDescriptor.Panel current() {
        return wizardPanels[position];
    }
    
    /**
     * Convenience method for accessing Bundle resources from this package.
     */
    protected final String getMessage(String key) {
        return NbBundle.getMessage(getClass(), key);
    }
    
    public final void addChangeListener(ChangeListener  l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    protected Set<FileObject> getCreatedFiles(final CreatedModifiedFiles cmf, final Project project) throws IOException {
        Collection<DataObject> toBeShown = new HashSet<DataObject>();
        Set<FileObject> set = new HashSet<FileObject>();
        for (String path : cmf.getCreatedPaths()) {
            FileObject fo = project.getProjectDirectory().getFileObject(path);
            assert fo != null : path;
            formatFile(fo);
            DataObject dObj = DataObject.find(fo);
            if (dObj != null && toBeShown.size() < 10 && toBeShown.add(dObj)) {
                set.add(fo);
            }
        }
        return set;
    }
    
    private static BaseDocument getDocument(final FileObject fo) throws DataObjectNotFoundException, IOException {
        BaseDocument doc = null;
        DataObject dObj = DataObject.find(fo);
        if (dObj != null) {
            EditorCookie editor = dObj.getCookie(EditorCookie.class);
            if (editor != null) {
                doc = (BaseDocument) editor.openDocument();
            }
        }
        return doc;
    }
    
    // copy-pasted-adjusted from org.netbeans.editor.ActionFactory.FormatAction
    private static void formatFile(final FileObject fo) {
        assert fo != null;
        BaseDocument doc = null;
        boolean reformatted = false;
        try {
            doc = BasicWizardIterator.getDocument(fo);
            if (doc == null) {
                return;
            }
            Reformat reformat = Reformat.get(doc);
            reformat.lock();
            try {
                doc.atomicLock();
                try {
                    reformat.reformat(0, doc.getLength());
                } finally {
                    doc.atomicUnlock();
                }
            } finally {
                reformat.unlock();
            }
            try {
                DataObject.find(fo).getCookie(EditorCookie.class).saveDocument();
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        } catch (Exception ex) {
            // no disaster
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot reformat the file: " + fo.getPath()); // NOI18N
        }
    }
    
    private static final class PrivateWizardPanel extends BasicWizardPanel {
        
        private BasicWizardIterator.Panel panel;
        
        PrivateWizardPanel(BasicWizardIterator.Panel panel, String[] allSteps, int stepIndex) {
            super(panel.getSettings());
            panel.addPropertyChangeListener(this);
            panel.setName(panel.getPanelName()); // NOI18N
            this.panel = panel;
            panel.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(stepIndex)); // NOI18N
            // names of currently used steps
            panel.putClientProperty("WizardPanel_contentData", allSteps); // NOI18N
        }
        
        private BasicWizardIterator.Panel getPanel() {
            return panel;
        }
        
        public Component getComponent() {
            return getPanel();
        }
        
        public @Override void storeSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            if (WizardDescriptor.NEXT_OPTION.equals(wiz.getValue()) ||
                    WizardDescriptor.FINISH_OPTION.equals(wiz.getValue())) {
                panel.storeToDataModel();
            }
            //XXX hack
            ((WizardDescriptor) settings).putProperty("NewFileWizard_Title", null); // NOI18N
        }
        
        public @Override void readSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            // mkleint - copied from someplace.. is definitely weird..
            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewProjectWizard to modify the title
            Object substitute = getPanel().getClientProperty("NewFileWizard_Title"); // NOI18N
            if (substitute != null) {
                wiz.putProperty("NewFileWizard_Title", substitute); // NOI18N
            }
            
            if (WizardDescriptor.NEXT_OPTION.equals(wiz.getValue()) || wiz.getValue() == null) {
                panel.readFromDataModel();
            }
        }
        
        public @Override HelpCtx getHelp() {
            return getPanel().getHelp();
        }
        
    }
    
}

