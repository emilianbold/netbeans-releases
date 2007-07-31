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

package org.netbeans.modules.xml.schema.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.RetrieverEngine;
import org.netbeans.modules.xml.xam.ui.ProjectConstants;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;


public final class RetrieveXMLResourceWizardIterator implements TemplateWizard.Iterator {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    private int index;
    
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new RetrieveXMLResourceWizardPanel1()
            };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }
    
    public Set instantiate(TemplateWizard wizard) throws IOException {
        
        RetrieveXMLResourceVisualPanel1.SourceType srcType = (RetrieveXMLResourceVisualPanel1.SourceType) wizard.getProperty(IConstants.SOURCE_LOCATION_TYPE_KEY);
        if(srcType == RetrieveXMLResourceVisualPanel1.SourceType.LOCAL_FILE){
            return instantiateLocalFile();
        }
        if(srcType == RetrieveXMLResourceVisualPanel1.SourceType.URL_ADDR){
            return instantiateURL();
        }
        return Collections.singleton(wizard.getTargetFolder());
    }
    
    private Set instantiateLocalFile() throws IOException{
        URI getFileURI = null;
        try {
            getFileURI = new URI((String) wizard.getProperty(IConstants.SOURCE_LOCATION_KEY));
        } catch (URISyntaxException ex) {
            throw new IOException(ex.getMessage());
        }
        File storedFile = (File) wizard.getProperty(IConstants.TARGET_FILE_KEY);
        boolean overwriteFiles = ((Boolean)wizard.getProperty(IConstants.OVERWRITE_FILES)).booleanValue();
        new ImportDirectory(new File(getFileURI), storedFile, overwriteFiles);
        if (storedFile == null) {
            // Doesn't matter what it is, just so it's not null.
            storedFile = new File(System.getProperty("user.dir"));
        }
        // Must put something in the result set so the wizard appears in
        // the projectui module's New file menu.
        FileObject fobj = FileUtil.toFileObject(FileUtil.normalizeFile(storedFile));
        if(fobj == null)
            return Collections.EMPTY_SET;
        return Collections.singleton(fobj);
    }
    
    private Set instantiateURL() throws IOException{
        File saveRootFile = (File) wizard.getProperty(IConstants.TARGET_FILE_KEY);
        String sourceURL = (String) wizard.getProperty(IConstants.SOURCE_LOCATION_KEY);
        File selectedSaveRootFolder = (File) wizard.getProperty(IConstants.USER_SELECTED_SAVE_ROOT);
        boolean overwriteFiles = ((Boolean)wizard.getProperty(IConstants.OVERWRITE_FILES)).booleanValue();
        RetrieverEngine instance = RetrieverEngine.getRetrieverEngine(selectedSaveRootFolder);
        RetrieveEntry rent = null;
        if(((Boolean) wizard.getProperty(IConstants.RETRIVE_CLOSURE_KEY)).booleanValue())
            rent = new RetrieveEntry(null, sourceURL, null, null, DocumentTypesEnum.schema, true);
        else
            rent = new RetrieveEntry(null, sourceURL, null, saveRootFile, DocumentTypesEnum.schema, false);
        instance.addResourceToRetrieve(rent);
        instance.setFileOverwrite(overwriteFiles);
        instance.start();
        if (saveRootFile == null) {
            // Doesn't matter what it is, just so it's not null.
            saveRootFile = new File(System.getProperty("user.dir"));
        }
        // Must put something in the result set so the wizard appears in
        // the projectui module's New file menu.
        FileObject fobj = FileUtil.toFileObject(FileUtil.normalizeFile(saveRootFile));
        if(fobj == null)
            return Collections.EMPTY_SET;
        return Collections.singleton(fobj);
    }
    
    public void initialize(TemplateWizard wizard) {
        
        //System.out.println("Initialize");
        Project currentProject = Templates.getProject(wizard);
        Sources sources = ProjectUtils.getSources(currentProject);
        List<SourceGroup> roots = new ArrayList<SourceGroup>();
        SourceGroup[] javaRoots =
                sources.getSourceGroups(ProjectConstants.JAVA_SOURCES_TYPE);
        roots.addAll(Arrays.asList(javaRoots));
        if (roots.isEmpty()) {
            SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            roots.addAll(Arrays.asList(sourceGroups));
        }
        DataFolder folder = DataFolder.findFolder(roots.get(0).getRootFolder());
        DataFolder projectFolder =
                DataFolder.findFolder(currentProject.getProjectDirectory());
        try {
            if (wizard.getTargetFolder().equals(projectFolder)) {
                wizard.setTargetFolder(folder);
            }
        } catch (IOException ioe) {
            wizard.setTargetFolder(folder);
        }
        
        wizard.putProperty(IConstants.THIS_WIZARD_ITERATOR_KEY, this);
        wizard.putProperty(IConstants.CURRENT_PROJECT_KEY, currentProject);
        FileObject currentFolder = Templates.getTargetFolder(wizard);
        wizard.putProperty(IConstants.CURRENT_FOLDER_KEY, currentFolder);
        this.wizard = wizard;
    }
    
    public void uninitialize(TemplateWizard wizard) {
        //System.out.println("UNINIT");
        //System.out.println("$$$$"+wizard.getValue()+"%%%"+wizard.FINISH_OPTION+"^^^"+wizard.CLOSED_OPTION);
        panels = null;
    }
    
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }
    
    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }
    
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private transient Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
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
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        listeners = new HashSet<ChangeListener>(1);
    }
     */
    
    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData");
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        
        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }
        
        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }
    
}

