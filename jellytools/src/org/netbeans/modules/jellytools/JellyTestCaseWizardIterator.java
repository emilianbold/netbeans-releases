/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jellytools;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/** Wizard to create a new JellyTestCase based test. */
public class JellyTestCaseWizardIterator implements TemplateWizard.Iterator {
    
    /** Singleton instance of JavaWizardIterator, should it be ever needed. */
    private static JellyTestCaseWizardIterator instance;
    
    /** Wizard instance. */
    private TemplateWizard wizard;
    
    /** index of step Name and Location */
    private static final int INDEX_TARGET = 2;
    
    /** name of panel Name and Location */
    private final String NAME_AND_LOCATION = NbBundle.getMessage(JellyTestCaseWizardIterator.class,
                                                                 "LBL_panel_Target");  //NOI18N
    /** index of the current panel */
    private int current;
    /** panel for choosing name and target location of the test class */
    private WizardDescriptor.Panel targetPanel;
    private Project lastSelectedProject = null;
    
    /** Returns JavaWizardIterator singleton instance. This method is used
     * for constructing the instance from filesystem.attributes.
     */
    public static synchronized JellyTestCaseWizardIterator singleton() {
        if (instance == null) {
            instance = new JellyTestCaseWizardIterator();
        }
        return instance;
    }
    
    /** Not needed.  */
    public void addChangeListener(ChangeListener l) {
    }
    
    /** Not needed. */
    public void removeChangeListener(ChangeListener l) {
    }
    
    /** Returns true if previous panel exists. */
    public boolean hasPrevious() {
        return current > INDEX_TARGET;
    }
    
    /** Returns true if next panel exists. */
    public boolean hasNext() {
        return current < INDEX_TARGET;
    }
    
    /** Returns previous panel index.  */
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        current--;
    }
    
    /** Returns next panel index. */
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        current++;
    }
    
    /** Returns current panel. */
    public WizardDescriptor.Panel current() {
        switch (current) {
            case INDEX_TARGET:
                return getTargetPanel();
            default:
                throw new IllegalStateException();
        }
    }
    
    /** Returns Name and Location panel for selected project. */
    private WizardDescriptor.Panel getTargetPanel() {
        final Project project = Templates.getProject(wizard);
        if (targetPanel == null || project != lastSelectedProject) {
            SourceGroup[] javaSourceGroups =
                    ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            targetPanel = JavaTemplates.createPackageChooser(project, javaSourceGroups);
            lastSelectedProject = project;
        }
        return targetPanel;
    }
    
    /** Returns name of panel.  */
    public String name() {
        switch (current) {
            case INDEX_TARGET:
                return NAME_AND_LOCATION;
            default:
                throw new AssertionError(current);
        }
    }
    
    /** Initialize wizard. */
    public void initialize(TemplateWizard wiz) {
        this.wizard = wiz;
        current = INDEX_TARGET;
        
        String [] panelNames =  new String [] {
            NbBundle.getMessage(JellyTestCaseWizardIterator.class,"LBL_panel_chooseFileType"),
            NAME_AND_LOCATION};
        
        ((javax.swing.JComponent)getTargetPanel().getComponent()).putClientProperty("WizardPanel_contentData", panelNames);
        ((javax.swing.JComponent)getTargetPanel().getComponent()).putClientProperty("WizardPanel_contentSelectedIndex", new Integer(0));
    }
    
    /** Uninitialize wizard. */
    public void uninitialize(TemplateWizard wiz) {
        this.wizard = null;
        targetPanel = null;
        lastSelectedProject = null;
    }
    
    /** Create a new file from template. */
    public Set instantiate(TemplateWizard wizard) throws IOException {
        return Collections.singleton(
                wizard.getTemplate().createFromTemplate(wizard.getTargetFolder(), wizard.getTargetName()));
    }
}
