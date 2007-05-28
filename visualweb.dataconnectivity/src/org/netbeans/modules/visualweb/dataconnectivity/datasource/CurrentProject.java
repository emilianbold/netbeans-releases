
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
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.visualweb.dataconnectivity.model.OpenProjectDetector;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectChangeEvent;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectChangeListener;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectOpenedEvent;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectOpenedListener;
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
public class CurrentProject implements ProjectOpenedListener  {
    private static CurrentProject _instance = null;
    private Project project = null;
    private final PropertyChangeListener topComponentRegistryListener = new TopComponentRegistryListener();
    private TopComponent.Registry registry  = null;
    Set listeners = new HashSet<WeakReference>();
    private Project previousProject = null;
    private static boolean datasourcesUpdated = false;
    private OpenProjectDetector openedProject;
    
    /** Creates a new instance of CurrentProject */
    private CurrentProject() {                        
        DataObject obj = Utilities.actionsGlobalContext().lookup(DataObject.class);
        if (obj != null) {
            FileObject fileObject = obj.getPrimaryFile();
            project = FileOwnerQuery.getOwner(fileObject);
        }
        
        if (project != null){
            setPreviousProject(project);
        }
        
        openedProject = new OpenProjectDetector();
        openedProject.addProjectOpenedListener(this);
    }

    
    public static CurrentProject getInstance() {
        if (_instance == null) {
            _instance = new CurrentProject();                    
        } 
                
        return _instance;
    }
    
    private void multiViewChange() {
        Lookup lookup = TopComponent.getRegistry().getActivated().getLookup();
        DataObject obj = (DataObject)lookup.lookup(DataObject.class);
        if (obj != null) {
            FileObject fileObject = obj.getPrimaryFile();
            project = FileOwnerQuery.getOwner(fileObject);
            
            ProjectChangeEvent evt = new  ProjectChangeEvent(project);
            Iterator iter = listeners.iterator();
            while(iter.hasNext()) {
                ((ProjectChangeListener)((WeakReference)iter.next()).get()).projectChanged(evt);
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
        project = prj;
    }
    
    private class TopComponentRegistryListener implements PropertyChangeListener {
                
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName()))
                if (MultiViews.findMultiViewHandler(registry.getActivated()) != null)
                   multiViewChange();                
        }
    } 
     
    public void addProjectChangeListener(ProjectChangeListener listener){
        listeners.add(new WeakReference(listener));
    }
    
    public void removeProjectChangeListener(ProjectChangeListener listener){
        listeners.remove(listener);
    }
    
    public void setPreviousProject(Project prj) {
        previousProject = prj;        
    } 
    
    public Project getPreviousProject() {
        return previousProject;
    }

    public void addProjectOpened(ProjectOpenedEvent evt) {
        setProject(evt.getProject());
    }

    public void removeProjectOpened(ProjectOpenedEvent evt) {
        openedProject.removeProjectOpenedListener(this);
    }
                
}
