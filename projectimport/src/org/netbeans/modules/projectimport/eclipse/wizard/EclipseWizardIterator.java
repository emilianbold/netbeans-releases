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

import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.projectimport.eclipse.EclipseProject;
import org.openide.WizardDescriptor;

/**
 * DOCDO
 *
 * @author mkrauskopf
 */
final class EclipseWizardIterator implements WizardDescriptor.Iterator {
    
    private ProjectSelectionPanel projectSelector = new ProjectSelectionPanel();
    private WorkspaceSelectionPanel workspaceSelector = new WorkspaceSelectionPanel();
    private WizardDescriptor.Panel current = workspaceSelector;
    
    private boolean hasNext = true;
    private boolean hasPrevious;
    
    private EclipseProject eclProject;
    
    Set getProjects() {
        return projectSelector.getProjects();
    }
    
    int getNumberOfNeededProjects() {
        return projectSelector.getNumberOfNeededProjects();
    }
    
    String getDestination() {
        return projectSelector.getDestination();
    }
    
    public void removeChangeListener(ChangeListener l) {
    }
    
    public void addChangeListener(ChangeListener l) {
    }
    
    public void previousPanel() {
        if (current == projectSelector) {
            current = workspaceSelector;
            hasPrevious = false;
            hasNext = true;
        }
    }
    
    public void nextPanel() {
        if (current == workspaceSelector) {
            projectSelector.loadProjects(workspaceSelector.getWorkspaceDir());
            current = projectSelector;
            hasPrevious = true;
            hasNext = false;
        }
    }
    
    public String name() {
        return (current == workspaceSelector) ?
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
}
