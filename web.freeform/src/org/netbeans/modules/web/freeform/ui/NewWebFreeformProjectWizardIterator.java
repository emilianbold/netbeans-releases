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

package org.netbeans.modules.web.freeform.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.spi.support.NewJavaFreeformProjectSupport;
import org.netbeans.modules.java.freeform.ui.NewJ2SEFreeformProjectWizardIterator;
import org.netbeans.modules.java.freeform.ui.ProjectModel;
import org.netbeans.modules.web.freeform.WebProjectGenerator;
import org.netbeans.modules.web.freeform.WebProjectNature;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny, Radko Najman
 */
public class NewWebFreeformProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {

    // web sources
    public static final String PROP_WEB_WEBMODULES = "webModules"; // <List> NOI18N
    public static final String PROP_WEB_SOURCE_FOLDERS = "webSourceFolders"; // <List> NOI18N
    
    protected static final String PROP_WEB_CLASSPATH = "webClasspath"; // <String> NOI18N
    
    private static final long serialVersionUID = 1L;
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public NewWebFreeformProjectWizardIterator() {
    }
    
    private WizardDescriptor.Panel[] createPanels () {
        List l = new ArrayList();
        List extraTargets = new ArrayList();
        extraTargets.add(WebProjectNature.getExtraTarget());
        l.add(NewFreeformProjectSupport.createBasicProjectInfoWizardPanel());
        l.add(NewFreeformProjectSupport.createTargetMappingWizardPanel(extraTargets));
        l.add(new WebLocationsWizardPanel());
        l.addAll(Arrays.asList(NewJavaFreeformProjectSupport.createJavaPanels()));
        l.add(new WebClasspathWizardPanel());
        return (WizardDescriptor.Panel[])l.toArray(new WizardDescriptor.Panel[l.size()]);
    }
    
    public Set/*<FileObject>*/ instantiate () throws IOException {
        final WizardDescriptor wiz = this.wiz;
        final IOException[] ioe = new IOException[1];
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    AntProjectHelper helper = NewFreeformProjectSupport.instantiateBasicProjectInfoWizardPanel(wiz);
                    NewFreeformProjectSupport.instantiateTargetMappingWizardPanel(helper, wiz);
                    NewJavaFreeformProjectSupport.instantiateJavaPanels(helper, wiz);
                    
                    List webSources = (List)wiz.getProperty(PROP_WEB_SOURCE_FOLDERS);
                    AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(helper);
                    WebProjectGenerator.putWebSourceFolder(helper, webSources);
        
                    List webModules = (List) wiz.getProperty(PROP_WEB_WEBMODULES);
                    if (webModules != null) {
                        // Save the web classpath for the web module
                        String webClasspath = (String)wiz.getProperty(NewWebFreeformProjectWizardIterator.PROP_WEB_CLASSPATH);
                        Iterator iter = webModules.iterator();
                        WebProjectGenerator.WebModule wm;
                        while(iter.hasNext()){
                            wm = (WebProjectGenerator.WebModule)iter.next();
                            wm.classpath = webClasspath;
                        }
                        WebProjectGenerator.putWebModules (helper, aux, webModules);
                    }
                    
                    Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                    ProjectManager.getDefault().saveProject(p);
                } catch (IOException e) {
                    ioe[0] = e;
                    return;
                }
            }});
        if (ioe[0] != null) {
            throw ioe[0];
        }
        File nbProjectFolder = (File)wiz.getProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER);
        Set resultSet = new HashSet();
        resultSet.add(FileUtil.toFileObject(nbProjectFolder));
        File f = nbProjectFolder.getParentFile();
        if (f != null) {
            ProjectChooser.setProjectsFolder(f);
        }
        return resultSet;
    }
    
        
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        
        List l = new ArrayList();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            l.add(jc.getName());
        }
        String[] steps = (String[])l.toArray(new String[l.size()]);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            // Step #.
            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
            // Step name (actually the whole list for reference).
            jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            // set title
            jc.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage (NewWebFreeformProjectWizardIterator.class, "TXT_NewWebFreeformProjectWizardIterator_NewProjectWizardTitle")); // NOI18N
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        NewFreeformProjectSupport.uninitializeBasicProjectInfoWizardPanel(wiz);
        NewFreeformProjectSupport.uninitializeTargetMappingWizardPanel(wiz);
        NewJavaFreeformProjectSupport.uninitializeJavaPanels(wiz);
        wiz.putProperty(PROP_WEB_SOURCE_FOLDERS, null);
        wiz.putProperty(PROP_WEB_WEBMODULES, null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format (NbBundle.getMessage(NewWebFreeformProjectWizardIterator.class, "TXT_NewWebFreeformProjectWizardIterator_TitleFormat"), // NOI18N
            new Object[] {new Integer (index + 1), new Integer (panels.length) });
    }
    
    public boolean hasNext() {
        if (!NewJavaFreeformProjectSupport.enableNextButton(current())) {
            return false;
        }
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
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}

    
}
