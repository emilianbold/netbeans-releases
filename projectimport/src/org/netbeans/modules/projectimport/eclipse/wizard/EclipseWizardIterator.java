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

package org.netbeans.modules.projectimport.eclipse.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.projectimport.ProjectImporterException;
import org.netbeans.modules.projectimport.eclipse.ProjectFactory;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;

/**
 * Iterates on the sequence of Eclipse wizard panels.
 *
 * @author mkrauskopf
 */
final class EclipseWizardIterator implements
        WizardDescriptor.Iterator, ChangeListener {
    
    private String errorMessage;
    private SelectionWizardPanel workspacePanel;
    private ProjectWizardPanel projectPanel;
    private ImporterWizardPanel current;
    
    private boolean hasNext;
    private boolean hasPrevious;
    
    /** Registered ChangeListeners */
    private List changeListeners;
    
    /** Initialize and create an instance. */
    EclipseWizardIterator() {
        workspacePanel = new SelectionWizardPanel();
        workspacePanel.addChangeListener(this);
        projectPanel = new ProjectWizardPanel();
        projectPanel.addChangeListener(this);
        current = workspacePanel;
    }
    
    /** Returns projects selected by selection panel */
    Set getProjects() {
        if (workspacePanel.isWorkspaceChosen()) {
            return projectPanel.getProjects();
        } else {
            Set prjs = new HashSet();
            try {
                prjs.add(ProjectFactory.getInstance().load(
                        new File(workspacePanel.getProjectDir())));
            } catch (ProjectImporterException e) {
                ErrorManager.getDefault().log(ErrorManager.ERROR,
                        "ProjectImporterException catched: " + e); // NOI18N
                e.printStackTrace();
            }
            return prjs;
        }
    }
    
    /**
     * Returns number of projects which will be imported (including both
     * required and selected projects)
     */
    int getNumberOfImportedProject() {
        return (workspacePanel.isWorkspaceChosen() ?
            projectPanel.getNumberOfImportedProject() : 1);
    }
    
    /**
     * Returns destination directory where new NetBeans projects will be stored.
     */
    String getDestination() {
        return (workspacePanel.isWorkspaceChosen() ?
            projectPanel.getDestination() :
            workspacePanel.getProjectDestinationDir());
    }
    
    /**
     * Returns whether selected projects should be imported recursively or not.
     */
    boolean getRecursively() {
        return workspacePanel.isWorkspaceChosen();
    }
    
    public void addChangeListener(ChangeListener l) {
        if (changeListeners == null) {
            changeListeners = new ArrayList(2);
        }
        changeListeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        if (changeListeners != null) {
            if (changeListeners.remove(l) && changeListeners.isEmpty()) {
                changeListeners = null;
            }
        }
    }
    
    protected void fireChange() {
        if (changeListeners != null) {
            ChangeEvent e = new ChangeEvent(this);
            for (Iterator i = changeListeners.iterator(); i.hasNext(); ) {
                ((ChangeListener) i.next()).stateChanged(e);
            }
        }
    }
    
    public void previousPanel() {
        if (current == projectPanel) {
            current = workspacePanel;
            hasPrevious = false;
            hasNext = true;
            updateErrorMessage();
        }
    }
    
    public void nextPanel() {
        if (current == workspacePanel) {
            projectPanel.loadProjects(workspacePanel.getWorkspaceDir());
            current = projectPanel;
            hasPrevious = true;
            hasNext = false;
            updateErrorMessage();
        }
    }
    
    public String name() {
        return (current == workspacePanel) ?
            (workspacePanel.isWorkspaceChosen() ?
                ImporterWizardPanel.WORKSPACE_LOCATION_STEP :
                ImporterWizardPanel.PROJECT_SELECTION_STEP) :
                ImporterWizardPanel.PROJECTS_SELECTION_STEP;
    }
    
    public boolean hasPrevious() {
        return hasPrevious;
    }
    
    public boolean hasNext() {
        return hasNext;
    }
    
    public WizardDescriptor.Panel current() {
        return current;
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        if (current == workspacePanel && current.isValid()) {
            if (workspacePanel.isWorkspaceChosen()) {
                hasNext = true;
            } else {
                hasNext = false;
            }
        }
        updateErrorMessage();
    }
    
    void updateErrorMessage() {
        errorMessage = current.getErrorMessage();
        fireChange();
    }
    
    String getErrorMessage() {
        return errorMessage;
    }
}
