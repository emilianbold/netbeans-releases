/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.List;

import org.netbeans.modules.project.ui.OpenProjectList;

/**
 * Provides simple information about recent projects and fires PropertyChangeEvent
 * in case of change in the list of recent projects
 * @author Milan Kubec
 * @since 1.9.0
 */
public final class RecentProjects {
    
    /**
     * Property representing recent project information
     */
    public static final String PROP_RECENT_PROJECT_INFO = "RecentProjectInformation"; // NOI18N
    
    private static RecentProjects INSTANCE;
    
    private PropertyChangeSupport pch;
    
    public static RecentProjects getDefault() {
        if (INSTANCE == null) {
            return new RecentProjects();
        } else {
            return INSTANCE;
        }
    }
    
    /**
     * Creates a new instance of RecentProjects
     */
    private RecentProjects() {
        pch = new PropertyChangeSupport(this);
        OpenProjectList.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(OpenProjectList.PROPERTY_RECENT_PROJECTS)) {
                    pch.firePropertyChange(new PropertyChangeEvent(RecentProjects.class,
                            PROP_RECENT_PROJECT_INFO, null, null));
                }
            }
        });
    }
    
    /**
     * Gets simple info (@link UnloadedProjectInformation) about recent projects in IDE.
     * Project in the list might not exist or might not be valid e.g. in case when
     * project was deleted or changed. It's responsibility of the user of the API
     * to make sure the project exists and is valid.
     * @return list of project information about recently opened projects
     */
    public List/*<UnloadedProjectInformation>*/ getRecentProjectInformation() {
        return OpenProjectList.getDefault().getRecentProjectsInformation();
    }
    
    /**
     * Adds a listener, use WeakListener or properly remove listeners
     * @param listener listener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pch.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a listener
     * @param listener listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pch.removePropertyChangeListener(listener);
    }
    
}
