/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.junit.CreateTestAction;
import org.netbeans.modules.junit.GuiUtils;
import org.netbeans.modules.junit.JUnitSettings;
import org.netbeans.modules.junit.TestUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 */
public class TestSuiteWizardIterator
        implements TemplateWizard.Iterator {

    /** */
    private static TestSuiteWizardIterator instance;

    /** */
    private TemplateWizard wizard;

    /** index of step &quot;Name &amp; Location&quot; */
    private static final int INDEX_TARGET = 2;

    /** name of panel &quot;Name &amp; Location&quot; */
    private final String nameTarget = NbBundle.getMessage(
            TestSuiteWizardIterator.class,
            "LBL_panel_Target");                                        //NOI18N
    /** index of the current panel */
    private int current;
    /** registered change listeners */
    private List changeListeners;
    /** panel for choosing name and target location of the test class */
    private WizardDescriptor.Panel targetPanel;
    private Project lastSelectedProject = null;
    /** */
    private WizardDescriptor.Panel optionsPanel;

    /** */
    private SourceGroup[] testSrcGroups;
    
    /**
     */
    public void addChangeListener(ChangeListener l) {
        if (changeListeners == null) {
            changeListeners = new ArrayList(2);
        }
        changeListeners.add(l);
    }

    /**
     */
    public void removeChangeListener(ChangeListener l) {
        if (changeListeners != null) {
            changeListeners.remove(l);
            if (changeListeners.isEmpty()) {
                changeListeners = null;
            }
        }
    }

    /**
     * Notifies all registered listeners about a change.
     *
     * @see  #addChangeListener
     * @see  #removeChangeListener
     */
    private void fireChange() {
        if (changeListeners != null) {
            ChangeEvent e = new ChangeEvent(this);
            Iterator i = changeListeners.iterator();
            while (i.hasNext()) {
                ((ChangeListener) i.next()).stateChanged(e);
            }
        }
    }

    /**
     */
    public boolean hasPrevious() {
        return current > INDEX_TARGET;
    }

    /**
     */
    public boolean hasNext() {
        return current < INDEX_TARGET;
    }

    /**
     */
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        current--;
    }

    /**
     */
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        current++;
    }

    /**
     */
    public WizardDescriptor.Panel current() {
        switch (current) {
            case INDEX_TARGET:
                return getTargetPanel();
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Returns a panel for choosing name and target location of the test
     * class. If the panel already exists, returns the existing panel,
     * otherwise creates a new panel.
     *
     * @return  existing panel or a newly created panel if it did not exist
     */
    private WizardDescriptor.Panel getTargetPanel() {
        Project project = Templates.getProject(wizard);
        if (targetPanel == null || project != lastSelectedProject) {
            Collection sourceGroups = Utils.getTestSourceGroups(project);
            if (sourceGroups.isEmpty()) {
                targetPanel = new StepProblemMessage(
                        project,
                        NbBundle.getMessage(EmptyTestCaseWizardIterator.class,
                                            "MSG_NoTestSourceGroup"));  //NOI18N
            } else {
                sourceGroups.toArray(
                        testSrcGroups = new SourceGroup[sourceGroups.size()]);
                if (optionsPanel == null) {
                    optionsPanel = new TestSuiteStepLocation();
                }
                targetPanel = JavaTemplates.createPackageChooser(project,
                                                                 testSrcGroups,
                                                                 optionsPanel);
            }
            lastSelectedProject = project;
        }
        return targetPanel;
    }

    /**
     */
    public String name() {
        switch (current) {
            case INDEX_TARGET:
                return nameTarget;
            default:
                throw new AssertionError(current);
        }
    }

    private void loadSettings(TemplateWizard wizard) {
        JUnitSettings settings = JUnitSettings.getDefault();
        
        wizard.putProperty(GuiUtils.CHK_SETUP,
                           Boolean.valueOf(settings.isGenerateSetUp()));
        wizard.putProperty(GuiUtils.CHK_TEARDOWN,
                           Boolean.valueOf(settings.isGenerateTearDown()));
        wizard.putProperty(GuiUtils.CHK_HINTS,
                           Boolean.valueOf(settings.isBodyComments()));
    }

    private void saveSettings(TemplateWizard wizard) {
        JUnitSettings settings = JUnitSettings.getDefault();
        
        settings.setGenerateSetUp(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_SETUP)));
        settings.setGenerateTearDown(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_TEARDOWN)));
        settings.setBodyComments(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_HINTS)));
    }

    /**
     * <!-- PENDING -->
     */
    public void initialize(TemplateWizard wiz) {
        this.wizard = wiz;
        current = INDEX_TARGET;
        loadSettings(wiz);

        String [] panelNames =  new String [] {
          NbBundle.getMessage(EmptyTestCaseWizardIterator.class,"LBL_panel_chooseFileType"),
          NbBundle.getMessage(EmptyTestCaseWizardIterator.class,"LBL_panel_Target")};

        ((javax.swing.JComponent)getTargetPanel().getComponent()).putClientProperty("WizardPanel_contentData", panelNames); 
        ((javax.swing.JComponent)getTargetPanel().getComponent()).putClientProperty("WizardPanel_contentSelectedIndex", new Integer(0)); 

    }

    /**
     * <!-- PENDING -->
     */
    public void uninitialize(TemplateWizard wiz) {
        this.wizard = null;
        
        targetPanel = null;
        lastSelectedProject = null;
        optionsPanel = null;
        testSrcGroups = null;
        
        changeListeners = null;
    }

    public Set instantiate(TemplateWizard wiz) throws IOException {
        saveSettings(wiz);
        
        /* get the template DataObject... */
        String templatePath = NbBundle.getMessage(
                                      CreateTestAction.class,
                                      "PROP_testSuiteTemplate");        //NOI18N
        FileObject template = Repository.getDefault().getDefaultFileSystem()
                              .findResource(templatePath);
        DataObject templateDataObj;
        try {
            templateDataObj = DataObject.find(template);
        } catch (DataObjectNotFoundException ex) {
            String msg = NbBundle.getMessage(
                    CreateTestAction.class,
                    "MSG_template_not_found",                           //NOI18N
                    templatePath);
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                            msg, NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }
        
        /* collect and build necessary data: */
        String name = Templates.getTargetName(wizard);
        FileObject targetFolder = Templates.getTargetFolder(wizard);
        DataFolder targetDataFolder = DataFolder.findFolder(targetFolder);
        FileObject testRootFolder = findTestRootFolder(targetFolder);
        assert testRootFolder != null;
        ClassPath testClassPath = ClassPathSupport.createClassPath(
                new FileObject[] {testRootFolder});
        List testClassNames = TestUtil.getJavaFileNames(targetFolder,
                                                        testClassPath);
        
        /* create test class(es) for the selected source class: */
        try {
            DataObject suite = CreateTestAction.createSuiteTest(
                testClassPath, targetDataFolder, name,
                new LinkedList(testClassNames),
                templateDataObj, null, null);
        
            return Collections.singleton(suite);
        } catch (CreateTestAction.CreationError e) {
            throw new IOException();
        }            
    }
    
    /** */
    private FileObject findTestRootFolder(FileObject targetFolder) {
        for (int i = 0; i < testSrcGroups.length; i++) {
            FileObject rootFolder = testSrcGroups[i].getRootFolder();
            if (rootFolder == targetFolder
                    || FileUtil.isParentOf(rootFolder, targetFolder)) {
                return rootFolder;
            }
        }
        return null;
    }

    /**
     */
    public static TestSuiteWizardIterator singleton() {
        if (instance == null) {
            // PENDING - it should not be kept forever
            instance = new TestSuiteWizardIterator();
        }
        return instance;
    }

}
