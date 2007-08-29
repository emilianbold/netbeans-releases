
/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.dataconnectivity.datasource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectChangeEvent;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectChangeListener;
import org.netbeans.modules.visualweb.dataconnectivity.naming.ProjectContextManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 *
 * @author JohnBaker
 */
public class CurrentProject {
    private static CurrentProject _instance = null;
    private Project project = null;
    private final PropertyChangeListener topComponentRegistryListener = new TopComponentRegistryListener();
    private TopComponent.Registry registry  = null;
    Set listeners = new HashSet();
    protected ProjectsChangedListener changedProjectsListener = new ProjectsChangedListener();
        
    /** Creates a new instance of CurrentProject */
    private CurrentProject() {                        
        DataObject obj = Utilities.actionsGlobalContext().lookup(DataObject.class);
        OpenProjects.getDefault().addPropertyChangeListener(WeakListeners.create(PropertyChangeListener.class, changedProjectsListener, OpenProjects.getDefault()));
        
        if (obj != null) {
            FileObject fileObject = obj.getPrimaryFile();
            project = FileOwnerQuery.getOwner(fileObject);            
        }
        
        if (project != null){
            setPreviousProject(project);
        }               
    }

    
    public static CurrentProject getInstance() {
        if (_instance == null) {
            _instance = new CurrentProject();                    
        } 
                
        return _instance;
    }
    
    private void multiViewChange() {
        Lookup lookup = TopComponent.getRegistry().getActivated().getLookup();
        DataObject obj = lookup.lookup(DataObject.class);
        if (obj != null) {
            FileObject fileObject = obj.getPrimaryFile();
            project = FileOwnerQuery.getOwner(fileObject);
            setPreviousProject(project);
            
            ProjectChangeEvent evt = new  ProjectChangeEvent(project);
            Iterator iter = listeners.iterator();
            while(iter.hasNext()) {
                ((ProjectChangeListener)iter.next()).projectChanged(evt);
            }
        }       
    }
    
    // Initialize listeners for page switch
    public void setup() {        
        registry = TopComponent.getRegistry();
        registry.addPropertyChangeListener(WeakListeners.propertyChange(topComponentRegistryListener, registry));

    }            
    
    public Project getProject() {        
        return project;
    }
    
    public void setProject(Project prj) {
        PreviousProject.getInstance().setProject(project);
        project = prj;
    }
    
    private class TopComponentRegistryListener implements PropertyChangeListener {
                
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName()))
                if (MultiViews.findMultiViewHandler(registry.getActivated()) != null)
                   multiViewChange();                
        }
    } 
    
    
    public class ProjectsChangedListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            
            // The list of open projects has changed; clean up any old projects we may be holding on to.
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(event.getPropertyName())) {
                
                List<Project> oldOpenProjectsList = Arrays.asList((Project[]) event.getOldValue());
                List<Project> newOpenProjectsList = Arrays.asList((Project[]) event.getNewValue());
                Set<Project> closedProjectsSet = new LinkedHashSet<Project>(oldOpenProjectsList);
                closedProjectsSet.removeAll(newOpenProjectsList);
                for (Project project : closedProjectsSet) {
                    // Project has been closed; null the project; reset to the previous project
                    if (_instance.project == project) {
                        _instance.project = null;
                        ProjectContextManager.getInstance().removeEntry(project);
                        OpenProjects.getDefault().removePropertyChangeListener(this);
                    }
                     
                    // if there are still some projects opened, reset the current project to the last project in the list
                    // otherwise set the previous project to null and remove the property change listener
                    if (!newOpenProjectsList.isEmpty()) {
                        PreviousProject.getInstance().setProject(newOpenProjectsList.get(newOpenProjectsList.size()-1));
                        _instance.project = PreviousProject.getInstance().getProject();
                    } else {
                        PreviousProject.getInstance().setProject(null);
                    }
                }                
                
                Set<Project> openedProjectsSet = new LinkedHashSet<Project>(newOpenProjectsList);
                openedProjectsSet.removeAll(oldOpenProjectsList);
                for (Project project : openedProjectsSet) {
                    // fire an event to notify listeners that a project opened
                    _instance.project = project;
                    PreviousProject.getInstance().setProject(project);

                }
            }
        }
    }

    public void addProjectChangeListener(ProjectChangeListener listener){
        listeners.add(listener);
    }
    
    public void removeProjectChangeListener(ProjectChangeListener listener){
        listeners.remove(listener);
    }
    
    public void setPreviousProject(Project prj) {
        PreviousProject.getInstance().setProject(prj);        
    } 
    
    public Project getPreviousProject() {
        return PreviousProject.getInstance().getProject();
    }
    
    /**
     * getOpenedProject returns the project that is being opened 
     */     
    public Project getOpenedProject() {
        if (TopComponent.getRegistry().getActivated() != null) {
            Lookup lookup = TopComponent.getRegistry().getActivated().getLookup();
            DataObject obj = lookup.lookup(DataObject.class);

            if (obj != null) {
                FileObject fileObject = obj.getPrimaryFile();
                project = FileOwnerQuery.getOwner(fileObject);
                setPreviousProject(project);
            }
        }

        return project;
    }
             
}
