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

package org.netbeans.modules.projectimport.eclipse.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.projectimport.eclipse.EclipseProject;
import org.openide.WizardDescriptor;

/**
 * DOCDO
 *
 * @author mkrauskopf
 */
final class EclipseWizardIterator implements
        WizardDescriptor.Iterator, ChangeListener {
    
    private String errorMessage;
    private WorkspaceSelectionPanel workspacePanel;
    private ProjectSelectionPanel projectPanel;
    private ImporterWizardPanel current;
    
    private boolean hasNext = true;
    private boolean hasPrevious;
    
    private EclipseProject eclProject;
    
    /** Registered ChangeListeners */
    private List changeListeners;
    
    /** Initialize and create an instance. */
    EclipseWizardIterator() {
        workspacePanel = new WorkspaceSelectionPanel();
        projectPanel = new ProjectSelectionPanel();
        workspacePanel.addChangeListener(this);
        projectPanel.addChangeListener(this);
        current = workspacePanel;
    }
    
    /** Returns projects selected by selection panel */
    Set getProjects() {
        return projectPanel.getProjects();
    }
    
    /**
     * Return number of projects which will be imported (including required
     * projects.
     */
    int getNumberOfNeededProjects() {
        return projectPanel.getNumberOfNeededProjects();
    }
    
    /**
     * Return destination directory where new NetBeans projects will be stored.
     */
    String getDestination() {
        return projectPanel.getDestination();
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
            ImporterWizardPanel.WORKSPACE_LOCATION_STEP :
            ImporterWizardPanel.PROJECT_SELECTION_STEP;
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
        updateErrorMessage();
    }
    
    void updateErrorMessage() {
        this.errorMessage = current.getErrorMessage();
        fireChange();
    }
    
    String getErrorMessage() {
        return errorMessage;
    }
}
