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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    
    private boolean listenersRegistered;
    
    public OpenProjectsTrampolineImpl() {
        pchSupport = new PropertyChangeSupport( this );
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
        boolean shouldRegisterListener;
        
        synchronized (this) {
            if (shouldRegisterListener = !listenersRegistered) {
                listenersRegistered = true;
            }
        }
        
        if (shouldRegisterListener) {
            //make sure we are listening on OpenProjectList so the events are be propagated.
            //see issue #65928:
            OpenProjectList.getDefault().addPropertyChangeListener( this );
        }
        
        pchSupport.addPropertyChangeListener( listener );        
    }
    
    public void removePropertyChangeListenerAPI( PropertyChangeListener listener ) {
        pchSupport.removePropertyChangeListener( listener );        
    }
    
    public void propertyChange( PropertyChangeEvent e ) {
        
        if ( e.getPropertyName().equals( OpenProjectList.PROPERTY_OPEN_PROJECTS ) ) {        
            pchSupport.firePropertyChange( OpenProjects.PROPERTY_OPEN_PROJECTS, e.getOldValue(), e.getNewValue() );
        }
        if ( e.getPropertyName().equals( OpenProjectList.PROPERTY_MAIN_PROJECT ) ) {        
            pchSupport.firePropertyChange( OpenProjects.PROPERTY_MAIN_PROJECT, e.getOldValue(), e.getNewValue() );
        }
    }
        
    public Project getMainProject() {
        return OpenProjectList.getDefault().getMainProject();
    }
    
    public void setMainProject(Project project) {
        OpenProjectList.getDefault().setMainProject(project);
    }
    
}
