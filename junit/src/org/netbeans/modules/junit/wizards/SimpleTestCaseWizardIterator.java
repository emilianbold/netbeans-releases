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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.junit.CreateTestAction;
import org.netbeans.modules.junit.GuiUtils;
import org.netbeans.modules.junit.JUnitSettings;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 */
public class SimpleTestCaseWizardIterator
        implements TemplateWizard.Iterator {

    /** */
    private static SimpleTestCaseWizardIterator instance;

    /** */
    private TemplateWizard wizard;

    /** index of step &quot;Name &amp; Location&quot; */
    private static final int INDEX_CHOOSE_CLASS = 2;
    /** index of step &quot;Settings&quot; */
    private static final int INDEX_SETTINGS = 3;

    /** name of panel &quot;Name &amp; Location&quot; */
    private final String nameChooseClass = NbBundle.getMessage(
            SimpleTestCaseWizardIterator.class,
            "LBL_panel_ChooseClass");                                   //NOI18N
    /** index of the current panel */
    private int current;
    /** registered change listeners */
    private List changeListeners;
    /** */
    private Project lastSelectedProject = null;
    /** panel for choosing name and target location of the test class */
    private WizardDescriptor.Panel classChooserPanel;

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
        return current > INDEX_CHOOSE_CLASS;
    }

    /**
     */
    public boolean hasNext() {
        return current < INDEX_CHOOSE_CLASS;
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
            case INDEX_CHOOSE_CLASS:
                return getClassChooserPanel();
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
    private WizardDescriptor.Panel getClassChooserPanel() {
        final Project project = Templates.getProject(wizard);
        if (classChooserPanel == null || project != lastSelectedProject) {
            final Utils utils = new Utils(project);
            if (utils.getSourcesToTestsMap(true).isEmpty()) {
                classChooserPanel = new StepProblemMessage(
                        project,
                        NbBundle.getMessage(EmptyTestCaseWizardIterator.class,
                                            "MSG_NoTestSourceGroup"));  //NOI18N
            } else {
                if (classChooserPanel == null) {
                    classChooserPanel = new SimpleTestStepLocation();
                }
                ((SimpleTestStepLocation) classChooserPanel).setUp(utils);
            }
        }
        lastSelectedProject = project;
        return classChooserPanel;
    }

    /**
     */
    public String name() {
        switch (current) {
            case INDEX_CHOOSE_CLASS:
                return nameChooseClass;
            default:
                throw new AssertionError(current);
        }
    }
    
    private void loadSettings(TemplateWizard wizard) {
        JUnitSettings settings = JUnitSettings.getDefault();
        
        wizard.putProperty(GuiUtils.CHK_PUBLIC,
                           Boolean.valueOf(settings.isMembersPublic()));
        wizard.putProperty(GuiUtils.CHK_PROTECTED,
                           Boolean.valueOf(settings.isMembersProtected()));
        wizard.putProperty(GuiUtils.CHK_PACKAGE,
                           Boolean.valueOf(settings.isMembersPackage()));
        wizard.putProperty(GuiUtils.CHK_SETUP,
                           Boolean.valueOf(settings.isGenerateSetUp()));
        wizard.putProperty(GuiUtils.CHK_TEARDOWN,
                           Boolean.valueOf(settings.isGenerateTearDown()));
        wizard.putProperty(GuiUtils.CHK_METHOD_BODIES,
                           Boolean.valueOf(settings.isBodyContent()));
        wizard.putProperty(GuiUtils.CHK_JAVADOC,
                           Boolean.valueOf(settings.isJavaDoc()));
        wizard.putProperty(GuiUtils.CHK_HINTS,
                           Boolean.valueOf(settings.isBodyComments()));

        wizard.putProperty("NewFileWizard_Title", NbBundle.getMessage(SimpleTestStepLocation.class, "LBL_simpleTestWizard_stepName"));
    }

    private void saveSettings(TemplateWizard wizard) {
        JUnitSettings settings = JUnitSettings.getDefault();
        
        settings.setMembersPublic(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PUBLIC)));
        settings.setMembersProtected(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PROTECTED)));
        settings.setMembersPackage(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PACKAGE)));
        settings.setGenerateSetUp(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_SETUP)));
        settings.setGenerateTearDown(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_TEARDOWN)));
        settings.setBodyContent(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_METHOD_BODIES)));
        settings.setJavaDoc(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_JAVADOC)));
        settings.setBodyComments(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_HINTS)));
    }

    /**
     * <!-- PENDING -->
     */
    public void initialize(TemplateWizard wiz) {
        this.wizard = wiz;
        current = INDEX_CHOOSE_CLASS;
        loadSettings(wiz);

        String [] panelNames =  new String [] {
          NbBundle.getMessage(EmptyTestCaseWizardIterator.class,"LBL_panel_chooseFileType"),
          NbBundle.getMessage(EmptyTestCaseWizardIterator.class,"LBL_panel_ChooseClass")};

        ((javax.swing.JComponent)getClassChooserPanel().getComponent()).putClientProperty("WizardPanel_contentData", panelNames); 
        ((javax.swing.JComponent)getClassChooserPanel().getComponent()).putClientProperty("WizardPanel_contentSelectedIndex", new Integer(0)); 

    }

    /**
     * <!-- PENDING -->
     */
    public void uninitialize(TemplateWizard wiz) {
        if ((classChooserPanel != null)
                && !(classChooserPanel instanceof StepProblemMessage)) {
            
            assert classChooserPanel instanceof SimpleTestStepLocation;
            
            ((SimpleTestStepLocation) classChooserPanel).cleanUp();
        }
        classChooserPanel = null;
        changeListeners = null;
        this.wizard = null;
    }

    public Set instantiate(TemplateWizard wiz) throws IOException {
        saveSettings(wiz);
        
        /* get the template DataObject... */
        String templatePath = NbBundle.getMessage(
                                      CreateTestAction.class,
                                      "PROP_testClassTemplate");        //NOI18N
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
        FileObject classToTest = (FileObject)
                wizard.getProperty(SimpleTestCaseWizard.PROP_CLASS_TO_TEST);
        FileObject testRootFolder = (FileObject)
                wizard.getProperty(SimpleTestCaseWizard.PROP_TEST_ROOT_FOLDER);
        ClassPath testClassPath = ClassPathSupport.createClassPath(
                new FileObject[] {testRootFolder});
                
        /* create test class(es) for the selected source class: */
        try {
            return CreateTestAction.createSingleTest(
                testClassPath, classToTest,
                templateDataObj, null,
                null, null, false).getCreated();
        } catch (CreateTestAction.CreationError ex) {
            throw new IOException();
        }
    }

    /**
     */
    public static SimpleTestCaseWizardIterator singleton() {
        if (instance == null) {
            // PENDING - it should not be kept forever
            instance = new SimpleTestCaseWizardIterator();
        }
        return instance;
    }

}
