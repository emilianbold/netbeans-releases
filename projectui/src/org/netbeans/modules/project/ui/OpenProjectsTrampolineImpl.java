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

package org.netbeans.modules.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;

/**
 * List of projects open in the GUI.
 * @author Petr Hrebejk
 */
public final class OpenProjectsTrampolineImpl implements OpenProjectsTrampoline, PropertyChangeListener  {
    
    /** Property change listeners registered through API */
    private PropertyChangeSupport pchSupport;
    
    
    public OpenProjectsTrampolineImpl() {
        pchSupport = new PropertyChangeSupport( this );
        OpenProjectList.getDefault().addPropertyChangeListener( this );
    }
    
    public Project[] getOpenProjectsAPI() {
        return OpenProjectList.getDefault().getOpenProjects();
    }

    public void openAPI (Project[] projects, boolean openRequiredProjects) {
        OpenProjectList.getDefault().open (projects, openRequiredProjects);
    }

    public void closeAPI(Project[] projects) {
        OpenProjectList.getDefault().close(projects);
    }

    public void addPropertyChangeListenerAPI( PropertyChangeListener listener ) {
        pchSupport.addPropertyChangeListener( listener );        
    }
    
    public void removePropertyChangeListenerAPI( PropertyChangeListener listener ) {
        pchSupport.removePropertyChangeListener( listener );        
    }
    
    public void propertyChange( PropertyChangeEvent e ) {
        
        if ( e.getPropertyName().equals( OpenProjectList.PROPERTY_OPEN_PROJECTS ) ) {        
            pchSupport.firePropertyChange( OpenProjects.PROPERTY_OPEN_PROJECTS, e.getOldValue(), e.getNewValue() );
        }
    }
        
    public Project getMainProject() {
        return OpenProjectList.getDefault().getMainProject();
    }
    
    public void setMainProject(Project project) {
        OpenProjectList.getDefault().setMainProject(project);
    }
    
}
