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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.junit.CreateTestAction;
import org.netbeans.modules.junit.JUnitSettings;
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
    /** panel for choosing name and target location of the test class */
    private SimpleTestStepLocation classChooserPanel;

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
        if (classChooserPanel == null) {
            classChooserPanel = new SimpleTestStepLocation();
        }
        classChooserPanel.setProject(Templates.getProject(wizard));
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

    /**
     * <!-- PENDING -->
     */
    public void initialize(TemplateWizard wiz) {
        this.wizard = wiz;
        current = INDEX_CHOOSE_CLASS;
    }

    /**
     * <!-- PENDING -->
     */
    public void uninitialize(TemplateWizard wiz) {
        this.wizard = null;
        
        classChooserPanel = null;
        
        changeListeners = null;
    }

    public Set instantiate(TemplateWizard wiz) throws IOException {
        /* get the template DataObject... */
        String templatePath = JUnitSettings.getDefault().getClassTemplate();
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
        
        /* ... determine the target folder... */
        String classToTest = (String) wizard.getProperty(
                                      SimpleTestCaseWizard.PROP_CLASS_TO_TEST);
        System.out.println("class to test = " + classToTest);
        
        String classPkg;
        String className;
        int lastDotIndex = classToTest.lastIndexOf('.');
        if (lastDotIndex == -1) {
            classPkg = "";                                              //NOI18N
            className = classToTest;
        } else {
            classPkg = classToTest.substring(0, lastDotIndex);
            className = classToTest.substring(lastDotIndex + 1);
        }
        
        Project project = Templates.getProject(wizard);
        FileObject testsRoot = Utils.findTestsRoot(project);
        FileObject targetFolder = Utils.getPackageFolder(testsRoot, classPkg);
        DataFolder targetFolderDataObj = DataFolder.findFolder(targetFolder);
        
        /* ... and instantiate the object: */
        System.out.println("name = " + className);
        DataObject testDataObj = templateDataObj.createFromTemplate(
                                         targetFolderDataObj, className);
        System.out.println("Test Created.");
        return Collections.singleton(testDataObj);
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
