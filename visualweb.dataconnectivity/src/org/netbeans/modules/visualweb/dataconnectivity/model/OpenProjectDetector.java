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
package org.netbeans.modules.visualweb.dataconnectivity.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectOpenedEvent;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectOpenedListener;

/**
 *
 * @author John Baker
 */
public class OpenProjectDetector {
    private Set listeners = new HashSet();
    
    public OpenProjectDetector() {        
    }
    
    public void notifyProjectOpened() {
        OpenProjects.getDefault().addPropertyChangeListener(
                new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                    List<Project> oldOpenProjectsList = Arrays.asList((Project[])evt.getOldValue());
                    List<Project> newOpenProjectsList = Arrays.asList((Project[])evt.getNewValue());
//                    
//                    Set<Project> closedProjectsSet = new LinkedHashSet<Project>(oldOpenProjectsList);
//                    closedProjectsSet.removeAll(newOpenProjectsList);
//                    for (Project project : closedProjectsSet) {
//                        // do nothing for now
//                    }
//                    
                    Set<Project> openedProjectsSet = new LinkedHashSet<Project>(newOpenProjectsList);
                    openedProjectsSet.removeAll(oldOpenProjectsList);
                    for (Project project : openedProjectsSet) {
                        // fire an event to notify listeners that a project opened
                        new ProjectOpenedEvent(project);
                    }
                }
            }
        }
        );
    }
    
    public void addProjectOpenedListener(ProjectOpenedListener listener) {
        listeners.add(new WeakReference(listener));
        
    }
    
    public void removeProjectOpenedListener(ProjectOpenedListener listener) {
        // need listeners for duration of IDE session
    }
    
}
