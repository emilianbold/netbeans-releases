/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.projectimport.eclipse.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.projectimport.ProjectImporterException;
import org.netbeans.modules.projectimport.eclipse.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.ProjectFactory;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;

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
    List<EclipseProject> getProjects() {
        if (workspacePanel.isWorkspaceChosen()) {
            return projectPanel.getProjects();
        } else {
            try {
                File projectDirF = FileUtil.normalizeFile(new File(workspacePanel.getProjectDir()));
                return Collections.<EclipseProject>singletonList(ProjectFactory.getInstance().load(projectDirF));
            } catch (ProjectImporterException e) {
                ErrorManager.getDefault().log(ErrorManager.ERROR,
                        "ProjectImporterException catched: " + e); // NOI18N
                e.printStackTrace();
                return Collections.<EclipseProject>emptyList();
            }
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
