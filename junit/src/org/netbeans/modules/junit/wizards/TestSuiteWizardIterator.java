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
    private TestSuiteStepLocation targetPanel;

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
        if (current == INDEX_TARGET) {
            //try {
                //DataFolder folder = Templates.getTargetFolder(wizard);
                
                //PENDING - this is a workaround - the above code does not work!
                //DataFolder folder = wizard.getTargetFolder();
                
                //String name = Templates.getTargetName(wizard);
                
                //PENDING - this is a workaround - the above code does not work!
               // String name = wizard.getTargetName();
                
                //wizard.putProperty(EmptyTestCaseWizard.PROP_TARGET_FOLDER,
                //                   folder);
                //wizard.putProperty(EmptyTestCaseWizard.PROP_TARGET_NAME,
                //                   name);
            //} catch (IOException ex) {
            //    String msg = NbBundle.getMessage(
            //            EmptyTestCaseWizardIterator.class,
            //            "MSG_Could_not_create_target_dir");             //NOI18N
            //    DialogDisplayer.getDefault().notify(
            //            new NotifyDescriptor.Message(
            //                    msg,
            //                    NotifyDescriptor.ERROR_MESSAGE));
            //    return;
            //}
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
        if (targetPanel == null) {
            //Project project = Templates.getProject(wizard);
            //Sources sources = ProjectUtils.getSources(project);

            //PENDING - for Java projects, we should use SOURCES_TYPE_JAVA
            //SourceGroup[] sourceGroups
            //        = sources.getSourceGroups(Sources.TYPE_GENERIC);

            //targetPanel = Templates.createSimpleTargetChooser(project,
            //                                                  sourceGroups);
            targetPanel = new TestSuiteStepLocation();
        }
        targetPanel.setProject(Templates.getProject(wizard));
        return targetPanel;
    }

    /**
     */
    public String name() {
        switch (current) {
            case INDEX_TARGET:
                return nameTarget;
            //case INDEX_SETTINGS:
            //    return nameSettings;
            default:
                throw new AssertionError(current);
        }
    }

    /**
     * <!-- PENDING -->
     */
    public void initialize(TemplateWizard wiz) {
        this.wizard = wiz;
        current = INDEX_TARGET;
    }

    /**
     * <!-- PENDING -->
     */
    public void uninitialize(TemplateWizard wiz) {
        this.wizard = null;
        
        targetPanel = null;
        //settingsPanel = null;
        
        changeListeners = null;
    }

    public Set instantiate(TemplateWizard wiz) throws IOException {
        
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
        
        /* ... determine the target folder... */
        String pkg = (String) wizard.getProperty(
                                      TestSuiteWizard.PROP_PACKAGE);
        System.out.println("pkg = " + pkg);
        Project project = Templates.getProject(wizard);
        FileObject testsRoot = Utils.findTestsRoot(project);
        FileObject targetFolder = Utils.getPackageFolder(testsRoot, pkg);
        DataFolder targetFolderDataObj = DataFolder.findFolder(targetFolder);
        
        /* ... and instantiate the object: */
        String name = (String) wizard.getProperty(
                                            TestSuiteWizard.PROP_CLASS_NAME)
                      + "";  //NOI18N
        System.out.println("name = " + name);
        DataObject testDataObj = templateDataObj.createFromTemplate(
                                         targetFolderDataObj, name);
        System.out.println("Test Created.");
        return Collections.singleton(testDataObj);
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
